import java.util.*;

class Metrics {
    static long startTime;
    static int nodesCount;
    static int maxDepth;
    static int endGameNodesCount;
    public static void reset() {
        startTime = System.nanoTime();
        nodesCount = 0;
        maxDepth = 0;
        endGameNodesCount = 0;
    }
    public static void print() {
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1000000;
        System.out.printf("Time: %dms, ", durationMs);
        System.out.printf("Nodes: %d, ", nodesCount);
        System.out.printf("Max Depth: %d, ", maxDepth);
        System.out.printf("EGNs Found: %d ", endGameNodesCount);
        System.out.println();
    }
}

public class MinimaxBot extends OthelloPlayer {
    
    // makeMove gets a current OthelloBoard game state as input
    // and then returns an OthelloMove object
    
    // Your bot knows what color it is playing
    //    because it has a playerColor int field
    
    // Your bot can get an ArrayList of legal moves:
    //    ArrayList<OthelloMove> moves = board.legalMoves(playerColor);
    
    // The constructor for OthelloMove needs the row, col, and player color ints.
    // For example, play your token in row 1, col 2:
    //   OthelloMove m = new OthelloMove(1, 2, playerColor);
    
    // OthelloBoard objects have a public size field defining the
    // size of the game board:
    //   board.size
    
    // You can ask the OthelloBoard if a particular OthelloMove
    //  flanks in a certain direction.
    // For example:
    //  board.flanksLeft(m) will return true if you can capture pieces to the left of move, m
    
    // You can ask the board what the current score is.
    //  This is just the difference in checker counts
    //  return the point differential in black's favor
    //  +3 means black is up by 3
    //  -5 means white is up by 5
    // int score = board.getBoardScore();

    // OthelloBoard has a toString:
    //  System.out.println(board);
    
    // OthelloPlayer superclass has a method to get the color for your opponent:
    //  int opponentColor = getOpponentColor();
    
    class Node {
        int score = 0;
        boolean visited = false;
        int depth = 0;
        OthelloBoard board;
        OthelloMove move;
        ArrayList<Node> children = new ArrayList<Node>();
        public Node (OthelloBoard board, OthelloMove move, int depth) {
            this.board = board;
            this.move = move;
            this.depth = depth;
        }
        public void addChild(Node child) {
            children.add(child);
        }
    }

    class Timer {
        float secsLength = 0;
        long startTime = 0;
        public Timer(float secsLength) {
            this.secsLength = secsLength;
        }
        public void start() {
            startTime = System.nanoTime();
        }
        public boolean hasFinished() {
            long endTime = System.nanoTime();
            double seconds = ((double)(endTime - startTime)) / 1000000000.0;
            if(seconds > secsLength) {
                return true;
            }
            return false;
        }
    }

    static final int INITIAL_DEPTH_LIMIT = 3;
    static final int DEPTH_INCREMENT = 2;

    int currentMove;
    int depthLimit = INITIAL_DEPTH_LIMIT;
    Node previousMove = null;
    Queue<Node> q;
    int alpha;
    int beta;
    Timer timer = new Timer(0.9f);
    
    public MinimaxBot(Integer _color) {
        super(_color);
    }
    
    public OthelloMove makeMove(OthelloBoard board) {
        Metrics.reset();
        timer.start();
        if(playerColor == OthelloBoard.BLACK) {
            alpha = -Integer.MAX_VALUE;
            beta = Integer.MAX_VALUE;
        } else {
            alpha = Integer.MAX_VALUE;
            beta = -Integer.MAX_VALUE;
        }
        // see if we should reset currentMove
        if(board.isStartGrid()) {
            if(playerColor == OthelloBoard.WHITE)
                currentMove = 1;
            else
                currentMove = 0;
        }
        depthLimit = INITIAL_DEPTH_LIMIT;
        q = new LinkedList<Node>();
        Node root = null;
        if(previousMove != null) {
            // find root as child of previousMove
            for(Node child : previousMove.children) {
                if(child.board.equals(board)) {
                    root = child;
                    break;
                }
            }
        }
        if(root == null) {
            root = new Node(board, null, currentMove);
            /*
            if(currentMove > 0)
                System.out.printf("Failed to find board in children for move %d\n", currentMove);
            */
        }
        dfs(root);
        while(!q.isEmpty() && !timer.hasFinished()) {
            Node nextNode = q.poll();
            if(nextNode.depth == currentMove + depthLimit) {
                depthLimit += DEPTH_INCREMENT;
                // System.out.println("Increasing Depth Limit!");
            }
            dfs(nextNode);
        }
        Node bestChild = root.children.get(0);
        for(Node child : root.children) {
            if(playerColor == OthelloBoard.BLACK && child.score > bestChild.score) {
                bestChild = child;
            } else if(playerColor == OthelloBoard.WHITE && child.score < bestChild.score) {
                bestChild = child;
            }
        }
        System.out.printf("Move %d (%d,%d) => ", currentMove,
            bestChild.move.row, bestChild.move.col);
        Metrics.print();
        currentMove+=2;
        previousMove = bestChild;
        return bestChild.move;
    }

    private void dfs(Node root) {
        if(root.depth < currentMove) {
            // this branch is irrelevant
            return;
        }
        if(root.board.gameOver()) {
            // found end-game node!
            Metrics.endGameNodesCount++;
            if(root.board.getBoardScore() > 0) {
                // black wins!
                root.score = Integer.MAX_VALUE;
            } else if(root.board.getBoardScore() < 0) {
                // white wins!
                root.score = -Integer.MAX_VALUE;
            } else {
                // draw!
                if(playerColor == OthelloBoard.BLACK) {
                    root.score = -Integer.MAX_VALUE;
                } else {
                    root.score = Integer.MAX_VALUE;
                }
            }
            return;
        }
        if(root.depth == currentMove + depthLimit || timer.hasFinished()) {
            // estimate score
            root.score = root.board.getBoardScore();
            // add to queue for next search
            q.offer(root);
            return;
        }
        Metrics.nodesCount++;
        if(root.depth > Metrics.maxDepth)
            Metrics.maxDepth = root.depth;
        // continue searching down child nodes
        ArrayList<OthelloMove> moves = root.board.legalMoves(
            root.depth % 2 == 0 ? OthelloBoard.BLACK : OthelloBoard.WHITE);
        for(OthelloMove move : moves) {
            Node newNode = null;
            // check if move already in child nodes
            for(Node child : root.children) {
                if(child.move.row == move.row && child.move.col == move.col
                        && child.move.player == move.player) {
                    newNode = child;
                    break;
                }
            }
            if(newNode != null && newNode.visited) {
                // skip this child
                continue;
            } else if(newNode == null) {
                // create new child
                OthelloBoard newBoard = new OthelloBoard(root.board);
                newBoard.addPiece(move);
                newNode = new Node(newBoard, move, root.depth+1);
                root.addChild(newNode);
            }
            dfs(newNode);
            // check alpha, beta for pruning
            if(root.depth == currentMove) {
                if(playerColor == OthelloBoard.BLACK) {
                    if(newNode.score > alpha)
                        alpha = newNode.score;
                    else if(newNode.score < beta)
                        beta = newNode.score;
                } else {
                    if(newNode.score < alpha)
                        alpha = newNode.score;
                    else if(newNode.score > beta)
                        beta = newNode.score;
                }
                if((playerColor == OthelloBoard.BLACK && alpha > beta) ||
                    (playerColor == OthelloBoard.WHITE && alpha < beta)) {
                    // prune. stop searching through children.
                    alpha = beta;
                    root.score = newNode.score;
                    root.visited = true;
                    // System.out.println("Pruning!");
                    return;
                }
            }
        }
        root.visited = true;
        // back-propogate scores
        if(root.depth % 2 == 0) {
            // black's turn
            // choose max score of children
            int maxScore = -Integer.MAX_VALUE;
            for(Node child : root.children) {
                if(child.score > maxScore)
                    maxScore = child.score;
            }
            root.score = maxScore;
        } else {
            // white's turn
            // choose min score of children
            int minScore = Integer.MAX_VALUE;
            for(Node child : root.children) {
                if(child.score < minScore)
                    minScore = child.score;
            }
            root.score = minScore;
        }
    }
}
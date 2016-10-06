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
        Node parent;
        ArrayList<Node> children = new ArrayList<Node>();
        public Node (OthelloBoard board, OthelloMove move, int depth, Node parent) {
            this.board = board;
            this.move = move;
            this.depth = depth;
            this.parent = parent;
        }
        public void addChild(Node child) {
            children.add(child);
        }
    }

    class Timer {
        float secsLength = 0;
        long startTime = 0;
        long endTime;
        double seconds;
        boolean finished = false;
        public Timer(float secsLength) {
            this.secsLength = secsLength;
        }
        public void start() {
            finished = false;
            startTime = System.nanoTime();
        }
        public void subtractMs(long ms) {
            startTime -= ms * 1000000;
        }
        public boolean hasFinished() {
            if(finished)
                return finished;
            endTime = System.nanoTime();
            seconds = ((double)(endTime - startTime)) / 1000000000.0;
            if(seconds > secsLength) {
                finished = true;
            }
            return finished;
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
    HashSet<Node> nodesWithParent;
    Timer timer = new Timer(0.9f);
    
    public MinimaxBot(Integer _color) {
        super(_color);
    }
    
    public OthelloMove makeMove(OthelloBoard board, int move) {
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
        if(move == 0 || move == 1) {
            previousMove = null;
            q = new LinkedList<Node>();
            if(playerColor == OthelloBoard.WHITE)
                currentMove = 1;
            else
                currentMove = 0;
        }
        depthLimit = INITIAL_DEPTH_LIMIT;
        Node root = null;
        if(previousMove != null) {
            // try to find root as child of previousMove
            for(Node child : previousMove.children) {
                if(child.board.equals(board)) {
                    root = child;
                    break;
                }
            }
        }
        if(root == null) {
            // couldn't find child with matching board,
            // so it's a scenario we didn't consider
            root = new Node(board, null, currentMove, null);
            // empty queue
            q = new LinkedList<Node>();
        } else {
            // remove anything from queue not in this branch
            long startTime = System.nanoTime();
            nodesWithParent = new HashSet<Node>();
            Queue<Node> newQ = new LinkedList<Node>();
            while(!q.isEmpty()) {
                Node potentialChild = q.poll();
                if(hasParent(potentialChild, root)) {
                    newQ.offer(potentialChild);
                }
            }
            q = newQ;
            long endTime = System.nanoTime();
            long ms = (endTime - startTime) / 1000000;
            // System.out.printf("Cleaning Queue took %dms\n", ms);
            // Subtract the time taken to clean queue from timer limit.
            timer.subtractMs(ms);
        }
        dfs(root);
        while(!q.isEmpty() && !timer.hasFinished()) {
            Node nextNode = q.poll();
            if(nextNode.visited)
                continue;
            if(nextNode.depth == currentMove + depthLimit) {
                depthLimit += DEPTH_INCREMENT;
                // System.out.println("Increasing Depth Limit!");
            }
            dfs(nextNode);
        }
        // Choose best scored child as next move
        Node bestChild = root.children.get(0);
        for(Node child : root.children) {
            if((playerColor == OthelloBoard.BLACK && child.score > bestChild.score) ||
                (playerColor == OthelloBoard.WHITE && child.score < bestChild.score)) {
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

    private boolean hasParent(Node node, Node parent) {
        LinkedList<Node> parents = new LinkedList<Node>();
        while(node != null && !nodesWithParent.contains(node) && 
                node.parent != parent && node.depth > parent.depth) {
            parents.add(node);
            node = node.parent;
        }
        if(node == null || node.depth <= parent.depth)
            return false;
        for(Node n : parents) {
            nodesWithParent.add(n);
        }
        return true;
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
                    // record as win for white
                    root.score = -Integer.MAX_VALUE;
                } else {
                    // record as win for black
                    root.score = Integer.MAX_VALUE;
                }
            }
            propogateScore(root.parent);
            return;
        }
        if(root.depth == currentMove + depthLimit || timer.hasFinished()) {
            // estimate score
            root.score = estimateScore(root.board);
            propogateScore(root.parent);
            // add to queue for when depth limit is increased
            if(!timer.hasFinished()) {
                q.offer(root);
            }
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
                newNode = new Node(newBoard, move, root.depth+1, root);
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
        propogateScore(root);
    }

    final static int CORNER_WEIGHT = 5;
    final static int SIDE_WEIGHT = 1;

    private int estimateScore(OthelloBoard b) {
        int score = b.getBoardScore();
        // check corners
        if(b.board[0][0] > 0) {
            score += CORNER_WEIGHT*(b.board[0][0] == 1 ? 1 : -1);
        }
        if(b.board[0][b.size-1] > 0) {
            score += CORNER_WEIGHT*(b.board[0][b.size-1] == 1 ? 1 : -1);
        }
        if(b.board[b.size-1][b.size-1] > 0) {
            score += CORNER_WEIGHT*(b.board[b.size-1][b.size-1] == 1 ? 1 : -1);
        }
        if(b.board[b.size-1][0] > 0) {
            score += CORNER_WEIGHT*(b.board[b.size-1][0] == 1 ? 1 : -1);
        }
        // check sides
        for(int i=0; i<b.size; i++) {
            if(b.board[i][0] > 0) {
                score += SIDE_WEIGHT*(b.board[i][0] == 1 ? 1 : -1);
            }
            if(b.board[0][i] > 0) {
                score += SIDE_WEIGHT*(b.board[0][i] == 1 ? 1 : -1);
            }
            if(b.board[b.size-1][i] > 0) {
                score += SIDE_WEIGHT*(b.board[b.size-1][i] == 1 ? 1 : -1);
            }
            if(b.board[i][b.size-1] > 0) {
                score += SIDE_WEIGHT*(b.board[i][b.size-1] == 1 ? 1 : -1);
            }
        }
        return score;
    }

    private void propogateScore(Node node) {
        long startTime = System.nanoTime();
        backPropogateScore(node);
        long endTime = System.nanoTime();
        long ms = (endTime - startTime) / 1000000;
        timer.subtractMs(ms);
    }

    private void backPropogateScore(Node node) {
        if(node == null)
            return;
        int oldScore = node.score;
        if(node.depth % 2 == 0) {
            // black's turn
            // choose max score of children
            int maxScore = -Integer.MAX_VALUE;
            for(Node child : node.children) {
                if(child.score > maxScore)
                    maxScore = child.score;
            }
            node.score = maxScore;
        } else {
            // white's turn
            // choose min score of children
            int minScore = Integer.MAX_VALUE;
            for(Node child : node.children) {
                if(child.score < minScore)
                    minScore = child.score;
            }
            node.score = minScore;
        }
        // only reevaluate parent if this child's score has changed,
        // and if the parent has already been visited
        if(oldScore != node.score && node.parent != null && node.parent.visited) {
            backPropogateScore(node.parent);
        }
    }
}
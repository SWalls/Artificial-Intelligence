import java.util.*;

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

    static final int INITIAL_DEPTH_LIMIT = 5;
    static final int DEPTH_INCREMENT = 2;

    int currentMove = 0;
    int depthLimit = INITIAL_DEPTH_LIMIT;
    Queue<Node> q = new LinkedList<Node>();
    int blackAlpha = -Integer.MAX_VALUE;
    int blackBeta = Integer.MAX_VALUE;
    int whiteAlpha = Integer.MAX_VALUE;
    int whiteBeta = -Integer.MAX_VALUE;
    Timer timer = new Timer(0.9f);
    
    public MinimaxBot(Integer _color) {
        super(_color);
    }
    
    public OthelloMove makeMove(OthelloBoard board) {
        timer.start();
        Node root = new Node(board, null, 0);
        dfs(root);
        while(!q.isEmpty() && !timer.hasFinished()) {
            Node nextNode = q.poll();
            if(nextNode.depth > depthLimit) {
                depthLimit += DEPTH_INCREMENT;
                System.out.println("Increasing Depth Limit!");
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
        return bestChild.move;
    }

    private void dfs(Node root) {
        if(root.depth < currentMove) {
            // this branch is irrelevant
            root.visited = true;
            return;
        }
        if(root.board.gameOver()) {
            // found end-game node!
            if(root.board.getBoardScore() > 0 && playerColor == OthelloBoard.BLACK) {
                // black wins!
                root.score = Integer.MAX_VALUE;
            } else if(root.board.getBoardScore() < 0 && playerColor == OthelloBoard.WHITE) {
                // white wins!
                root.score = -Integer.MAX_VALUE;
            } else {
                // draw!
                root.score = 0;
            }
            return;
        }
        if(root.depth == depthLimit || timer.hasFinished()) {
            // estimate score
            root.score = root.board.getBoardScore();
            // add to queue for next search
            q.offer(root);
            return;
        }
        // continue searching down
        ArrayList<OthelloMove> moves = root.board.legalMoves(playerColor);
        for(OthelloMove move : moves) {
            OthelloBoard newBoard = new OthelloBoard(root.board);
            newBoard.addPiece(move);
            Node newNode = new Node(newBoard, move, root.depth+1);
            root.addChild(newNode);
            dfs(newNode);
            // check alpha, beta for pruning
            if(root.depth % 2 == 0) {
                // black's turn
                if(newNode.score > blackAlpha)
                    blackAlpha = newNode.score;
                else if(newNode.score < blackBeta)
                    blackBeta = newNode.score;
            } else {
                // white's turn
                if(newNode.score < whiteAlpha)
                    whiteAlpha = newNode.score;
                else if(newNode.score > whiteBeta)
                    whiteBeta = newNode.score;
            }
            if((root.depth % 2 == 0 && blackAlpha > blackBeta) ||
                (root.depth % 2 == 1 && whiteAlpha < whiteBeta)) {
                // prune. stop searching through children.
                if(root.depth % 2 == 0)
                    blackAlpha = blackBeta;
                else
                    whiteAlpha = whiteBeta;
                root.score = newNode.score;
                root.visited = true;
                System.out.println("Pruning!");
                return;
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
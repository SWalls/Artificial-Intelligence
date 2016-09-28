import java.util.*;

// SlidingBoard has a public field called size
// A size of 3 means a 3x3 board

// SlidingBoard has a method to getLegalMoves
//   ArrayList<SlidingMove> legalMoves = board.getLegalMoves();

// You can create possible moves using SlidingMove:
// This moves the piece at (row, col) into the empty slot
//   SlidingMove move = new SlidingMove(row, col);

// SlidingBoard can check a single SlidingMove for legality:
//   boolean legal = board.isLegalMove(move);

// SlidingBoard can check if a position is a winning one:
//   boolean hasWon = board.isSolved();

// SlidingBoard can perform a SlidingMove:
//   board.doMove(move);

// You can undo a move by saying the direction of the previous move
// For example, to undo the last move that moved a piece down into
// the empty space from above use:
//   board.undoMove(m, 0);

// You can dump the board to view with toString:
//   System.out.println(board);


class BFSBot extends SlidingPlayer {

    class Node {
        SlidingBoard board;
        int depth;
        ArrayList<SlidingMove> moves;
        public Node(SlidingBoard board, int depth) {
            this.board = board;
            this.depth = depth;
            moves = new ArrayList<SlidingMove>();
        }
    }
    
    int currentMove = 0;
    ArrayList<SlidingMove> correctMoves = null;
    Set<String> configsTried = new HashSet<String>();
    Queue<Node> q = new LinkedList<Node>();

    // The constructor gets the initial board
    public BFSBot(SlidingBoard _sb) {
        super(_sb);
        findSolution(_sb);
    }

    private void findSolution(SlidingBoard _sb) {
        currentMove = 0;
        correctMoves = null;
        configsTried = new HashSet<String>();
        q = new LinkedList<Node>();
        Node solvedNode = null;
        int currentDepth = -1;
        q.offer(new Node(_sb, 0));
        while(!q.isEmpty() && solvedNode == null) {
            Node node = q.poll();
            SlidingBoard board = node.board;
            if(board.isSolved()) {
                System.out.println("Found solution at depth "+node.depth);
                solvedNode = node;
                break;
            }
            if(node.depth > currentDepth) {
                currentDepth++;
                // System.out.println("Exploring depth "+currentDepth);
            }
            ArrayList<SlidingMove> legalMoves = board.getLegalMoves();
            for(SlidingMove move : legalMoves) {
                SlidingBoard newBoard = new SlidingBoard(board);
                newBoard.doMove(move);
                if(configsTried.contains(newBoard.toString())) {
                    continue;
                }
                configsTried.add(newBoard.toString());
                // System.out.println(newBoard);
                Node newNode = new Node(newBoard, node.depth+1);
                newNode.moves = new ArrayList<SlidingMove>(node.moves);
                newNode.moves.add(move);
                if(newBoard.isSolved()) {
                    System.out.println("Found solution at depth "+newNode.depth);
                    solvedNode = newNode;
                    break;
                }
                q.offer(newNode);
            }
        }
        if(solvedNode != null) {
            correctMoves = solvedNode.moves;
        } else {
            System.out.println("Failed to find a solution to the board.");
        }
    }
    
    // Perform a single move based on the current given board state
    public SlidingMove makeMove(SlidingBoard board) {
        if(currentMove >= correctMoves.size())
            findSolution(board);
        return correctMoves.get(currentMove++);
    }
}
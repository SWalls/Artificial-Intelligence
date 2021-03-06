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
        Node parent;
        SlidingMove move;
        public Node(SlidingBoard board, int depth, Node parent, SlidingMove move) {
            this.board = board;
            this.depth = depth;
            this.parent = parent;
            this.move = move;
        }
    }
    
    int currentMove;
    Deque<SlidingMove> correctMoves; // stack
    Set<String> configsTried;
    Queue<Node> q;

    // The constructor gets the initial board
    public BFSBot(SlidingBoard _sb) {
        super(_sb);
        findSolution(_sb);
    }

    private void findSolution(SlidingBoard _sb) {
        long memory = 1;
        long startTime = System.nanoTime();
        currentMove = 0;
        correctMoves = null;
        configsTried = new HashSet<String>();
        configsTried.add(_sb.toString());
        q = new LinkedList<Node>();
        Node solvedNode = null;
        int currentDepth = -1;
        q.offer(new Node(_sb, 0, null, null));
        while(!q.isEmpty() && solvedNode == null) {
            Node node = q.poll();
            SlidingBoard board = node.board;
            if(board.isSolved()) {
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
                memory++;
                Node newNode = new Node(newBoard, node.depth+1, node, move);
                if(newBoard.isSolved()) {
                    solvedNode = newNode;
                    break;
                }
                q.offer(newNode);
            }
        }
        long moves = 1;
        if(solvedNode != null) {
            System.out.println("BFS Solution depth: "+solvedNode.depth);
            moves = solvedNode.depth;
            correctMoves = new ArrayDeque<SlidingMove>();
            while(solvedNode.move != null) {
                correctMoves.push(solvedNode.move);
                solvedNode = solvedNode.parent;
            }
        } else {
            System.out.println("Failed to find a solution to the board.");
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        long bytes = memory*((_sb.size*_sb.size)+3);
        System.out.printf("Memory used: %d nodes (~%d bytes)\n", memory, bytes);
        System.out.printf("Memory used per move decision: ~%d nodes (%d bytes)\n", memory/moves, bytes/moves);
        System.out.printf("Execution time: %dms\n", duration);
        System.out.printf("Time per move decision: ~%dms\n", duration/moves);
        System.out.println();
    }
    
    // Perform a single move based on the current given board state
    public SlidingMove makeMove(SlidingBoard board) {
        if(correctMoves.size() == 0)
            findSolution(board);
        return correctMoves.pop();
    }
}
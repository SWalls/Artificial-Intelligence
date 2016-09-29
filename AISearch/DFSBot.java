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


class DFSBot extends SlidingPlayer {

    class Node {
        SlidingBoard board;
        int depth;
        Node parent;
        SlidingMove move;
        ArrayList<Node> children = new ArrayList<Node>();
        boolean visited = false;
        public Node(SlidingBoard board, int depth, Node parent, SlidingMove move) {
            this.board = board;
            this.depth = depth;
            this.parent = parent;
            this.move = move;
        }
    }
    
    int countBacktracks;
    int currentMove;
    Deque<SlidingMove> correctMoves; // stack
    Set<String> configsTried;

    // The constructor gets the initial board
    public DFSBot(SlidingBoard _sb) {
        super(_sb);
        findSolution(_sb);
    }

    private void findSolution(SlidingBoard _sb) {
        long memory = 1;
        long startTime = System.nanoTime();
        countBacktracks = 0;
        currentMove = 0;
        correctMoves = null;
        configsTried = new HashSet<String>();
        configsTried.add(_sb.toString());
        Node currentNode = new Node(_sb, 0, null, null);
        while(currentNode != null && !currentNode.board.isSolved()) {
            // System.out.println("Searching at depth " + currentNode.depth);
            SlidingBoard board = currentNode.board;
            ArrayList<SlidingMove> legalMoves = board.getLegalMoves();
            for(SlidingMove move : legalMoves) {
                SlidingBoard newBoard = new SlidingBoard(board);
                newBoard.doMove(move);
                // make sure not to repeat any board configurations
                if(configsTried.contains(newBoard.toString())) {
                    continue;
                }
                configsTried.add(newBoard.toString());
                memory++;
                Node newNode = new Node(newBoard, currentNode.depth+1, currentNode, move);
                currentNode.children.add(newNode);
            }
            Node nextNotVisitedChild = null;
            for(Node child : currentNode.children) {
                if(!child.visited) {
                    nextNotVisitedChild = child;
                    break;
                }
            }
            if(nextNotVisitedChild != null) {
                // visit the next child that hasn't been visited
                currentNode = nextNotVisitedChild;
            } else {
                // we've visited all children. backtrack.
                currentNode.visited = true;
                while(currentNode != null && currentNode.visited) {
                    currentNode = currentNode.parent;
                }
                countBacktracks++;
                // System.out.println("Backtracked to depth " + currentNode.depth);
            }
        }
        long moves = 1;
        if(currentNode != null && currentNode.board.isSolved()) {
            System.out.println("DFS Solution depth: " + currentNode.depth);
            System.out.println("Backtracks: " + countBacktracks);
            moves = currentNode.depth;
            correctMoves = new ArrayDeque<SlidingMove>();
            while(currentNode.move != null) {
                correctMoves.push(currentNode.move);
                currentNode = currentNode.parent;
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
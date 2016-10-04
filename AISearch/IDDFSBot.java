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


class IDDFSBot extends SlidingPlayer {

    class Node {
        SlidingBoard board;
        int depth;
        Node parent;
        SlidingMove move;
        ArrayList<Node> children = new ArrayList<Node>();
        boolean visited = false;
        public Node(SlidingBoard board, int depth, Node parent, SlidingMove move) {
            IDDFSBot.memoryUsed++;
            this.board = board;
            this.depth = depth;
            this.parent = parent;
            this.move = move;
        }
        public void eraseReferences() {
            IDDFSBot.memoryUsed--;
            IDDFSBot.memoryFreed++;
            parent = null;
            move = null;
            board = null;
            for(Node child : children) {
                child.eraseReferences();
            }
            children.clear();
        }
    }
    
    int countBacktracks;
    int currentMove;
    Deque<SlidingMove> correctMoves; // stack
    Set<String> configsTried;
    static long memoryUsed;
    static long memoryFreed;

    // The constructor gets the initial board
    public IDDFSBot(SlidingBoard _sb) {
        super(_sb);
        findSolution(_sb);
    }
    
    static final int INITIAL_DEPTH_LIMIT = 5;
    static final int DEPTH_INCREMENT = 3;

    private void findSolution(SlidingBoard _sb) {
        memoryUsed = 0;
        memoryFreed = 0;
        long startTime = System.nanoTime();
        countBacktracks = 0;
        currentMove = 0;
        correctMoves = null;
        int depthLimit = INITIAL_DEPTH_LIMIT;
        Node solvedNode = null;
        while((solvedNode = findDFSSolution(_sb, depthLimit)) == null || !solvedNode.board.isSolved()) {
            depthLimit += DEPTH_INCREMENT;
        }
        long moves = 1;
        if(solvedNode != null && solvedNode.board.isSolved()) {
            System.out.println("ID Solution depth: " + solvedNode.depth);
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
        long bytesUsed = memoryUsed*((_sb.size*_sb.size)+3);
        long bytesFreed = memoryFreed*((_sb.size*_sb.size)+3);
        System.out.println("Backtracks: " + countBacktracks);
        System.out.printf("Memory freed: %d nodes (~%d bytes)\n", memoryFreed, bytesFreed);
        System.out.printf("Memory used (after freeing): %d nodes (~%d bytes)\n", memoryUsed, bytesUsed);
        System.out.printf("Memory used per move decision: ~%f nodes (%f bytes)\n", ((float)memoryUsed)/moves, ((float)bytesUsed)/moves);
        System.out.printf("Execution time: %dms\n", duration);
        System.out.printf("Time per move decision: ~%dms\n", duration/moves);
        System.out.println();
    }

    private Node findDFSSolution(SlidingBoard _sb, int depthLimit) {
        configsTried = new HashSet<String>();
        configsTried.add(_sb.toString());
        Node currentNode = new Node(_sb, 0, null, null);
        while(currentNode != null && !currentNode.board.isSolved()) {
            // System.out.println("Searching at depth " + currentNode.depth);
            SlidingBoard board = currentNode.board;
            ArrayList<SlidingMove> legalMoves = board.getLegalMoves();
            Node nextNotVisitedChild = null;
            if(currentNode.depth < depthLimit) {
                for(SlidingMove move : legalMoves) {
                    // check if already has child node for this move
                    Node matchedChild = null;
                    for(Node child : currentNode.children) {
                        if(child.move.row == move.row && child.move.col == move.col) {
                            matchedChild = child;
                            break;
                        }
                    }
                    if(matchedChild != null) {
                        // we've already checked this move
                        continue;
                    }
                    // found the next move for which we should create a child node!
                    SlidingBoard newBoard = new SlidingBoard(board);
                    newBoard.doMove(move);
                    // make sure not to repeat any board configurations
                    if(configsTried.contains(newBoard.toString())) {
                        continue;
                    }
                    configsTried.add(newBoard.toString());
                    Node newNode = new Node(newBoard, currentNode.depth+1, currentNode, move);
                    currentNode.children.add(newNode);
                    nextNotVisitedChild = newNode;
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
                    // clear all children for garbage collection
                    for(Node child : currentNode.children) {
                        child.eraseReferences();
                    }
                    currentNode.children.clear();
                    currentNode = currentNode.parent;
                }
                countBacktracks++;
                // System.out.println("Backtracked to depth " + currentNode.depth);
            }
        }
        return currentNode;
    }
    
    // Perform a single move based on the current given board state
    public SlidingMove makeMove(SlidingBoard board) {
        if(correctMoves == null) {
            System.out.println("ERROR: Cannot execute moves.");
            System.exit(0);
        }
        if(correctMoves.size() == 0)
            findSolution(board);
        return correctMoves.pop();
    }
}
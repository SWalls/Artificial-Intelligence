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


class AStarBot extends SlidingPlayer {

    class Node {
        SlidingBoard board;
        int depth;
        Node parent;
        SlidingMove move;
        boolean visited = false;
        int estimateToGoal = 0;
        public Node(SlidingBoard board, int depth, Node parent, SlidingMove move, int estimateToGoal) {
            this.board = board;
            this.depth = depth;
            this.parent = parent;
            this.move = move;
            this.estimateToGoal = estimateToGoal;
        }
    }

    class NodeComparator implements Comparator<Node>
    {
        @Override
        public int compare(Node x, Node y)
        {
            int fx = x.depth + x.estimateToGoal;
            int fy = y.depth + y.estimateToGoal;
            if (fx < fy) {
                return -1;
            } else if (fx > fy) {
                return 1;
            }
            return 0;
        }
    }

    private int heuristic(SlidingBoard sb) {
        int movesAway = 0;
        for(int r=0; r<sb.size; r++) {
            for(int c=0; c<sb.size; c++) {
                int[] correctSpot = { sb.board[r][c] / sb.size, sb.board[r][c] % sb.size };
                int offBy = Math.abs(r - correctSpot[0]) + Math.abs(c - correctSpot[1]);
                movesAway += offBy;
            }
        }
        return movesAway;
    }
    
    final int INITIAL_DEPTH = 20;
    final int DEPTH_INCREMENT = 3;

    int countBacktracks;
    int currentMove;
    Deque<SlidingMove> correctMoves; // stack
    Set<String> configsTried;
    PriorityQueue<Node> q;

    // The constructor gets the initial board
    public AStarBot(SlidingBoard _sb) {
        super(_sb);
        findSolution(_sb);
    }

    private void findSolution(SlidingBoard _sb) {
        // performance statistics
        int depthLimit = INITIAL_DEPTH;
        long memory = 1;
        long startTime = System.nanoTime();
        // initialize variables
        currentMove = 0;
        correctMoves = null;
        configsTried = new HashSet<String>();
        configsTried.add(_sb.toString());
        Node solvedNode = null;
        q = new PriorityQueue<Node>(10, new NodeComparator());
        Node node = new Node(_sb, 0, null, null, heuristic(_sb));
        q.offer(node);
        // main loop
        while(!q.isEmpty()) {
            Node currentNode = q.poll();
            // System.out.println("Searching at depth " + currentNode.depth);
            SlidingBoard board = currentNode.board;
            if(board.isSolved()) {
                solvedNode = currentNode;
                break;
            }
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
                Node newNode = new Node(newBoard, currentNode.depth+1, currentNode, move, heuristic(newBoard));
                q.offer(newNode);
            }
        }
        long moves = 1;
        if(solvedNode != null) {
            System.out.println("A* Solution depth: " + solvedNode.depth);
            moves = solvedNode.depth;
            correctMoves = new ArrayDeque<SlidingMove>();
            while(solvedNode.move != null) {
                correctMoves.push(solvedNode.move);
                solvedNode = solvedNode.parent;
            }
        } else {
            System.out.println("Failed to find a solution to the board.");
        }
        // print performance statistics
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
import java.util.Random;
import java.util.ArrayList;

public class RandomBot extends OthelloPlayer {
    
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
    
    
    public RandomBot(Integer _color) {
        super(_color);
    }
    
    public OthelloMove makeMove(OthelloBoard board) {
        
        ArrayList<OthelloMove> moves = board.legalMoves(playerColor);
        
        Random r = new Random();
        int chosenMoveIndex = r.nextInt(moves.size());
        
        return moves.get(chosenMoveIndex);
    }
}
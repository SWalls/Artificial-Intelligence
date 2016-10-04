import java.util.Scanner;

public class HumanPlayer extends OthelloPlayer {
    
    private Scanner s;
    
    public HumanPlayer(Integer _color) {
        super(_color);
        s = new Scanner(System.in);
    }

    public OthelloMove makeMove(OthelloBoard board) {
        //System.out.println(board);
        
        while (true) {
            System.out.println("Which move?");
            String line = s.nextLine();
            String tokens[] = line.split(" ");
            OthelloMove m = new OthelloMove(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), playerColor);
            if (board.isLegalMove(m)) {
                return m;
            }
        }

    }
}
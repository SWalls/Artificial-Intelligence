
import java.util.ArrayList;

public class GreedyBot extends OthelloPlayer {
    
    public GreedyBot(Integer _color) {
        super(_color);
    }

    public int getMoveScore(OthelloBoard b, OthelloMove m) {
        if (!b.isLegalMove(m)) {
            return -1;
        }
        else {
            int score = 0;
            score += scoreUp(b, m);
            score += scoreDown(b, m);
            score += scoreLeft(b, m);
            score += scoreRight(b, m);
            score += scoreUpLeft(b, m);
            score += scoreUpRight(b, m);
            score += scoreDownLeft(b, m);
            score += scoreDownRight(b, m);
            
            return score;
        }
    }
    
    public OthelloMove makeMove(OthelloBoard b) {
        
        //System.out.println(b);
        
        ArrayList<OthelloMove> moves = b.legalMoves(playerColor);
        
        OthelloMove highestMove = moves.get(0);
        int highestScore = getMoveScore(b, highestMove);
        
        for (OthelloMove m : moves) {
            int score  = getMoveScore(b, m);
            //System.out.println(m + " -> " + score);
            if (score > highestScore) {
                highestScore = score;
                highestMove = m;
            }
        }
        //System.out.println("Made move: " + highestMove);
        //System.out.println("SCORE: " + highestScore);

        return highestMove;

    }

    public int scoreUp(OthelloBoard b, OthelloMove m) {
        int currRow = m.row - 1;
        while (currRow >= 0 && b.board[currRow][m.col] != m.player && b.board[currRow][m.col] != 0) {
            currRow--;
        }
        if (currRow >= 0 && b.board[currRow][m.col] == m.player) {
            return m.row - currRow - 1;
        }
        return 0;
    }
    
    
    public int scoreDown(OthelloBoard b, OthelloMove m) {
        int currRow = m.row + 1;
        while (currRow < b.size && b.board[currRow][m.col] != m.player && b.board[currRow][m.col] != 0) {
            currRow++;
        }
        if (currRow < b.size && b.board[currRow][m.col] == m.player) {
            return currRow - m.row - 1;
        }
        return 0;
    }
    
    public int scoreRight(OthelloBoard b, OthelloMove m) {
        int currCol = m.col + 1;
        while (currCol < b.size && b.board[m.row][currCol] != m.player && b.board[m.row][currCol] != 0) {
            currCol++;
        }
        if (currCol < b.size && b.board[m.row][currCol] == m.player) {
            return currCol - m.col - 1;
        }
        return 0;
    }
    
    
    public int scoreLeft(OthelloBoard b, OthelloMove m) {
        int currCol = m.col - 1;
        while (currCol >= 0 && b.board[m.row][currCol] != m.player && b.board[m.row][currCol] != 0) {
            currCol--;
        }
        if (currCol >= 0 && b.board[m.row][currCol] == m.player) {
            return m.col-currCol - 1;
        }
        return 0;
    }
    
    public int scoreUpRight(OthelloBoard b, OthelloMove m) {
        int currRow = m.row - 1;
        int currCol = m.col + 1;
        while (currCol < b.size && currRow >= 0 && b.board[currRow][currCol] != m.player && b.board[currRow][currCol] != 0) {
            currCol++;
            currRow--;
        }
        if (currCol < b.size && currRow >= 0 && b.board[currRow][currCol] == m.player) {
            return currCol - m.col - 1;
        }
        return 0;
    }
    
    
    public int scoreUpLeft(OthelloBoard b, OthelloMove m) {
        int currRow = m.row - 1;
        int currCol = m.col - 1;
        while (currCol >= 0 && currRow >= 0 && b.board[currRow][currCol] != m.player && b.board[currRow][currCol] != 0) {
            currCol--;
            currRow--;
        }
        if (currCol >= 0 && currRow >= 0 && b.board[currRow][currCol] == m.player) {
            return m.row - currRow - 1;
        }
        return 0;
    }
    
    
    public int scoreDownRight(OthelloBoard b, OthelloMove m) {
        int currRow = m.row + 1;
        int currCol = m.col + 1;
        while (currCol < b.size && currRow < b.size && b.board[currRow][currCol] != m.player && b.board[currRow][currCol] != 0) {
            currCol++;
            currRow++;
        }
        if (currCol < b.size && currRow < b.size && b.board[currRow][currCol] == m.player) {
            return currRow - m.row - 1;
        }
        return 0;
    }
    
    
    public int scoreDownLeft(OthelloBoard b, OthelloMove m) {
        int currRow = m.row + 1;
        int currCol = m.col - 1;
        while (currCol >= 0 && currRow < b.size && b.board[currRow][currCol] != m.player && b.board[currRow][currCol] != 0) {
            currCol--;
            currRow++;
        }
        if (currCol >= 0 && currRow < b.size && b.board[currRow][currCol] == m.player) {
            return currRow - m.row - 1;
        }
        return 0;
    }
}

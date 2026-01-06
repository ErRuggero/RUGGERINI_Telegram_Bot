import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Puzzle
{
    @SerializedName("game")
    private Game game;
    @SerializedName("puzzle")
    private PuzzleDetails puzzle;

    private String fen;

    public String getPgn()
    {
        return game.pgn;
    }

    public List<String> getSolution()
    {
        return puzzle.solution;
    }

    public String getFen()
    {
        return fen;
    }

    public void setFen(String fen)
    {
        this.fen = fen;
    }

    @Override
    public String toString()
    {
        return "Game: " + game.toString() + "\nPuzzle: " + puzzle.toString();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class Game
    {
        private String pgn;

        @Override
        public String toString()
        {
            return "PGN: " + pgn;
        }
    }

    private static class PuzzleDetails
    {
        private List<String> solution;

        public List<String> getSolution()
        {
            return solution;
        }

        @Override
        public String toString()
        {
            return "Solution: " + solution;
        }
    }
}

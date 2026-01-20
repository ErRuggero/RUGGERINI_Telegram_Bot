package Deserialized;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Puzzle
{
    @SerializedName("game")
    private Game game = new Game();
    @SerializedName("puzzle")
    private PuzzleDetails puzzle = new PuzzleDetails();

    private String fen;
    private String initialFen;

    private int numMoves = 0;

    public Puzzle()
    {}

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

    public void setSolution(List<String> solution)
    {
        this.puzzle.solution = solution;
    }

    public int getNumMoves()
    {
        return numMoves;
    }

    public List<String> getThemes()
    {
        return puzzle.themes;
    }

    public void increaseMoves()
    {
        numMoves++;
    }

    public void setFen(String fen)
    {
        this.fen = fen;
    }

    public void setInizialFen(String setInizialFen)
    {
        this.initialFen = setInizialFen;
    }

    public String getInitialFen()
    {
        return initialFen;
    }


    @Override
    public String toString()
    {
        return "Game: " + String.valueOf(game) + "\nPuzzle: " + String.valueOf(puzzle);
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
            return "PGN: " + String.valueOf(pgn);
        }
    }

    private static class PuzzleDetails
    {
        private List<String> solution = new ArrayList<>();

        private List<String> themes = new ArrayList<>();;

        public List<String> getSolution()
        {
            return solution;
        }

        @Override
        public String toString()
        {
            return "Solution: "+ String.valueOf(solution);
        }
    }
}

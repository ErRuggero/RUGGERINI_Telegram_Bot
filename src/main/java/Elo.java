import com.google.gson.annotations.SerializedName;

public class Elo {

    // USO IL "SerializedName" IN MODO DA DARE UN NOME DIVERSO AGLI ATTRIBUTI
    @SerializedName("chess_rapid")
    private GameMode chessRapid;

    @SerializedName("chess_bullet")
    private GameMode chessBullet;

    @SerializedName("chess_blitz")
    private GameMode chessBlitz;

    // Constructor
    public Elo(GameMode chessRapid, GameMode chessBullet, GameMode chessBlitz)
    {
        this.chessRapid = chessRapid;
        this.chessBullet = chessBullet;
        this.chessBlitz = chessBlitz;
    }

    // Stampo tutto
    @Override
    public String toString()
    {
        return "Chess Rapid:\n" + chessRapid.toString() + "\n" +
                "Chess Bullet:\n" + chessBullet.toString() + "\n" +
                "Chess Blitz:\n" + chessBlitz.toString();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    // Classe interna per rappresentare le modalità di gioco
    public static class GameMode
    {
        private Last last;
        private Best best;
        private Record record;

        public GameMode(Last last, Best best, Record record)
        {
            this.last = last;
            this.best = best;
            this.record = record;
        }

        @Override
        public String toString()
        {
            return "Last:\n" + last.toString() + "\n" +
                    "Best:\n" + best.toString() + "\n" +
                    "Record:\n" + record.toString();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    // Classe per salvare ultima partita giocata
    public static class Last
    {
        private int rating;
        private long date;

        public Last(int rating, long date)
        {
            this.rating = rating;
            this.date = date;
        }

        @Override
        public String toString()
        {
            return "Rating: " + rating + "\n" +
                    "Date: " + date;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    // Classe per salvare miglior partita giocata
    public static class Best
    {
        private int rating;
        private long date;

        public Best(int rating, long date)
        {
            this.rating = rating;
            this.date = date;
        }

        @Override
        public String toString()
        {
            return "Rating: " + rating + "\n" +
                    "Date: " + date;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    // Classe per salvare i record di sempre
    public static class Record
    {
        private int win;
        private int loss;
        private int draw;

        public Record(int win, int loss, int draw)
        {
            this.win = win;
            this.loss = loss;
            this.draw = draw;
        }

        @Override
        public String toString()
        {
            return "Wins: " + win + "\n" +
                    "Losses: " + loss + "\n" +
                    "Draws: " + draw;
        }
    }
}

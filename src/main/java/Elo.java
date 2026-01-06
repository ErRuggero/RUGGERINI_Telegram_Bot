import com.google.gson.annotations.SerializedName;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Elo
{
    // USO IL "SerializedName" IN MODO DA DARE UN NOME DIVERSO AGLLI ATTRIBUTI
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

    // no-arg constructor necessario per Gson
    public Elo()
    {}

    // Chiamare questo metodo subito dopo il parsing per normalizzare i campi mancanti
    public void normalize()
    {
        if (chessRapid == null)
            chessRapid = new GameMode();

        if (chessBullet == null)
            chessBullet = new GameMode();

        if (chessBlitz == null)
            chessBlitz = new GameMode();

        chessRapid.ensureDefaults();
        chessBullet.ensureDefaults();
        chessBlitz.ensureDefaults();

        // Aggiorna le date convertite
        chessRapid.updateDateConverted();
        chessBullet.updateDateConverted();
        chessBlitz.updateDateConverted();
    }

    // Stampo tutto (null-safe)
    @Override
    public String toString()
    {
        return "Chess Rapid:\n" + (chessRapid != null ? chessRapid.toString() : "Not played\n") + "\n" +
                "Chess Bullet:\n" + (chessBullet != null ? chessBullet.toString() : "Not played\n") + "\n" +
                "Chess Blitz:\n" + (chessBlitz != null ? chessBlitz.toString() : "Not played\n");
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

        // no-arg constructor necessario per Gson
        public GameMode()
        {}

        // Assicura che i sotto-oggetti non siano null e imposta default pragmatici
        void ensureDefaults()
        {
            if (last == null) last = new Last();
            if (best == null) best = new Best();
            if (record == null) record = new Record();
        }

        // Aggiorna i campi date_converted di last e best
        void updateDateConverted()
        {
            if (last != null) last.updateDateConverted();
            if (best != null) best.updateDateConverted();
        }

        @Override
        public String toString()
        {
            return "Last:\n" + (last != null ? last.toString() : "No last\n") + "\n" +
                    "Best:\n" + (best != null ? best.toString() : "No best\n") + "\n" +
                    "Record:\n" + (record != null ? record.toString() : "No record\n");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    // Classe per salvare ultima partita giocata
    public static class Last
    {
        // uso wrapper per poter differenziare "mancante" da 0
        private Integer rating;
        private Long date;

        private String date_converted; // NUOVO

        public Last(Integer rating, Long date)
        {
            this.rating = rating;
            this.date = date;
        }

        // no-arg constructor con valori null (mancanti)
        public Last()
        {}

        // Calcola e imposta date_converted a partire da date (epoch secondi)
        public void updateDateConverted()
        {
            if (date == null)
            {
                date_converted = null;
                return;
            }

            // SEMPRE epoch secondi
            this.date_converted = Instant.ofEpochSecond(this.date)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        @Override
        public String toString()
        {
            return "Rating: " + (rating != null ? rating : "N/A") + "\n" +
                    "Date: " + (date_converted != null ? date_converted : "N/A");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    // Classe per salvare miglior partita giocata
    public static class Best
    {
        private Integer rating;
        private Long date;

        private String date_converted; // NUOVO

        public Best(Integer rating, Long date)
        {
            this.rating = rating;
            this.date = date;
        }

        public Best()
        {}

        // Calcola e imposta date_converted a partire da date (epoch secondi)
        public void updateDateConverted()
        {
            if (date == null)
            {
                date_converted = null;
                return;
            }

            // SEMPRE epoch secondi
            this.date_converted = Instant.ofEpochSecond(this.date)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        @Override
        public String toString()
        {
            return "Rating: " + (rating != null ? rating : "N/A") + "\n" +
                    "Date: " + (date_converted != null ? date_converted : "N/A");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    // Classe per salvare i record di sempre
    public static class Record
    {
        private Integer win;
        private Integer loss;
        private Integer draw;

        public Record(Integer win, Integer loss, Integer draw)
        {
            this.win = win;
            this.loss = loss;
            this.draw = draw;
        }

        // default pragmatico: zero alle statistiche
        public Record()
        {
            this.win = 0;
            this.loss = 0;
            this.draw = 0;
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

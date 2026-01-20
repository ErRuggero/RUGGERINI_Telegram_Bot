package Deserialized;

import com.google.gson.annotations.SerializedName;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Elo
{
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

    // Chiamare subito dopo il parsing
    public void normalize()
    {
        if (chessRapid == null) chessRapid = new GameMode();
        if (chessBullet == null) chessBullet = new GameMode();
        if (chessBlitz == null) chessBlitz = new GameMode();

        chessRapid.ensureDefaults();
        chessBullet.ensureDefaults();
        chessBlitz.ensureDefaults();

        chessRapid.updateDateConverted();
        chessBullet.updateDateConverted();
        chessBlitz.updateDateConverted();
    }

    // ===================== TO STRING =====================

    @Override
    public String toString()
    {
        return """
               
               ─────────── ELO STATS ───────────
               %s
               %s
               %s
               ─────────────────────────────────
               """.formatted(
                formatGameMode("CHESS RAPID", chessRapid),
                formatGameMode("CHESS BULLET", chessBullet),
                formatGameMode("CHESS BLITZ", chessBlitz)
        );
    }

    private String formatGameMode(String title, GameMode mode)
    {
        if (mode == null)
        {
            return title + "\n  Not played\n";
        }

        return """
               %s
               ---------------------------------
               %s
               """.formatted(title, mode.toString());
    }

    // =====================================================
    // ===================== GAME MODE =====================
    // =====================================================

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

        public GameMode()
        {}

        void ensureDefaults()
        {
            if (last == null) last = new Last();
            if (best == null) best = new Best();
            if (record == null) record = new Record();
        }

        void updateDateConverted()
        {
            if (last != null) last.updateDateConverted();
            if (best != null) best.updateDateConverted();
        }

        @Override
        public String toString()
        {
            return """
              ▶ Last
            %s
            
              ▶ Best
            %s
            
              ▶ Record
            %s
            """.formatted(
                    indent(last),
                    indent(best),
                    indent(record)
            );
        }

        private String indent(Object obj)
        {
            if (obj == null)
                return "      No data";

            return obj.toString()
                    .indent(6)
                    .stripTrailing();
        }
    }

    // =====================================================
    // ======================== LAST =======================
    // =====================================================

    public static class Last
    {
        private Integer rating;
        private Long date;
        private String date_converted;

        public Last(Integer rating, Long date)
        {
            this.rating = rating;
            this.date = date;
        }

        public Last()
        {}

        void updateDateConverted()
        {
            if (date == null)
            {
                date_converted = null;
                return;
            }

            this.date_converted = Instant.ofEpochSecond(date)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        @Override
        public String toString()
        {
            return """
            Rating : %s
            Date   : %s
            """.formatted(
                    rating != null ? rating : "N/A",
                    date_converted != null ? date_converted : "N/A"
            );
        }
    }

    // =====================================================
    // ======================== BEST =======================
    // =====================================================

    public static class Best
    {
        private Integer rating;
        private Long date;
        private String date_converted;

        public Best(Integer rating, Long date)
        {
            this.rating = rating;
            this.date = date;
        }

        public Best()
        {}

        void updateDateConverted()
        {
            if (date == null)
            {
                date_converted = null;
                return;
            }

            this.date_converted = Instant.ofEpochSecond(date)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        @Override
        public String toString()
        {
            return """
            Rating : %s
            Date   : %s
            """.formatted(
                    rating != null ? rating : "N/A",
                    date_converted != null ? date_converted : "N/A"
            );
        }
    }

    // =====================================================
    // ======================= RECORD ======================
    // =====================================================

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

        public Record()
        {
            this.win = 0;
            this.loss = 0;
            this.draw = 0;
        }

        @Override
        public String toString()
        {
            return """
                Wins   : %d
                Losses : %d
                Draws  : %d
                """.formatted(win, loss, draw);
        }
    }
}

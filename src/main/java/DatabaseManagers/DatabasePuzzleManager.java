package DatabaseManagers;

import java.sql.*;
import java.util.List;

import API.LichessAPI;
import Deserialized.*;
import com.google.gson.Gson;
import database.Database;
import org.telegram.telegrambots.meta.api.objects.InputFile;

public class DatabasePuzzleManager
{
    private Connection connection;
    private static Database database;

    private static DatabasePuzzleManager instance;

    private DatabasePuzzleManager() throws SQLException
    {
        try
        {
            database = Database.getInstance();
            connection = database.getConnection();
        }
        catch (Exception e)
        {
            System.err.println("Impossibile per 'DatabaseManagers.DatabasePuzzleManager' inizializzare delle classi.");
        }
    }

    public static DatabasePuzzleManager getInstance() throws SQLException
    {
        if (instance == null)
        {
            instance = new DatabasePuzzleManager();
        }

        return instance;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public void addPuzzleStat(Long chatId, Puzzle puzzle, boolean win)
    {

        // -------------------------------------------------------------------
        // 1. Controllo connessione

        // Controllo connessione
        try
        {
            if (connection == null || !connection.isValid(5))
            {
                System.err.println("Connessione null o invalida");
            }
        }
        catch (SQLException e)
        {
            System.err.println("Connessione null o invalida");
        }

        try
        {

            // -------------------------------------------------------------------
            // 2. Recupero iduser corrispondente al chatId

            // Trova iduser corrispondente al chatId
            String querySelect = "SELECT iduser FROM users WHERE chatid = ? AND isinactive = FALSE";
            PreparedStatement stmt = connection.prepareStatement(querySelect);
            stmt.setLong(1, chatId);
            ResultSet risultato = stmt.executeQuery();

            if (!risultato.next())
            {
                System.err.println("Utente non trovato per chatId: " + chatId);
                return;
            }

            // Salvataggio dell'iduser
            int iduser = risultato.getInt("iduser");

            // Questo serve per poter salvare le soluzioni di un puzzle con le mosse che ci si aspetta dall'utente
            int numSoluzioni = 0;
            for (int i = 0; i < puzzle.getSolution().size(); i++)
            {
                if (i % 2 == 0)
                {
                    // Posizione indici dispari (0, 2, 4, ...)
                    numSoluzioni++;
                }
            }

            // -------------------------------------------------------------------
            // 3. Inserimento puzzle

            String queryInsert = "INSERT INTO puzzles (iduser, fen, iswon, totsolution, totmoves, ratiomoves) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt1 = connection.prepareStatement(queryInsert);

            stmt1.setInt(1, iduser);                       // id dell'utente
            stmt1.setString(2, puzzle.getFen());           // fen
            stmt1.setBoolean(3, win);                      // se vinto
            stmt1.setInt(4, numSoluzioni);                 // numero di soluzioni totali
            stmt1.setInt(5, puzzle.getNumMoves());         // numero di mosse totali

            // Si preleva il num di mosse fatte
            double mosse = puzzle.getNumMoves();

            // SERVE PER EVITARE DIVIONE n/0
            if (mosse == 0)
            {
                mosse = 1;
                stmt1.setDouble(6, 0.0);
            }
            else
            {
                stmt1.setDouble(6, Math.round(((double) numSoluzioni / mosse) * 1000.0) / 1000.0); // rapporto (3 cifre decimali)
            }


            stmt1.executeUpdate();
        }
        catch (SQLException e)
        {
            System.err.println("Errore nell'inserimento del puzzle");
            e.printStackTrace();
        }
    }

    public String addFavouritePuzzle(Long chatId)
    {
        // -------------------------------------------------------------------
        // 1. Controllo connessione

        // Controllo connessione
        try
        {
            if (connection == null || !connection.isValid(5))
            {
                return "Connessione non valida.";
            }
        }
        catch (SQLException e)
        {
            return "Connessione non valida.";
        }

        // -------------------------------------------------------------------
        // 2. Recupero iduser corrispondente al chatId
        try
        {
            // Recupero iduser a partire da chatId
            int iduser;
            String querySelect = "SELECT iduser FROM users WHERE chatid = ? AND isinactive = FALSE";

            try
            {
                PreparedStatement stmt = connection.prepareStatement(querySelect);
                stmt.setLong(1, chatId);


                ResultSet risultato = stmt.executeQuery();

                // Se trova utente lo salva
                if (!risultato.next())
                {
                    return "Utente non trovato.";
                }

                iduser = risultato.getInt("iduser");
            }
            catch (SQLException e)
            {
                System.err.println("Errore nel recupero dell'utente: " + e.getMessage());
                throw new RuntimeException(e);
            }

            // -------------------------------------------------------------------
            // 3. Recupero puzzle da Lichess e inserimento nel database

            LichessAPI lichessAPI = LichessAPI.getInstance();
            Puzzle puzzle = lichessAPI.getPuzzleWithId(chatId);

            String fen = puzzle.getInitialFen();
            List<String> listaSoluzioni = puzzle.getSolution();

            // Inserimento nel database con ON CONFLICT DO NOTHING (PostgreSQL)
            String queryInsert = "INSERT INTO favourite_puzzles (iduser, fen, solutions) VALUES (?, ?, ?) ON CONFLICT (iduser, fen) DO NOTHING";

            try
            {
                PreparedStatement stmt = connection.prepareStatement(queryInsert);

                stmt.setInt(1, iduser);
                stmt.setString(2, fen);
                stmt.setString(3, listaSoluzioni.toString());


                // Si controlla che il puzzle non sia già tra i preferiti
                int verifica = stmt.executeUpdate();
                if (verifica == 0)
                {
                    return "Puzzle già tra i preferiti. Ritorno allo stato iniziale.";
                }
            }
            catch (SQLException e)
            {
                System.err.println("Errore nell'inserimento del puzzle: " + e.getMessage());
                throw new RuntimeException(e);
            }

            return "Puzzle aggiunto ai preferiti.";
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "Errore nell'inserimento del puzzle.";
        }
    }

    public String showFavouritePuzzle(Long chatId)
    {
        // -------------------------------------------------------------------
        // 1. Controllo connessione

        // Controllo connessione
        try
        {
            if (connection == null || !connection.isValid(5))
            {
                return "Connessione non valida.";
            }
        }
        catch (SQLException e)
        {
            return "Connessione non valida.";
        }

        // -------------------------------------------------------------------
        // 2. Recupero iduser ATTIVO

        Long idUser = null;
        String querySelect = "SELECT iduser FROM users WHERE chatid = ? AND isinactive = 0";

        try
        {
            PreparedStatement stmt = connection.prepareStatement(querySelect);
            stmt.setLong(1, chatId);
            ResultSet risultato = stmt.executeQuery();

            // Se c'è risultato, lo salva
            if (risultato.next())
            {
                idUser = risultato.getLong("iduser");
            }
            else
            {
                return "Nessun account attivo trovato per questo utente.";
            }
        }
        catch (SQLException e)
        {
            return "Errore nel recupero dell'utente.";
        }

        // -------------------------------------------------------------------
        // 3. Recupero puzzle preferiti

        String query = "SELECT * FROM favourite_puzzles WHERE iduser = ? ORDER BY addtime DESC";
        StringBuilder sb = new StringBuilder();

        try
        {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setLong(1, idUser);
            ResultSet risultato = stmt.executeQuery();

            // Controllo se ci sono risultati
            if (!risultato.next())
            {
                return "Nessun puzzle preferito trovato.";
            }

            // Si salva tutti i puzzle preferiti tanti quanti ce ne sono
            do
            {
                int idPref = risultato.getInt("idfavourites");
                String fen = risultato.getString("fen");
                String solutions = risultato.getString("solutions");
                String addtime = risultato.getString("addtime");


                sb.append("-------------------------------------------------\n");
                sb.append("FavouriteID : ").append(idPref).append("\n");
                sb.append("FEN         : ").append(fen).append("\n");
                sb.append("Soluzioni   : ").append(solutions).append("\n");
                sb.append("Aggiunto il : ").append(addtime).append("\n");
            }
            while (risultato.next());
        }
        catch (SQLException e)
        {
            return "Errore nel prelevamento dei puzzle preferiti.";
        }

        return sb.toString();
    }

    public InputFile findAndPlayFavouritePuzzle(Long chatId, String idfavourite)
    {
        // ------------------------------------------------------------
        // 1. Controllo connessione
        try
        {
            if (connection == null || !connection.isValid(5))
            {
                return null;
            }
        }
        catch (SQLException e)
        {
            return null;
        }

        // ------------------------------------------------------------
        // 2. Recupero iduser ATTIVO dal chatId

        int iduser;
        String querySelect = "SELECT iduser FROM users WHERE chatid = ? AND isinactive = FALSE";

        try
        {
            PreparedStatement stmt = connection.prepareStatement(querySelect);
            stmt.setLong(1, chatId);
            ResultSet risultato = stmt.executeQuery();

            if (!risultato.next())
            {
                return null;
            }

            iduser = risultato.getInt("iduser");
        }
        catch (SQLException e)
        {
            return null;
        }

        // ------------------------------------------------------------
        // 3. Recupero puzzle preferito tramite idfavourites

        String query = "SELECT fen, solutions FROM favourite_puzzles WHERE idfavourites = ? AND iduser = ?";

        try
        {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(idfavourite));
            stmt.setInt(2, iduser);

            ResultSet risultato = stmt.executeQuery();

            if (!risultato.next())
            {
                // Puzzle non trovato
                return null;
            }

            // Salvo il fen e le soluzioni
            String fen = risultato.getString("fen");
            String soluzioni = risultato.getString("solutions");

            //
            LichessAPI lichessAPI = LichessAPI.getInstance();
            InputFile file = lichessAPI.fetchFavouritePuzzle(chatId, fen, soluzioni);

            if (file != null)
            {
                System.out.println("Preferito: " + idfavourite + " ----- Utente: " + chatId + " ----- Soluzione (pos dispari = user): " + soluzioni);
            }

            return file;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /*
    public String checkFavouritePuzzle(Long chatId)
    {
        // Controllo connessione
        try
        {
            if (connection == null || !connection.isValid(5))
            {
                return "Connessione non valida.";
            }
        }
        catch (SQLException e)
        {
            return "Connessione non valida.";
        }

        try
        {
            // ------------------------------------------------------------
            // 1. Recupero iduser ATTIVO
            int iduser;
            String getUserQuery = "SELECT iduser FROM users WHERE chatid = ? AND isinactive = FALSE";
            PreparedStatement psUser = connection.prepareStatement(getUserQuery);
            psUser.setLong(1, chatId);

            ResultSet rsUser = psUser.executeQuery();

            if (!rsUser.next())
            {
                return "Utente non trovato.";
            }

            iduser = rsUser.getInt("iduser");

            // ------------------------------------------------------------
            // 2. Recupero puzzle corrente da Lichess
            LichessAPI lichessAPI = LichessAPI.getInstance();
            Puzzle puzzle = lichessAPI.getPuzzleWithId(chatId);

            String fen = puzzle.getFen();
            String solutions = puzzle.getSolution().toString();

            // ------------------------------------------------------------
            // 3. Controllo se il puzzle è già nei preferiti
            String checkQuery = """
            SELECT 1 
            FROM favourite_puzzles 
            WHERE iduser = ? AND fen = ? AND solutions = ?
        """;

            PreparedStatement psCheck = connection.prepareStatement(checkQuery);
            psCheck.setInt(1, iduser);
            psCheck.setString(2, fen);
            psCheck.setString(3, solutions);

            ResultSet rsCheck = psCheck.executeQuery();

            if (rsCheck.next())
            {
                return "Puzzle già tra i preferiti.";
            }

            // ------------------------------------------------------------
            // Puzzle non presente → ok per l'inserimento
            return "OK";
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "Errore nel controllo dei puzzle preferiti.";
        }
    }
    */


    public String puzzleLog(Long chatId)
    {
        // -------------------------------------------------------------------
        // 1. Controllo connessione

        // Controllo connessione
        try
        {
            if (connection == null || !connection.isValid(5))
            {
                return "Connessione non valida.";
            }
        }
        catch (SQLException e)
        {
            return "Connessione non valida.";
        }

        // -------------------------------------------------------------------
        // 2. Recupero iduser ATTIVO

        Long idUser = null;

        String querySelect = "SELECT iduser FROM users WHERE chatid = ? AND isinactive = 0";

        try
        {
            PreparedStatement stmt = connection.prepareStatement(querySelect);

            stmt.setLong(1, chatId);
            ResultSet risultato = stmt.executeQuery();

            if (risultato.next())
            {
                idUser = risultato.getLong("iduser");
            }
            else
            {
                return "Nessun account attivo trovato per questo utente.";
            }
        }
        catch (SQLException e)
        {
            return "Errore nel recupero dell'utente.";
        }

        // -------------------------------------------------------------------
        // 3. Recupero puzzle

        String query = "SELECT * FROM puzzles WHERE iduser = ?";
        StringBuilder sb = new StringBuilder();

        try
        {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setLong(1, idUser);

            ResultSet risultato = stmt.executeQuery();

            if (!risultato.next())
            {
                return "Nessun puzzle trovato.";
            }

            do
            {
                int idPuzzle = risultato.getInt("idpuzzle");
                String fen = risultato.getString("fen");
                int isWon = risultato.getInt("iswon");
                int totSolution = risultato.getInt("totsolution");
                int totMoves = risultato.getInt("totmoves");
                double ratioMoves = risultato.getDouble("ratiomoves");

                sb.append("""
        
                ─────────────────────────────────
                Puzzle ID   : %d
                FEN         : %s
                Risultato   : %s
                Soluzioni   : %d
                Num Mosse   : %d
                Ratio       : %.3f
                """.formatted(
                        idPuzzle,
                        fen,
                        isWon == 1 ? "Vinto" : "Perso",
                        totSolution,
                        totMoves,
                        ratioMoves
                ));
            }
            while (risultato.next());
        }
        catch (SQLException e)
        {
            return "Errore nel prelevamento dei puzzle.";
        }

        return sb.toString();
    }
}

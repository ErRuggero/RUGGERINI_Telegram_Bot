package DatabaseManagers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Deserialized.*;
import database.Database;


public class DatabaseMatchManager
{
    private static DatabaseMatchManager instance;

    private Connection connection;
    private static Database database;

    public DatabaseMatchManager()
    {
        try
        {
            database = Database.getInstance();
            connection = database.getConnection();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Impossibile per 'DatabaseManagers.DatabaseMatchManager' inizializzare delle classi.");
        }
    }

    public static DatabaseMatchManager getInstance() throws Exception
    {
        if (instance == null)
        {
            instance = new DatabaseMatchManager();
        }

        return instance;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public void addMatch(Match match, int finalStatus)
    {
        // Controllo connessione
        try
        {
            if (connection == null || !connection.isValid(5))
            {
                System.err.println("Connessione null o invalida");
                return;
            }
        }
        catch (SQLException e)
        {
            System.err.println("Connessione null o invalida");
            return;
        }

        try
        {
            // Fa la query
            String query = "INSERT INTO matches (iduser_white, iduser_black, result) VALUES (?, ?, ?)";

            // Fa il prepare
            PreparedStatement stmt = connection.prepareStatement(query);

            String result = "";

            // 0 = nero vince, 1 = bianco vince, 2 = pareggio
            if (finalStatus == 0)
            {
                result = "BLACK";
            }
            else if (finalStatus == 1)
            {
                result = "WHITE";
            }
            else if (finalStatus == 2)
            {
                result = "DRAW";
            }

            stmt.setInt(1, match.userIdBianco().intValue());        // iduser_white
            stmt.setInt(2, match.userIdNero().intValue());          // iduser_black
            stmt.setString(3, result);                              // "WHITE", "BLACK", "DRAW"

            // Esegue la query
            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            System.err.println("Errore nell'inserimento del match");
            e.printStackTrace();
        }
    }

    public String matchLog(Long chatId)
    {
        StringBuilder sb = new StringBuilder();

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
        // 2. Recupera iduser ATTIVO

        Long idUser = null;

        String getIdUser = "SELECT iduser FROM users WHERE chatid = ? AND isinactive = 0";

        try
        {
            // Prepara lo statement
            PreparedStatement stmt = connection.prepareStatement(getIdUser);

            stmt.setLong(1, chatId);
            ResultSet risultato = stmt.executeQuery();

            // Se risultato ha qualcosa, lo salva
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
        // 3. Recupero match dell’utente ATTIVO

        String query = """
        SELECT m.idmatch,
               uw.username AS whiteUser,
               ub.username AS blackUser,
               m.result,
               m.finished
        FROM matches m
        JOIN users uw ON m.iduser_white = uw.iduser
        JOIN users ub ON m.iduser_black = ub.iduser
        WHERE m.iduser_white = ? OR m.iduser_black = ?
        ORDER BY m.finished DESC
    """;

        try
        {
            PreparedStatement stmt = connection.prepareStatement(query);

            // Serve per assicurarsi che l'utente sia o il bianco o il nero
            stmt.setLong(1, idUser);
            stmt.setLong(2, idUser);

            ResultSet risultato = stmt.executeQuery();

            // Serve per tenere conto se almeno una partita è stata trovata
            boolean partite = false;

            while (risultato.next())
            {
                partite = true;
                sb.append("""
                        
                        ─────────────────────────────────
                        Match ID   : %d
                        Bianco     : %s
                        Nero       : %s
                        Risultato  : %s
                        Finito     : %s
                        """.formatted(
                        risultato.getInt("idmatch"),
                        risultato.getString("whiteUser"),
                        risultato.getString("blackUser"),
                        risultato.getString("result"),
                        risultato.getString("finished")
                ));
            }

            if (partite == false)
            {
                return "Nessun match trovato per questo utente.";
            }
        }
        catch (SQLException e)
        {
            return "Errore durante il recupero dei match.";
        }

        return sb.toString();
    }
}
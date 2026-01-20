package DatabaseManagers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.Database;


public class DatabaseUsersManager
{
    private static DatabaseUsersManager instance;

    private static Database database;
    private Connection connection;

    public DatabaseUsersManager()
    {
        try
        {
            database = Database.getInstance();
            connection = database.getConnection();
        }
        catch (Exception e)
        {
            System.err.println("Impossibile per 'DatabaseManagers.DatabaseUsersManager' inizializzare delle classi.");
        }
    }

    // Ritorna l'istanza singleton
    public static DatabaseUsersManager getInstance() throws SQLException
    {
        if (instance == null)
        {
            instance = new DatabaseUsersManager();
        }
        return instance;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    // Metodo signUp
    public void signUp(Long chatId, String username)
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

        // Controllo se esiste già l'utente
        String queryCheck =  "SELECT iduser FROM users WHERE chatid = ? AND isinactive = FALSE";
        try
        {
            PreparedStatement stmt = connection.prepareStatement(queryCheck);

            stmt.setLong(1, chatId);
            ResultSet risultato = stmt.executeQuery();

            if (risultato.next())
            {
                // Se l'utente è già presente, si aggiorna solo il login
                login(chatId);
                return;
            }
        }
        catch (SQLException e)
        {
            System.err.println("Errore nel controllo utente esistente");
            e.printStackTrace();
            return;
        }


        // Se l'utente è nuovo, lo si inserisce
        String query = "INSERT INTO users (username, chatid) VALUES (?, ?)";
        try
        {
            PreparedStatement insertStmt = connection.prepareStatement(query);

            insertStmt.setString(1, username);
            insertStmt.setLong(2, chatId);
            insertStmt.executeUpdate();
        }
        catch (SQLException e)
        {
            System.err.println("Errore nell'inserimento utente");
            e.printStackTrace();
        }
    }


    private void login(Long chatId)
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

        // Si aggiorna solo il campo login
        String query = "UPDATE users SET login = CURRENT_TIMESTAMP WHERE chatid = ? AND isinactive = FALSE";
        try
        {
            PreparedStatement stmt = connection.prepareStatement(query);

            stmt.setLong(1, chatId);
            int risultato = stmt.executeUpdate();

            if (risultato == 0)
            {
                System.err.println("Login fallito: utente non attivo");
            }
        }
        catch (SQLException e)
        {
            System.err.println("Errore durante l'aggiornamento del login");
            e.printStackTrace();
        }
    }

    public boolean deleteAccount(Long chatId)
    {
        // Controllo connessione
        try
        {
            if (connection == null || !connection.isValid(5))
            {
                System.err.println("Connessione null o invalida");
                return false;
            }
        }
        catch (SQLException e)
        {
            System.err.println("Connessione null o invalida");
            return false;
        }

        String query = "UPDATE users SET isinactive = TRUE WHERE chatid = ? AND isinactive = FALSE";
        try
        {
            PreparedStatement stmt = connection.prepareStatement(query);

            stmt.setLong(1, chatId);
            int risultato = stmt.executeUpdate();

            // Check se ha funzionato
            if (risultato > 0)
            {
                return true;
            }

            return false;
        }
        catch (SQLException e)
        {
            System.err.println("Errore durante deleteAccount");
            e.printStackTrace();
            return false;
        }
    }

    public String showStatUser(Long chatId)
    {
        //return "Log di tutti i match:\n" + DatabaseManager.databaseMatchManager.matchLog(chatId) + "\n\nLog di tutti i puzzle:\n" + DatabaseManager.databasePuzzleManager.puzzleLog(chatId);

        return """
           
           ───────────── MATCH LOG ─────────────
           %s

           ───────────── PUZZLE LOG ────────────
           %s
           """.formatted(
                DatabaseManager.databaseMatchManager.matchLog(chatId),
                DatabaseManager.databasePuzzleManager.puzzleLog(chatId)
        );

    }
}

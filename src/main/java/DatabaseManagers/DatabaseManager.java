package DatabaseManagers;

import java.sql.SQLException;


public class DatabaseManager
{
    private static DatabaseManager instance;

    public static DatabaseMatchManager databaseMatchManager;
    public static DatabaseUsersManager databaseUsersManager;
    public static DatabasePuzzleManager databasePuzzleManager;

    public DatabaseManager()
    {
        try
        {
            // Tutte istanze dei DBManager associati
            databaseMatchManager = DatabaseMatchManager.getInstance();
            databaseUsersManager = DatabaseUsersManager.getInstance();
            databasePuzzleManager = DatabasePuzzleManager.getInstance();
        }
        catch (Exception e)
        {
            System.err.println("Impossibile associare uno o più database a 'DatabaseManagers.DatabaseManager'");
        }
    }

    // Ritorna l'istanza singleton
    public static DatabaseManager getInstance() throws SQLException
    {
        if (instance == null)
        {
            instance = new DatabaseManager();
        }

        return instance;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
}

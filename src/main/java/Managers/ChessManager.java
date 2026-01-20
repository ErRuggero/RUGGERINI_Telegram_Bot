package Managers;


public class ChessManager
{
    private static ChessManager instance;

    public static MatchManager matchManager;

    public ChessManager()
    {
        try
        {
            matchManager = MatchManager.getInstance();
        }
        catch (Exception e)
        {
            System.err.println("Impossibile per 'DatabaseManagers.DatabaseMatchManager' inizializzare delle classi.");
        }
    }

    public static ChessManager getInstance() throws Exception
    {
        if (instance == null)
        {
            instance = new ChessManager();
        }

        return instance;
    }


}

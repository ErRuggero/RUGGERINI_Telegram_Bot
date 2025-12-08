import java.sql.*;

public class Database
{
    private Connection connection;
    private static Database instance;

    private Database() throws SQLException
    {
        String url = "jdbc:sqlite:database/pokemon.db";
        connection = DriverManager.getConnection(url);
        System.out.println("Connesso al database");
    }

    // RITORNA UN ISTANZA DEL DATABASE
    public static Database getInstance() throws SQLException
    {
        if (instance == null)
        {
            instance = new Database();
        }

        return instance;
    }
}
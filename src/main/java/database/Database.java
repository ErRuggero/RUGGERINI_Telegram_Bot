package database;
import java.sql.*;
import java.io.File;

public class Database
{
    private Connection connection;
    private static Database instance;

    private Database() throws SQLException
    {
        // Path del DB (relativo alla root del progetto)
        File dbFile = new File("src/main/java/database/database.db");
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        // Connessione
        connection = DriverManager.getConnection(url);

        // Crea le tabelle del DB (se serve)
        createTables();
    }

    public static Database getInstance() throws SQLException
    {
        if (instance == null)
        {
            instance = new Database();
        }

        return instance;
    }

    private void createTables()
    {
        try
        {
            Statement stmt = this.connection.createStatement();

            // Creazione tabella users
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    iduser INTEGER PRIMARY KEY AUTOINCREMENT,
                    username VARCHAR(64) NOT NULL,
                    chatid INTEGER NOT NULL,
                    signup TEXT DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    login TEXT DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    isinactive BOOLEAN DEFAULT FALSE NOT NULL
                )
            """);

            // Creazione tabella puzzles
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS puzzles (
                idpuzzle INTEGER PRIMARY KEY AUTOINCREMENT,
                iduser INTEGER NOT NULL,
                fen VARCHAR(100) NOT NULL,
                iswon INTEGER NOT NULL,
                totsolution INTEGER NOT NULL,
                totmoves INTEGER NOT NULL,
                ratiomoves REAL NOT NULL,
                FOREIGN KEY (iduser) REFERENCES users(iduser)
            )
        """);

            // Creazione tabella matches
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS matches (
                idmatch INTEGER PRIMARY KEY AUTOINCREMENT,   
                iduser_white INTEGER NOT NULL,
                iduser_black INTEGER NOT NULL,        
                result VARCHAR(5) NOT NULL,
                finished TEXT DEFAULT CURRENT_TIMESTAMP NOT NULL,
                FOREIGN KEY (iduser_white) REFERENCES users(iduser),
                FOREIGN KEY (iduser_black) REFERENCES users(iduser)
            )
        """);

            // Creazione tabella favourite_puzzles
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS favourite_puzzles (
                idfavourites INTEGER PRIMARY KEY AUTOINCREMENT,
                iduser INTEGER NOT NULL,
                fen VARCHAR(100) NOT NULL,
                solutions TEXT NOT NULL,
                addtime TEXT DEFAULT CURRENT_TIMESTAMP NOT NULL,
                FOREIGN KEY (iduser) REFERENCES users(iduser),
                UNIQUE(iduser, fen)
            )
        """);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public Connection getConnection()
    {
        return connection;
    }
}
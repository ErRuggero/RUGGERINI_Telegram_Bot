import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import org.telegram.telegrambots.meta.api.objects.InputFile;

public class Chesslib
{
    private static Chesslib instance;

    public Chesslib()
    {}

    public static Chesslib getInstance() throws Exception
    {
        if (instance == null)
        {
            instance = new Chesslib();
        }

        return instance;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public InputFile createPuzzleWithPGN(String pgn)
    {
        // Il PGN viene convertito in FEN TRONCATO
        String fen = convertPGNToFEN(pgn);

        // Poi viene passato per generare una scacchiera
        return showBoard(fen);
    }

    public String convertPGNToFEN(String pgn)
    {
        // Crea una lista di mosse che vengono assegnate tramite il PGN
        MoveList list = new MoveList();
        list.loadFromSan(pgn);

        // Prende la traduzione del PGN (list.getFen()) e lo tronca
        return Chesslib.takeBeforeSpaces(list.getFen());
    }

    public String makeMove(String fen, String mossa)
    {
        // Scrive in maiuscolo la mossa
        mossa = mossa.toUpperCase();

        // Crea una scacchiera temporanea
        Board board = new Board();

        // Questo parte esiste puramente per evitare di fare lavoro inutile in più
        // (il metodo vuole come parametro un FEN per esteso, quindi per averlo aggiungo a quello troncato la parte finale)
        // (l'aggiunta è stata scelta così perché è giusto)
        fen = fen + " w - - 2 44";

        // Alla scacchiera viene passato il FEN
        board.loadFromFen(fen);

        // La mossa viene divisa in cella iniziale e finale
        String valInizio = mossa.substring(0, 2);  // es. "A2"
        String valFine = mossa.substring(2, 4);    // es. "H6"

        // Converte la posizione di partenza (ad esempio "A2") in un oggetto Square (cella)
        Square cellaInizio = convertToSquare(valInizio);
        Square cellaFine = convertToSquare(valFine);

        // Esegue la mossa sulla scacchiera
        board.doMove(new Move(cellaInizio, cellaFine));

        // Ritorna il FEN della scacchiera dopo la mossa
        return board.getFen();
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public InputFile showBoard(String fen)
    {
        // Fa' l'url per l'immagine: preleva il FEN e prende tutto prima dello spazio inutile
        String url = "https://www.chess.com/dynboard?fen=" + Chesslib.takeBeforeSpaces(fen) + "%20w%20-%20-%200%201&size=2";

        // Genera l'immagine
        return ChesscomAPI.generateBoardURL(url);
    }

    private Square convertToSquare(String posizione)
    {
        // "posizione" è una stringa che rappresenta la cella
        char lettera = posizione.charAt(0);     // 'A'
        char numero = posizione.charAt(1);      // '2'

        // Ottiene l'indice della colonna
        int indexColonna = lettera - 'A';   // 'h' -> 7, 'a' -> 0, etc.

        // Ottieni l'indice della riga
        int indexRiga = numero - '1';       // '2' -> 6, '1' -> 7, etc.

        // Usa l'indice del file e del rango per ottenere la casella
        return Square.values()[indexRiga * 8 + indexColonna];  // Mappa (indexRiga, indexColonna) a un valore dell'enum Square
    }

    public static String takeBeforeSpaces(String input)
    {
        // HA IL COMPITO DI RETURNARE UNA STRINGA PRIMA DI UNO SPAZIO DENTRO DI SE' (se ce l'ha)

        if (input == null)
        {
            return null;
        }

        int indexSpazio = input.indexOf(' ');

        if (indexSpazio == -1)
        {
            return input;
        }

        return input.substring(0, indexSpazio);
    }
}
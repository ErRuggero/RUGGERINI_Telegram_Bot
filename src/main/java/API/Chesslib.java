package API;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.util.List;

import Deserialized.*;


public class Chesslib
{
    private static Chesslib instance;

    public Chesslib()
    {}

    public static Chesslib getInstance()
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

    public MatchMoveResult matchMove(Match match, String mossa, Long chatId)
    {
        if (match == null)
        {
            return null;
        }

        // Controllo turno
        if (match.isWhite())
        {
            // Turno del Bianco
            if (!chatId.equals(match.userIdBianco()))
            {
                return new MatchMoveResult("Non è il tuo turno. Tocca al Bianco.", chatId, -1);
            }
        }
        else
        {
            // Turno del Nero
            if (!chatId.equals(match.userIdNero()))
            {
                return new MatchMoveResult("Non è il tuo turno. Tocca al Nero.", chatId, -1);
            }
        }

        // Controllo che mossa sia di 4 caratteri
        if (mossa.length() != 4)
        {
            return new MatchMoveResult("Mossa non valida.", chatId, -1);
        }

        // Scrive in maiuscolo la mossa
        mossa = mossa.toUpperCase();

        // Crea una scacchiera temporanea
        Board board = new Board();

        // Alla scacchiera viene passato il FEN
        board.loadFromFen(match.getFEN());

        // La mossa viene divisa in cella iniziale e finale
        String valInizio = mossa.substring(0, 2);  // es. "A2"
        String valFine = mossa.substring(2, 4);    // es. "H6"

        // Converte la posizione di partenza (ad esempio "A2") in un oggetto Square (cella)
        Square cellaInizio = convertToSquare(valInizio);
        Square cellaFine = convertToSquare(valFine);

        Move move = new Move(cellaInizio, cellaFine);

        // Preleva tutte le mosse legali possibili
        List<Move> mosseLegali = board.legalMoves();
        System.out.println("Mosse Legali: " + mosseLegali.toString());

        // Scorre tutte le mosse legali possibili e verifica che la mossa inserita sia tra queste
        boolean mossaValida = false;
        for (Move mossaLegale : mosseLegali)
        {
            if (mossaLegale.equals(move))
            {
                mossaValida = true;
                break;
            }
        }

        // Se non è valida, avvisa
        if (mossaValida == false)
        {
            return new MatchMoveResult("Mossa non valida.", chatId, -1);
        }

        // Altrimenti fa la mossa
        board.doMove(new Move(cellaInizio, cellaFine));

        // Set del fen del match
        match.setFEN(board.getFen());

        // Variabili per messaggio e stato
        String risultato = "";
        int status = -1; // -1 = partita in corso, 0 = nero vince, 1 = bianco vince, 2 = pareggio

        // Controlla se partita finita
        if (board.isMated())
        {
            risultato = "Scacco matto";
            // Chi ha il turno adesso ha perso, quindi l'avversario ha vinto

            /// TODO: RIGUARDARE SE VERO
            status = match.isWhite() ? 0 : 1;
        }
        else if (board.isStaleMate())
        {
            risultato = "Stalemate";
            status = 2;
        }
        else if (board.isInsufficientMaterial())
        {
            risultato = "Materiale insufficiente";
            status = 2;
        }
        else if (board.isKingAttacked())
        {
            risultato = "Scacco";
            status = -1;
        }

        // Costruisce il messaggio finale da restituire
        String messaggioFinale;

        if (!risultato.isEmpty())
        {
            messaggioFinale = risultato + "\nMosso: " + move.toString();
        }
        else
        {
            messaggioFinale = "Mosso: " + move.toString();
        }

        // Cambio del giocatore
        match.changeTurn();

        // Ritorna il risultato
        return new MatchMoveResult(messaggioFinale, showBoard(board.getFen()), board.getFen(), status);
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
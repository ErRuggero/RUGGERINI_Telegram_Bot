package API;

import DatabaseManagers.DatabaseManager;
import com.google.gson.Gson;
import database.Database;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import Deserialized.*;


public class LichessAPI
{
    private final String endpointBase = "https://lichess.org/api/";
    private HttpClient client = null;
    private static LichessAPI instance;
    private Gson deserializzatore = new Gson();

    private Map<Long, Puzzle> puzzleMap = new HashMap<>();          // Tiene conto di chi fa quale puzzle [chatId, Deserializzati.Puzzle]
    private Map<Long, Integer> puzzleIndexMap = new HashMap<>();    // Salva a che punto della soluzione è il puzzle [chatId, Integer]

    public LichessAPI()
    {
        client = HttpClient.newHttpClient();
    }

    public static LichessAPI getInstance() throws Exception
    {
        if (instance == null)
        {
            instance = new LichessAPI();
        }

        return instance;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public InputFile fetchPuzzle(Long chatId, String diffIns)
    {
        // -------------------------------------------------------------------
        // 1. PRELEVAMENTO DIFFICOLTA'

        int diffScelta = 1;
        String endpointFine = "";
        try
        {
            // Prova a convertire la stringa in un numero intero
            int num = Integer.parseInt(diffIns);

            // Verifica se il numero è compreso tra 1 e 5
            if (num >= 1 && num <= 5)
            {
                //System.out.println("La stringa è un numero valido e compreso tra 1 e 5: " + num);
                diffScelta = num;
            }
        }
        catch (NumberFormatException e)
        {
            // Se la stringa non è un numero valido, va avanti e difficoltà 1
            System.out.println("La stringa non è un numero valido.");
        }

        switch (diffScelta)
        {
            case 1:
                endpointFine = endpointBase + "puzzle/next?angle=&difficulty=easiest&color=white";
                break;
            case 2:
                endpointFine = endpointBase + "puzzle/next?angle=&difficulty=easier&color=white";
                break;
            case 3:
                endpointFine = endpointBase + "puzzle/next?angle=&difficulty=normal&color=white";
                break;
            case 4:
                endpointFine = endpointBase + "puzzle/next?angle=&difficulty=harder&color=white";
                break;
            case 5:
                endpointFine = endpointBase + "puzzle/next?angle=&difficulty=hardest&color=white";
                break;
        }

        // -------------------------------------------------------------------
        // 2. RICHIESTA PUZZLE E ASSEGNAZIONE PUZZLE

        Puzzle puzzle = null;
        int tentativi = 0;

        do
        {
            tentativi++;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpointFine))
                    .GET()
                    .build();

            try
            {
                HttpResponse<String> responseUser = client.send(request, HttpResponse.BodyHandlers.ofString());

                // Deserializza il puzzle
                puzzle = deserializzatore.fromJson(responseUser.body(), Puzzle.class);
            }
            catch (Exception e)
            {
                System.err.println("Errore nella richiesta API: " + e.getMessage());
                return null;
            }
        }
        while (!hasMateInTheme(puzzle));

        // Mostra le soluzioni in console
        System.out.println("Tentativi: " + tentativi + " ----- Utente: " + chatId.toString() + " ----- Soluzione (pos dispari = user): " + puzzle.getSolution().toString());

        // Salva chi fa quale puzzle
        puzzleMap.put(chatId, puzzle);

        // Salva a che punto della soluzione è il puzzle (0 perché è inizio)
        puzzleIndexMap.put(chatId, 0);

        // Prelevo il FEN iniziale del puzzle
        Chesslib chesslib = Chesslib.getInstance();
        String inizioFen = chesslib.convertPGNToFEN(puzzle.getPgn());

        // Salva nel puzzle il FEN
        puzzle.setFen(inizioFen);
        puzzle.setInizialFen(inizioFen);

        // -------------------------------------------------------------------
        // 3. CREAZIONE DELLA SCACCHIERA


        // Ritorna l'immagine della scacchiera
        /// TODO : TOGLIERE IL PGN E FARE IN MODO CHE VADA SOLO CON IL FEN!!!!
        //return chesslib.createPuzzleWithPGN(puzzle.getPgn());

        return chesslib.showBoard(inizioFen);
    }

    public InputFile fetchPuzzleRandom(Long chatId)
    {
        // Valore random tra 1 e 5 compreso
        return fetchPuzzle(chatId, String.valueOf((int)(Math.random() * 5) + 1));
    }

    public InputFile fetchFavouritePuzzle(Long chatId, String fen, String solutions)
    {
        // Crea un puzzle con fen del DB e lo setta
        Puzzle puzzle = new Puzzle();
        puzzle.setFen(fen);
        puzzle.setInizialFen(fen);

        // Converte la stringa solutions in List<String>
        String stringa = solutions.substring(1, solutions.length() - 1);
        List<String> listaSoluzioni = List.of(stringa.split(",\\s*"));

        // Setta le soluzioni
        puzzle.setSolution(listaSoluzioni);

        // Salva chi fa quale puzzle
        puzzleMap.put(chatId, puzzle);

        // Salva a che punto della soluzione è il puzzle (0 perché inizio)
        puzzleIndexMap.put(chatId, 0);

        // Ritorna l'immagine della scacchiera
        Chesslib chesslib = Chesslib.getInstance();
        return chesslib.showBoard(puzzle.getFen());
    }

    public int checkPuzzle(Long chatId, String risposta, boolean keepPlaying)
    {
        // -------------------------------------------------------------------
        // 1. PRELEVAMENTO PUZZLE E CONTROLLO SE CONTINUARE A GIOCARE O QUITTARE

        if (puzzleMap.get(chatId) == null)
        {
            // ERRORE
            return -1;
        }

        // Controlla che l'utente abbia inserito una possibile mossa
        if (risposta.length() != 4 && !risposta.equals("/quit"))
        {
            return -2;
        }

        // Preleva il puzzle associato all'id e le sue soluzioni
        Puzzle puzzle = puzzleMap.get(chatId);
        List<String> listaSoluzioni = puzzle.getSolution();

        // Preleva indice del punto a cui si è arrivati
        int index = puzzleIndexMap.get(chatId);

        // Se bisogna smettere...
        if (keepPlaying == false)
        {
            try
            {
                // Salva nel database dei puzzle che l'utente ha perso
                DatabaseManager.databasePuzzleManager.addPuzzleStat(chatId, puzzle, false);
                puzzleMap.remove(chatId);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }

            return 0;
        }

        // Se si è arrivati ad un punto dove più soluzione del massimo possibile
        // (è impossibile, ma nel caso...)
        if (index >= listaSoluzioni.size())
        {
            // ERRORE
            return -1;
        }

        // -------------------------------------------------------------------
        // 2. CHECK PUZZLE

        // Aumenta il numero di mosse fatte
        puzzle.increaseMoves();

        // Controllo soluzione
        // (la mossa del player è sempre pari nelle soluzioni, perché dispari è il nero)
        if (index % 2 == 0)
        {
            // Se corretto...
            if (risposta.equals(listaSoluzioni.get(index)))
            {
                try
                {
                    // ... fai la mossa
                    Chesslib chesslib = Chesslib.getInstance();
                    String nuovoFen = chesslib.makeMove(puzzle.getFen(), listaSoluzioni.get(index));

                    // Salva il nuovo FEN
                    puzzle.setFen(nuovoFen);

                    // Controllo se esistono altre soluzioni, e se sì muove allora il nero
                    if (index + 1 < listaSoluzioni.size() && (index + 1) % 2 == 1)
                    {
                        // Mossa del nero e salvataggio del FEN
                        nuovoFen = chesslib.makeMove(puzzle.getFen(), listaSoluzioni.get(index + 1));
                        puzzle.setFen(nuovoFen);
                        puzzleIndexMap.put(chatId, index + 2);
                    }
                    else
                    {
                        // Altrimenti avanza l'index di 1 così supera il limite e finisce
                        puzzleIndexMap.put(chatId, index + 1);
                    }

                    // Se index supera lunghezza delle soluzioni, allora fine
                    if (puzzleIndexMap.get(chatId) >= listaSoluzioni.size())
                    {
                        // Salva nel database che ha vinto
                        DatabaseManager.databasePuzzleManager.addPuzzleStat(chatId, puzzle, true);

                        // VINTO
                        return 2;
                    }
                    else
                    {
                        // GIUSTO, VA AVANTI
                        return 1;
                    }
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
            // Se sbagliato...
            else
            {
                // SBAGLIATO
                return 0;
            }
        }

        // ERRORE
        // (è abbastanza impossibile che questo accada siccome l'index deve esistere)
        return -1;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public InputFile getCurrentBoard(Long chatId)
    {
        // Controlla se esiste un puzzle per quell'id
        if (puzzleMap.get(chatId) == null)
        {
            return null;
        }

        // Nel caso lo preleva
        Puzzle puzzle = puzzleMap.get(chatId);

        // Se esiste una partita per quel user, allora stampa la scacchiera
        try
        {
            Chesslib chesslib = Chesslib.getInstance();
            return chesslib.showBoard(puzzle.getFen());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean hasMateInTheme(Puzzle puzzle)
    {
        // FA IN MODO CHE CONTINUI A CICLARE TUTTI I PUZZLE FINCHE' NON TROVA UN PUZZLE DOVE SI DEVE FARE SCACCO MATTO

        if (puzzle == null || puzzle.getThemes() == null)
            return false;

        Pattern pattern = Pattern.compile("mateIn\\d+");

        for (String tema : puzzle.getThemes())
        {
            if (pattern.matcher(tema).matches())
                return true;
        }

        return false;
    }

    public Puzzle getPuzzleWithId(Long chatId)
    {
        return puzzleMap.get(chatId);
    }
}
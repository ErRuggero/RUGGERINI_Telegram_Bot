import com.google.gson.Gson;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LichessAPI
{
    private final String endpointBase = "https://lichess.org/api/";
    private HttpClient client = null;
    private static LichessAPI instance;
    private Gson deserializzatore = new Gson();

    private Map<Long, Puzzle> puzzleMap = new HashMap<>();          // Tiene conto di chi fa quale puzzle [chatId, Puzzle]
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

        Puzzle puzzle;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpointFine))
                .GET()
                .build();

        try
        {
            HttpResponse<String> responseUser = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Deserializza il puzzle
            puzzle = deserializzatore.fromJson(responseUser.body(), Puzzle.class);

            // Mostra le soluzioni in console
            System.out.println("Utente: " + chatId.toString() + "\nSoluzione (pos dispari = user): " + puzzle.getSolution().toString());

            // Salva chi fa quale puzzle
            puzzleMap.put(chatId, puzzle);

            // Salva a che punto della soluzione è il puzzle (0 perché è inizio)
            puzzleIndexMap.put(chatId, 0);

            // Prelevo il FEN iniziale del puzzle
            Chesslib chesslib = Chesslib.getInstance();
            String inizioFen = chesslib.convertPGNToFEN(puzzle.getPgn());

            // Salva nel puzzle il FEN
            puzzle.setFen(inizioFen);
        }
        catch (Exception e)
        {
            System.err.println("Errore nella richiesta API: " + e.getMessage());
            return null;
        }

        // -------------------------------------------------------------------
        // 3. CREAZIONE DELLA SCACCHIERA

        try
        {
            Chesslib chesslib = Chesslib.getInstance();

            // Ritorna l'immagine della scacchiera
            return chesslib.createPuzzleWithPGN(puzzle.getPgn());
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public InputFile fetchPuzzleRandom(Long chatId)
    {
        Random random = new Random();

        // Valore random tra 1 e 5 compreso
        return fetchPuzzle(chatId, String.valueOf((int)(Math.random() * 5) + 1));
    }

    public int checkPuzzle(Long chatId, String risposta)
    {
        // -------------------------------------------------------------------
        // 1. PRELEVAMENTO PUZZLE

        if (puzzleMap.get(chatId) == null)
        {
            // ERRORE
            return -1;
        }

        // Preleva il puzzle associato all'id e le sue soluzioni
        Puzzle puzzle = puzzleMap.get(chatId);
        List<String> solution = puzzle.getSolution();

        // Preleva indice del punto a cui si è arrivati
        int index = puzzleIndexMap.get(chatId);

        // Se si è arrivati ad un punto dove più soluzione del massimo possibile
        // (è impossibile, ma nel caso...)
        if (index >= solution.size())
        {
            // ERRORE
            return -1;
        }

        // -------------------------------------------------------------------
        // 2. CHECK PUZZLE

        // Controllo soluzione
        // (la mossa del player è sempre pari nelle soluzioni, perché dispari è il nero)
        if (index % 2 == 0)
        {
            // Se corretto...
            if (risposta.equals(solution.get(index)))
            {
                try
                {
                    // ... fai la mossa
                    Chesslib chesslib = Chesslib.getInstance();
                    String nuovoFen = chesslib.makeMove(puzzle.getFen(), solution.get(index));

                    // Salva il nuovo FEN
                    puzzle.setFen(nuovoFen);

                    // Controllo se esistono altre soluzioni, e se sì muove allora il nero
                    if (index + 1 < solution.size() && (index + 1) % 2 == 1)
                    {
                        // Mossa del nero e salvataggio del FEN
                        nuovoFen = chesslib.makeMove(puzzle.getFen(), solution.get(index + 1));
                        puzzle.setFen(nuovoFen);
                        puzzleIndexMap.put(chatId, index + 2);
                    }
                    else
                    {
                        // Altrimenti avanza l'index di 1 così supera il limite e finisce
                        puzzleIndexMap.put(chatId, index + 1);
                    }

                    // Se index supera lunghezza delle soluzioni, allora fine
                    if (puzzleIndexMap.get(chatId) >= solution.size())
                    {
                        // FINITO CORRETTAMENTE
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

    public InputFile getCurrentBoard(Long chatId)
    {
        // Controlla se esiste un puzzle per quell'id
        if (puzzleMap.get(chatId) == null)
        {
            return null;
        }

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
}
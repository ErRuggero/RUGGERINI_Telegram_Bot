import API.ChesscomAPI;
import API.Chesslib;
import API.LichessAPI;
import DatabaseManagers.DatabaseManager;
import Deserialized.Match;
import Deserialized.MatchMoveResult;
import Managers.ChessManager;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import java.util.HashMap;
import java.util.Map;

public class TelegramBot implements LongPollingSingleThreadUpdateConsumer
{
    private final TelegramClient telegramClient = new OkHttpTelegramClient(MyConfiguration.getInstance().getProperty("BOT_TOKEN"));
    ChesscomAPI chesscomAPI;
    LichessAPI lichessAPI;
    Chesslib chesslib;
    DatabaseManager databaseManager;
    ChessManager chessManager;

    private Map<Long, String> statoConversazione = new HashMap<>();     // Tiene conto di quale utente è a quale stato di conversazione

    public TelegramBot()
    {
        try
        {
            chesscomAPI = ChesscomAPI.getInstance();
            lichessAPI = LichessAPI.getInstance();
            chesslib = Chesslib.getInstance();
            databaseManager = DatabaseManager.getInstance();
            chessManager = ChessManager.getInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void consume(Update update)
    {
        if (update.hasMessage())
        {
            // Preleva l'inserimento e diventa in minuscolo
            String input = update.getMessage().getText().toLowerCase();

            // Preleva chatId dell'utente
            Long chatId = update.getMessage().getChatId();

            // Variabili per salvare le varie risposte
            SendMessage message = null;
            SendMessage message2 = null;
            SendPhoto photo = null;
            SendPhoto photo2 = null;

            boolean isTesto = true;         // Indica se la risposta tiene testo                            [Testo = true / Foto = false]
            boolean isEntrambe = false;     // Indica se la risposta è composta sia da testo che da foto    [Testo e Foto = true / Solo uno = false]
            boolean isInvertito = false;    // Indica se prima si scrive foto e poi testo                   [Foto poi Testo = true / Testo poi Foto = false]
            boolean isMolti = false;

            if (input.equals("/start") && (!statoConversazione.containsKey(chatId)))
            {
                statoConversazione.put(chatId, "START");

                String username = update.getMessage().getFrom().getUserName();

                if (username == null)
                {
                    username = update.getMessage().getFrom().getFirstName();
                }

                DatabaseManager.databaseUsersManager.signUp(chatId, username);

                message = new SendMessage(chatId.toString(), "Benvenuto su Chessy!\n\nFare '/help' per vedere tutti i comandi.");
            }
            else if ((input.equals("/help") || input.equals("/?")) && ("START".equals(statoConversazione.get(chatId))))
            {
                message = new SendMessage(chatId.toString(), callHelp());
            }
            else if (input.startsWith("/chesscomuser ") && ("START".equals(statoConversazione.get(chatId))))
            {
                String user = input.substring(input.indexOf(" ") + 1);
                String risultato = chesscomAPI.fetchPlayer(user);
                message = new SendMessage(chatId.toString(), risultato);
            }
            else if ((input.equals("/userinfo") && ("START".equals(statoConversazione.get(chatId)))))
            {
                message = new SendMessage(chatId.toString(), DatabaseManager.databaseUsersManager.showStatUser(chatId));
            }
            //  || input.equals("/puzzle"))
            else if (((input.equals("/puzzle r") || input.equals("/puzzle rand") || input.equals("/puzzle random")  || input.equals("/puzzle_random")) && ("START".equals(statoConversazione.get(chatId)))))
            {
                statoConversazione.put(chatId, "PUZZLE");

                InputFile risultato = lichessAPI.fetchPuzzleRandom(chatId);

                if (risultato != null)
                {
                    photo = new SendPhoto(chatId.toString(), risultato);
                    message = new SendMessage(chatId.toString(), "Risolvi il puzzle! (Devi scrivere la cella di inizio e di fine attaccato, per esempio 'A1C3')\n\nGIOCHI COME IL BIANCO.");
                    isEntrambe = true;
                    isInvertito = true;
                }
                else
                {
                    message = new SendMessage(chatId.toString(), "Errore connessione con server per i puzzle. Riprova.");
                }
            }
            else if ((input.startsWith("/puzzle ") && !input.equals("/puzzle log")) && ("START".equals(statoConversazione.get(chatId))))
            {
                statoConversazione.put(chatId, "PUZZLE");

                String diff = input.substring(input.indexOf(" ") + 1);

                try
                {
                    // Controllo che sia un numero valido
                    int num = Integer.parseInt(diff);

                    InputFile risultato = lichessAPI.fetchPuzzle(chatId, diff);

                    if (risultato != null)
                    {
                        photo = new SendPhoto(chatId.toString(), risultato);
                        message = new SendMessage(chatId.toString(), "Risolvi il puzzle! (Devi scrivere la cella di inizio e di fine attaccato, per esempio 'A1C3')\n\nGIOCHI COME IL BIANCO.");
                        isEntrambe = true;
                        isInvertito = true;
                    }
                    else
                    {
                        message = new SendMessage(chatId.toString(), "Errore connessione con server per i puzzle. Riprova.");
                    }
                }
                catch (NumberFormatException e)
                {
                    message = new SendMessage(chatId.toString(), "Inserire un valore valido da 1 a 5.");
                }
            }
            else if (input.equals("/quit") && !("START".equals(statoConversazione.get(chatId))) && statoConversazione.get(chatId) != null)
            {
                // Verifica se l'utente è nello stato "PUZZLE"
                if ("PUZZLE".equals(statoConversazione.get(chatId)))
                {
                    // Reset dello stato e messaggio di conferma
                    statoConversazione.put(chatId, "START");
                    lichessAPI.checkPuzzle(chatId, input, false);   // FA FINIRE LA PARTITA
                    message = new SendMessage(chatId.toString(), "Hai abbandonato il puzzle. Verrà contato come sconfitta.");
                }
                else if ("MATCH_CREATE".equals(statoConversazione.get(chatId)))
                {
                    // Reset dello stato e messaggio di conferma
                    statoConversazione.put(chatId, "START");
                    message = new SendMessage(chatId.toString(), "Hai abbandonato la creazione della partita.");
                }
                else if ("IN_MATCH".equals(statoConversazione.get(chatId)))
                {
                    Match match = ChessManager.matchManager.findMatch(chatId);
                    //Deserializzati.Match match = DatabaseManagers.DatabaseManager.matchManager.findMatch(chatId);

                    Long bianco = match.userIdBianco();
                    Long nero = match.userIdNero();

                    boolean isChatIdWhite = chatId.equals(bianco);

                    /*
                    // Controllo: può arrendersi solo il colore del turno corrente
                    if (match.isWhite() != isChatIdWhite)
                    {
                        message = new SendMessage(chatId.toString(), "Non puoi arrenderti ora: non è il tuo turno.");
                    }
                     */

                    String arreso;
                    int finalStatus;

                    if (match.isWhite())
                    {
                        arreso = "Bianco";
                        finalStatus = 0;    // -1 = partita in corso, 0 = nero vince, 1 = bianco vince, 2 = pareggio
                    }
                    else
                    {
                        arreso = "Nero";
                        finalStatus = 1;    // -1 = partita in corso, 0 = nero vince, 1 = bianco vince, 2 = pareggio
                    }

                    isMolti = true;

                    ChessManager.matchManager.endMatch(match, finalStatus);

                    message = new SendMessage(bianco.toString(), "Il " + arreso + " si è arreso.\n\n-- PARTITA FINITA --");
                    message2 = new SendMessage(nero.toString(), "Il " + arreso + " si è arreso.\n\n-- PARTITA FINITA --");

                    // Aggiorno lo stato della conversazione
                    statoConversazione.put(bianco, "START");
                    statoConversazione.put(nero, "START");
                }
                else
                {
                    // Se l'utente non è nel puzzle, ignora /quit o invia un messaggio
                    message = new SendMessage(chatId.toString(), "Non sei né in un puzzle né in una partita. Usa /help per vedere cosa fare.");
                }
            }
            // Se l'utente sta rispondendo al puzzle
            else if ("PUZZLE".equals(statoConversazione.get(chatId)))
            {
                int risultato = lichessAPI.checkPuzzle(chatId, input, true);

                switch (risultato)
                {
                    case -2:
                        message = new SendMessage(chatId.toString(), "Mossa non valida.");
                        break;
                    case -1:    // ERRORE
                        message = new SendMessage(chatId.toString(), "Errore.");
                        break;
                    case 0:     // SBAGLIATO
                        message = new SendMessage(chatId.toString(), "Sbagliato! Riprova.");
                        break;
                    case 1:     // GIUSTO, VAI AVANTI
                        message = new SendMessage(chatId.toString(), "Corretto! Vai avanti.");
                        photo = new SendPhoto(chatId.toString(), lichessAPI.getCurrentBoard(chatId));
                        isEntrambe = true;
                        isInvertito = true;
                        break;
                    case 2:     // RISOLTO QUIZ
                        message = new SendMessage(chatId.toString(), "Bravo! Puzzle risolto.\n\nSalvare nei preferiti (Si/No)?");
                        photo = new SendPhoto(chatId.toString(), lichessAPI.getCurrentBoard(chatId));
                        isEntrambe = true;
                        isInvertito = true;
                        statoConversazione.put(chatId, "PUZZLE_FAVOURITES");
                        break;
                }
            }
            else if ((input.equals("/puzzlefav s") || input.equals("/puzzlefav show"))  && ("START".equals(statoConversazione.get(chatId))))
            {
                message = new SendMessage(chatId.toString(), DatabaseManager.databasePuzzleManager.showFavouritePuzzle(chatId));
            }
            else if ((input.startsWith("/puzzlefav p ") || input.startsWith("/puzzlefav play "))  && ("START".equals(statoConversazione.get(chatId))))
            {
                String idfavourite = "";
                if (input.startsWith("/puzzlefav play "))
                {
                    idfavourite = input.substring("/puzzlefav play ".length()).trim();
                }
                else
                {
                    idfavourite = input.substring("/puzzlefav p ".length()).trim();
                }

                InputFile file = DatabaseManager.databasePuzzleManager.findAndPlayFavouritePuzzle(chatId, idfavourite);

                if (file != null)
                {
                    statoConversazione.put(chatId, "PUZZLE");

                    photo = new SendPhoto(chatId.toString(), file);
                    message = new SendMessage(chatId.toString(), "Risolvi il puzzle! (Devi scrivere la cella di inizio e di fine attaccato, per esempio 'A1C3')\n\nGIOCHI COME IL BIANCO.");
                    isEntrambe = true;
                    isInvertito = true;
                }
                else
                {
                    message = new SendMessage(chatId.toString(), "Errore. Puzzle preferito non trovato. Controlla che l'id inserito sia giusto.");
                }
            }
            else if ("PUZZLE_FAVOURITES".equals(statoConversazione.get(chatId)))
            {
                if (input.equals("si") || input.equals("sì") || input.equals("si'") || input.equals("yes"))
                {
                    //String risultato = DatabaseManager.databasePuzzleManager.addFavouritePuzzle(chatId);

                    /*
                    if (risultato == "Puzzle aggiunto ai preferiti")
                    {

                    }
                    */
                    statoConversazione.put(chatId, "START");

                    message = new SendMessage(chatId.toString(), DatabaseManager.databasePuzzleManager.addFavouritePuzzle(chatId));
                }
                else if (input.equals("no"))
                {
                    // Aggiorno lo stato della conversazione
                    statoConversazione.put(chatId, "START");

                    message = new SendMessage(chatId.toString(), "Puzzle non salvato.");
                }
                else
                {
                    message = new SendMessage(chatId.toString(), "Errore. Inserire se si vuole procedere con il salvataggio (Sì/No).");
                }
            }
            else if ((input.equals("/match create") || input.equals("/match c")  || input.equals("/matchcreate")) && ("START".equals(statoConversazione.get(chatId))))
            {
                statoConversazione.put(chatId, "MATCH_CREATE");
                message = new SendMessage(chatId.toString(), "Scegli con cosa vuoi giocare. \n(B = bianco, N = Nero, R = random).");
            }
            else if ("MATCH_CREATE".equals(statoConversazione.get(chatId)))
            {
                if (input.equals("b") || input.equals("n") || input.equals("r"))
                {
                    statoConversazione.put(chatId, "MATCH_PENDING");

                    String username = update.getMessage().getFrom().getUserName();

                    if (username == null)
                    {
                        username = update.getMessage().getFrom().getFirstName();

                    }

                    ChessManager.matchManager.pending(chatId, username, input);
                    //matchManager.pending(chatId, username, input);

                    message = new SendMessage(chatId.toString(), "Hai creato una partita. Matchmaking iniziato. Attendi.");
                }
                else
                {
                    message = new SendMessage(chatId.toString(), "Scegli con cosa vuoi giocare. \n(B = bianco, N = Nero, R = random).");
                }
            }
            else if ((input.equals("/match find") || input.equals("/match f") || input.equals("/matchfind")) && ("START".equals(statoConversazione.get(chatId))))
            {
                message = new SendMessage(chatId.toString(), ChessManager.matchManager.checkPending());
            }
            else if ((input.startsWith("/match join ") || input.startsWith("/match j ")) && ("START".equals(statoConversazione.get(chatId))))
            {
                String join;
                if (input.startsWith("/match join "))
                {
                    join = input.substring("/match join ".length()).trim();
                }
                else
                {
                    join = input.substring("/match j ".length()).trim();
                }

                String username = update.getMessage().getFrom().getUserName();

                if (username == null)
                {
                    username = update.getMessage().getFrom().getFirstName();
                }

                // Provo a far entrare il giocatore nella partita
                Match match = ChessManager.matchManager.matchJoin(chatId, join, username);

                if (match.userIdBianco() == null || match.userIdNero() == null)
                {
                    message = new SendMessage(chatId.toString(), "Errore. Riprova.");
                }
                else
                {
                    // Imposto le variabili di stato
                    isMolti = true;
                    isEntrambe = true;
                    isInvertito = true;

                    // Invio le foto della partita
                    photo = new SendPhoto(match.userIdBianco().toString(), ChessManager.matchManager.beginMatch());
                    photo2 = new SendPhoto(match.userIdNero().toString(), ChessManager.matchManager.beginMatch());

                    // Invio i messaggi ai giocatori
                    message = new SendMessage(match.userIdBianco().toString(), "Partita avviata!\nPer muovere devi scrivere la cella di inizio e di fine attaccato, per esempio 'A1C3'.\nN.B.: Ricorda, si parte a contare da in basso a sinistra (A1)\n\nTU GIOCHI COME IL: BIANCO");
                    message2 = new SendMessage(match.userIdNero().toString(),"Partita avviata!\nPer muovere devi scrivere la cella di inizio e di fine attaccato, per esempio 'A1C3'\nN.B.: Ricorda, si parte a contare da in basso a sinistra (A1)\n\nTU GIOCHI COME IL: NERO");

                    // Aggiorno lo stato della conversazione
                    statoConversazione.put(match.userIdBianco(), "IN_MATCH");
                    statoConversazione.put(match.userIdNero(), "IN_MATCH");
                }
            }
            else if ("IN_MATCH".equals(statoConversazione.get(chatId)))
            {
                Match match = ChessManager.matchManager.findMatch(chatId);
                MatchMoveResult mmr = chesslib.matchMove(match, input, chatId);

                if (mmr.getImage() == null)
                {
                    message = new SendMessage(chatId.toString(), mmr.getMessage());
                }
                else
                {
                    isEntrambe = true;
                    isInvertito = true;
                    isMolti = true;

                    photo = new SendPhoto(match.userIdBianco().toString(), chesslib.showBoard(mmr.getFEN()));
                    photo2 = new SendPhoto(match.userIdNero().toString(), chesslib.showBoard(mmr.getFEN()));

                    if (mmr.getEnd() == true)
                    {
                        ChessManager.matchManager.endMatch(match, mmr.getFinalStatus());

                        message =  new SendMessage(match.userIdBianco().toString(), mmr.getMessage() + "\n\n -- PARTITA FINITA -- ");
                        message2 =  new SendMessage(match.userIdNero().toString(), mmr.getMessage() + "\n\n -- PARTITA FINITA -- ");

                        // Aggiorno lo stato della conversazione
                        statoConversazione.put(match.userIdBianco(), "START");
                        statoConversazione.put(match.userIdNero(), "START");
                    }
                    else
                    {
                        message =  new SendMessage(match.userIdBianco().toString(), mmr.getMessage());
                        message2 =  new SendMessage(match.userIdNero().toString(), mmr.getMessage());
                    }
                }
            }
            else if ((input.startsWith("/reset account") || (input.startsWith("/resetaccount")) && ("START".equals(statoConversazione.get(chatId)))))
            {
                // Aggiorno lo stato della conversazione
                statoConversazione.put(chatId, "RESET");

                message = new SendMessage(chatId.toString(), "Sei sicuro di resettare tutti i progressi fatti (Sì/No)?");
            }
            else if ("RESET".equals(statoConversazione.get(chatId)))
            {
                if (input.equals("si") || input.equals("sì") || input.equals("si'") || input.equals("yes"))
                {
                    boolean risultato = DatabaseManager.databaseUsersManager.deleteAccount(chatId);

                    if (risultato)
                    {
                        // Così bisogna rifare /start
                        statoConversazione.remove(chatId);
                        message = new SendMessage(chatId.toString(), "Account resettato con successo. Fare '/start' per cominciare.");
                    }
                    else
                    {
                        // Aggiorno lo stato della conversazione
                        statoConversazione.put(chatId, "START");

                        message = new SendMessage(chatId.toString(), "Account NON resettato. Sono stati riscontrati degli errori.");
                    }
                }
                else if (input.equals("no"))
                {
                    // Aggiorno lo stato della conversazione
                    statoConversazione.put(chatId, "START");

                    message = new SendMessage(chatId.toString(), "Reset dell'account annullato.");
                }
                else
                {
                    message = new SendMessage(chatId.toString(), "Errore. Inserire se si vuole procedere con il reset (Sì/No).");
                }
            }
            else if (input.startsWith("/match log") && ("START".equals(statoConversazione.get(chatId))))
            {
                String risultato = DatabaseManager.databaseMatchManager.matchLog(chatId);

                message = new SendMessage(chatId.toString(), "Log di tutti i match:\n" + risultato);
            }
            else if ((input.startsWith("/puzzle log") || input.startsWith("/puzzlelog")) && ("START".equals(statoConversazione.get(chatId))))
            {
                String risultato = DatabaseManager.databasePuzzleManager.puzzleLog(chatId);

                message = new SendMessage(chatId.toString(), "Log di tutti i puzzle:\n" + risultato);
            }
            else
            {
                if ((!statoConversazione.containsKey(chatId)))
                {
                    message = new SendMessage(chatId.toString(), "Fai '/start' per cominciare!");
                }
                else
                {
                    message = new SendMessage(chatId.toString(), "Comando non riconosciuto. Fai '/help' o '/?' per vedere i comandi possibili.");
                }
            }

            //////////////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////////////////////////////////////////////////////

            if (message != null || photo != null)
            {
                try
                {
                    if (isTesto == true && isEntrambe == false && isMolti == false)
                    {
                        telegramClient.execute(message);
                    }
                    else if (isTesto == false && isEntrambe == false && isMolti == false)
                    {
                        telegramClient.execute(photo);
                    }
                    else if ((((isTesto == true) || (isTesto == false)) && isEntrambe == true) && isInvertito == true && isMolti == false)
                    {
                        telegramClient.execute(photo);
                        telegramClient.execute(message);
                    }
                    else if ((((isTesto == true) || (isTesto == false)) && isEntrambe == true) && isInvertito == false && isMolti == false)
                    {
                        telegramClient.execute(message);
                        telegramClient.execute(photo);
                    }
                    else if (isTesto == true && isEntrambe == false && isInvertito == false && isMolti == true)
                    {
                        telegramClient.execute(message);
                        telegramClient.execute(message2);
                    }
                    else if (isTesto == true && isEntrambe == true && isInvertito == false && isMolti == true)
                    {
                        telegramClient.execute(message);
                        telegramClient.execute(message2);
                        telegramClient.execute(photo);
                        telegramClient.execute(photo2);
                    }
                    else if (isTesto == true && isEntrambe == true && isInvertito == true && isMolti == true)
                    {
                        telegramClient.execute(photo);
                        telegramClient.execute(message);
                        telegramClient.execute(photo2);
                        telegramClient.execute(message2);
                    }
                }
                catch (TelegramApiException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public String callHelp()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Comandi disponibili:\n\n");
        sb.append("'/?' oppure '/help'\nMostra elenco comandi.\n\n");
        sb.append("'/userinfo'\nMostra statistiche personali.\n\n");
        sb.append("'/chesscomuser {username}'\nMostra dati da chess.com per quell'user.\n\n");
        sb.append("'/puzzle r' oppure '/puzzle random'\nGenera un puzzle da risolve di difficoltà randomica.\n\n");
        sb.append("'/puzzle {1-5}'\nGenera un puzzle da risolve di difficoltà scelta da 1 (facile) a 5 (difficile).\n\n");
        sb.append("'/puzzle log'\nMostra il log di tutti i puzzle fatti.\n\n");
        sb.append("'/quit'\nPer uscire dal puzzle o match che si sta facendo.\n\n");
        sb.append("'/match create'\nAvvia il matchmaking della partita.\n\n");
        sb.append("'/match find'\nMostra tutte le partite in matchmaking.\n\n");
        sb.append("'/match join {codice}'\nEntra nella partita.\n\n");
        sb.append("'/reset account'\nElimina dal database l'account e tutto ciò a cui è legato.\n\n");

        return sb.toString();
    }
}
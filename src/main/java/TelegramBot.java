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
    private Map<Long, String> statoConversazione = new HashMap<>();     // Tiene conto di quale utente è a quale stato di conversazione

    public TelegramBot()
    {
        try
        {
            chesscomAPI = ChesscomAPI.getInstance();
            lichessAPI = LichessAPI.getInstance();
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
            SendPhoto photo = null;

            boolean isTesto = true;         // Indica se la risposta tiene testo                            [Testo = true / Foto = false]
            boolean isEntrambe = false;     // Indica se la risposta è composta sia da testo che da foto    [Testo e Foto = true / Solo uno = false]
            boolean isInvertito = false;    // Indica se prima si scrive foto e poi testo                   [Foto poi Testo = true / Testo poi Foto = false]

            if (input.equals("/help") || input.equals("/?"))
            {
                message = new SendMessage(chatId.toString(), callHelp());
            }
            else if (input.startsWith("/userinfo "))
            {
                String user = input.substring(input.indexOf(" ") + 1);
                String risultato = chesscomAPI.fetchPlayer(user);
                message = new SendMessage(chatId.toString(), risultato);
            }
            else if ((input.equals("/puzzle r") || input.equals("/puzzle rand") || input.equals("/puzzle random") || input.equals("/puzzle")) && !("PUZZLE".equals(statoConversazione.get(chatId))))
            {
                statoConversazione.put(chatId, "PUZZLE");

                InputFile risultato = lichessAPI.fetchPuzzleRandom(chatId);

                if (risultato != null)
                {
                    photo = new SendPhoto(chatId.toString(), risultato);
                    message = new SendMessage(chatId.toString(), "Risolvi il puzzle! (Devi scrivere la cella di inizio e di fine attaccato, per esempio 'A1C3')\nNota: i puzzle non consistono solo nel fare scacco matto.");
                    isEntrambe = true;
                    isInvertito = true;
                }
                else
                {
                    message = new SendMessage(chatId.toString(), "Errore connessione con server per i puzzle. Riprova.");
                }
            }
            else if (input.startsWith("/puzzle ") && !("PUZZLE".equals(statoConversazione.get(chatId))))
            {
                statoConversazione.put(chatId, "PUZZLE");

                String diff = input.substring(input.indexOf(" ") + 1);
                InputFile risultato = lichessAPI.fetchPuzzle(chatId, diff);

                if (risultato != null)
                {
                    photo = new SendPhoto(chatId.toString(), risultato);
                    message = new SendMessage(chatId.toString(), "Risolvi il puzzle! (Devi scrivere la cella di inizio e di fine attaccato, per esempio 'A1C3')\nNota: i puzzle non consistono solo nel fare scacco matto.");
                    isEntrambe = true;
                    isInvertito = true;
                }
                else
                {
                    message = new SendMessage(chatId.toString(), "Errore connessione con server per i puzzle. Riprova.");
                }
            }
            else if (input.equals("/quit"))
            {
                // Verifica se l'utente è nello stato "PUZZLE"
                if ("PUZZLE".equals(statoConversazione.get(chatId)))
                {
                    // Reset dello stato e messaggio di conferma
                    statoConversazione.put(chatId, "ATTESA");
                    message = new SendMessage(chatId.toString(), "Hai abbandonato il puzzle.");
                }
                else
                {
                    // Se l'utente non è nel puzzle, ignora /quit o invia un messaggio
                    message = new SendMessage(chatId.toString(), "Non sei in un puzzle. Usa /puzzle per iniziare.");
                }
            }
            // Se l'utente sta rispondendo al puzzle
            else if ("PUZZLE".equals(statoConversazione.get(chatId)))
            {
                int risultato = lichessAPI.checkPuzzle(chatId, input);

                switch (risultato)
                {
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
                        message = new SendMessage(chatId.toString(), "Bravo! Puzzle risolto.");
                        photo = new SendPhoto(chatId.toString(), lichessAPI.getCurrentBoard(chatId));
                        isEntrambe = true;
                        isInvertito = true;
                        statoConversazione.put(chatId, "ATTESA");
                        break;
                }

            }
            else
            {
                message = new SendMessage(chatId.toString(), "Comando non riconosciuto. Fai '/help' o '/?' per vedere i comandi possibili.");
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
                    if (isTesto == true && isEntrambe == false)
                    {
                        telegramClient.execute(message);
                    }
                    else if (isTesto == false && isEntrambe == false)
                    {
                        telegramClient.execute(photo);
                    }
                    else if ((((isTesto == true) || (isTesto == false)) && isEntrambe == true) && isInvertito == true)
                    {
                        telegramClient.execute(photo);
                        telegramClient.execute(message);
                    }
                    else if ((((isTesto == true) || (isTesto == false)) && isEntrambe == true) && isInvertito == false)
                    {
                        telegramClient.execute(message);
                        telegramClient.execute(photo);
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
        sb.append("'/userinfo {username}'\nMostra dati da chess.com per quell'user.\n\n");
        sb.append("'/puzzle r' oppure '/puzzle random'\nGenera un puzzle da risolve di difficoltà randomica.\n\n");
        sb.append("'/puzzle {1-5}'\nGenera un puzzle da risolve di difficoltà scelta da 1 (facile) a 5 (difficile).\n\n");
        sb.append("'/quit'\nPer uscire dal puzzle che si sta facendo\n\n");
        return sb.toString();
    }
}

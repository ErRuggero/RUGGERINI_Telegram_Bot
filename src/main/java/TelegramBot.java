import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class TelegramBot implements LongPollingSingleThreadUpdateConsumer
{
    private final TelegramClient telegramClient = new OkHttpTelegramClient(MyConfiguration.getInstance().getProperty("BOT_TOKEN"));
    ChesscomAPI chesscomAPI = new ChesscomAPI();

    @Override
    public void consume(Update update)
    {
        if (update.hasMessage())
        {
            // Prelevo l'inserimento e diventa in minuscolo
            String input = update.getMessage().getText().toLowerCase();

            SendMessage message = null;

            // .equals("/start");
            if (input.equals("/start"))
            {
                message = new SendMessage(update.getMessage().getChatId().toString(), "test");
            }
            else if (input.startsWith("/userinfo "))
            {
                String user = input.substring(input.indexOf(" ") + 1);
                String result = chesscomAPI.fetchPlayer(user);
                message = new SendMessage(update.getMessage().getChatId().toString(), result);
            }
            else if (input.startsWith("/userelo "))
            {
                String user = input.substring(input.indexOf(" ") + 1);

                /*
                System.out.println(result);

                message = new SendMessage(update.getMessage().getChatId().toString(), result);
                */

                String result = chesscomAPI.fetchPlayer(user);

                message = new SendMessage(update.getMessage().getChatId().toString(), result);
            }
            else
            {
                message = new SendMessage(update.getMessage().getChatId().toString(), "prova");
            }

            try
            {
                telegramClient.execute(message);
            }
            catch (TelegramApiException e)
            {
                e.printStackTrace();
            }
        }
    }
}

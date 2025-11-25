import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class TelegramBot implements LongPollingSingleThreadUpdateConsumer
{
    private final TelegramClient telegramClient = new OkHttpTelegramClient(MyConfiguration.getInstance().getProperty("BOT_TOKEN"));

    @Override
    public void consume(Update update)
    {
        if (update.hasMessage())
        {
            SendMessage message;

            // .equals("/start");
            if (update.getMessage().getText().startsWith("/start"))
            {
                message = new SendMessage(update.getMessage().getChatId().toString(), "test");
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

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class Main
{
    public static void main(String[] args)
    {
        MyConfiguration myConfiguration = MyConfiguration.getInstance();

        try
        {
            String botToken = myConfiguration.getProperty("BOT_TOKEN");
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(botToken, new TelegramBot());
        }
        catch (TelegramApiException e)
        {
            e.printStackTrace();
        }
    }
}
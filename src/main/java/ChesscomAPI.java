import com.google.gson.Gson;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class ChesscomAPI {
    private final String endpointBase = "https://api.chess.com/pub/";
    private HttpClient client = HttpClient.newHttpClient();
    private static ChesscomAPI instance;
    private Gson deserializzatore = new Gson();

    public ChesscomAPI()
    {}

    public static ChesscomAPI getInstance() throws Exception
    {
        if (instance == null)
        {
            instance = new ChesscomAPI();
        }
        return instance;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public String fetchPlayer(String username)
    {
        // -------------------------------------------------------------------
        // 1. RICHIESTA DATI DI UN UTENTE

        User user;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpointBase + "player/" + username))
                .GET()
                .build();

        try
        {
            // Chiama l'api e preleva i dati dell'utente
            HttpResponse<String> responseUser = client.send(request, HttpResponse.BodyHandlers.ofString());
            user = deserializzatore.fromJson(responseUser.body(), User.class);

            // Setta lo stato e la data
            user.setCountryFromUrlAndDate();

            System.out.println(user.toString());
        }
        catch (IOException | InterruptedException e)
        {
            System.err.println("Errore nella richiesta API: " + e.getMessage());
            return null;
        }

        // -------------------------------------------------------------------
        // 2. RICHIESTA ELO DI UN UTENTE

        request = HttpRequest.newBuilder()
                .uri(URI.create(endpointBase + "player/" + username + "/stats"))
                .GET()
                .build();

        try
        {
            // Chiama l'api e preleva l'elo dell'utente
            HttpResponse<String> responseElo = client.send(request, HttpResponse.BodyHandlers.ofString());
            Elo elo = deserializzatore.fromJson(responseElo.body(), Elo.class);

            // Normalizza, rimuove tutti i null e fa in modo che ci siano tutti i valori
            elo.normalize();

            // Associa all'utente i dati elo
            user.setElo(elo);
        }
        catch (IOException | InterruptedException e)
        {
            System.err.println("Errore nella richiesta API: " + e.getMessage());
            return null;
        }

        // Ritorna il tutto in un formato leggibile
        return user.allToString();
    }

    public static InputFile generateBoardURL(String url)
    {
        try
        {
            // Si crea un URL
            URL finalurl = new URL(url);

            // Si salva l'immagine presente nell'URL
            BufferedImage image = ImageIO.read(finalurl);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            baos.flush();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(baos.toByteArray());

            return new InputFile(inputStream, "imageURL.png");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}

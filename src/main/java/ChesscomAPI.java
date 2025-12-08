import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.Scanner;

public class ChesscomAPI
{
    private final String endpointBase = "https://api.chess.com/pub/";
    private HttpClient client = null;

    public ChesscomAPI()
    {
        client = HttpClient.newHttpClient();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public String fetchPlayer(String username)
    {
        User user;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpointBase + "player/" + username))
                .GET()
                .build();

        // CHIAMATA PER L'USER
        try
        {
            HttpResponse<String> responseUser = client.send(request, HttpResponse.BodyHandlers.ofString());
            Gson deserializzatore = new Gson();
            user = deserializzatore.fromJson(responseUser.body(), User.class);

            // TODO : RIMUOVERE QUESTA COSA E FARLA FUNZIONARE NEL COSTRUTTORE
            user.setCountryFromUrl();
            System.out.println(user.toString());

            //return user.toString();
        }
        catch (IOException | InterruptedException e)
        {
            System.err.println("Errore nella richiesta API: " + e.getMessage());
            return null;
        }




        // CHIAMATA PER L'ELO
        request = HttpRequest.newBuilder()
                .uri(URI.create(endpointBase + "player/" + username + "/stats"))
                .GET()
                .build();

        try
        {
            HttpResponse<String> responseElo = client.send(request, HttpResponse.BodyHandlers.ofString());
            Gson deserializzatore = new Gson();
            Elo elo = deserializzatore.fromJson(responseElo.body(), Elo.class);

            user.setElo(elo);
        }
        catch (IOException | InterruptedException e)
        {
            System.err.println("Errore nella richiesta API: " + e.getMessage());
            return null;
        }

        return user.allToString();
    }
}
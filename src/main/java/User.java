import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class User
{
    // Uguale ai valori del json
    private int player_id;
    private String username;
    private String country;
    private long last_online;
    private String status;
    private Elo elo;

    // Attributi calcolati
    private String last_online_date;

    public User(int player_id, String username, String countryURL, long last_online, String status)
    {
        this.player_id = player_id;
        this.username = username;

        // TODO : FARE FUNZIONARE QUESTO
        this.country = extractCountryCode(countryURL);

        this.last_online = last_online;
        this.status = status;
    }

    // Metodo per estrarre il codice del paese dalla URL
    private String extractCountryCode(String countryUrl)
    {
        String[] parts = countryUrl.split("/");  // Dividi la URL con il carattere "/"
        System.out.println(parts[parts.length - 1]);
        return parts[parts.length - 1];  // Restituisci l'ultima parte (il codice paese)
    }

    public void setCountryFromUrlAndDate()
    {
        this.country = extractCountryCode(this.country);  // Estrai il codice paese dalla URL

        // Converte in una data leggibile partendo dal valore epoch
        this.last_online_date = Instant.ofEpochSecond(this.last_online)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public void setElo(Elo elo)
    {
        this.elo = elo;
    }

    @Override
    public String toString()
    {
        return "Player ID: \t" + player_id +
                "\nUsername: \t" + username +
                "\nCountry: \t" + country +
                "\nLast Online: \t" + last_online_date +
                "\nStatus: \t" + status;
    }

    public String allToString()
    {
        return (toString() + "\n" + elo.toString());
    }
}
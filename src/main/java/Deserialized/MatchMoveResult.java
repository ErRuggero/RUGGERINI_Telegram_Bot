package Deserialized;

import org.telegram.telegrambots.meta.api.objects.InputFile;

public class MatchMoveResult
{
    private final String message;
    private InputFile image;
    private Long id;
    private Long[] allIds;
    private String fen;
    private boolean end;
    private int finalStatus;    // -1 = partita in corso, 0 = nero vince, 1 = bianco vince, 2 = pareggio


    /*
    // PER INVIARE SOLO UN MESSAGGIO
    public Deserializzati.MatchMoveResult(String message, Long id, boolean end)
    {
        this.message = message;
        this.id = id;
        this.end = end;
    }
    */
    /*
    // SERVE PER RITORNARE QUALCOSA A UNA SOLA PERSONA
    public Deserializzati.MatchMoveResult(String message, InputFile image, String fen, Long id, boolean end)
    {
        this.message = message;
        this.image = image;
        this.id = id;
        this.fen = fen;
        this.end = end;
    }
    */

    /*
    // SERVE PER RITORNARE AD ENTRAMBI
    public Deserializzati.MatchMoveResult(String message, InputFile image, String fen, Long[] allIds, boolean end)
    {
        this.message = message;
        this.image = image;
        this.allIds = allIds;
        this.fen = fen;
        this.end = end;
    }
    */

    public MatchMoveResult(String message, InputFile image, String fen, boolean end)
    {
        this.message = message;
        this.image = image;
        this.fen = fen;
        this.end = end;
    }

    // PER INVIARE SOLO UN MESSAGGIO
    public MatchMoveResult(String message, Long id, int finalStatus)
    {
        this.message = message;
        this.id = id;
        this.end = end;
    }

    public MatchMoveResult(String message, InputFile image, String fen, int finalStatus)
    {
        this.message = message;
        this.image = image;
        this.fen = fen;
        this.finalStatus = finalStatus;
    }

    public String getMessage()
    {
        return message;
    }

    public InputFile getImage()
    {
        return image;
    }

    public Long singleId()
    {
        return id;
    }

    public Long[] allIds()
    {
        return allIds;
    }

    public String getFEN()
    {
        return fen;
    }

    public boolean getEnd()
    {
        return end;
    }

    public int getFinalStatus()
    {
        return finalStatus;
    }
}

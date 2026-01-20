package Deserialized;

public class Match
{
    private Long userIdBianco;
    private Long userIdNero;

    private String usernameBianco;
    private String usernameNero;

    private String fen;

    private boolean isWhite = true;

    private MatchMoveResult mmr = null;

    public Match(Long userIdBianco, String usernameBianco, Long userIdNero, String usernameNero)
    {
        this.userIdBianco = userIdBianco;
        this.userIdNero = userIdNero;
        this.usernameBianco = usernameBianco;
        this.usernameNero = usernameNero;
        this.fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    }

    public Long userIdBianco()
    {
        return userIdBianco;
    }

    public Long userIdNero()
    {
        return userIdNero;
    }

    public String usernameBianco()
    {
        return usernameBianco;
    }

    public String usernameNero()
    {
        return usernameNero;
    }

    public String getFEN()
    {
        return fen;
    }

    public void setFEN(String fen)
    {
        this.fen = fen;
    }

    public MatchMoveResult getMMR()
    {
        return mmr;
    }

    public void setMMR(MatchMoveResult mmr)
    {
        this.mmr = mmr;
    }

    public boolean isWhite()
    {
        return isWhite;
    }

    public void changeTurn()
    {
        isWhite = !isWhite;
    }
}

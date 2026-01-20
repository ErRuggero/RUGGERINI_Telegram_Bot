package Managers;

import DatabaseManagers.DatabaseManager;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import API.*;
import Deserialized.*;

public class MatchManager
{
    private static MatchManager instance;

    private Map<Long, String> pendingMap = new HashMap<>();
    private Map<Long, String> pendingUserMap = new HashMap<>();
    private Map<Long, String> joinCodes = new HashMap<>(); // New map to store join codes

    private final List<Match> matchList = new ArrayList<>();

    private final Random random = new Random(); // For generating random join codes

    public MatchManager()
    {}

    public static MatchManager getInstance() throws Exception
    {
        if (instance == null)
        {
            instance = new MatchManager();
        }

        return instance;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public void pending(Long chatId, String username, String side)
    {
        pendingMap.put(chatId, side);
        pendingUserMap.put(chatId, username);

        // Generate a unique random 3-digit join code (000-999)
        String joinCode;
        do
        {
            joinCode = String.format("%03d", random.nextInt(1000));
        }
        while (joinCodes.containsValue(joinCode)); // Ensure uniqueness

        joinCodes.put(chatId, joinCode);
    }

    public String checkPending()
    {
        if (pendingUserMap == null || pendingUserMap.isEmpty())
        {
            return "Nessuna partita in matchmaking.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Partite in matchmaking:\n\n");

        for (Map.Entry<Long, String> entry : pendingUserMap.entrySet())
        {
            Long userId = entry.getKey();
            String username = entry.getValue();

            String valueFromPendingMap = pendingMap.get(userId);
            String joinCode = joinCodes.get(userId);

            sb.append(username);

            if (valueFromPendingMap != null)
            {
                sb.append(" -> ");

                switch (valueFromPendingMap)
                {
                    case "n":
                        sb.append("Nero");
                        break;
                    case "b":
                        sb.append("Bianco");
                        break;
                    case "r":
                        sb.append("Random");
                        break;
                }

                // Append the join code
                if (joinCode != null)
                {
                    sb.append(" (Codice: ").append(joinCode).append(")");
                }
            }
            else
            {
                sb.append(" -> Errore");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public Match matchJoin(Long chatId, String join, String joinerUsername)
    {
        // Trim the join code to handle any extra spaces
        join = join.trim();

        // Check if the join code exists in joinCodes
        Long hostChatId = null;
        for (Map.Entry<Long, String> entry : joinCodes.entrySet())
        {
            if (entry.getValue().equals(join))
            {
                hostChatId = entry.getKey();
                break;
            }
        }

        if (hostChatId == null)
        {
            return null; // Invalid join code
        }

        // Check if the joiner is trying to join themselves
        if (hostChatId.equals(chatId))
        {
            return null; // Cannot join own matchManager
        }

        // Check if the host is still pending
        if (!pendingUserMap.containsKey(hostChatId))
        {
            return null; // Managers.MatchManager no longer available
        }

        // Get host's username and side
        String hostUsername = pendingUserMap.get(hostChatId);
        String hostSide = pendingMap.get(hostChatId);

        // Determine sides
        Long userIdBianco;
        String usernameBianco;
        Long userIdNero;
        String usernameNero;

        if ("b".equals(hostSide))
        {
            userIdBianco = hostChatId;
            usernameBianco = hostUsername;
            userIdNero = chatId;
            usernameNero = joinerUsername;
        }
        else if ("n".equals(hostSide))
        {
            userIdNero = hostChatId;
            usernameNero = hostUsername;
            userIdBianco = chatId;
            usernameBianco = joinerUsername;
        }
        else // "r" for random
        {
            if (random.nextBoolean())
            {
                userIdBianco = hostChatId;
                usernameBianco = hostUsername;
                userIdNero = chatId;
                usernameNero = joinerUsername;
            }
            else
            {
                userIdNero = hostChatId;
                usernameNero = hostUsername;
                userIdBianco = chatId;
                usernameBianco = joinerUsername;
            }
        }

        // Create the matchManager
        Match match = new Match(userIdBianco, usernameBianco, userIdNero, usernameNero);
        matchList.add(match);

        // Remove the host from pending maps
        pendingMap.remove(hostChatId);
        pendingUserMap.remove(hostChatId);
        joinCodes.remove(hostChatId);

        // Return the two chatIds: [biancoId, neroId]
        return match;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public InputFile beginMatch()
    {
        try
        {
            Chesslib chesslib = Chesslib.getInstance();
            return chesslib.showBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void endMatch(Match match, int finalStatus)
    {
        matchList.remove(match);

        DatabaseManager.databaseMatchManager.addMatch(match, finalStatus);
    }

    // New method: Find a matchManager by chatId (returns the Deserializzati.Match if found, null otherwise)
    public Match findMatch(Long chatId)
    {
        for (Match match : matchList)
        {
            if (match.userIdBianco().equals(chatId) || match.userIdNero().equals(chatId))
            {
                return match;
            }
        }
        return null; // No matchManager found for this chatId
    }
}
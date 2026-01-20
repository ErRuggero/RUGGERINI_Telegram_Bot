
# Chessy Telegram Bot

## Descrizione Dettagliata del Progetto

Chessy è un bot Telegram interattivo progettato per appassionati di scacchi. Permette agli utenti di giocare partite in tempo reale contro altri giocatori, risolvere puzzle di scacchi di varia difficoltà, visualizzare statistiche personali e dati da piattaforme esterne come Chess.com. Il bot gestisce stati di conversazione per guidare l'utente attraverso diverse modalità (puzzle, matchmaking per partite, ecc.), utilizzando API esterne per recuperare dati e immagini di scacchi. È scritto in Java e utilizza un database per memorizzare utenti, partite, puzzle e statistiche.

### Funzionalità Principali
- **Registrazione e Statistiche Utente**: Gli utenti possono registrarsi con `/start` e visualizzare le proprie statistiche con `/userinfo`.
- **Puzzle di Scacchi**: Generazione e risoluzione di puzzle da Lichess, con possibilità di salvare nei preferiti e visualizzare log.
- **Partite in Tempo Reale**: Creazione di partite tramite matchmaking, unione a partite esistenti e gestione di mosse con validazione.
- **Integrazione con API Esterne**: Recupero di dati utente da Chess.com e puzzle da Lichess.
- **Gestione Database**: Salvataggio di utenti, match, puzzle e preferiti.
- **Comandi di Uscita e Reset**: Possibilità di abbandonare partite/puzzle o resettare l'account.

Il bot utilizza un sistema di stati di conversazione per gestire flussi complessi, come la risoluzione di puzzle o il matchmaking.

## API Utilizzate e Link alla Documentazione

- **Telegram Bots API**: Utilizzata per interagire con Telegram. Gestisce invio di messaggi, foto e polling degli aggiornamenti.
    - Documentazione: [https://core.telegram.org/bots/api](https://core.telegram.org/bots/api)
    - Libreria Java: `org.telegram.telegrambots` (inclusa nel codice).

- **Chess.com API**: Utilizzata per recuperare dati di giocatori (es. statistiche, profilo).
    - Documentazione: [https://www.chess.com/news/view/published-data-api](https://www.chess.com/news/view/published-data-api)
    - Classe nel codice: `ChesscomAPI`.

- **Lichess API**: Utilizzata per generare puzzle casuali o per difficoltà specifica, e per validare mosse durante la risoluzione.
    - Documentazione: [https://lichess.org/api](https://lichess.org/api)
    - Classe nel codice: `LichessAPI`.

- **Chesslib**: Libreria per la logica degli scacchi, inclusa validazione di mosse, generazione di immagini della scacchiera e gestione di partite.
    - Probabilmente basata su: [https://github.com/bhlangonijr/chesslib](https://github.com/bhlangonijr/chesslib) (una libreria Java per scacchi).
    - Classe nel codice: `Chesslib`.

## Istruzioni Complete per il Setup

### Prerequisiti
- **Java**: Versione 11 o superiore.
- **Maven o Gradle**: Per gestire le dipendenze (il codice sembra usare Maven basato sugli import).
- **Database**: SQLite o MySQL (il codice usa `DatabaseManager`, probabilmente configurabile per entrambi).
- **Token Bot Telegram**: Ottieni un token da [@BotFather](https://t.me/botfather) su Telegram.
- **Chiavi API (opzionali)**: Per Chess.com e Lichess, se necessarie per autenticazione avanzata (il codice non mostra chiavi esplicite, ma potrebbero essere richieste per limiti di rate).

### Installazione Dipendenze
1. Clona il repository del progetto (assumi che sia su GitHub).
2. Assicurati di avere Maven installato.
3. Nel file `pom.xml` (se Maven), aggiungi le dipendenze necessarie. Basato sul codice, includi:
    - `org.telegram.telegrambots:telegrambots:6.8.0` (o versione attuale).
    - Librerie per database (es. `org.xerial:sqlite-jdbc:3.42.0.0` per SQLite).
    - Librerie per HTTP (es. OkHttp per Telegram).
    - Chesslib e altre API personalizzate (assumi che siano moduli interni o JAR esterni).
4. Esegui `mvn clean install` per scaricare le dipendenze.

### Configurazione API Key
1. Crea un file `MyConfiguration.java` o un file di configurazione (es. `config.properties`) per memorizzare le chiavi.
    - **BOT_TOKEN**: Il token del bot Telegram ottenuto da BotFather.
    - **Eventuali chiavi per Chess.com e Lichess**: Se richieste, aggiungile (es. `CHESS_COM_API_KEY`, `LICHESS_API_KEY`).
2. Nel codice, `MyConfiguration.getInstance().getProperty("BOT_TOKEN")` recupera il token. Assicurati che il file sia nel classpath e sicuro (non committarlo su Git).

### Setup Database
1. Scegli un database: Il codice usa `DatabaseManager`, che probabilmente supporta SQLite per semplicità.
2. Crea le tabelle necessarie. Schema basato sul codice:
    - **Tabella `users`**: `chatId (LONG PRIMARY KEY), username (VARCHAR), stats (JSON o colonne separate per vittorie, sconfitte, ecc.)`.
    - **Tabella `matches`**: `matchId (VARCHAR PRIMARY KEY), userIdBianco (LONG), userIdNero (LONG), status (INT: -1 in corso, 0 nero vince, 1 bianco vince, 2 pareggio), fen (VARCHAR per posizione scacchi), log (TEXT per mosse)`.
    - **Tabella `puzzles`**: `puzzleId (VARCHAR PRIMARY KEY), difficulty (INT), fen (VARCHAR), solution (TEXT), image (BLOB o path)`.
    - **Tabella `favourites`**: `chatId (LONG), puzzleId (VARCHAR), FOREIGN KEY a users e puzzles`.
    - **Relazioni**: `users` collegata a `matches` e `favourites`; `matches` ha due utenti; `favourites` collega utenti a puzzle.
3. Nel `DatabaseManager`, configura la connessione (es. JDBC URL per SQLite: `jdbc:sqlite:chessy.db`).
4. Esegui script SQL per creare le tabelle al primo avvio.

## Guida all'Utilizzo con Lista Comandi Disponibili

Dopo aver avviato il bot, interagisci con esso su Telegram. Inizia con `/start`. Il bot guida l'utente attraverso stati di conversazione.

### Lista Comandi Disponibili
- `/?` oppure `/help`: Mostra elenco comandi.
- `/userinfo`: Mostra statistiche personali (es. vittorie, sconfitte in match e puzzle).
- `/chesscomuser {username}`: Mostra dati da Chess.com per quell'utente (es. rating, partite).
- `/puzzle r` oppure `/puzzle random`: Genera un puzzle casuale da Lichess.
- `/puzzle {1-5}`: Genera un puzzle di difficoltà specifica (1 facile, 5 difficile).
- `/puzzle log`: Mostra log di tutti i puzzle risolti.
- `/puzzlefav s` oppure `/puzzlefav show`: Mostra puzzle salvati nei preferiti.
- `/puzzlefav p {id}` oppure `/puzzlefav play {id}`: Gioca un puzzle dai preferiti.
- `/quit`: Abbandona puzzle o partita corrente (conta come sconfitta).
- `/match create`: Avvia creazione partita (scegli colore: B bianco, N nero, R random).
- `/match find`: Mostra partite in attesa di giocatori.
- `/match join {codice}`: Unisciti a una partita con codice.
- `/match log`: Mostra log di tutte le partite giocate.
- `/reset account`: Resetta account e dati (richiede conferma).

### Esempi di Conversazioni
1. **Avvio e Help**:
    - Utente: `/start`
    - Bot: "Benvenuto su Chessy! Fare '/help' per vedere tutti i comandi."
    - Utente: `/help`
    - Bot: [Elenco comandi come sopra].

2. **Risoluzione Puzzle**:
    - Utente: `/puzzle 3`
    - Bot: [Invia immagine puzzle] "Risolvi il puzzle! (Devi scrivere la cella di inizio e di fine attaccato, per esempio 'A1C3') GIOCHI COME IL BIANCO."
    - Utente: `E2E4`
    - Bot: [Se corretto] [Nuova immagine] "Corretto! Vai avanti." [Oppure "Sbagliato! Riprova."]
    - Dopo risoluzione: "Bravo! Puzzle risolto. Salvare nei preferiti (Si/No)?"
    - Utente: `Sì`
    - Bot: "Puzzle aggiunto ai preferiti."

3. **Partita**:
    - Utente1: `/match create`
    - Bot: "Scegli con cosa vuoi giocare. (B = bianco, N = Nero, R = random)."
    - Utente1: `R`
    - Bot: "Hai creato una partita. Matchmaking iniziato. Attendi."
    - Utente2: `/match find`
    - Bot: [Lista partite, es. "Partita ID: ABC123, Creatore: User1"]
    - Utente2: `/match join ABC123`
    - Bot: [A entrambi] [Immagine scacchiera] "Partita avviata! TU GIOCHI COME IL: BIANCO/NERO"
    - Utente1: `A2A4`
    - Bot: [Aggiorna immagine a entrambi] "Mossa valida. Turno dell'avversario."

(Screenshot: Immagina immagini di scacchiere con pezzi posizionati, messaggi di testo per feedback.)

## Schema del Database (Tabelle e Relazioni)

- **users**:
    - `iduser` (INTEGER, PRIMARY KEY AUTOINCREMENT): ID univoco dell'utente nel database.
    - `username` (VARCHAR(64), NOT NULL): Nome utente Telegram.
    - `chatid` (INTEGER, NOT NULL): ID chat Telegram (usato per identificare l'utente nelle interazioni).
    - `signup` (TEXT, DEFAULT CURRENT_TIMESTAMP, NOT NULL): Data e ora di registrazione.
    - `login` (TEXT, DEFAULT CURRENT_TIMESTAMP, NOT NULL): Data e ora dell'ultimo accesso (aggiornata automaticamente).
    - `isinactive` (BOOLEAN, DEFAULT FALSE, NOT NULL): Flag per indicare se l'utente è inattivo.
    - Relazioni: Collegata a `puzzles` (iduser), `matches` (iduser_white/iduser_black), `favourite_puzzles` (iduser).

- **puzzles**:
    - `idpuzzle` (INTEGER, PRIMARY KEY AUTOINCREMENT): ID univoco del puzzle.
    - `iduser` (INTEGER, NOT NULL, FOREIGN KEY a users(iduser)): Riferimento all'utente che ha giocato il puzzle.
    - `fen` (VARCHAR(100), NOT NULL): Posizione iniziale del puzzle in formato FEN (Forsyth-Edwards Notation).
    - `iswon` (INTEGER, NOT NULL): Indicatore di vittoria (es. 1 per vinto, 0 per perso).
    - `totsolution` (INTEGER, NOT NULL): Numero totale di soluzioni possibili o tentate.
    - `totmoves` (INTEGER, NOT NULL): Numero totale di mosse effettuate durante il tentativo.
    - `ratiomoves` (REAL, NOT NULL): Rapporto delle mosse (es. efficienza o accuratezza).
    - Relazioni: FOREIGN KEY a `users`.

- **matches**:
    - `idmatch` (INTEGER, PRIMARY KEY AUTOINCREMENT): ID univoco della partita.
    - `iduser_white` (INTEGER, NOT NULL, FOREIGN KEY a users(iduser)): ID dell'utente che gioca con i bianchi.
    - `iduser_black` (INTEGER, NOT NULL, FOREIGN KEY a users(iduser)): ID dell'utente che gioca con i neri.
    - `result` (VARCHAR(5), NOT NULL): Risultato della partita (es. "1-0" vittoria bianchi, "0-1" vittoria neri, "1/2-1/2" pareggio).
    - `finished` (TEXT, DEFAULT CURRENT_TIMESTAMP, NOT NULL): Data e ora di fine partita.
    - Relazioni: Due FOREIGN KEY a `users` (una per white, una per black).

- **favourite_puzzles**:
    - `idfavourites` (INTEGER, PRIMARY KEY AUTOINCREMENT): ID univoco del preferito.
    - `iduser` (INTEGER, NOT NULL, FOREIGN KEY a users(iduser)): Riferimento all'utente che ha salvato il puzzle.
    - `fen` (VARCHAR(100), NOT NULL): Posizione FEN del puzzle salvato.
    - `solutions` (TEXT, NOT NULL): Testo delle soluzioni o mosse corrette.
    - `addtime` (TEXT, DEFAULT CURRENT_TIMESTAMP, NOT NULL): Data e ora di aggiunta ai preferiti.
    - Vincolo UNIQUE su (iduser, fen): Un utente può salvare lo stesso FEN una sola volta.
    - Relazioni: FOREIGN KEY a `users`.

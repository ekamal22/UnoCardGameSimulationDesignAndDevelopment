package main.java.Game;

import main.java.Gui.MainMenuPage;
import main.java.Gui.ProfilePage;
import main.java.Player.Player;
import main.java.Object.Card;
import main.java.Object.Deck;
import main.java.User.UserData;
import main.java.Utils.AppFiles;
import main.java.Utils.CardImageLoader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.io.*;
import java.nio.file.*;
import java.util.logging.*;

public class Session extends JFrame {
    private String sessionName;
    private int playerCount;
    private String humanPlayerEmail;
    private JLabel sessionNameLabel;
    private JLabel directionLabel;
    private JPanel playerPanel;
    private JPanel drawPilePanel;
    private JPanel discardPilePanel;
    private JPanel userDeckPanel;
    private JButton unoButton;
    private JButton callOutButton;
    private JButton drawCardButton;
    private JComboBox<Card> playCardComboBox;
    private JButton playCardButton;
    private JButton skipTurnButton;

    // Game state
    private List<Player> players;
    private boolean isClockwise = true;
    private Deck drawPile;
    private List<Card> discardPile;
    private int currentPlayerIndex = 0;
    private static final Logger LOGGER = Logger.getLogger(Session.class.getName());
    
    private String currentColor;
    private boolean gameOver = false;
 // Prevents re-entrant bot runs triggered by repeated UI updates
    private boolean turnRunnerBusy = false;
    private static final String LOG_FILE_PATH = "game_logs.txt";
    

 // Session.java — constructors

 // Existing 3-arg constructor now delegates to the 4-arg one.
 public Session(String sessionName, int playerCount, String humanPlayerEmail) {
     this(sessionName, playerCount, humanPlayerEmail, /*deferInit=*/false);
 }

 // New constructor that can skip initializeGame() when loading a save.
 public Session(String sessionName, int playerCount, String humanPlayerEmail, boolean deferInit) {
     this.sessionName = sessionName;
     this.playerCount = playerCount;
     this.humanPlayerEmail = humanPlayerEmail;

     this.players = new ArrayList<>();
     this.drawPile = new Deck();
     this.discardPile = new ArrayList<>();
     this.currentColor = null;
     this.gameOver = false;
     this.turnRunnerBusy = false;

     setupLogger();
     setupMenu();
     setTitle("UNO Game - " + sessionName);
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     setSize(1000, 800);
     setLocationRelativeTo(null);
     setupUIComponents();

     if (!deferInit) {
         // Normal "New Game" flow
         initializeGame();
     } else {
         // Load-only flow: loadGame(...) will populate state; just render UI for now
         updateGameUI();
     }

     setVisible(true);
 }


    private void setupUIComponents() {
        drawPilePanel = new JPanel(new BorderLayout());
        discardPilePanel = new JPanel(new BorderLayout());
        userDeckPanel = new JPanel(new FlowLayout());
        playerPanel = new JPanel(new GridLayout(Math.max(1, playerCount - 1), 1));

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout());
        sessionNameLabel = new JLabel("Session: " + sessionName);
        directionLabel = new JLabel("Direction: Clockwise");
        topPanel.add(sessionNameLabel);
        topPanel.add(directionLabel);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        JPanel topCenterPanel = new JPanel(new GridLayout(1, 3));
        topCenterPanel.add(drawPilePanel);
        topCenterPanel.add(discardPilePanel);
        topCenterPanel.add(playerPanel);

        centerPanel.add(topCenterPanel);
        centerPanel.add(userDeckPanel);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        unoButton = new JButton("UNO");
        callOutButton = new JButton("Call Out");
        drawCardButton = new JButton("Draw Card");
        playCardComboBox = new JComboBox<>();
        playCardButton = new JButton("Play Card");
        skipTurnButton = new JButton("Skip Turn");
        skipTurnButton.addActionListener(e -> skipTurn());

        bottomPanel.add(unoButton);
        bottomPanel.add(callOutButton);
        bottomPanel.add(drawCardButton);
        bottomPanel.add(playCardComboBox);
        bottomPanel.add(playCardButton);
        bottomPanel.add(skipTurnButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setupButtonListeners();
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");

        JMenuItem profileMenuItem = new JMenuItem("Profile");
        profileMenuItem.addActionListener(e -> showProfile());

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(e -> saveGame());

        JMenuItem exitToMainMenuMenuItem = new JMenuItem("Exit to Main Menu");
        exitToMainMenuMenuItem.addActionListener(e -> exitToMainMenu());

        JMenuItem exitToDesktopMenuItem = new JMenuItem("Exit to Desktop");
        exitToDesktopMenuItem.addActionListener(e -> System.exit(0));

        JMenuItem loadGameMenuItem = new JMenuItem("Load Game");
        loadGameMenuItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try (BufferedReader in = Files.newBufferedReader(selectedFile.toPath())) {
                    loadGame(in);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to load the game: " + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        menu.add(profileMenuItem);
        menu.add(saveMenuItem);
        menu.add(loadGameMenuItem);
        menu.add(exitToMainMenuMenuItem);
        menu.add(exitToDesktopMenuItem);

        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    private void showProfile() {
        UserData currentUser = findUserByEmail(humanPlayerEmail);
        if (currentUser != null) {
            new ProfilePage(currentUser);
        } else {
            JOptionPane.showMessageDialog(this, "User data not found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean isUsersTurn() {
        // Assuming player 0 is the human user
        return currentPlayerIndex == 0 && !players.get(0).isBot();
    }
    
    

    private UserData findUserByEmail(String email) {
        try {
            Files.createDirectories(AppFiles.dataDir());
            if (Files.notExists(AppFiles.usersFile())) return null;

            try (BufferedReader reader = Files.newBufferedReader(AppFiles.usersFile())) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] details = line.split(",", -1);
                    if (details.length >= 9 && details[0].equalsIgnoreCase(email)) {
                        return new UserData(
                                details[0], details[1], details[2], details[3],
                                Integer.parseInt(details[4]),
                                Integer.parseInt(details[5]),
                                Integer.parseInt(details[6]),
                                Integer.parseInt(details[7]),
                                details[8]
                        );
                    }
                }
            }
        } catch (IOException ignored) {}
        return null;
    }

    private void saveGame() {
        try {
            Files.createDirectories(AppFiles.savesDir());
            int saveIndex = 1;
            Path saveFile;
            do {
                saveFile = AppFiles.savesDir().resolve("gameSave" + saveIndex + ".txt");
                saveIndex++;
            } while (Files.exists(saveFile));

            try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(saveFile))) {
                out.println("GameInfo");
                out.println(sessionName);
                out.println(playerCount);
                out.println(currentPlayerIndex);
                out.println(isClockwise);
                out.println(currentColor == null ? "None" : currentColor);

                out.println("PlayersStart");
                for (Player player : players) {
                    out.println(player.getName());
                    out.println(player.isBot());
                    out.println(player.getHand().size());
                    for (Card card : player.getHand()) {
                        out.println(card.getColor() + "," + card.getValue());
                    }
                }

                out.println("DrawPileStart");
                for (Card card : drawPile.getCards()) {
                    out.println(card.getColor() + "," + card.getValue());
                }

                out.println("DiscardPileStart");
                for (Card card : discardPile) {
                    out.println(card.getColor() + "," + card.getValue());
                }
            }

            JOptionPane.showMessageDialog(this, "Game saved to: " + saveFile, "Game Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save the game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadGame(BufferedReader in) {
        try {
            String line;

            // --- Header ---
            line = in.readLine();
            if (line == null || !line.trim().equals("GameInfo")) {
                throw new IOException("Invalid save file: missing GameInfo header");
            }

            // Session name
            line = in.readLine();
            if (line == null) throw new IOException("Unexpected EOF reading sessionName");
            this.sessionName = line;

            // Player count
            line = in.readLine();
            if (line == null) throw new IOException("Unexpected EOF reading playerCount");
            this.playerCount = Integer.parseInt(line.trim());

            // Current player index
            line = in.readLine();
            if (line == null) throw new IOException("Unexpected EOF reading currentPlayerIndex");
            this.currentPlayerIndex = Integer.parseInt(line.trim());

            // Direction
            line = in.readLine();
            if (line == null) throw new IOException("Unexpected EOF reading isClockwise");
            this.isClockwise = Boolean.parseBoolean(line.trim());

            // Current color (can be "None")
            line = in.readLine();
            if (line == null) throw new IOException("Unexpected EOF reading currentColor");
            line = line.trim();
            this.currentColor = ("None".equalsIgnoreCase(line) ? null : line);

            // --- Players & hands ---
            line = in.readLine();
            if (line == null || !line.trim().equals("PlayersStart")) {
                throw new IOException("Missing PlayersStart section");
            }

            this.players.clear();
            for (int i = 0; i < this.playerCount; i++) {
                // name
                String name = in.readLine();
                if (name == null) throw new IOException("Unexpected EOF reading player name (" + i + ")");
                // isBot
                String isBotStr = in.readLine();
                if (isBotStr == null) throw new IOException("Unexpected EOF reading player isBot (" + i + ")");
                boolean isBot = Boolean.parseBoolean(isBotStr.trim());
                // card count
                String countStr = in.readLine();
                if (countStr == null) throw new IOException("Unexpected EOF reading player card count (" + i + ")");
                int cardCount = Integer.parseInt(countStr.trim());

                Player p = new Player(name, isBot);
                for (int j = 0; j < cardCount; j++) {
                    String cardLine = in.readLine();
                    if (cardLine == null) throw new IOException("Unexpected EOF reading player card " + j + " for player " + i);
                    String[] parts = cardLine.split(",", 2);
                    if (parts.length != 2) throw new IOException("Invalid card entry: " + cardLine);
                    p.addCard(new Card(parts[0], parts[1]));
                }
                this.players.add(p);
            }

            // Ensure consistency
            if (this.players.size() != this.playerCount) {
                this.playerCount = this.players.size();
            }
            if (this.currentPlayerIndex < 0 || this.currentPlayerIndex >= this.players.size()) {
                this.currentPlayerIndex = 0;
            }

            // --- Draw pile ---
            line = in.readLine();
            if (line == null || !line.trim().equals("DrawPileStart")) {
                throw new IOException("Missing DrawPileStart section");
            }

            java.util.List<Card> drawPileCards = new java.util.ArrayList<>();
            while (true) {
                line = in.readLine();
                if (line == null) throw new IOException("Unexpected EOF inside DrawPile section");
                line = line.trim();
                if (line.equals("DiscardPileStart")) break;
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", 2);
                if (parts.length != 2) throw new IOException("Invalid draw pile card: " + line);
                drawPileCards.add(new Card(parts[0], parts[1]));
            }
            this.drawPile.getCards().clear();
            this.drawPile.getCards().addAll(drawPileCards);

            // --- Discard pile (rest of file) ---
            this.discardPile.clear();
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", 2);
                if (parts.length != 2) throw new IOException("Invalid discard pile card: " + line);
                this.discardPile.add(new Card(parts[0], parts[1]));
            }

            // Sanity: ensure we have at least one card in discard
            if (this.discardPile.isEmpty() && !this.drawPile.isEmpty()) {
                this.discardPile.add(this.drawPile.drawCard());
            }

            JOptionPane.showMessageDialog(this, "Game loaded successfully!", "Game Loaded", JOptionPane.INFORMATION_MESSAGE);

            // Refresh UI and, if it's a bot's turn, let it act
            updateGameUI();
            runBotIfNeeded();

        } catch (IOException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load the game: " + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    private void exitToMainMenu() {
        new MainMenuPage();
        dispose();
    }

    private void skipTurn() {
        nextPlayer();
        updateGameUI();
    }

 // Session.java
    private void initializeGame() {
        // Reset core state
        this.gameOver = false;
        this.currentColor = null;
        this.isClockwise = true;
        this.currentPlayerIndex = 0;

        // Fresh deck / piles
        this.drawPile = new Deck();
        this.discardPile.clear();

        // Deal players
        this.players.clear();
        drawPile.shuffleDeck();
        initializePlayers();   // deals 7 cards to each player

        // Flip the first discard; avoid starting with a Wild Draw Four
        Card first = drawPile.drawCard();
        while (first != null && "Wild Draw Four".equals(first.getValue())) {
            // put it back and reshuffle to pick another starter
            drawPile.getCards().add(first);
            drawPile.shuffleDeck();
            first = drawPile.drawCard();
        }
        if (first == null) {
            throw new IllegalStateException("Deck exhausted at game start.");
        }
        discardPile.add(first);

        // Set the current color from the starter card
        if (first.getValue().startsWith("Wild")) {
            // If a plain Wild starts the game, choose a random color
            this.currentColor = getRandomColor();
        } else {
            this.currentColor = first.getColor();
        }

        // Log and paint
        log("Game initialized. Top discard: " + first + ", currentColor=" + currentColor
                + ", players=" + players.size() + ", direction=" + (isClockwise ? "Clockwise" : "Counter-Clockwise"));
        updateGameUI();

        // In case a bot would start (unlikely since user is index 0), this is safe and no-op otherwise
        runBotIfNeeded();
    }


    private void initializePlayers() {
        for (int i = 0; i < playerCount; i++) {
            Player player = new Player(i == 0 ? "User" : "Bot " + i, i != 0);
            for (int j = 0; j < 7; j++) {
                if (!drawPile.isEmpty()) {
                    player.addCard(drawPile.drawCard());
                }
            }
            players.add(player);
        }
    }

    private void updateGameUI() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("[DEBUG] updateGameUI: players=" + players.size()
                    + ", currentPlayerIndex=" + currentPlayerIndex
                    + ", currentPlayer=" + players.get(currentPlayerIndex).getName()
                    + ", clockwise=" + isClockwise
                    + ", currentColor=" + currentColor);

            // ---- Draw pile ----
            drawPilePanel.removeAll();
            drawPilePanel.add(new JLabel("Cards: " + drawPile.size()), BorderLayout.CENTER);

            // ---- Discard pile (show top card image if available) ----
            discardPilePanel.removeAll();
            if (discardPile.isEmpty()) {
                discardPilePanel.add(new JLabel("Discard Pile is empty"), BorderLayout.CENTER);
            } else {
                Card topCard = discardPile.get(discardPile.size() - 1);
                ImageIcon topCardImage = CardImageLoader.getCardImage(topCard.toString(), 140); // bigger
                if (topCardImage != null) {
                    discardPilePanel.add(new JLabel(topCardImage), BorderLayout.CENTER);
                } else {
                    discardPilePanel.add(new JLabel("Top Discard: " + topCard.toString()), BorderLayout.CENTER);
                }
            }

            // ---- User hand (assume player 0 is the human) ----
            userDeckPanel.removeAll();
            Player userPlayer = players.get(0);
            for (Card card : userPlayer.getHand()) {
                ImageIcon icon = CardImageLoader.getCardImage(card.toString(), 100);
                if (icon != null) userDeckPanel.add(new JLabel(icon));
                else userDeckPanel.add(new JLabel(card.toString()));
            }

            // ---- Fill combo box with user's cards ----
            playCardComboBox.removeAllItems();
            if (!userPlayer.isBot()) {
                for (Card c : userPlayer.getHand()) {
                    playCardComboBox.addItem(c);
                }
                if (playCardComboBox.getItemCount() > 0) {
                    playCardComboBox.setSelectedIndex(0);
                }
            }

            // ---- Other players' summaries ----
            playerPanel.removeAll();
            for (int i = 1; i < players.size(); i++) {
                Player p = players.get(i);
                playerPanel.add(new JLabel(p.getName() + ": " + p.getCardCount() + " cards"));
            }

            // ---- Direction label ----
            directionLabel.setText("Direction: " + (isClockwise ? "Clockwise" : "Counter-Clockwise"));

            // ---- Enable/disable controls based on turn ----
            boolean usersTurn = isUsersTurn();
            System.out.println("[DEBUG] updateGameUI: usersTurn=" + usersTurn);

            playCardButton.setEnabled(usersTurn);
            playCardComboBox.setEnabled(usersTurn && playCardComboBox.getItemCount() > 0);
            drawCardButton.setEnabled(usersTurn);
            skipTurnButton.setEnabled(usersTurn);

            // UNO button: only when it's the user's turn, exactly one card, and UNO not already called
            boolean unoEnabled = usersTurn
                    && players.get(currentPlayerIndex).getCardCount() == 1
                    && !players.get(currentPlayerIndex).hasCalledUno();
            unoButton.setEnabled(unoEnabled);

            // Call Out button: typically enabled when it's NOT the user's turn and the previous player is eligible
            int prevIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
            Player prev = players.get(prevIndex);
            boolean canCallOut = !usersTurn && prev.getCardCount() == 1 && !prev.hasCalledUno();
            callOutButton.setEnabled(canCallOut);

            revalidate();
            repaint();

            // If a bot should act now, let it run after UI refresh
            runBotIfNeeded();
        });
    }



    private void setupLogger() {
        try {
            LOGGER.setUseParentHandlers(false);
            LOGGER.setLevel(java.util.logging.Level.INFO);

            // File log
            java.util.logging.FileHandler fh = new java.util.logging.FileHandler(LOG_FILE_PATH, true);
            fh.setFormatter(new java.util.logging.SimpleFormatter());
            fh.setLevel(java.util.logging.Level.INFO);
            LOGGER.addHandler(fh);

            // Console log (Eclipse Console)
            java.util.logging.ConsoleHandler ch = new java.util.logging.ConsoleHandler();
            ch.setFormatter(new java.util.logging.SimpleFormatter());
            ch.setLevel(java.util.logging.Level.INFO);
            LOGGER.addHandler(ch);
        } catch (java.io.IOException e) {
            System.err.println("Failed to set up logger: " + e.getMessage());
        }
    }
    
    private void log(String message) {
        String who = (players != null && !players.isEmpty())
                ? players.get(currentPlayerIndex).getName()
                : "?";
        String tag = "[TURN -> " + who + "] ";
        LOGGER.info(tag + message);
    }


    private void playSelectedCard() {
        if (!isUsersTurn()) {
            JOptionPane.showMessageDialog(this, "It's not your turn.", "Not your turn", JOptionPane.WARNING_MESSAGE);
            System.out.println("[DEBUG] playSelectedCard blocked: currentPlayerIndex=" + currentPlayerIndex
                    + ", currentPlayer=" + players.get(currentPlayerIndex).getName());
            return;
        }

        Card card = (Card) playCardComboBox.getSelectedItem();
        System.out.println("[DEBUG] playSelectedCard: selected=" + card
                + ", userHandSize=" + players.get(0).getCardCount()
                + ", currentPlayerIndex=" + currentPlayerIndex
                + ", currentPlayer=" + players.get(currentPlayerIndex).getName());

        if (card != null) {
            playCard(card); // delegates to existing validation & UI updates
        } else {
            JOptionPane.showMessageDialog(this, "No card selected.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
        }
    }

 // Session.java
    private void playCard(Card card) {
        if (card == null) return;

        Player currentPlayer = players.get(currentPlayerIndex);
        if (discardPile.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Discard pile is empty; cannot play yet.", "Error", JOptionPane.ERROR_MESSAGE);
            log("ERROR: discard pile empty when trying to play " + card);
            return;
        }

        Card topCard = discardPile.get(discardPile.size() - 1);
        log(currentPlayer.getName() + " attempts to play " + card + " on " + topCard
                + (currentColor != null ? (" (currentColor=" + currentColor + ")") : ""));

        // Validate move
        if (!canPlayCard(card, topCard)) {
            JOptionPane.showMessageDialog(this, "Invalid card played!", "Invalid Move", JOptionPane.ERROR_MESSAGE);
            log("Move rejected: " + card + " cannot be played on " + topCard
                    + (currentColor != null ? (" with currentColor=" + currentColor) : ""));
            return;
        }

        // Remove from hand (value-based removal via Player.removeCard and Card.equals)
        boolean removed = currentPlayer.removeCard(card);
        if (!removed) {
            JOptionPane.showMessageDialog(this, "Failed to remove card!", "Error", JOptionPane.ERROR_MESSAGE);
            log("ERROR: removeCard() returned false for " + card + " (hand size: " + currentPlayer.getCardCount() + ")");
            return;
        }

        // Place on discard
        discardPile.add(card);
        log(currentPlayer.getName() + " played " + card + ". Hand now: " + currentPlayer.getCardCount());

        // Handle Wilds (choose/announce color)
        if (card.getValue().startsWith("Wild")) {
            if (!currentPlayer.isBot()) {
                currentColor = getUserSelectedColor();
                log(currentPlayer.getName() + " chose color: " + currentColor);
            } else {
                currentColor = getRandomColor();
                log(currentPlayer.getName() + " (bot) chose color: " + currentColor);
                JOptionPane.showMessageDialog(this,
                        currentPlayer.getName() + " changes the color to " + currentColor,
                        "Color Changed", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            // Once a non-wild is played, the transient wild color no longer matters
            currentColor = null;
        }

        // Action cards
        String v = card.getValue();
        if ("Reverse".equals(v)) {
            isClockwise = !isClockwise;
            log("Direction reversed. Now " + (isClockwise ? "Clockwise" : "Counter-Clockwise"));
            JOptionPane.showMessageDialog(this, "Game direction reversed!", "Reverse Card",
                    JOptionPane.INFORMATION_MESSAGE);
        } else if ("Skip".equals(v)) {
            int skippedIdx = nextPlayerIndex();
            log(currentPlayer.getName() + " played Skip. " + players.get(skippedIdx).getName() + " is skipped.");
            JOptionPane.showMessageDialog(this,
                    currentPlayer.getName() + " plays Skip! Next player loses turn.",
                    "Skip Card", JOptionPane.INFORMATION_MESSAGE);
            skipNextPlayer(); // advances over the next player
            updateGameUI();   // reflect immediately
            return;           // turn already advanced by skipNextPlayer()
        } else if ("Wild Draw Four".equals(v)) {
            int target = nextPlayerIndex();
            log(currentPlayer.getName() + " played Wild Draw Four. " + players.get(target).getName() + " draws 4.");
            applyDraw(target, 4);
            // (Rules usually also skip their turn; keeping existing game flow unless you choose to skip here.)
        } else if ("Draw Two".equals(v)) {
            int target = nextPlayerIndex();
            log(currentPlayer.getName() + " played Draw Two. " + players.get(target).getName() + " draws 2.");
            applyDraw(target, 2);
            // (Rules usually also skip their turn; keeping existing game flow unless you choose to skip here.)
        }

        // Refresh UI after effects
        updateGameUI();

        // Win / UNO checks
        if (currentPlayer.getCardCount() == 0) {
            // If the player reached 0 from 1, check UNO status (may apply penalty)
            boolean unoOk = checkUno(currentPlayer);
            if (currentPlayer.getCardCount() == 0) { // still zero after any penalty
                log(currentPlayer.getName() + " has no cards left and wins!");
                JOptionPane.showMessageDialog(this,
                        "Congratulations, " + currentPlayer.getName() + " has won the game!",
                        "Game Over", JOptionPane.INFORMATION_MESSAGE);
                endGame();
                return;
            } else {
                log(currentPlayer.getName() + " failed UNO check; penalty applied.");
            }
        }

        // Advance to next turn
        nextPlayer();
    }

    private int nextPlayerIndex() {
        int nextIndex = (currentPlayerIndex + (isClockwise ? 1 : -1)) % players.size();
        if (nextIndex < 0) nextIndex += players.size();
        return nextIndex;
    }

 // Session.java
    private void skipNextPlayer() {
        if (players == null || players.isEmpty()) return;

        // The player who would have played next (and will be skipped)
        int skippedIdx = nextPlayerIndex();
        String skippedName = players.get(skippedIdx).getName();

        // Advance over the skipped player
        currentPlayerIndex = skippedIdx;          // move to the skipped player
        currentPlayerIndex = nextPlayerIndex();   // move past them to the following player

        String nowName = players.get(currentPlayerIndex).getName();
        log("Skip applied: " + skippedName + " loses their turn. It's now " + nowName + "'s turn.");

        // Refresh UI and, if it's a bot now, let it act
        updateGameUI();
        runBotIfNeeded();
    }


    private void applyDraw(int playerIndex, int cardsCount) {
        // Guard invalid inputs
        if (playerIndex < 0 || playerIndex >= players.size() || cardsCount <= 0) {
            return;
        }

        Player player = players.get(playerIndex);
        int actuallyDrawn = 0;

        for (int i = 0; i < cardsCount; i++) {
            if (drawPile.isEmpty()) {
                // Try to rebuild the draw pile from the discard pile (keeping top discard)
                reshuffleDiscardIntoDraw();
            }
            if (drawPile.isEmpty()) {
                // Still empty — nothing left to draw
                log("applyDraw: deck exhausted while drawing for " + player.getName()
                        + ". Drawn so far: " + actuallyDrawn + "/" + cardsCount);
                break;
            }

            Card c = drawPile.drawCard();
            if (c == null) {
                log("applyDraw: drawPile.drawCard() returned null");
                break;
            }
            player.addCard(c);
            actuallyDrawn++;
        }

        log(player.getName() + " draws " + actuallyDrawn + " card(s) (requested " + cardsCount + "). Hand: "
                + player.getCardCount());
        JOptionPane.showMessageDialog(this,
                player.getName() + " draws " + actuallyDrawn + " card" + (actuallyDrawn == 1 ? "" : "s") + ".",
                "Cards Drawn",
                JOptionPane.INFORMATION_MESSAGE);
        // Note: UI refresh is handled by the caller (e.g., playCard -> updateGameUI()).
    }

    private String getRandomColor() {
        String[] colors = {"Red", "Yellow", "Blue", "Green"};
        return colors[new Random().nextInt(colors.length)];
    }

    private String getUserSelectedColor() {
        Object[] options = {"Red", "Yellow", "Blue", "Green"};
        int n = JOptionPane.showOptionDialog(this, "Choose a color:", "Wild Card Played",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        return options[n].toString();
    }

    private boolean canPlayCard(Card card, Card topCard) {
        if (card.getValue().startsWith("Wild")) return true;

        if (topCard.getValue().startsWith("Wild")) {
            return card.getColor().equals(currentColor);
        } else if (card.getColor().equals(topCard.getColor()) || card.getValue().equals(topCard.getValue())) {
            return true;
        }
        return false;
    }

 // Session.java
    private void drawCard() {
        // Only the human user (player 0) can trigger this button
        if (!isUsersTurn()) {
            JOptionPane.showMessageDialog(this, "It's not your turn.", "Not your turn", JOptionPane.WARNING_MESSAGE);
            log("Human attempted to draw out of turn.");
            return;
        }

        Player currentPlayer = players.get(currentPlayerIndex);

        // If draw pile is empty, try to rebuild it from the discard pile (keeping top discard)
        if (drawPile.isEmpty()) {
            log("Draw pile empty; reshuffling discard into draw.");
            reshuffleDiscardIntoDraw();
        }

        // Draw 1 card
        if (!drawPile.isEmpty()) {
            Card drawnCard = drawPile.drawCard();
            currentPlayer.addCard(drawnCard);
            log(currentPlayer.getName() + " draws " + drawnCard + ". Hand now: " + currentPlayer.getCardCount());
        } else {
            // Still empty after reshuffle: nothing to draw
            log("No cards to draw even after reshuffle.");
        }

        // Refresh, check end conditions, then pass the turn to the next player
        updateGameUI();
        checkGameEnd();
        nextPlayer();
    }


    private void checkGameEnd() {
        Player currentPlayer = players.get(currentPlayerIndex);
        if (currentPlayer.getCardCount() == 0) {
            if (checkUno(currentPlayer)) {
                JOptionPane.showMessageDialog(this, "Game Over, " + currentPlayer.getName() + " wins!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                endGame();
            } else {
                updateGameUI();
            }
        }
    }

    private void applyPenalty(Player player) {
        for (int i = 0; i < 2; i++) {
            if (drawPile.isEmpty()) reshuffleDiscardIntoDraw();
            if (!drawPile.isEmpty()) player.addCard(drawPile.drawCard());
        }
        updateGameUI();
    }

    private void reshuffleDiscardIntoDraw() {
        if (discardPile.size() > 1) {
            Card lastCard = discardPile.remove(discardPile.size() - 1);
            drawPile.getCards().addAll(discardPile);
            drawPile.shuffleDeck();
            discardPile.clear();
            discardPile.add(lastCard);
        } else {
            JOptionPane.showMessageDialog(this, "No cards left to reshuffle!", "Reshuffle Error", JOptionPane.ERROR_MESSAGE);
        }
        updateGameUI();
    }

    private void endGame() {
        Player winningPlayer = players.get(currentPlayerIndex);
        int totalScore = 0;

        StringBuilder scoreMessage = new StringBuilder("Game Over\n");

        for (Player player : players) {
            if (player != winningPlayer) totalScore += calculatePlayerScore(player);
        }

        scoreMessage.append(winningPlayer.getName()).append(": ").append(totalScore).append(" points\n");
        for (Player player : players) {
            if (player != winningPlayer) scoreMessage.append(player.getName()).append(": 0 points\n");
        }
        scoreMessage.append(winningPlayer.getName()).append(" wins and earns ").append(totalScore).append(" points\n");

        JOptionPane.showMessageDialog(this, scoreMessage.toString(), "Game Over", JOptionPane.INFORMATION_MESSAGE);

        updateLeaderboard(winningPlayer, totalScore);

        Timer timer = new Timer(5000, e -> {
            dispose();
            new MainMenuPage();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void updateLeaderboard(Player winningPlayer, int score) {
        List<UserData> usersData = new ArrayList<>();
        try {
            Files.createDirectories(AppFiles.dataDir());
            if (Files.notExists(AppFiles.usersFile())) return;

            try (BufferedReader reader = Files.newBufferedReader(AppFiles.usersFile())) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] details = line.split(",", -1);
                    if (details.length >= 9) {
                        String email = details[0];
                        String password = details[1];
                        String sex = details[2];
                        String age = details[3];
                        int totalScore = Integer.parseInt(details[4]);
                        int wins = Integer.parseInt(details[5]);
                        int losses = Integer.parseInt(details[6]);
                        int gamesPlayed = Integer.parseInt(details[7]);
                        String profilePicturePath = details[8];

                        UserData user = new UserData(email, password, sex, age, totalScore, wins, losses, gamesPlayed, profilePicturePath);
                        if (winningPlayer.getName().equals("User") && email.equalsIgnoreCase(humanPlayerEmail)) {
                            user.addWin(score);
                        } else if (winningPlayer.getName().equals(email)) {
                            user.addWin(score);
                        } else {
                            user.addLoss();
                        }
                        usersData.add(user);
                    }
                }
            }

            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(AppFiles.usersFile()))) {
                for (UserData user : usersData) {
                    writer.println(String.join(",",
                            user.getUsername(),
                            user.getPassword(),
                            user.getSex(),
                            user.getAge(),
                            String.valueOf(user.getTotalScore()),
                            String.valueOf(user.getWins()),
                            String.valueOf(user.getLosses()),
                            String.valueOf(user.getGamesPlayed()),
                            user.getProfilePicturePath() == null ? "" : user.getProfilePicturePath()
                    ));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error updating user data", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int calculatePlayerScore(Player player) {
        int score = 0;
        for (Card card : player.getHand()) score += cardScore(card);
        return score;
    }

    private int cardScore(Card card) {
        String value = card.getValue();
        if (value.equals("Draw Two") || value.equals("Reverse") || value.equals("Skip")) return 20;
        if (value.equals("Wild") || value.equals("Wild Draw Four")) return 50;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void nextPlayer() {
        if (gameOver || players == null || players.isEmpty()) return;

        // Advance turn index (wraps with direction)
        currentPlayerIndex = nextPlayerIndex();

        // Safety: clamp if something went odd
        if (currentPlayerIndex < 0) currentPlayerIndex = 0;
        if (currentPlayerIndex >= players.size()) currentPlayerIndex = players.size() - 1;

        Player cp = players.get(currentPlayerIndex);
        log("Next turn: " + cp.getName()
                + " (hand: " + cp.getCardCount() + " cards), direction="
                + (isClockwise ? "Clockwise" : "Counter-Clockwise")
                + (currentColor != null ? (", currentColor=" + currentColor) : ""));

        // If it becomes the human's turn, evaluate UNO for the previous player right away
        if (!cp.isBot()) {
            int prevIdx = (currentPlayerIndex - 1 + players.size()) % players.size();
            Player prev = players.get(prevIdx);
            checkUno(prev);
        }

        // Refresh UI and schedule bot if needed
        updateGameUI();
        runBotIfNeeded();
    }


    private void playTurn() {
        Player currentPlayer = players.get(currentPlayerIndex);
        Card topCard = discardPile.get(discardPile.size() - 1);

        Card cardToPlay = currentPlayer.findPlayableCard(topCard);
        if (cardToPlay != null) {
            playCard(cardToPlay);
        } else {
            drawCardUntilPlayable(topCard, currentPlayer);
        }

        checkGameEnd();
        nextPlayer();
    }

    private void drawCardUntilPlayable(Card topCard, Player currentPlayer) {
        boolean cardPlayed = false;
        do {
            if (drawPile.isEmpty()) reshuffleDiscardIntoDraw();
            Card drawnCard = drawPile.drawCard();
            if (drawnCard == null) break;
            currentPlayer.addCard(drawnCard);
            if (canPlayCard(drawnCard, topCard)) {
                playCard(drawnCard);
                cardPlayed = true;
            }
        } while (!cardPlayed && currentPlayer == players.get(currentPlayerIndex));
    }

    private boolean checkUno(Player player) {
        if (player == null) return true; // nothing to check

        // UNO matters only when a player has exactly one card left
        if (player.getCardCount() == 1) {
            if (player.hasCalledUno()) {
                // Good: UNO was called in time
                log(player.getName() + " has UNO!");
                return true;
            } else {
                // Missed UNO → +2 penalty
                log(player.getName() + " forgot to call UNO! +2 penalty applied.");
                JOptionPane.showMessageDialog(
                        this,
                        player.getName() + " forgot to call UNO! Adding 2 penalty cards.",
                        "Missed UNO!",
                        JOptionPane.ERROR_MESSAGE
                );
                applyPenalty(player); // draws 2 and refreshes UI
                return false;
            }
        }

        // Not at UNO (0 or ≥2 cards) — no action needed
        return true;
    }


    private void playBotTurn(Player bot) {
        Card topCard = discardPile.get(discardPile.size() - 1);
        Card cardToPlay = bot.findPlayableCard(topCard);

        if (cardToPlay != null) {
            playCard(cardToPlay);
            if (cardToPlay.getValue().startsWith("Wild")) {
                String newColor = getRandomColor();
                currentColor = newColor;
            }
        } else {
            drawCardUntilPlayable(topCard, bot);
        }

        if (bot.getCardCount() == 1 && !bot.hasCalledUno()) {
            bot.callUno();
        }

        checkGameEnd();
    }

    private void onUnoButtonClicked() {
        if (!isUsersTurn()) {
            JOptionPane.showMessageDialog(this, "You can only call UNO on your turn.", "Not your turn", JOptionPane.WARNING_MESSAGE);
            System.out.println("[DEBUG] onUnoButtonClicked blocked: currentPlayerIndex=" + currentPlayerIndex
                    + ", currentPlayer=" + players.get(currentPlayerIndex).getName());
            return;
        }

        Player me = players.get(currentPlayerIndex);
        if (me.getCardCount() == 1) {
            me.callUno();
            JOptionPane.showMessageDialog(this, me.getName() + " has called UNO!", "UNO Called", JOptionPane.INFORMATION_MESSAGE);
            LOGGER.info(me.getName() + " has called Uno.");
            updateGameUI(); // immediately reflect the disabled UNO button
        } else {
            JOptionPane.showMessageDialog(this, "You can only call UNO when you are about to play your last card!", "UNO Call Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCallOutButtonClicked() {
        System.out.println("[DEBUG] onCallOutButtonClicked: currentPlayer=" + players.get(currentPlayerIndex).getName());

        int prevIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
        Player target = players.get(prevIndex);

        if (target.getCardCount() == 1 && !target.hasCalledUno()) {
            applyPenalty(target); // draw 2 for the target; reshuffles if needed
            JOptionPane.showMessageDialog(this, target.getName() + " didn't call UNO! Penalized with 2 cards.", "Call Out", JOptionPane.INFORMATION_MESSAGE);
            LOGGER.info("Call out applied to " + target.getName());
            updateGameUI();
        } else {
            JOptionPane.showMessageDialog(this, "No eligible player to call out right now.", "Call Out", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void setGameState(Deck drawPile, List<Card> discardPile, List<Player> players, int currentPlayerIndex, boolean isClockwise) {
        this.drawPile = drawPile;
        this.discardPile = discardPile;
        this.players = players;
        this.currentPlayerIndex = currentPlayerIndex;
        this.isClockwise = isClockwise;
        updateGameUI();
    }

    private void setupButtonListeners() {
        unoButton.addActionListener(e -> onUnoButtonClicked());
        callOutButton.addActionListener(e -> onCallOutButtonClicked());
        drawCardButton.addActionListener(e -> drawCard());
        playCardButton.addActionListener(e -> playSelectedCard());
    }
    
    private void runBotIfNeeded() {
        if (gameOver) return;
        if (players == null || players.isEmpty()) return;
        if (currentPlayerIndex < 0 || currentPlayerIndex >= players.size()) return;
        if (turnRunnerBusy) return;

        Player current = players.get(currentPlayerIndex);
        if (current == null || !current.isBot()) return;

        turnRunnerBusy = true;

        new javax.swing.Timer(600, e -> {
            try {
                ((javax.swing.Timer) e.getSource()).stop();

                // Re-check all preconditions on the EDT before acting
                if (gameOver) return;
                if (players == null || players.isEmpty()) return;
                if (currentPlayerIndex < 0 || currentPlayerIndex >= players.size()) return;

                Player cp = players.get(currentPlayerIndex);
                if (cp == null || !cp.isBot()) return;

                System.out.println("[DEBUG] runBotIfNeeded: executing bot turn for " + cp.getName());
                playBotTurn(cp);

                // If it's still a bot after that action (e.g., drew but couldn't play), advance; else just repaint
                if (!gameOver) {
                    if (currentPlayerIndex >= 0 && currentPlayerIndex < players.size()
                            && players.get(currentPlayerIndex).isBot()) {
                        nextPlayer();
                    } else {
                        updateGameUI();
                    }
                }
            } finally {
                turnRunnerBusy = false;
            }
        }).start();
    }

}

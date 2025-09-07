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

    public Session(String sessionName, int playerCount, String humanPlayerEmail) {
        this.sessionName = sessionName;
        this.playerCount = playerCount;
        this.humanPlayerEmail = humanPlayerEmail;
        this.players = new ArrayList<>();
        this.drawPile = new Deck();
        this.discardPile = new ArrayList<>();
        this.currentColor = null;

        setupLogger();
        setupMenu();
        setTitle("UNO Game - " + sessionName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setupUIComponents();
        initializeGame();
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
            if ((line = in.readLine()) == null || !line.equals("GameInfo")) throw new IOException("Invalid save file format");

            sessionName = in.readLine();
            playerCount = Integer.parseInt(in.readLine());
            currentPlayerIndex = Integer.parseInt(in.readLine());
            isClockwise = Boolean.parseBoolean(in.readLine());
            currentColor = in.readLine();
            if ("None".equals(currentColor)) currentColor = null;

            if ((line = in.readLine()) == null || !line.equals("PlayersStart")) throw new IOException("Missing PlayersStart marker");
            players.clear();
            for (int i = 0; i < playerCount; i++) {
                String name = in.readLine();
                boolean isBot = Boolean.parseBoolean(in.readLine());
                int cardCount = Integer.parseInt(in.readLine());
                Player player = new Player(name, isBot);
                for (int j = 0; j < cardCount; j++) {
                    String[] cardData = in.readLine().split(",");
                    player.addCard(new Card(cardData[0], cardData[1]));
                }
                players.add(player);
            }

            if ((line = in.readLine()) == null || !line.equals("DrawPileStart")) throw new IOException("Missing DrawPileStart marker");
            List<Card> drawPileCards = new ArrayList<>();
            while (!(line = in.readLine()).equals("DiscardPileStart")) {
                String[] cardData = line.split(",");
                drawPileCards.add(new Card(cardData[0], cardData[1]));
            }
            drawPile.getCards().clear();
            drawPile.getCards().addAll(drawPileCards);

            discardPile.clear();
            while ((line = in.readLine()) != null) {
                String[] cardData = line.split(",");
                discardPile.add(new Card(cardData[0], cardData[1]));
            }

            JOptionPane.showMessageDialog(this, "Game loaded successfully!", "Game Loaded", JOptionPane.INFORMATION_MESSAGE);
            updateGameUI();
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Failed to load the game: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
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

    private void initializeGame() {
        drawPile.shuffleDeck();
        initializePlayers();
        discardPile.add(drawPile.drawCard());
        currentPlayerIndex = 0;
        isClockwise = true;
        updateGameUI();
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
            drawPilePanel.removeAll();
            drawPilePanel.add(new JLabel("Cards: " + drawPile.size()), BorderLayout.CENTER);

            discardPilePanel.removeAll();
            if (discardPile.isEmpty()) {
                discardPilePanel.add(new JLabel("Discard Pile is empty"), BorderLayout.CENTER);
            } else {
                Card topCard = discardPile.get(discardPile.size() - 1);
                ImageIcon topCardImage = CardImageLoader.getCardImage(topCard.toString());
                if (topCardImage != null) {
                    discardPilePanel.add(new JLabel(topCardImage), BorderLayout.CENTER);
                } else {
                    discardPilePanel.add(new JLabel("Top Discard: " + topCard.toString()), BorderLayout.CENTER);
                }
            }

            userDeckPanel.removeAll();
            Player userPlayer = players.get(0);
            for (Card card : userPlayer.getHand()) {
                ImageIcon cardImage = CardImageLoader.getCardImage(card.toString());
                userDeckPanel.add(new JLabel(cardImage != null ? cardImage : new ImageIcon(), SwingConstants.CENTER) {
                    { setText(cardImage == null ? card.toString() : null); }
                });
            }

            playCardComboBox.removeAllItems();
            if (!userPlayer.isBot()) {
                for (Card card : userPlayer.getHand()) playCardComboBox.addItem(card);
            }

            playerPanel.removeAll();
            for (int i = 1; i < players.size(); i++) {
                Player p = players.get(i);
                playerPanel.add(new JLabel(p.getName() + ": " + p.getCardCount() + " cards"));
            }

            directionLabel.setText("Direction: " + (isClockwise ? "Clockwise" : "Counter-Clockwise"));
            unoButton.setEnabled(players.get(currentPlayerIndex).getCardCount() == 1);

            revalidate();
            repaint();
        });
    }

    private void setupLogger() {
        try {
            Files.createDirectories(AppFiles.dataDir());
            Path logPath = AppFiles.dataDir().resolve("game_logs.txt");
            FileHandler handler = new FileHandler(logPath.toString(), true);
            handler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(handler);
        } catch (IOException e) {
            System.err.println("Failed to set up logger: " + e.getMessage());
        }
    }

    private void playSelectedCard() {
        Card card = (Card) playCardComboBox.getSelectedItem();
        if (card != null) playCard(card);
    }

    private void playCard(Card card) {
        Player currentPlayer = players.get(currentPlayerIndex);
        Card topCard = discardPile.get(discardPile.size() - 1);
        LOGGER.info("Attempting to play: " + card + " | Hand: " + currentPlayer.getCardCount() + " | UNO: " + currentPlayer.hasCalledUno());

        if (canPlayCard(card, topCard)) {
            boolean removed = currentPlayer.removeCard(card);
            if (removed) {
                discardPile.add(card);

                if (card.getValue().startsWith("Wild")) {
                    if (!currentPlayer.isBot()) {
                        currentColor = getUserSelectedColor();
                    } else {
                        currentColor = getRandomColor();
                        JOptionPane.showMessageDialog(this, currentPlayer.getName() + " changes the color to " + currentColor, "Color Changed", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

                if (card.getValue().equals("Reverse")) {
                    isClockwise = !isClockwise;
                    JOptionPane.showMessageDialog(this, "Game direction reversed!", "Reverse Card", JOptionPane.INFORMATION_MESSAGE);
                } else if (card.getValue().equals("Skip")) {
                    JOptionPane.showMessageDialog(this, currentPlayer.getName() + " plays Skip! Next player loses turn.", "Skip Card", JOptionPane.INFORMATION_MESSAGE);
                    skipNextPlayer();
                    updateGameUI();
                    return;
                }

                if (card.getValue().equals("Wild Draw Four")) {
                    applyDraw(nextPlayerIndex(), 4);
                } else if (card.getValue().equals("Draw Two")) {
                    applyDraw(nextPlayerIndex(), 2);
                }

                updateGameUI();

                if (currentPlayer.getCardCount() == 0) {
                    checkUno(currentPlayer);
                    if (currentPlayer.getCardCount() == 0) {
                        JOptionPane.showMessageDialog(this, "Congratulations, " + currentPlayer.getName() + " has won the game!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                        LOGGER.info(currentPlayer.getName() + " wins the game.");
                        endGame();
                        return;
                    }
                }
                nextPlayer();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove card!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid card played!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int nextPlayerIndex() {
        int nextIndex = (currentPlayerIndex + (isClockwise ? 1 : -1)) % players.size();
        if (nextIndex < 0) nextIndex += players.size();
        return nextIndex;
    }

    private void skipNextPlayer() {
        currentPlayerIndex = nextPlayerIndex();
        currentPlayerIndex = nextPlayerIndex();
        LOGGER.info("Skipping player: " + players.get(currentPlayerIndex).getName());
        updateGameUI();
    }

    private void applyDraw(int playerIndex, int cardsCount) {
        Player player = players.get(playerIndex);
        for (int i = 0; i < cardsCount; i++) {
            if (drawPile.isEmpty()) reshuffleDiscardIntoDraw();
            if (!drawPile.isEmpty()) player.addCard(drawPile.drawCard());
        }
        JOptionPane.showMessageDialog(this, player.getName() + " draws " + cardsCount + " cards.", "Cards Drawn", JOptionPane.INFORMATION_MESSAGE);
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

    private void drawCard() {
        Player currentPlayer = players.get(currentPlayerIndex);
        if (!drawPile.isEmpty()) {
            Card drawnCard = drawPile.drawCard();
            currentPlayer.addCard(drawnCard);
        } else {
            reshuffleDiscardIntoDraw();
        }
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
        currentPlayerIndex = nextPlayerIndex();
        Player currentPlayer = players.get(currentPlayerIndex);

        if (!currentPlayer.isBot()) {
            checkUno(players.get((currentPlayerIndex - 1 + players.size()) % players.size()));
        } else {
            playBotTurn(currentPlayer);
        }
        updateGameUI();
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
        if (player.getCardCount() == 1 && !player.hasCalledUno()) {
            JOptionPane.showMessageDialog(this, player.getName() + " forgot to call UNO! Adding 2 penalty cards.", "Missed UNO!", JOptionPane.ERROR_MESSAGE);
            applyPenalty(player);
            return false;
        }
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
        Player currentPlayer = players.get(currentPlayerIndex);
        if (currentPlayer.getCardCount() == 1) {
            currentPlayer.callUno();
            JOptionPane.showMessageDialog(this, currentPlayer.getName() + " has called UNO!", "UNO Called", JOptionPane.INFORMATION_MESSAGE);
            LOGGER.info(currentPlayer.getName() + " has called Uno.");
        } else {
            JOptionPane.showMessageDialog(this, "You can only call UNO when you are about to play your last card!", "UNO Call Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCallOutButtonClicked() {
        Player currentPlayer = players.get(currentPlayerIndex);
        if (currentPlayer.getCardCount() == 1) {
            for (int i = 0; i < 2; i++) currentPlayer.addCard(drawPile.drawCard());
            JOptionPane.showMessageDialog(this, currentPlayer.getName() + " didn't call UNO! Penalized with 2 cards.", "Call Out", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Can't call out a player now!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        updateGameUI();
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
}

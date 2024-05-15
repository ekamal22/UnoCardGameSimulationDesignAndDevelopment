package main.java.Game;

import main.java.Gui.MainMenuPage; // Make sure to import MainMenuPage
import main.java.Player.Player;
import main.java.Object.Card;
import main.java.Object.Deck;
import main.java.User.UserData;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.io.*;
import java.util.logging.*;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class Session extends JFrame {
    private String sessionName;
    private int playerCount;
    private String humanPlayerEmail;  // Track human player's email
    private JLabel sessionNameLabel;
    private JLabel directionLabel;
    private JPanel playerPanel;
    private JPanel drawPilePanel;
    private JPanel discardPilePanel;
    private JButton unoButton;
    private JButton callOutButton;
    private JButton drawCardButton;
    private JComboBox<Card> playCardComboBox;
    private JButton playCardButton;
    private JButton skipTurnButton;

    // Game state variables
    private List<Player> players;
    private boolean isClockwise = true;
    private Deck drawPile;
    private List<Card> discardPile;
    private int currentPlayerIndex = 0;
    private static final Logger LOGGER = Logger.getLogger(Session.class.getName());
    private static final String LOG_FILE_PATH = "game_logs.txt";
    private String currentColor;
    private boolean gameOver = false;

    // Constructor to initialize the game session
    public Session(String sessionName, int playerCount, String humanPlayerEmail) {
        this.sessionName = sessionName;
        this.playerCount = playerCount;
        this.humanPlayerEmail = humanPlayerEmail;
        this.players = new ArrayList<>();
        this.drawPile = new Deck(); // Initialize the deck
        this.discardPile = new ArrayList<>(); // Initialize the discard pile
        this.currentColor = null;

        setupLogger();
        setupMenu();
        setTitle("UNO Game - " + sessionName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setupUIComponents();  // Ensure all UI components are created before game initialization
        initializeGame(); // Now it's safe to call initializeGame
        setVisible(true);
    }

    // Method to set up the UI components for the game session
    private void setupUIComponents() {
        drawPilePanel = new JPanel(new BorderLayout());
        discardPilePanel = new JPanel(new BorderLayout());
        playerPanel = new JPanel(new GridLayout(playerCount + 1, 1)); // +1 for the main player

        // Setting up the main panel and other UI elements
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout());
        sessionNameLabel = new JLabel("Session: " + sessionName);
        directionLabel = new JLabel("Direction: Clockwise");
        topPanel.add(sessionNameLabel);
        topPanel.add(directionLabel);

        JPanel centerPanel = new JPanel(new GridLayout(1, 3));
        centerPanel.add(drawPilePanel);
        centerPanel.add(discardPilePanel);
        centerPanel.add(playerPanel);

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

    // Method to set up the menu for the game session
    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");

        // Existing Menu Items
        JMenuItem profileMenuItem = new JMenuItem("Profile");
        profileMenuItem.addActionListener(e -> showProfile());

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(e -> saveGame());

        JMenuItem exitToMainMenuMenuItem = new JMenuItem("Exit to Main Menu");
        exitToMainMenuMenuItem.addActionListener(e -> exitToMainMenu());

        JMenuItem exitToDesktopMenuItem = new JMenuItem("Exit to Desktop");
        exitToDesktopMenuItem.addActionListener(e -> System.exit(0));

        // New Menu Item for Loading Game
        JMenuItem loadGameMenuItem = new JMenuItem("Load Game");
        loadGameMenuItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try (BufferedReader in = new BufferedReader(new FileReader(selectedFile))) {
                    loadGame(in);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to load the game: " + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Adding items to the menu
        menu.add(profileMenuItem);
        menu.add(saveMenuItem);
        menu.add(loadGameMenuItem);  // Add the load game menu item here
        menu.add(exitToMainMenuMenuItem);
        menu.add(exitToDesktopMenuItem);

        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    // Method to display the player's profile
    private void showProfile() {
        JOptionPane.showMessageDialog(this, "Show player's profile (to be implemented)", "Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    // Method to save the current game state to a file
    private void saveGame() {
        try (PrintWriter out = new PrintWriter(new FileWriter("gameSave.txt"))) {
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

            JOptionPane.showMessageDialog(this, "Game saved successfully.", "Game Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save the game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to load a saved game state from a file
    public void loadGame(BufferedReader in) {
        try {
            String line;
            if ((line = in.readLine()) == null || !line.equals("GameInfo")) throw new IOException("Invalid save file format");

            sessionName = in.readLine();
            playerCount = Integer.parseInt(in.readLine());
            currentPlayerIndex = Integer.parseInt(in.readLine());
            isClockwise = Boolean.parseBoolean(in.readLine());
            currentColor = in.readLine();
            if (currentColor.equals("None")) currentColor = null;

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

    // Method to exit to the main menu
    private void exitToMainMenu() {
        new MainMenuPage(); // Exit to main menu by opening the MainMenuPage and closing the current game session
        dispose();
    }

    // Method to skip the current player's turn
    private void skipTurn() {
        System.out.println("Turn skipped by " + players.get(currentPlayerIndex).getName());
        nextPlayer();
        updateGameUI();
    }

    // Method to initialize the game
    private void initializeGame() {
        drawPile.shuffleDeck(); // Ensure this method is correctly shuffling the cards
        initializePlayers();    // Make sure players are getting their cards
        discardPile.add(drawPile.drawCard()); // This assumes drawPile is not empty
        currentPlayerIndex = 0; // Start with the first player
        isClockwise = true; // Game direction
        updateGameUI(); // Update UI to reflect the initial game state
    }

    // Method to initialize the players
    private void initializePlayers() {
        for (int i = 0; i < playerCount; i++) {
            Player player = new Player(i == 0 ? "User" : "Bot " + i, i != 0);
            for (int j = 0; j < 7; j++) { // Each player gets 7 cards
                if (!drawPile.isEmpty()) {
                    player.addCard(drawPile.drawCard());
                } else {
                    System.out.println("Error: Not enough cards in the deck to distribute");
                }
            }
            players.add(player);
        }
    }

    // Method to update the game UI
    private void updateGameUI() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Updating Game UI");

            // Debugging: Print the number of cards in the draw pile and discard pile
            System.out.println("Draw Pile Size: " + drawPile.size());
            System.out.println("Discard Pile Size: " + discardPile.size());

            // Clear and repopulate the draw pile UI
            drawPilePanel.removeAll();
            drawPilePanel.add(new JLabel("Cards: " + drawPile.size()), BorderLayout.CENTER);

            // Clear and repopulate the discard pile UI
            discardPilePanel.removeAll();
            if (discardPile.isEmpty()) {
                System.out.println("Discard Pile is empty");
                discardPilePanel.add(new JLabel("Discard Pile is empty"), BorderLayout.CENTER);
            } else {
                Card topCard = discardPile.get(discardPile.size() - 1);
                System.out.println("Top Discard Card: " + topCard.toString());
                discardPilePanel.add(new JLabel("Top Discard: " + topCard.toString()), BorderLayout.CENTER);
            }

            // Update players' hands
            playerPanel.removeAll();
            for (Player player : players) {
                System.out.println(player.getName() + " has " + player.getCardCount() + " cards");
                playerPanel.add(new JLabel(player.getName() + ": " + player.getCardCount() + " cards"));
            }

            // Repopulate card selection combo box
            playCardComboBox.removeAllItems();
            Player currentPlayer = players.get(currentPlayerIndex);
            if (!currentPlayer.isBot()) {
                System.out.println("Current player's turn: " + currentPlayer.getName() + ", loading cards:");
                for (Card card : currentPlayer.getHand()) {
                    playCardComboBox.addItem(card);
                    System.out.println("Adding card: " + card);
                }
            } else {
                System.out.println("Bot's turn. No cards loaded in dropdown.");
            }

            // Set game direction
            directionLabel.setText("Direction: " + (isClockwise ? "Clockwise" : "Counter-Clockwise"));
            System.out.println("Game direction: " + (isClockwise ? "Clockwise" : "Counter-Clockwise"));
            unoButton.setEnabled(players.get(currentPlayerIndex).getCardCount() == 1);

            // Ensure UI components are refreshed
            revalidate();
            repaint();
        });
    }

    // Method to set up the logger for the game session
    private void setupLogger() {
        try {
            FileHandler handler = new FileHandler(LOG_FILE_PATH, true);
            handler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(handler);
        } catch (IOException e) {
            System.err.println("Failed to set up logger: " + e.getMessage());
        }
    }

    // Method to handle the event when the play card button is clicked
    private void playSelectedCard() {
        System.out.println("Play Card Button Clicked");
        Card card = (Card) playCardComboBox.getSelectedItem();
        if (card != null) {
            playCard(card);
        }
    }

    // Method to play a card
    private void playCard(Card card) {
        Player currentPlayer = players.get(currentPlayerIndex);
        Card topCard = discardPile.get(discardPile.size() - 1);
        LOGGER.info("Attempting to play card: " + card + ". Current card count: " + currentPlayer.getCardCount() + ", Uno called: " + currentPlayer.hasCalledUno());

        if (canPlayCard(card, topCard)) {
            boolean wasRemoved = currentPlayer.removeCard(card);
            if (wasRemoved) {
                discardPile.add(card);

                // Handling wild cards and prompt for color selection
                if (card.getValue().startsWith("Wild")) {
                    if (!currentPlayer.isBot()) {
                        currentColor = getUserSelectedColor(); // Prompt user to select color
                    } else {
                        currentColor = getRandomColor(); // Bot selects a random color
                        JOptionPane.showMessageDialog(this, currentPlayer.getName() + " changes the color to " + currentColor, "Color Changed", JOptionPane.INFORMATION_MESSAGE);
                        System.out.println(currentPlayer.getName() + " changes the color to " + currentColor);
                    }
                }

                // Handling Reverse and Skip actions
                if (card.getValue().equals("Reverse")) {
                    isClockwise = !isClockwise; // swap game direction
                    JOptionPane.showMessageDialog(this, "Game direction reversed!", "Reverse Card", JOptionPane.INFORMATION_MESSAGE);
                } else if (card.getValue().equals("Skip")) {
                    JOptionPane.showMessageDialog(this, currentPlayer.getName() + " plays Skip! Next player loses turn.", "Skip Card", JOptionPane.INFORMATION_MESSAGE);
                    skipNextPlayer(); // Advances to the next player, skipping one
                    return; // Return early to prevent further action
                }

                // Handle draw actions for Draw Two and Wild Draw Four
                if (card.getValue().equals("Wild Draw Four")) {
                    applyDraw(nextPlayerIndex(), 4); // Apply four cards to the next player
                } else if (card.getValue().equals("Draw Two")) {
                    applyDraw(nextPlayerIndex(), 2); // Apply two cards to the next player
                }

                updateGameUI();
                LOGGER.info("Card played. New card count: " + currentPlayer.getCardCount() + ", Uno called: " + currentPlayer.hasCalledUno());

                // Check if this was the last card and handle game end
                if (currentPlayer.getCardCount() == 0) {
                    checkUno(currentPlayer);
                    if (currentPlayer.getCardCount() == 0) {
                        JOptionPane.showMessageDialog(this, "Congratulations, " + currentPlayer.getName() + " has won the game!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                        LOGGER.info(currentPlayer.getName() + " wins the game.");
                        endGame();
                    }
                } else {
                    nextPlayer();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove card!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid card played!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to get the index of the next player
    private int nextPlayerIndex() {
        int nextIndex = (currentPlayerIndex + (isClockwise ? 1 : -1)) % players.size();
        if (nextIndex < 0) {
            nextIndex += players.size(); // Correct for negative index wrap-around
        }
        return nextIndex;
    }

    // Method to skip the next player's turn
    private void skipNextPlayer() {
        currentPlayerIndex = nextPlayerIndex(); // Advance to the next player
        currentPlayerIndex = nextPlayerIndex(); // Skip the next player due to Skip card
        LOGGER.info("Skipping player: " + players.get(currentPlayerIndex).getName());
        updateGameUI();
    }

    // Method to apply the draw card action
    private void applyDraw(int playerIndex, int cardsCount) {
        Player player = players.get(playerIndex);
        for (int i = 0; i < cardsCount; i++) {
            if (drawPile.isEmpty()) {
                reshuffleDiscardIntoDraw();
            }
            if (!drawPile.isEmpty()) {
                player.addCard(drawPile.drawCard());
            }
        }
        JOptionPane.showMessageDialog(this, player.getName() + " draws " + cardsCount + " cards.", "Cards Drawn", JOptionPane.INFORMATION_MESSAGE);
    }

    // Method to get a random color
    private String getRandomColor() {
        String[] colors = {"Red", "Yellow", "Blue", "Green"};
        return colors[new Random().nextInt(colors.length)];
    }

    // Method to get the user-selected color
    private String getUserSelectedColor() {
        Object[] options = {"Red", "Yellow", "Blue", "Green"};
        int n = JOptionPane.showOptionDialog(this, "Choose a color:", "Wild Card Played",
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        return options[n].toString();
    }

    // Method to check if a card can be played
    private boolean canPlayCard(Card card, Card topCard) {
        if (card.getValue().startsWith("Wild")) {
            return true; // Wild cards can always be played
        }
        if (topCard.getValue().startsWith("Wild")) {
            // When top card is Wild, check if the played card matches the current color set by Wild
            if (card.getColor().equals(currentColor)) {
                System.out.println("Playing " + card + " as it matches the current wild color: " + currentColor);
                return true;
            }
        } else if (card.getColor().equals(topCard.getColor()) || card.getValue().equals(topCard.getValue())) {
            // Normal play rule: match by color or value
            System.out.println("Playing " + card + " as it matches the color or value of " + topCard);
            return true;
        }
        System.out.println("Cannot play " + card + " on top of " + topCard + " with current wild color " + currentColor);
        return false;
    }

    // Method for drawing a card for the current player
    private void drawCard() {
        Player currentPlayer = players.get(currentPlayerIndex);
        if (!drawPile.isEmpty()) {
            Card drawnCard = drawPile.drawCard();
            currentPlayer.addCard(drawnCard);
        } else {
            reshuffleDiscardIntoDraw();
        }
        updateGameUI();
        checkGameEnd();  // Check if drawing the last card ends the game
        nextPlayer();
    }

    // Method to check if the game should end
    private void checkGameEnd() {
        Player currentPlayer = players.get(currentPlayerIndex);
        // Checking if game should end based on current player's card count
        if (currentPlayer.getCardCount() == 0) {
            if (checkUno(currentPlayer)) {
                // If UNO was called or not needed, congratulate and end game
                JOptionPane.showMessageDialog(this, "Game Over, " + currentPlayer.getName() + " wins!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                System.out.println(currentPlayer.getName() + " wins the game!");
                endGame();
            } else {
                // If checkUno applied a penalty, the game continues
                updateGameUI(); // Updating UI to reflect the penalty
            }
        }
    }

    // Penalty method
    private void applyPenalty(Player player) {
        for (int i = 0; i < 2; i++) {
            if (drawPile.isEmpty()) {
                reshuffleDiscardIntoDraw(); // Reshuffling if the draw pile is empty before drawing a penalty card
            }
            if (!drawPile.isEmpty()) {
                player.addCard(drawPile.drawCard());
            }
        }
        updateGameUI(); // Updating the UI after applying the penalty
    }

    // reshuffle the discard pile into the draw pile
    private void reshuffleDiscardIntoDraw() {
        if (discardPile.size() > 1) {
            Card lastCard = discardPile.remove(discardPile.size() - 1); // Preserving the last card on the discard pile
            drawPile.getCards().addAll(discardPile); // Moving all other cards to the draw pile
            drawPile.shuffleDeck(); // Shuffling the new draw pile
            discardPile.clear();
            discardPile.add(lastCard); // Putting the last card back on the discard pile
        } else {
            // Handling the potential edge case where there is no card to reshuffle
            JOptionPane.showMessageDialog(this, "No cards left to reshuffle!", "Reshuffle Error", JOptionPane.ERROR_MESSAGE);
        }
        updateGameUI(); // Updating the UI after reshuffling
    }

    // logic to end the game and to update the leaderboard
    private void endGame() {
        Player winningPlayer = players.get(currentPlayerIndex);
        int totalScore = 0;

        StringBuilder scoreMessage = new StringBuilder("Game Over\n");

        //calculating the total score from the losing players' hands
        for (Player player : players) {
            if (player != winningPlayer) {
                totalScore += calculatePlayerScore(player);
            }
        }

        // Appending the winning player's score
        scoreMessage.append(winningPlayer.getName()).append(": ").append(totalScore).append(" points\n");

        // Appending the losing players' scores (which are 0)
        for (Player player : players) {
            if (player != winningPlayer) {
                scoreMessage.append(player.getName()).append(": 0 points\n");
            }
        }

        // Summary message for the winning player
        scoreMessage.append(winningPlayer.getName()).append(" wins and earns ").append(totalScore).append(" points\n");

        JOptionPane.showMessageDialog(this, scoreMessage.toString(), "Game Over", JOptionPane.INFORMATION_MESSAGE);

        // Update leaderboard
        updateLeaderboard(winningPlayer, totalScore);

        // Using a timer to delay the closure of the game window which gives time to see the game over message
        Timer timer = new Timer(5000, e -> {
            dispose(); // Closes the game window
            new MainMenuPage(); // redirects to the main menu
        });
        timer.setRepeats(false);
        timer.start();
    }

    // updating the leaderboard with the latest game results
    private void updateLeaderboard(Player winningPlayer, int score) {
        File file = new File("C:\\Users\\Effendi Jabid Kamal\\eclipse-workspace\\UnoCardGameSimulationDesignAndDevelopment\\src\\main\\java\\DataFiles\\users.txt");
        List<UserData> usersData = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details.length >= 8) { // to ensure it matches the new format
                    String email = details[0];
                    String password = details[1];
                    String sex = details[2];
                    String age = details[3];
                    int totalScore = Integer.parseInt(details[4]);
                    int wins = Integer.parseInt(details[5]);
                    int losses = Integer.parseInt(details[6]);
                    int gamesPlayed = Integer.parseInt(details[7]);

                    UserData user = new UserData(email, password, sex, age, totalScore, wins, losses, gamesPlayed);
                    if (winningPlayer.getName().equals("User") && email.equals(humanPlayerEmail)) {
                        user.addWin(score);
                    } else if (winningPlayer.getName().equals(email)) {
                        user.addWin(score);
                    } else {
                        user.addLoss();
                    }
                    usersData.add(user);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading user data", "Error", JOptionPane.ERROR_MESSAGE);
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (UserData user : usersData) {
                writer.println(user.getUsername() + "," + user.getPassword() + "," + user.getSex() + "," + user.getAge() + "," + user.getTotalScore() + "," + user.getWins() + "," + user.getLosses() + "," + user.getGamesPlayed());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving user data", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to calculate the score of a player's hand
    private int calculatePlayerScore(Player player) {
        int score = 0;
        for (Card card : player.getHand()) {
            score += cardScore(card);
        }
        return score;
    }

    // Method to get the score value of a card
    private int cardScore(Card card) {
        String value = card.getValue();
        if (value.equals("Draw Two") || value.equals("Reverse") || value.equals("Skip")) {
            return 20; // Action cards score
        } else if (value.equals("Wild") || value.equals("Wild Draw Four")) {
            return 50; // Wild cards score
        } else {
            try {
                return Integer.parseInt(value); // Number cards score
            } catch (NumberFormatException e) {
                System.err.println("Invalid card value: " + value);
                return 0; // Default to 0 for any unexpected values
            }
        }
    }

    // Logic for progressing to the next player's turn
    private void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        Player currentPlayer = players.get(currentPlayerIndex);

        if (!currentPlayer.isBot()) {
            System.out.println("Human player's turn: " + currentPlayer.getName());
            // For checking Uno right at the beginning of the turn for the last player
            checkUno(players.get((currentPlayerIndex - 1 + players.size()) % players.size()));
        } else {
            playBotTurn(currentPlayer);
        }
        updateGameUI();
    }

    // Method which handles the current player's turn
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

    // Method which draw cards until a playable card is found
    private void drawCardUntilPlayable(Card topCard, Player currentPlayer) {
        boolean cardPlayed = false;
        do {
            if (drawPile.isEmpty()) {
                reshuffleDiscardIntoDraw();
            }
            Card drawnCard = drawPile.drawCard();
            if (drawnCard == null) {
                break; // Break if no cards left to draw
            }
            currentPlayer.addCard(drawnCard);
            if (canPlayCard(drawnCard, topCard)) {
                // (extra feature!): To optionally play the drawn card immediately if it's playable
                playCard(drawnCard);
                cardPlayed = true;
            }
        } while (!cardPlayed && currentPlayer == players.get(currentPlayerIndex));
        // Only loop if the current player is still the active one and has not played a card
    }

    // Method to check if a player has called UNO
    private boolean checkUno(Player player) {
        if (player.getCardCount() == 1 && !player.hasCalledUno()) {
            JOptionPane.showMessageDialog(this, player.getName() + " forgot to call UNO! Adding 2 penalty cards.", "Missed UNO!", JOptionPane.ERROR_MESSAGE);
            applyPenalty(player);
            return false; // Return false which indicates UNO was not called properly
        }
        return true; // Return true if UNO was called 
    }

    // Method to handle the bot's turn
    private void playBotTurn(Player bot) {
        Card topCard = discardPile.get(discardPile.size() - 1);
        Card cardToPlay = bot.findPlayableCard(topCard);

        if (cardToPlay != null) {
            System.out.println(bot.getName() + " is playing " + cardToPlay);
            playCard(cardToPlay);
            if (cardToPlay.getValue().startsWith("Wild")) {
                String newColor = getRandomColor(); // Bot selects a random color
                currentColor = newColor; // Set the current color after a wild card
                System.out.println(bot.getName() + " changed the color to " + newColor);
            }
        } else {
            System.out.println(bot.getName() + " cannot play, drawing cards...");
            drawCardUntilPlayable(topCard, bot);
        }

        if (bot.getCardCount() == 1 && !bot.hasCalledUno()) {
            System.out.println(bot.getName() + " has one card left but did not call UNO!");
            bot.callUno(); // Simulate bot calling UNO, or handle missing UNO
        }

        checkGameEnd();
    }

    // Method which handles the event when the UNO button is clicked
    private void onUnoButtonClicked() {
        Player currentPlayer = players.get(currentPlayerIndex);
        if (currentPlayer.getCardCount() == 1) { // Player is about to play the last card
            currentPlayer.callUno();
            JOptionPane.showMessageDialog(this, currentPlayer.getName() + " has called UNO!", "UNO Called", JOptionPane.INFORMATION_MESSAGE);
            LOGGER.info(currentPlayer.getName() + " has called Uno.");
        } else {
            JOptionPane.showMessageDialog(this, "You can only call UNO when you are about to play your last card!", "UNO Call Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method which handles the event when the Call Out button is clicked
    private void onCallOutButtonClicked() {
        System.out.println("Call Out Button Clicked");
        Player currentPlayer = players.get(currentPlayerIndex);
        if (currentPlayer.getCardCount() == 1) {
            for (int i = 0; i < 2; i++) {
                currentPlayer.addCard(drawPile.drawCard());
            }
            JOptionPane.showMessageDialog(this, currentPlayer.getName() + " didn't call UNO! Penalized with 2 cards.", "Call Out", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Can't call out a player now!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        updateGameUI();
    }

    // Method to set the game state
    public void setGameState(Deck drawPile, List<Card> discardPile, List<Player> players, int currentPlayerIndex, boolean isClockwise) {
        this.drawPile = drawPile;
        this.discardPile = discardPile;
        this.players = players;
        this.currentPlayerIndex = currentPlayerIndex;
        this.isClockwise = isClockwise;
        updateGameUI();  // Ensuring the UI reflects the loaded state
    }

    // Method to set up button listeners for the game
    private void setupButtonListeners() {
        unoButton.addActionListener(e -> onUnoButtonClicked());
        callOutButton.addActionListener(e -> onCallOutButtonClicked());
        drawCardButton.addActionListener(e -> drawCard());
        playCardButton.addActionListener(e -> playSelectedCard());
    }

}

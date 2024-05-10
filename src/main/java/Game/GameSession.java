package main.java.Game;

import main.java.Gui.MainMenuPage; // Make sure to import MainMenuPage
import main.java.Player.Player;
import main.java.Object.Card;
import main.java.Object.Deck;

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

public class GameSession extends JFrame {
    private String sessionName;
    private int playerCount;
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
    private static final Logger LOGGER = Logger.getLogger(GameSession.class.getName());
    private static final String LOG_FILE_PATH = "game_logs.txt";
    private String currentColor;


    public GameSession(String sessionName, int playerCount) {
        this.sessionName = sessionName;
        this.playerCount = playerCount;
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
    
    private void setupUIComponents() {
        // Initialize all UI components here
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
        loadGameMenuItem.addActionListener(e -> loadGame());

        // Adding items to the menu
        menu.add(profileMenuItem);
        menu.add(saveMenuItem);
        menu.add(loadGameMenuItem);  // Add the load game menu item here
        menu.add(exitToMainMenuMenuItem);
        menu.add(exitToDesktopMenuItem);

        menuBar.add(menu);
        setJMenuBar(menuBar);
    }


    private void showProfile() {
        JOptionPane.showMessageDialog(this, "Show player's profile (to be implemented)", "Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveGame() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("gameSave.ser"))) {
            out.writeObject(drawPile); // Write the deck to the output stream
            out.writeObject(discardPile); // Write the discard pile to the output stream
            out.writeObject(players); // Write the list of players to the output stream
            out.writeInt(currentPlayerIndex); // Write the current player index to the output stream
            out.writeBoolean(isClockwise); // Write the game direction to the output stream
            JOptionPane.showMessageDialog(this, "Game saved successfully.", "Game Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save the game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void loadGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(".")); // Ensure this is the correct path
        fileChooser.setDialogTitle("Select Saved Game File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("UNO Save Files", "ser")); // Make sure the extension is correct

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(selectedFile))) {
                drawPile = (Deck) in.readObject();
                discardPile = (List<Card>) in.readObject();
                players = (List<Player>) in.readObject();
                currentPlayerIndex = in.readInt();
                isClockwise = in.readBoolean();

                setGameState(drawPile, discardPile, players, currentPlayerIndex, isClockwise);
                JOptionPane.showMessageDialog(this, "Game loaded successfully!", "Game Loaded", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace(); // This will print more detailed information about the exception
                JOptionPane.showMessageDialog(this, "Failed to load the game: " + (ex.getMessage() != null ? ex.getMessage() : "Unknown error"), "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }






    private void exitToMainMenu() {
        // Exit to main menu by opening the MainMenuPage and closing the current game session
        new MainMenuPage();
        dispose();
    }
    
    private void skipTurn() {
        System.out.println("Turn skipped by " + players.get(currentPlayerIndex).getName());
        nextPlayer();
        updateGameUI();
    }
    

    private void initializeGame() {
        drawPile.shuffleDeck(); // Ensure this method is correctly shuffling the cards
        initializePlayers();    // Make sure players are getting their cards
        discardPile.add(drawPile.drawCard()); // This assumes drawPile is not empty
        currentPlayerIndex = 0; // Start with the first player
        isClockwise = true; // Game direction
        updateGameUI(); // Update UI to reflect the initial game state
    }
    
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

    private void initializeGameState() {
        drawPile.shuffleDeck();
        discardPile.add(drawPile.drawCard()); // Ensure this is not null
        updateGameUI(); // Make sure this is called after setting up the game state
    }
    
    
    private void updateGameUI() {
        System.out.println("Updating Game UI");

        // Debugging: Print the number of cards in the draw pile and discard pile
        System.out.println("Draw Pile Size: " + drawPile.size());
        System.out.println("Discard Pile Size: " + discardPile.size());

        // Clear and repopulate the draw pile UI
        drawPilePanel.removeAll();
        drawPilePanel.add(new JLabel("Cards: " + drawPile.size()), BorderLayout.CENTER);

        // Clear and repopulate the discard pile UI
        discardPilePanel.removeAll();
        if (!discardPile.isEmpty()) {
            Card topCard = discardPile.get(discardPile.size() - 1);
            System.out.println("Top Discard Card: " + topCard.toString()); // Debugging: Output the top card of the discard pile
            discardPilePanel.add(new JLabel("Top Discard: " + topCard.toString()), BorderLayout.CENTER);
        } else {
            System.out.println("Discard Pile is empty"); // Debugging: Indicate that the discard pile is empty
            discardPilePanel.add(new JLabel("Discard Pile is empty"), BorderLayout.CENTER);
        }

        // Update players' hands
        playerPanel.removeAll();
        for (Player player : players) {
            System.out.println(player.getName() + " has " + player.getCardCount() + " cards"); // Debugging: Output each player's card count
            playerPanel.add(new JLabel(player.getName() + ": " + player.getCardCount() + " cards"));
        }

        // Repopulate card selection combo box
        playCardComboBox.removeAllItems();
        if (!players.isEmpty() && currentPlayerIndex < players.size()) {
            Player currentPlayer = players.get(currentPlayerIndex);
            System.out.println("Current Player: " + currentPlayer.getName() + " with hand:");
            for (Card card : currentPlayer.getHand()) {
                System.out.println(card); // Debug: Output each card
                playCardComboBox.addItem(card);
            }
        }

        // Set game direction
        directionLabel.setText("Direction: " + (isClockwise ? "Clockwise" : "Counter-Clockwise"));
        System.out.println("Game direction: " + (isClockwise ? "Clockwise" : "Counter-Clockwise")); // Debugging: Indicate the current direction of play

        // Ensure UI components are refreshed
        revalidate();
        repaint();
    }



    
    private void setupLogger() {
        try {
            FileHandler handler = new FileHandler(LOG_FILE_PATH, true);
            handler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(handler);
        } catch (IOException e) {
            System.err.println("Failed to set up logger: " + e.getMessage());
        }
    }

    

    private void playSelectedCard() {
        System.out.println("Play Card Button Clicked");
        Card card = (Card) playCardComboBox.getSelectedItem();
        if (card != null) {
            playCard(card);
        }
    }

    private void playCard(Card card) {
        Player currentPlayer = players.get(currentPlayerIndex);
        System.out.println("Attempting to play card: " + card);

        // Check if the card can be played
        Card topCard = discardPile.get(discardPile.size() - 1);
        if (canPlayCard(card, topCard)) {
            // Remove the card from the player's hand
            boolean wasRemoved = currentPlayer.removeCard(card);
            if (wasRemoved) {
                System.out.println("Card " + card + " removed from " + currentPlayer.getName() + "'s hand.");
            } else {
                System.out.println("Failed to remove card " + card + " from " + currentPlayer.getName() + "'s hand.");
            }

            // Add the card to the discard pile
            discardPile.add(card);
            System.out.println("Card " + card + " added to the discard pile.");

            // Handle wild card color change if needed
            if (card.getValue().startsWith("Wild")) {
                String newColor = currentPlayer.isBot() ? getRandomColor() : getUserSelectedColor();
                currentColor = newColor; // Update current color for gameplay
                JOptionPane.showMessageDialog(this, "Color changed to " + newColor, "Color Change", JOptionPane.INFORMATION_MESSAGE);
                System.out.println("Wild card played. Color changed to: " + newColor);
            }

            // Check if player has only one card left
            if (currentPlayer.getCardCount() == 1 && !currentPlayer.hasCalledUno()) {
                JOptionPane.showMessageDialog(this, "UNO! " + currentPlayer.getName() + " has one card left!", "UNO", JOptionPane.INFORMATION_MESSAGE);
                currentPlayer.callUno();
            }

            // Update the UI
            updateGameUI();

            checkGameEnd();  // Ensure this is called right after the UI update if a card is played
            if (currentPlayer.getCardCount() > 0) {
                nextPlayer();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid card played!", "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Invalid card played. Card does not match the top card of the discard pile.");
        }
        }
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

    
    
    private void checkGameEnd() {
        Player currentPlayer = players.get(currentPlayerIndex);
        // Check if the current player has no cards left
        if (currentPlayer.getCardCount() == 0) {
            if (currentPlayer.hasCalledUno()) {
                // Player has correctly called UNO and won the game
                int score = calculateScore();  // Assuming this method calculates the total score based on remaining cards in other players' hands
                LOGGER.info(currentPlayer.getName() + " won the game with " + score + " points!");
                JOptionPane.showMessageDialog(this, "Game Over, Player: " + currentPlayer.getName() + " wins!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                endGame(); // Terminate the game
            } else {
                // Player did not call UNO but reached 0 cards, penalty applied
                JOptionPane.showMessageDialog(this, currentPlayer.getName() + " did not call UNO! Penalized with 2 cards.", "Missed UNO!", JOptionPane.ERROR_MESSAGE);
                applyPenalty(currentPlayer);
                nextPlayer();  // Continue the game allowing other players to play
            }
        } else {
            // No one has won yet, continue the game
            nextPlayer();
        }
    }


    private void applyPenalty(Player player) {
        for (int i = 0; i < 2; i++) {
            if (drawPile.isEmpty()) {
                reshuffleDiscardIntoDraw(); // Reshuffle if the draw pile is empty before drawing a penalty card
            }
            if (!drawPile.isEmpty()) {
                player.addCard(drawPile.drawCard());
            }
        }
        updateGameUI(); // Update the UI after applying the penalty
    }

    private void reshuffleDiscardIntoDraw() {
        if (discardPile.size() > 1) {
            Card lastCard = discardPile.remove(discardPile.size() - 1); // Preserve the last card on the discard pile
            drawPile.getCards().addAll(discardPile); // Move all other cards to the draw pile
            drawPile.shuffleDeck(); // Shuffle the new draw pile
            discardPile.clear();
            discardPile.add(lastCard); // Put the last card back on the discard pile
        } else {
            // Handle potential edge case where there is no card to reshuffle
            JOptionPane.showMessageDialog(this, "No cards left to reshuffle!", "Reshuffle Error", JOptionPane.ERROR_MESSAGE);
        }
        updateGameUI(); // Update the UI after reshuffling
    }


    
    private void endGame() {
        // Use a timer to delay the closure of the game window, providing time to see the game over message
        Timer timer = new Timer(5000, e -> {
            dispose(); // Close the game window
            new MainMenuPage(); // Optionally, redirect to the main menu
        });
        timer.setRepeats(false);
        timer.start();
    }



    private int calculateScore() {
        int score = 0;
        for (Player player : players) {
            for (Card card : player.getHand()) {
                score += cardScore(card);
            }
        }
        return score;
    }


    private int cardScore(Card card) {
        // Define the score based on card type
        String value = card.getValue();
        if (value.equals("Draw Two") || value.equals("Reverse") || value.equals("Skip")) {
            return 20;
        } else if (value.equals("Wild") || value.equals("Wild Draw Four")) {
            return 50;
        } else {
            return Integer.parseInt(value); // Score for number cards
        }
    }

    private void nextPlayer() {
        // Calculate the next player index considering the direction of play
        int nextIndex = isClockwise ? (currentPlayerIndex + 1) % playerCount : (currentPlayerIndex - 1 + playerCount) % playerCount;
        
        // Check if a "Skip" card was played; if so, skip the next player's turn
        if (!discardPile.isEmpty() && discardPile.get(discardPile.size() - 1).getValue().equals("Skip")) {
            System.out.println("Skipping turn due to a Skip card.");
            nextIndex = isClockwise ? (nextIndex + 1) % playerCount : (nextIndex - 1 + playerCount) % playerCount; // Advance one more spot
        }

        currentPlayerIndex = nextIndex; // Set the new current player index

        // Check if it's a bot's turn, if yes, let the bot play automatically
        Player currentPlayer = players.get(currentPlayerIndex);
        if (currentPlayer.isBot()) {
            playBotTurn(currentPlayer);
        }

        updateGameUI(); // Update the user interface after changing the player
    }

    
    private void playTurn() {
        Player currentPlayer = players.get(currentPlayerIndex);
        Card topCard = discardPile.get(discardPile.size() - 1);

        Card cardToPlay = currentPlayer.findPlayableCard(topCard);
        if (cardToPlay != null) {
            playCard(cardToPlay);
        } else {
            drawCardUntilPlayable(topCard, currentPlayer);  // Include currentPlayer here
        }

        checkGameEnd();
        nextPlayer();
    }


    
    
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
                // Optionally play the drawn card immediately if it's playable
                playCard(drawnCard);
                cardPlayed = true;
            }
        } while (!cardPlayed && currentPlayer == players.get(currentPlayerIndex));
        // Only loop if the current player is still the active one and has not played a card
    }
    
    private void checkUno(Player player) {
        if (player.getCardCount() == 1 && !player.hasCalledUno()) {
            JOptionPane.showMessageDialog(this, player.getName() + " forgot to call UNO! Adding 2 penalty cards.", "Missed UNO!", JOptionPane.ERROR_MESSAGE);
            for (int i = 0; i < 2; i++) {  // Apply penalty
                if (!drawPile.isEmpty()) {
                    player.addCard(drawPile.drawCard());
                }
            }
        }
        updateGameUI();
    }



    private void playBotTurn(Player bot) {
        Card topCard = discardPile.get(discardPile.size() - 1);
        Card cardToPlay = bot.findPlayableCard(topCard);

        if (cardToPlay != null) {
            playCard(cardToPlay);
        } else {
            drawCardUntilPlayable(topCard, bot);  // Correctly pass the bot as the player
        }
    }

    private void onUnoButtonClicked() {
        System.out.println("UNO Button Clicked");
        Player currentPlayer = players.get(currentPlayerIndex);
        if (currentPlayer.getCardCount() == 1) {
            JOptionPane.showMessageDialog(this, currentPlayer.getName() + " called UNO!", "UNO", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "You can't call UNO now!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

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
    
    

    
    public void setGameState(Deck drawPile, List<Card> discardPile, List<Player> players, int currentPlayerIndex, boolean isClockwise) {
        this.drawPile = drawPile;
        this.discardPile = discardPile;
        this.players = players;
        this.currentPlayerIndex = currentPlayerIndex;
        this.isClockwise = isClockwise;
        updateGameUI();  // Ensure the UI reflects the loaded state
    }

    
    private void setupButtonListeners() {
        unoButton.addActionListener(e -> onUnoButtonClicked());
        callOutButton.addActionListener(e -> onCallOutButtonClicked());
        drawCardButton.addActionListener(e -> drawCard());
        playCardButton.addActionListener(e -> playSelectedCard());
    }

    
}
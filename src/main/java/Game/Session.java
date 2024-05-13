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

public class Session extends JFrame {
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
    private static final Logger LOGGER = Logger.getLogger(Session.class.getName());
    private static final String LOG_FILE_PATH = "game_logs.txt";
    private String currentColor;


    public Session(String sessionName, int playerCount) {
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
        Player currentPlayer = players.get(currentPlayerIndex);
        if (!currentPlayer.isBot()) {
            System.out.println("Current player's turn: " + currentPlayer.getName() + ", loading cards:");
            for (Card card : currentPlayer.getHand()) {
                System.out.println("Adding card: " + card);
                playCardComboBox.addItem(card);
            }
        } else {
            System.out.println("Bot's turn. No cards loaded in dropdown.");
        }


        // Set game direction
        directionLabel.setText("Direction: " + (isClockwise ? "Clockwise" : "Counter-Clockwise"));
        System.out.println("Game direction: " + (isClockwise ? "Clockwise" : "Counter-Clockwise")); // Debugging: Indicate the current direction of play
        unoButton.setEnabled(players.get(currentPlayerIndex).getCardCount() == 1);
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
        Card topCard = discardPile.get(discardPile.size() - 1);

        LOGGER.info("Attempting to play card. Current card count: " + currentPlayer.getCardCount() + ", Uno called: " + currentPlayer.hasCalledUno());

        if (canPlayCard(card, topCard)) {
            boolean wasRemoved = currentPlayer.removeCard(card);
            if (wasRemoved) {
                discardPile.add(card);
                updateGameUI();

                LOGGER.info("Card played. New card count: " + currentPlayer.getCardCount() + ", Uno called: " + currentPlayer.hasCalledUno());

                // Check if the player played their last card
                if (currentPlayer.getCardCount() == 0) {
                	System.out.println(currentPlayer.hasCalledUno());
                    if (currentPlayer.hasCalledUno()) {
                        // Handle the case where Uno was called correctly when playing the last card
                        JOptionPane.showMessageDialog(this, "Congratulations, " + currentPlayer.getName() + " has won the game!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                        LOGGER.info(currentPlayer.getName() + " wins the game with a correct Uno call.");
                        currentPlayer.clearUnoCall(); // Clear the Uno call after a successful win
                        checkGameEnd();
                    } else {
                        // Handle the case where Uno was NOT called correctly when playing the last card
                        applyPenalty(currentPlayer);
                        JOptionPane.showMessageDialog(this, currentPlayer.getName() + " did not call UNO before playing the last card! Penalty applied.", "Missed UNO", JOptionPane.ERROR_MESSAGE);
                        LOGGER.info(currentPlayer.getName() + " failed to call Uno on the last card and received a penalty.");
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







    
    
    private void promptUnoCall(Player player) {
        if (player.isBot()) {
            // Simulate bot behavior for calling Uno
            player.callUno();
            LOGGER.info(player.getName() + " calls Uno automatically.");
        } else {
            int response = JOptionPane.showConfirmDialog(this, "Do you want to call Uno?", "Call Uno?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                player.callUno();
                JOptionPane.showMessageDialog(this, "Uno called!", "Uno", JOptionPane.INFORMATION_MESSAGE);
                LOGGER.info(player.getName() + " has called Uno.");
            } else {
                applyPenalty(player);
                JOptionPane.showMessageDialog(this, player.getName() + " forgot to call Uno! Penalty applied.", "Missed Uno", JOptionPane.ERROR_MESSAGE);
                LOGGER.info(player.getName() + " missed calling Uno and received a penalty.");
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
        if (currentPlayer.getCardCount() == 0) {
            if (currentPlayer.hasCalledUno()) {
                JOptionPane.showMessageDialog(this, "Game Over, " + currentPlayer.getName() + " wins!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                System.out.println(currentPlayer.getName() + " wins the game!");
                endGame();
            } else {
                // Apply penalty for not calling UNO
                currentPlayer.addCard(drawPile.drawCard());
                currentPlayer.addCard(drawPile.drawCard());
                JOptionPane.showMessageDialog(this, currentPlayer.getName() + " reached 0 cards without calling UNO! Penalty applied.", "Penalty", JOptionPane.ERROR_MESSAGE);
                updateGameUI(); // Update UI to reflect the penalty
                nextPlayer(); // Continue the game
            }
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
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        Player currentPlayer = players.get(currentPlayerIndex);

        // Clear Uno call at the start of a new turn if necessary
        if (currentPlayer.hasCalledUno() && currentPlayer.getCardCount() > 1) {
            currentPlayer.clearUnoCall();
        }

        if (!currentPlayer.isBot()) {
            System.out.println("Human player's turn: " + currentPlayer.getName());
        } else {
            playBotTurn(currentPlayer);
        }
        updateGameUI();  // Ensure UI is updated whenever the turn changes
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
package main.java.Game;

import main.java.Gui.MainMenuPage; // Make sure to import MainMenuPage
import main.java.Player.Player;
import main.java.Object.Card;
import main.java.Object.Deck;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
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

    // Game state variables
    private List<Player> players;
    private boolean isClockwise = true;
    private Deck drawPile;
    private List<Card> discardPile;
    private int currentPlayerIndex = 0;
    private static final Logger LOGGER = Logger.getLogger(GameSession.class.getName());
    private static final String LOG_FILE_PATH = "game_logs.txt";

    public GameSession(String sessionName, int playerCount) {
        this.sessionName = sessionName;
        this.playerCount = playerCount;
        this.players = new ArrayList<>();
        this.drawPile = new Deck(); // Initialize the deck
        this.discardPile = new ArrayList<>(); // Initialize the discard pile
        
        
        
        setupLogger();
        setupMenu();
        setTitle("UNO Game - " + sessionName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);

        // Initialize game state and GUI here
        initializeGame();
        setVisible(true);
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

        menu.add(profileMenuItem);
        menu.add(saveMenuItem);
        menu.add(exitToMainMenuMenuItem);
        menu.add(exitToDesktopMenuItem);

        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    private void showProfile() {
        JOptionPane.showMessageDialog(this, "Show player's profile (to be implemented)", "Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveGame() {
        try (FileOutputStream fileOut = new FileOutputStream(sessionName + "_game.ser");
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            // Save players and their hands
            out.writeObject(players);
            
            // Save the draw pile and discard pile
            out.writeObject(drawPile);
            out.writeObject(discardPile);
            
            // Save the current game state
            out.writeBoolean(isClockwise);
            out.writeInt(currentPlayerIndex);
            
            JOptionPane.showMessageDialog(this, "Game saved successfully!", "Save", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving game: " + e.getMessage(), "Save", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exitToMainMenu() {
        // Exit to main menu by opening the MainMenuPage and closing the current game session
        new MainMenuPage();
        dispose();
    }    
    

    private void initializeGame() {
        // Initialize and set up game state and UI components
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Top panel for session name and direction
        JPanel topPanel = new JPanel(new FlowLayout());
        sessionNameLabel = new JLabel("Session: " + sessionName);
        directionLabel = new JLabel("Direction: Clockwise");
        topPanel.add(sessionNameLabel);
        topPanel.add(directionLabel);

        // Center panel for game area
        JPanel centerPanel = new JPanel(new GridLayout(1, 3));
        drawPilePanel = new JPanel(new BorderLayout());
        discardPilePanel = new JPanel(new BorderLayout());
        playerPanel = new JPanel(new GridLayout(playerCount + 1, 1)); // +1 for the main player

        drawPilePanel.setBorder(BorderFactory.createTitledBorder("Draw Pile"));
        discardPilePanel.setBorder(BorderFactory.createTitledBorder("Discard Pile"));
        playerPanel.setBorder(BorderFactory.createTitledBorder("Players"));

        centerPanel.add(drawPilePanel);
        centerPanel.add(discardPilePanel);
        centerPanel.add(playerPanel);

        // Bottom panel for controls
        JPanel bottomPanel = new JPanel(new FlowLayout());
        unoButton = new JButton("UNO");
        unoButton.addActionListener(e -> onUnoButtonClicked());
        callOutButton = new JButton("Call Out");
        callOutButton.addActionListener(e -> onCallOutButtonClicked());
        drawCardButton = new JButton("Draw Card");
        drawCardButton.addActionListener(e -> drawCard());
        playCardComboBox = new JComboBox<>();
        playCardButton = new JButton("Play Card");
        playCardButton.addActionListener(e -> playSelectedCard());

        bottomPanel.add(unoButton);
        bottomPanel.add(callOutButton);
        bottomPanel.add(drawCardButton);
        bottomPanel.add(playCardComboBox);
        bottomPanel.add(playCardButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Initialize players and game state
        initializePlayers();
        initializeGameState();
        updateGameUI();
    }
    
    private void initializePlayers() {
        players.add(new Player("User", false)); // Add the human player

        // Add computer bots
        for (int i = 1; i < playerCount; i++) {
            players.add(new Player("Bot " + i, true));
        }

        // Deal initial cards to players
        for (Player player : players) {
            for (int i = 0; i < 7; i++) {
                player.addCard(drawPile.drawCard());
            }
        }
    }

    private void initializeGameState() {
        // Add the first card from the draw pile to the discard pile
        discardPile.add(drawPile.drawCard());
    }

    private void updateGameUI() {
        // Update the draw pile, discard pile, and player panels
        drawPilePanel.removeAll();
        drawPilePanel.add(new JLabel("Cards: " + drawPile.size()), BorderLayout.CENTER);

        discardPilePanel.removeAll();
        if (!discardPile.isEmpty()) {
            discardPilePanel.add(new JLabel(discardPile.get(discardPile.size() - 1).toString()), BorderLayout.CENTER);
        }

        playerPanel.removeAll();
        for (Player player : players) {
            playerPanel.add(new JLabel(player.getName() + ": " + player.getCardCount() + " cards"));
        }

        // Update play card combo box
        Player currentPlayer = players.get(currentPlayerIndex);
        playCardComboBox.removeAllItems();
        for (Card card : currentPlayer.getHand()) {
            playCardComboBox.addItem(card);
        }

        directionLabel.setText("Direction: " + (isClockwise ? "Clockwise" : "Counter-Clockwise"));

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
        Card card = (Card) playCardComboBox.getSelectedItem();
        if (card != null) {
            playCard(card);
        }
    }

    private void playCard(Card card) {
        Card topCard = discardPile.get(discardPile.size() - 1);
        if (canPlayCard(card, topCard)) {
            discardPile.add(card);
            players.get(currentPlayerIndex).playCard(card);
            updateGameUI();
            nextPlayer();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid card played!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean canPlayCard(Card card, Card topCard) {
        return card.getColor().equals(topCard.getColor()) ||
               card.getValue().equals(topCard.getValue()) ||
               card.getColor().equals("Wild");
    }

    private void drawCard() {
        Card drawnCard = drawPile.drawCard();
        if (drawnCard != null) {
            players.get(currentPlayerIndex).addCard(drawnCard);
            updateGameUI();
            nextPlayer();
        } else {
            JOptionPane.showMessageDialog(this, "No more cards to draw!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void checkGameEnd() {
        Player currentPlayer = players.get(currentPlayerIndex);
        if (currentPlayer.getCardCount() == 0) {
            int score = calculateScore();
            LOGGER.info(currentPlayer.getName() + " won the game with " + score + " points!");
            JOptionPane.showMessageDialog(this, currentPlayer.getName() + " wins with " + score + " points!");
            // Reset or end the game logic here
        }
    }

    private int calculateScore() {
        int score = 0;
        for (Player player : players) {
            if (player.getCardCount() > 0) {
                for (Card card : player.getHand()) {
                    score += cardScore(card);
                }
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
        currentPlayerIndex = isClockwise ? (currentPlayerIndex + 1) % playerCount
                                         : (currentPlayerIndex - 1 + playerCount) % playerCount;
        // If it's a bot's turn, let the bot play automatically
        Player currentPlayer = players.get(currentPlayerIndex);
        if (currentPlayer.isBot()) {
            playBotTurn(currentPlayer);
        }
    }

    private void playBotTurn(Player bot) {
        // Basic bot logic: play the first matching card, or draw a card if none match
        Card topCard = discardPile.get(discardPile.size() - 1);
        for (Card card : bot.getHand()) {
            if (canPlayCard(card, topCard)) {
                playCard(card);
                return;
            }
        }
        drawCard();
    }

    private void onUnoButtonClicked() {
        // Handle UNO button click logic
        Player currentPlayer = players.get(currentPlayerIndex);
        if (currentPlayer.getCardCount() == 1) {
            JOptionPane.showMessageDialog(this, currentPlayer.getName() + " called UNO!", "UNO", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "You can't call UNO now!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCallOutButtonClicked() {
        // Handle Call Out button click logic
        Player currentPlayer = players.get(currentPlayerIndex);
        if (currentPlayer.getCardCount() == 1) {
            // Penalize player for not calling UNO
            for (int i = 0; i < 2; i++) {
                currentPlayer.addCard(drawPile.drawCard());
            }
            JOptionPane.showMessageDialog(this, currentPlayer.getName() + " didn't call UNO! Penalized with 2 cards.", "Call Out", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Can't call out a player now!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        updateGameUI();
    }
    
    
}
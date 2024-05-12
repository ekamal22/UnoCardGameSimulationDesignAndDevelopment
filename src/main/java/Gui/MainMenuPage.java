package main.java.Gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import main.java.Game.Session;
import main.java.Object.Card;
import main.java.Object.Deck;
import main.java.Player.Player;
import main.java.User.UserData;
import main.java.Utils.NonEditableTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MainMenuPage extends JFrame {
    private JTable leaderboardTable;
    private NonEditableTableModel leaderboardModel; // Use the custom table model
    private JTextArea userStatsTextArea;
    private List<UserData> usersData;
    private List<String> savedSessions;

    public MainMenuPage() {
        setTitle("Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Leaderboard Section
        String[] columnNames = {"Username", "Total Score"};
        leaderboardModel = new NonEditableTableModel(columnNames, 0);
        leaderboardTable = new JTable(leaderboardModel);
        loadLeaderboardData();
        leaderboardTable.getSelectionModel().addListSelectionListener(e -> showUserStats());
        JScrollPane leaderboardScrollPane = new JScrollPane(leaderboardTable);

        // User Statistics Section
        userStatsTextArea = new JTextArea(8, 30);
        userStatsTextArea.setEditable(false);
        JScrollPane userStatsScrollPane = new JScrollPane(userStatsTextArea);

        // Game Options Section
        JPanel gameOptionsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        JButton newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> startNewGame());
        JButton continueGameButton = new JButton("Continue Game");
        continueGameButton.addActionListener(e -> continueExistingGame());
       

        JButton logOutButton = new JButton("Log Out");
        logOutButton.addActionListener(e -> logOut());

        gameOptionsPanel.add(newGameButton);
        gameOptionsPanel.add(continueGameButton);
        gameOptionsPanel.add(logOutButton);
        savedSessions = new ArrayList<>();
        
        // Adding components to the frame
        add(leaderboardScrollPane, BorderLayout.WEST);
        add(userStatsScrollPane, BorderLayout.CENTER);
        add(gameOptionsPanel, BorderLayout.EAST);

        setSize(800, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadLeaderboardData() {
        File file = new File("C:\\Users\\Effendi Jabid Kamal\\eclipse-workspace\\UnoCardGameSimulationDesignAndDevelopment\\src\\main\\java\\DataFiles\\users.txt");
        usersData = new ArrayList<>();
        savedSessions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details.length >= 4) {
                    String email = details[0];
                    int totalScore = 0; // Default to zero if parsing fails
                    int wins = 0;
                    int losses = 0;
                    int gamesPlayed = 0;

                    try {
                        totalScore = Integer.parseInt(details.length > 3 ? details[3] : "0");
                        wins = Integer.parseInt(details.length > 4 ? details[4] : "0");
                        losses = Integer.parseInt(details.length > 5 ? details[5] : "0");
                        gamesPlayed = Integer.parseInt(details.length > 6 ? details[6] : "0");
                    } catch (NumberFormatException e) {
                        // Log the exception and continue with default values
                        System.err.println("Error parsing user data for " + email + ": " + e.getMessage());
                    }

                    UserData user = new UserData(email, totalScore, wins, losses, gamesPlayed);
                    usersData.add(user);
                    leaderboardModel.addRow(new Object[]{email, totalScore});
                } else {
                    System.err.println("Skipping malformed line: " + line);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading leaderboard data", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showUserStats() {
        int selectedRow = leaderboardTable.getSelectedRow();
        if (selectedRow != -1) {
            String username = leaderboardModel.getValueAt(selectedRow, 0).toString();
            UserData user = findUserByUsername(username);
            if (user != null) {
                userStatsTextArea.setText("Statistics for " + username + "\n\n"
                    + "Total Score: " + user.getTotalScore() + "\n"
                    + "Wins: " + user.getWins() + "\n"
                    + "Losses: " + user.getLosses() + "\n"
                    + "Games Played: " + user.getGamesPlayed() + "\n"
                );
            }
        }
    }

    private UserData findUserByUsername(String username) {
        for (UserData user : usersData) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    private void startNewGame() {
        String sessionName = JOptionPane.showInputDialog(this, "Enter Session Name:", "New Game", JOptionPane.QUESTION_MESSAGE);
        if (sessionName != null && !sessionName.trim().isEmpty()) {
            String playerCountStr = JOptionPane.showInputDialog(this, "Enter Number of Players (2-10):", "New Game", JOptionPane.QUESTION_MESSAGE);
            try {
                int playerCount = Integer.parseInt(playerCountStr);
                if (playerCount >= 2 && playerCount <= 10) {
                    new Session(sessionName, playerCount);
                    savedSessions.add(sessionName);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Player count must be between 2 and 10", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid number entered", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void continueExistingGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));  // Set to the directory of the game or a specified path
        fileChooser.setDialogTitle("Select Saved Game File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("UNO Save Files", "ser"));  // Assuming the save files have a .ser extension

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(selectedFile))) {
                Deck drawPile = (Deck) in.readObject();
                List<Card> discardPile = (List<Card>) in.readObject();
                List<Player> players = (List<Player>) in.readObject();
                int currentPlayerIndex = in.readInt();
                boolean isClockwise = in.readBoolean();

                // Start a new GameSession with loaded data
                Session loadedGameSession = new Session("Loaded Game", players.size());  // 'Loaded Game' and player size are placeholders
                loadedGameSession.setGameState(drawPile, discardPile, players, currentPlayerIndex, isClockwise);
                loadedGameSession.setVisible(true);
                this.dispose();  // Close the MainMenuPage

            } catch (IOException | ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load the game: " + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void logOut() {
        new LoginPage();
        dispose();
    }

    public static void main(String[] args) {
        new MainMenuPage();
    }
}

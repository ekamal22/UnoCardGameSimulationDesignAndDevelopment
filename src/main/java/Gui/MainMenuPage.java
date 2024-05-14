/************** Pledge of Honor ******************************************
I hereby certify that I have completed this programming project on my own
without any help from anyone else. The effort in the project thus belongs
completely to me. I did not search for a solution, or I did not consult any
program written by others or did not copy any program from other sources. I
read and followed the guidelines provided in the project description.
READ AND SIGN BY WRITING YOUR NAME SURNAME AND STUDENT ID
SIGNATURE: <Effendi Jabid Kamal, 0082496>
*************************************************************************/

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
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setDialogTitle("Select Saved Game File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("UNO Save Files", "txt"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                String sessionName = reader.readLine();
                if (sessionName == null) throw new IOException("Corrupted file: Missing session name.");
                int playerCount = Integer.parseInt(reader.readLine());
                int currentPlayerIndex = Integer.parseInt(reader.readLine());
                boolean isClockwise = Boolean.parseBoolean(reader.readLine());
                String currentColor = reader.readLine();

                List<Player> players = new ArrayList<>();
                List<Card> discardPile = new ArrayList<>();
                Deck drawPile = new Deck();
                drawPile.getCards().clear(); // Clear the newly initialized deck

                // Load players
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] playerInfo = line.split(",");
                    if (playerInfo.length < 3) {
                        System.err.println("Skipping player due to insufficient data: " + line);
                        continue;
                    }
                    Player player = new Player(playerInfo[0], Boolean.parseBoolean(playerInfo[1]));
                    int cardCount = Integer.parseInt(playerInfo[2]);
                    for (int i = 0; i < cardCount; i++) {
                        line = reader.readLine();
                        if (line == null) throw new IOException("Corrupted file: Missing card data.");
                        String[] cardData = line.split(",");
                        if (cardData.length < 2) {
                            System.err.println("Skipping card due to insufficient data: " + line);
                            continue;
                        }
                        player.addCard(new Card(cardData[0], cardData[1]));
                    }
                    players.add(player);
                }

                // Load draw pile
                while ((line = reader.readLine()) != null && !line.equals("DiscardPileStart")) {
                    String[] cardData = line.split(",");
                    if (cardData.length < 2) {
                        System.err.println("Skipping card in draw pile due to insufficient data: " + line);
                        continue;
                    }
                    drawPile.getCards().add(new Card(cardData[0], cardData[1]));
                }

                // Load discard pile
                while ((line = reader.readLine()) != null) {
                    String[] cardData = line.split(",");
                    if (cardData.length < 2) {
                        System.err.println("Skipping card in discard pile due to insufficient data: " + line);
                        continue;
                    }
                    discardPile.add(new Card(cardData[0], cardData[1]));
                }

                Session loadedGameSession = new Session(sessionName, playerCount);
                loadedGameSession.setGameState(drawPile, discardPile, players, currentPlayerIndex, isClockwise);
                loadedGameSession.setVisible(true);
                this.dispose();  // Close the MainMenuPage
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to load the game: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



    /*// Method to load game state from a text file
    private Session loadGameFromTextFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String sessionName = reader.readLine();
        int playerCount = Integer.parseInt(reader.readLine());
        int currentPlayerIndex = Integer.parseInt(reader.readLine());
        boolean isClockwise = Boolean.parseBoolean(reader.readLine());
        String currentColor = reader.readLine();

        List<Player> players = new ArrayList<>();
        List<Card> discardPile = new ArrayList<>();
        Deck drawPile = new Deck(); // You will need to populate this based on the saved data
        drawPile.getCards().clear();

        // Additional parsing logic here to populate players, drawPile, and discardPile from the file

        Session loadedGameSession = new Session(sessionName, playerCount);
        loadedGameSession.setGameState(drawPile, discardPile, players, currentPlayerIndex, isClockwise);
        return loadedGameSession;
    }*/




    private void logOut() {
        new LoginPage();
        dispose();
    }

    public static void main(String[] args) {
        new MainMenuPage();
    }
}

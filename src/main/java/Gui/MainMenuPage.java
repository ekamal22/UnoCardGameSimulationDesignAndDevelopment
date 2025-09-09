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

import main.java.Game.Session;
import main.java.User.UserData;
import main.java.Utils.AppFiles;
import main.java.Utils.NonEditableTableModel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MainMenuPage extends JFrame {
    private JTable leaderboardTable;
    private NonEditableTableModel leaderboardModel;
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
        leaderboardTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader header = leaderboardTable.getTableHeader();
        header.setReorderingAllowed(false);
        loadLeaderboardData();
        leaderboardTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showUserStats();
        });
        JScrollPane leaderboardScrollPane = new JScrollPane(leaderboardTable);

        // User Statistics Section
        userStatsTextArea = new JTextArea(10, 30);
        userStatsTextArea.setEditable(false);
        userStatsTextArea.setMargin(new Insets(8, 8, 8, 8));
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

        // Layout
        add(leaderboardScrollPane, BorderLayout.WEST);
        add(userStatsScrollPane, BorderLayout.CENTER);
        add(gameOptionsPanel, BorderLayout.EAST);

        setSize(900, 450);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadLeaderboardData() {
        usersData = new ArrayList<>();
        leaderboardModel.setRowCount(0);

        try {
            Files.createDirectories(AppFiles.dataDir());
            if (Files.notExists(AppFiles.usersFile())) {
                // No users yet; create empty file so later writes succeed.
                Files.writeString(AppFiles.usersFile(), "");
            }

            try (BufferedReader reader = Files.newBufferedReader(AppFiles.usersFile())) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] details = line.split(",", -1);
                    // Expected: 8 or 9 fields (9 if profile picture path present)
                    if (details.length < 8) continue;

                    String email = details[0];
                    String password = details.length > 1 ? details[1] : "";
                    String sex = details.length > 2 ? details[2] : "";
                    String age = details.length > 3 ? details[3] : "";

                    int totalScore, wins, losses, gamesPlayed;
                    try {
                        totalScore = Integer.parseInt(details[4]);
                        wins = Integer.parseInt(details[5]);
                        losses = Integer.parseInt(details[6]);
                        gamesPlayed = Integer.parseInt(details[7]);
                    } catch (NumberFormatException nfe) {
                        continue; // skip malformed numeric row
                    }

                    String profilePicturePath = (details.length >= 9) ? details[8] : "";
                    UserData user = new UserData(email, password, sex, age, totalScore, wins, losses, gamesPlayed, profilePicturePath);
                    usersData.add(user);
                    leaderboardModel.addRow(new Object[]{email, totalScore});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading leaderboard data", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showUserStats() {
        int selectedRow = leaderboardTable.getSelectedRow();
        if (selectedRow == -1) return;

        String username = leaderboardModel.getValueAt(selectedRow, 0).toString();
        UserData user = findUserByUsername(username);
        if (user != null) {
            userStatsTextArea.setText(
                    "Statistics for " + username + "\n\n" +
                            "Total Score: " + user.getTotalScore() + "\n" +
                            "Wins: " + user.getWins() + "\n" +
                            "Losses: " + user.getLosses() + "\n" +
                            "Games Played: " + user.getGamesPlayed() + "\n" +
                            "Average Score per Game: " + user.getAverageScore() + "\n" +
                            "Win/Loss Ratio: " + user.getWinLossRatio() + "\n"
            );
        } else {
            userStatsTextArea.setText("No stats available.");
        }
    }

    private UserData findUserByUsername(String username) {
        for (UserData user : usersData) {
            if (user.getUsername().equals(username)) return user;
        }
        return null;
    }

    private void startNewGame() {
        if (usersData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No users loaded. Cannot start a new game.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sessionName = JOptionPane.showInputDialog(this, "Enter Session Name:", "New Game", JOptionPane.QUESTION_MESSAGE);
        if (sessionName == null || sessionName.trim().isEmpty()) return;

        String playerCountStr = JOptionPane.showInputDialog(this, "Enter Number of Players (2-10):", "New Game", JOptionPane.QUESTION_MESSAGE);
        if (playerCountStr == null) return;

        try {
            int playerCount = Integer.parseInt(playerCountStr);
            if (playerCount < 2 || playerCount > 10) {
                JOptionPane.showMessageDialog(this, "Player count must be between 2 and 10", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Use the first loaded user as the human player email (as in your original)
            new Session(sessionName, playerCount, usersData.get(0).getUsername());
            savedSessions.add(sessionName);
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number entered", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void continueExistingGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Saved Game File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("UNO Save Files (*.txt)", "txt"));
        fileChooser.setCurrentDirectory(new File(".")); // change if you have a dedicated saves folder

        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return; // user cancelled
        }

        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile == null || !selectedFile.isFile()) {
            JOptionPane.showMessageDialog(this, "Please select a valid save file.", "Invalid Selection", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedReader reader = java.nio.file.Files.newBufferedReader(selectedFile.toPath())) {
            // Use the "load-only" Session constructor so initializeGame() is NOT called with 0 players.
            String humanEmail = (usersData != null && !usersData.isEmpty()) ? usersData.get(0).getUsername() : "";
            Session loadedGameSession = new Session("Loaded Game", 0, humanEmail, /*deferInit=*/true);

            loadedGameSession.loadGame(reader);   // populates state and calls updateGameUI() inside
            loadedGameSession.setVisible(true);
            this.dispose();                       // close the main menu
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load the game: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void logOut() {
        new LoginPage();
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenuPage::new);
    }
}

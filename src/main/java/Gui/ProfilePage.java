package main.java.Gui;

import main.java.User.UserData;
import main.java.Utils.AppFiles;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * ProfilePage
 * - Loads and saves user data from a portable location (~/.uno-game/users.csv).
 * - Keeps CSV format:
 *   email,password,sex,age,totalScore,wins,losses,gamesPlayed,profilePicturePath
 */
public class ProfilePage extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel profilePictureLabel;
    private UserData userData;

    public ProfilePage(UserData userData) {
        this.userData = userData;

        setTitle("Profile Page");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Email
        JLabel emailLabel = new JLabel("Email:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(emailLabel, gbc);

        emailField = new JTextField(userData.getUsername(), 20);
        emailField.setEditable(false); // email is the key; keep immutable
        gbc.gridx = 1;
        add(emailField, gbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(userData.getPassword(), 20);
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Age (read-only per original)
        JLabel ageLabel = new JLabel("Age:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(ageLabel, gbc);

        JTextField ageField = new JTextField(userData.getAge(), 20);
        ageField.setEditable(false);
        gbc.gridx = 1;
        add(ageField, gbc);

        // Profile picture preview
        JLabel ppText = new JLabel("Profile Picture:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(ppText, gbc);

        profilePictureLabel = new JLabel();
        profilePictureLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (userData.getProfilePicturePath() != null && !userData.getProfilePicturePath().isEmpty()) {
            profilePictureLabel.setIcon(new ImageIcon(userData.getProfilePicturePath()));
        }
        gbc.gridx = 1;
        add(profilePictureLabel, gbc);

        // Buttons
        JButton changeProfilePictureButton = new JButton("Change Profile Picture");
        changeProfilePictureButton.addActionListener(e -> chooseProfilePicture());
        gbc.gridx = 1;
        gbc.gridy = 4;
        add(changeProfilePictureButton, gbc);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveUserData(this.userData));
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        add(saveButton, gbc);

        setSize(450, 320);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void chooseProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "gif", "jpeg"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
            String absolutePath = fileChooser.getSelectedFile().getAbsolutePath();
            userData.setProfilePicturePath(absolutePath);
            profilePictureLabel.setIcon(new ImageIcon(absolutePath));
        }
    }

    private void saveUserData(UserData updatedUser) {
        try {
            Files.createDirectories(AppFiles.dataDir());
            Path usersPath = AppFiles.usersFile();
            if (Files.notExists(usersPath)) {
                // create empty file if not present
                Files.writeString(usersPath, "");
            }

            // Read all users
            List<UserData> usersData = new ArrayList<>();
            try (BufferedReader reader = Files.newBufferedReader(usersPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] d = line.split(",", -1);
                    if (d.length < 8) continue; // skip malformed
                    String email = d[0];
                    String password = d.length > 1 ? d[1] : "";
                    String sex = d.length > 2 ? d[2] : "";
                    String age = d.length > 3 ? d[3] : "";
                    int totalScore = parseOrZero(d.length > 4 ? d[4] : null);
                    int wins = parseOrZero(d.length > 5 ? d[5] : null);
                    int losses = parseOrZero(d.length > 6 ? d[6] : null);
                    int gamesPlayed = parseOrZero(d.length > 7 ? d[7] : null);
                    String profilePicturePath = d.length > 8 ? d[8] : "";

                    usersData.add(new UserData(email, password, sex, age, totalScore, wins, losses, gamesPlayed, profilePicturePath));
                }
            }

            // Update the matching user by email
            boolean found = false;
            for (UserData u : usersData) {
                if (u.getUsername().equalsIgnoreCase(updatedUser.getUsername())) {
                    u.setPassword(new String(passwordField.getPassword())); // update password from field
                    u.setProfilePicturePath(updatedUser.getProfilePicturePath()); // updated when choosing picture
                    found = true;
                    break;
                }
            }

            if (!found) {
                JOptionPane.showMessageDialog(this, "User not found in database.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Write back
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(usersPath))) {
                for (UserData u : usersData) {
                    writer.println(String.join(",",
                            u.getUsername(),
                            u.getPassword(),
                            u.getSex(),
                            u.getAge(),
                            String.valueOf(u.getTotalScore()),
                            String.valueOf(u.getWins()),
                            String.valueOf(u.getLosses()),
                            String.valueOf(u.getGamesPlayed()),
                            u.getProfilePicturePath() == null ? "" : u.getProfilePicturePath()
                    ));
                }
            }

            JOptionPane.showMessageDialog(this, "Profile saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving user data", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static int parseOrZero(String s) {
        try {
            return (s == null || s.isBlank()) ? 0 : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static void main(String[] args) {
        // Dummy data for testing
        SwingUtilities.invokeLater(() -> {
            UserData userData = new UserData("test@example.com", "password", "Male", "21", 100, 10, 5, 15, "");
            new ProfilePage(userData);
        });
    }
}

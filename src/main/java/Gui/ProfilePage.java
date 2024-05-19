package main.java.Gui;

import main.java.User.UserData;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel emailLabel = new JLabel("Email:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(emailLabel, gbc);
        emailField = new JTextField(userData.getUsername(), 15);
        gbc.gridx = 1;
        add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passwordLabel, gbc);
        passwordField = new JPasswordField(userData.getPassword(), 15);
        gbc.gridx = 1;
        add(passwordField, gbc);

        JLabel ageLabel = new JLabel("Age:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(ageLabel, gbc);
        JTextField ageField = new JTextField(userData.getAge(), 15);
        ageField.setEditable(false);
        gbc.gridx = 1;
        add(ageField, gbc);

        JLabel profilePictureLabel = new JLabel("Profile Picture:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(profilePictureLabel, gbc);
        this.profilePictureLabel = new JLabel();
        if (userData.getProfilePicturePath() != null && !userData.getProfilePicturePath().isEmpty()) {
            this.profilePictureLabel.setIcon(new ImageIcon(userData.getProfilePicturePath()));
        }
        gbc.gridx = 1;
        add(this.profilePictureLabel, gbc);

        JButton changeProfilePictureButton = new JButton("Change Profile Picture");
        gbc.gridx = 1;
        gbc.gridy = 4;
        add(changeProfilePictureButton, gbc);
        changeProfilePictureButton.addActionListener(e -> chooseProfilePicture());

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveUserData(userData));
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        add(saveButton, gbc);

        setSize(400, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void chooseProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "gif", "jpeg"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            userData.setProfilePicturePath(selectedFile.getAbsolutePath());
            profilePictureLabel.setIcon(new ImageIcon(userData.getProfilePicturePath()));
        }
    }

    private void saveUserData(UserData userData) {
        File file = new File("C:\\Users\\Effendi Jabid Kamal\\eclipse-workspace\\UnoCardGameSimulationDesignAndDevelopment\\src\\main\\java\\DataFiles\\users.txt");
        List<UserData> usersData = new ArrayList<>();

        // Read existing user data
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details.length == 9) {
                    UserData user = new UserData(details[0], details[1], details[2], details[3], Integer.parseInt(details[4]), Integer.parseInt(details[5]), Integer.parseInt(details[6]), Integer.parseInt(details[7]), details[8]);
                    usersData.add(user);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Update user data
        for (UserData user : usersData) {
            if (user.getUsername().equals(userData.getUsername())) {
                user.setPassword(userData.getPassword());
                user.setProfilePicturePath(userData.getProfilePicturePath());
                break;
            }
        }

        // Write updated user data back to file
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (UserData user : usersData) {
                writer.println(user.getUsername() + "," + user.getPassword() + "," + user.getSex() + "," + user.getAge() + "," + user.getTotalScore() + "," + user.getWins() + "," + user.getLosses() + "," + user.getGamesPlayed() + "," + user.getProfilePicturePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Dummy data for testing
        UserData userData = new UserData("test@example.com", "password", "Male", "21", 100, 10, 5, 15, "");
        new ProfilePage(userData);
    }
}

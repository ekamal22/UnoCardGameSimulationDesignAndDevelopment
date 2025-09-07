package main.java.Utils;

import main.java.Gui.MainMenuPage;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * LoginHandler
 * - Reads users from a portable location (see AppFiles.usersFile()).
 * - Compares plaintext password (kept for compatibility with your current CSV).
 *   Consider upgrading to salted+hashed passwords later.
 *
 * CSV format (per line):
 * email,password,sex,age,totalScore,wins,losses,gamesPlayed,profilePicturePath
 */
public class LoginHandler {

    public static void handleLogin(String email, String password, JFrame loginFrame) {
        try {
            // Ensure data dir exists
            Files.createDirectories(AppFiles.dataDir());

            Path usersPath = AppFiles.usersFile();
            if (Files.notExists(usersPath)) {
                JOptionPane.showMessageDialog(
                        null,
                        "No users registered yet.",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            boolean loggedIn = false;

            try (BufferedReader reader = Files.newBufferedReader(usersPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Keep empty fields if any
                    String[] details = line.split(",", -1);
                    if (details.length >= 2) {
                        String storedEmail = details[0].trim();
                        String storedPassword = details[1]; // plaintext for compatibility
                        if (storedEmail.equalsIgnoreCase(email.trim()) && storedPassword.equals(password)) {
                            loggedIn = true;
                            break;
                        }
                    }
                }
            }

            if (loggedIn) {
                JOptionPane.showMessageDialog(
                        null,
                        "Login successful!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                new MainMenuPage();
                if (loginFrame != null) {
                    loginFrame.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Invalid email or password",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error reading user data",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}

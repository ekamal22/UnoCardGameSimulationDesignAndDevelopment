package main.java.Utils;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.StandardOpenOption.*;

/**
 * RegistrationHandler
 * Portable user registration that appends to ~/.uno-game/users.csv
 * (via AppFiles.usersFile()) in CSV format:
 * email,password,sex,age,totalScore,wins,losses,gamesPlayed,profilePicturePath
 */
public class RegistrationHandler {

    public static void handleRegistration(String email,
                                          String password,
                                          String sex,
                                          String age,
                                          String profilePicturePath) {
        // --- Validate inputs (kept close to your original logic) ---
        if (email == null || !email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(null, "Invalid email format", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password == null ||
                !password.matches("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$")) {
            JOptionPane.showMessageDialog(null,
                    "Password must be alphanumeric with at least 1 special character and length at least 8 characters",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final int ageInt;
        try {
            ageInt = Integer.parseInt(age);
            if (ageInt < 7) {
                JOptionPane.showMessageDialog(null, "User must be at least 7 years old", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid age format", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (profilePicturePath == null || profilePicturePath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Profile picture is required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- Prepare sanitized fields (avoid breaking the CSV with commas) ---
        String safeEmail = email.trim();
        String safePassword = password; // plaintext for compatibility with current CSV
        String safeSex = (sex == null ? "" : sex.replace(",", " ")).trim();
        String safeAge = String.valueOf(ageInt);
        String safeProfilePath = profilePicturePath.replace(",", " ").trim();

        // Initial stats
        String totalScore = "0", wins = "0", losses = "0", gamesPlayed = "0";

        try {
            // Ensure data directory and users file exist
            Files.createDirectories(AppFiles.dataDir());
            Path usersPath = AppFiles.usersFile();
            if (Files.notExists(usersPath)) {
                Files.writeString(usersPath, "", CREATE);
            }

            // Prevent duplicate registration (email is the key)
            try (BufferedReader reader = Files.newBufferedReader(usersPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] details = line.split(",", -1);
                    if (details.length >= 1 && details[0].equalsIgnoreCase(safeEmail)) {
                        JOptionPane.showMessageDialog(null,
                                "An account with this email already exists.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            // Append new user row
            String row = String.join(",",
                    safeEmail,
                    safePassword,
                    safeSex,
                    safeAge,
                    totalScore,
                    wins,
                    losses,
                    gamesPlayed,
                    safeProfilePath) + System.lineSeparator();

            Files.writeString(usersPath, row, CREATE, APPEND);

            JOptionPane.showMessageDialog(null, "Registration successful", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving user details", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

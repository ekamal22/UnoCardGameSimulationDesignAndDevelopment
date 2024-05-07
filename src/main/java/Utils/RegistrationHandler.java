package main.java.Utils;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RegistrationHandler {
    public static void handleRegistration(String email, String password, String sex, String age) {
        // Validate email
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(null, "Invalid email format", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate password
        if (!password.matches("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$")) {
            JOptionPane.showMessageDialog(null, "Password must be alphanumeric with at least 1 special character and length at least 8 characters", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate age
        try {
            int ageInt = Integer.parseInt(age);
            if (ageInt < 7) {
                JOptionPane.showMessageDialog(null, "User must be at least 7 years old", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid age format", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Save user details
        String userDetails = email + "," + password + "," + sex + "," + age + "\n";
        try {
            File file = new File("C:\\Users\\Effendi Jabid Kamal\\eclipse-workspace\\UnoCardGameSimulationDesignAndDevelopment\\src\\main\\java\\DataFiles\\users.txt");
            FileWriter writer = new FileWriter(file, true);
            writer.write(userDetails);
            writer.close();
            JOptionPane.showMessageDialog(null, "Registration successful", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving user details", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
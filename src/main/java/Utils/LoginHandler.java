package main.java.Utils;

import main.java.Gui.MainMenuPage;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LoginHandler {
    public static void handleLogin(String email, String password, JFrame loginFrame) {
        File file = new File("C:\\Users\\Effendi Jabid Kamal\\eclipse-workspace\\UnoCardGameSimulationDesignAndDevelopment\\src\\main\\java\\DataFiles\\users.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean loggedIn = false;

            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details[0].equals(email) && details[1].equals(password)) {
                    loggedIn = true;
                    break;
                }
            }

            if (loggedIn) {
                JOptionPane.showMessageDialog(null, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Open the main menu page and close the login page
                new MainMenuPage();
                loginFrame.dispose(); // Close the login frame
            } else {
                JOptionPane.showMessageDialog(null, "Invalid email or password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading user data", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

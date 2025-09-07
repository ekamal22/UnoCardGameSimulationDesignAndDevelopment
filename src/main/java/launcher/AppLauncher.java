package main.java.launcher;

import main.java.Utils.AppFiles;
import main.java.Gui.LoginPage;  // or: import main.java.Gui.MainMenuPage;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * AppLauncher
 * - Ensures portable data directories/files exist (see AppFiles).
 * - Boots the UI on the Swing Event Dispatch Thread.
 *
 * Place this file at: src/main/java/launcher/AppLauncher.java
 * Run configuration main class: launcher.AppLauncher
 */
public class AppLauncher {

    public static void main(String[] args) {
        // 1) Ensure ~/.uno-game (or your AppFiles dir) exists, along with users.csv and saves/
        try {
            AppFiles.ensureInitialized();
        } catch (Exception e) {
            // Not fatal for UI launch, but you can alert/log as needed
            e.printStackTrace();
        }

        // 2) Start Swing UI on the EDT
        SwingUtilities.invokeLater(() -> {
            try {
                // Use the system look & feel for a native appearance
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignore) {
                // Fallback to default LAF
            }

            // Launch your entry page
            new LoginPage();       // or: new MainMenuPage();
        });
    }
}

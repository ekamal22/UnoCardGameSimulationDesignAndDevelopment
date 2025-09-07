package main.java.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Centralizes portable file locations for the app.
 * - Data dir: ~/.uno-game
 * - Users CSV: ~/.uno-game/users.csv
 * - Saves dir: ~/.uno-game/saves
 */
public final class AppFiles {

    private AppFiles() { /* no instances */ }

    /** ~/.uno-game */
    public static Path dataDir() {
        return Paths.get(System.getProperty("user.home"), ".uno-game");
    }

    /** ~/.uno-game/users.csv */
    public static Path usersFile() {
        return dataDir().resolve("users.csv");
    }

    /** ~/.uno-game/saves */
    public static Path savesDir() {
        return dataDir().resolve("saves");
    }

    /**
     * Optional helper: call once on startup to ensure folders/files exist.
     */
    public static void ensureInitialized() throws IOException {
        Files.createDirectories(dataDir());
        Files.createDirectories(savesDir());
        if (Files.notExists(usersFile())) {
            Files.createFile(usersFile());
        }
    }
}

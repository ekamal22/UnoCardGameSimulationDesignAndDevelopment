package main.java.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CardImageLoader (scaled, cached, no-move)
 *
 * Features:
 *  - Returns reasonably-sized ImageIcons (default height: 120 px, aspect preserved).
 *  - Overload lets you request a specific height per call.
 *  - Caches scaled icons for performance.
 *  - Finds assets in this order (no config needed):
 *      1) Classpath (if someone puts them under src/main/resources)
 *      2) JVM override: -Duno.images=/absolute/path/to/DataFiles
 *      3) Auto-detect repo layouts near working dir:
 *         ./src/main/java/DataFiles, ./src/main/resources/DataFiles, ./DataFiles
 *         (and the same up to two parent directories)
 *  - Supports GIF/PNG/JPG and both number name styles: "Blue 5" and "Blue (5)".
 */
public class CardImageLoader {

    // ---------- sizing ----------
    private static volatile int DEFAULT_HEIGHT = 120; // px; change with setDefaultHeight(...)
    public static void setDefaultHeight(int heightPx) { DEFAULT_HEIGHT = Math.max(24, heightPx); }

    // Optional override: point to a DataFiles folder on disk (dev convenience)
    private static final String OVERRIDE_DIR = System.getProperty("uno.images");

    // Cache: key = cardName + "|" + targetHeight
    private static final Map<String, ImageIcon> CACHE = new ConcurrentHashMap<>();

    // Auto-detected DataFiles base (…/DataFiles). Null until first lookup.
    private static volatile File autoDetectedBase;

    /** Usual call: scaled to DEFAULT_HEIGHT */
    public static ImageIcon getCardImage(String cardName) {
        return getCardImage(cardName, DEFAULT_HEIGHT);
    }

    /** Overload: choose target pixel height for this icon. Aspect ratio is preserved. */
    public static ImageIcon getCardImage(String cardName, int targetHeightPx) {
        if (cardName == null || cardName.isBlank()) return null;
        final int H = Math.max(16, targetHeightPx);
        final String cacheKey = cardName.trim() + "|" + H;

        ImageIcon cached = CACHE.get(cacheKey);
        if (cached != null) return cached;

        // Locate original (unscaled) image
        List<String> candidates = buildCandidates(cardName.trim());

        // 1) Classpath
        ImageIcon original = null;
        for (String path : candidates) {
            URL url = CardImageLoader.class.getResource(path);
            if (url == null) url = CardImageLoader.class.getResource(path.replace(" ", "%20"));
            if (url != null) {
                original = new ImageIcon(url);
                break;
            }
        }

        // 2) Explicit filesystem override
        if (original == null && OVERRIDE_DIR != null && !OVERRIDE_DIR.isBlank()) {
            for (String path : candidates) {
                File f = new File(toFilesystemPath(new File(OVERRIDE_DIR), path));
                if (f.isFile()) {
                    original = new ImageIcon(f.getAbsolutePath());
                    break;
                }
            }
        }

        // 3) Auto-detect typical repo locations
        if (original == null) {
            File base = detectBaseDir();
            if (base != null) {
                for (String path : candidates) {
                    File f = new File(toFilesystemPath(base, path));
                    if (f.isFile()) {
                        original = new ImageIcon(f.getAbsolutePath());
                        break;
                    }
                }
            }
        }

        if (original == null || original.getIconWidth() <= 0 || original.getIconHeight() <= 0) {
            // Diagnostics to help pinpoint a mismatch
            System.err.println("[CardImageLoader] MISSING IMAGE for \"" + cardName + "\"");
            for (String p : candidates) System.err.println("  tried resource: " + p);
            if (OVERRIDE_DIR != null && !OVERRIDE_DIR.isBlank()) {
                for (String p : candidates) System.err.println("  tried file    : " + toFilesystemPath(new File(OVERRIDE_DIR), p));
            }
            File base = autoDetectedBase != null ? autoDetectedBase : detectBaseDir();
            if (base != null) {
                for (String p : candidates) System.err.println("  tried file    : " + toFilesystemPath(base, p));
            }
            return null;
        }

        // Scale to target height (preserve aspect). For GIFs this keeps things simple & fast.
        int ow = original.getIconWidth();
        int oh = original.getIconHeight();
        if (oh <= 0) return original;

        int nh = H;
        int nw = Math.max(1, (int) Math.round(ow * (nh / (double) oh)));
        Image scaled = original.getImage().getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
        ImageIcon result = new ImageIcon(scaled);
        CACHE.put(cacheKey, result);
        return result;
    }

    // ---------- candidate path generation ----------

    private static List<String> buildCandidates(String cardName) {
        List<String> out = new ArrayList<>();

        // Wilds
        if (cardName.equalsIgnoreCase("Wild")) {
            addAllExt(out, "/DataFiles/UNO Cards/Wild/Wild");
            return out;
        }
        if (cardName.equalsIgnoreCase("Wild Draw Four")) {
            addAllExt(out, "/DataFiles/UNO Cards/Wild/Wild draw 4");
            addAllExt(out, "/DataFiles/UNO Cards/Wild/Wild Draw 4"); // tolerate capitalization
            return out;
        }

        // Expect "<Color> <Value>"
        int sp = cardName.indexOf(' ');
        if (sp <= 0 || sp >= cardName.length() - 1) return out;

        String color = cardName.substring(0, sp).trim();    // Blue/Red/Green/Yellow
        String value = cardName.substring(sp + 1).trim();   // 0..9, Reverse, Skip, Draw Two

        // Numbers: try "Color N" and "Color (N)"
        if (isDigit(value)) {
            addAllExt(out, "/DataFiles/UNO Cards/Number/" + color + " " + value);
            addAllExt(out, "/DataFiles/UNO Cards/Number/" + color + " (" + value + ")");
            return out;
        }

        // Actions (case tolerant)
        String lower = value.toLowerCase();
        if (lower.equals("reverse")) {
            addAllExt(out, "/DataFiles/UNO Cards/Action/" + color + " reverse");
            addAllExt(out, "/DataFiles/UNO Cards/Action/" + capitalize(color) + " reverse");
            return out;
        }
        if (lower.equals("skip")) {
            addAllExt(out, "/DataFiles/UNO Cards/Action/" + color + " skip");
            addAllExt(out, "/DataFiles/UNO Cards/Action/" + capitalize(color) + " skip");
            return out;
        }
        if (lower.equals("draw two")) {
            addAllExt(out, "/DataFiles/UNO Cards/Action/" + color + " draw 2");
            addAllExt(out, "/DataFiles/UNO Cards/Action/" + capitalize(color) + " draw 2");
            addAllExt(out, "/DataFiles/UNO Cards/Action/" + color + " Draw 2");
            return out;
        }

        return out;
    }

    private static void addAllExt(List<String> list, String baseNoExt) {
        // Your assets are GIFs; try .gif first, then .png, then .jpg
        list.add(baseNoExt + ".gif");
        list.add(baseNoExt + ".png");
        list.add(baseNoExt + ".jpg");
    }

    private static boolean isDigit(String s) {
        return s.length() == 1 && Character.isDigit(s.charAt(0));
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + (s.length() > 1 ? s.substring(1) : "");
    }

    // ---------- filesystem helpers ----------

    private static String toFilesystemPath(File dataFilesBase, String resourcePath) {
        String relative = resourcePath.startsWith("/DataFiles/")
                ? resourcePath.substring("/DataFiles/".length())
                : resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        return new File(dataFilesBase, relative).getPath();
    }

    private static File detectBaseDir() {
        if (autoDetectedBase != null) return autoDetectedBase;

        File wd = new File(System.getProperty("user.dir", "."));
        File found = findNearbyDataFiles(wd);
        if (found == null) {
            File parent = wd.getParentFile();
            if (parent != null) found = findNearbyDataFiles(parent);
            if (found == null && parent != null && parent.getParentFile() != null) {
                found = findNearbyDataFiles(parent.getParentFile());
            }
        }
        autoDetectedBase = found; // may be null
        if (autoDetectedBase != null) {
            System.out.println("[CardImageLoader] auto-detected DataFiles at: " + autoDetectedBase.getAbsolutePath());
        }
        return autoDetectedBase;
    }

    private static File findNearbyDataFiles(File root) {
        File[] candidates = new File[] {
            new File(root, "src/main/java/DataFiles"),
            new File(root, "src/main/resources/DataFiles"),
            new File(root, "DataFiles")
        };
        for (File base : candidates) {
            if (new File(base, "UNO Cards").isDirectory()) {
                return base;
            }
        }
        return null;
    }

    // Debug helper to see all tried paths for a card
    public static java.util.List<String> debugCandidates(String cardName) {
        return Collections.unmodifiableList(buildCandidates(cardName));
    }
}

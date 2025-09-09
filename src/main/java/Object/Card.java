package main.java.Object;

import java.util.Objects;

/**
 * Card
 * - Represents a single UNO card.
 * - Equality is value-based so UI selections match the card in hand.
 *   • Number/Action cards: equals if color AND value match.
 *   • Wild cards ("Wild", "Wild Draw Four"): equals if value matches (color ignored).
 * - toString() returns formats used by the UI/loader:
 *   • "Wild" / "Wild Draw Four"
 *   • "<Color> <Value>" for others, e.g. "Blue 7"
 */
public class Card {
    private final String color;       // "Red", "Yellow", "Green", "Blue", or null/"" for wilds
    private final String value;       // "0"-"9", "Reverse", "Skip", "Draw Two", "Wild", "Wild Draw Four"
    private String currentColor;      // active color after a Wild is played (may differ from 'color')

    public Card(String color, String value) {
        this.color = color;
        this.value = value;
        this.currentColor = color; // initialize to the printed color; may be changed after Wild is played
    }

    // Getters
    public String getColor() { return color; }
    public String getValue() { return value; }
    public String getCurrentColor() { return currentColor; }

    // Setter only for runtime-selected color after Wild
    public void setCurrentColor(String currentColor) { this.currentColor = currentColor; }

    public boolean isWild() {
        return value != null && value.startsWith("Wild");
    }

    @Override
    public String toString() {
        if ("Wild".equals(value) || "Wild Draw Four".equals(value)) {
            return value;
        }
        return (color == null ? "" : color) + " " + (value == null ? "" : value);
    }

    // ---- value-based equality so UI-selected Card matches a hand Card instance ----
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card other = (Card) o;

        // For wilds, ignore color (wilds are equivalent by value only)
        if (this.isWild() && other.isWild()) {
            return Objects.equals(this.value, other.value);
        }
        // For number/action cards, color + value must match
        return Objects.equals(this.color, other.color) &&
               Objects.equals(this.value, other.value);
    }

    @Override
    public int hashCode() {
        // Keep consistent with equals: wilds hash on value only; others on color+value
        if (isWild()) {
            return Objects.hash(value);
        }
        return Objects.hash(color, value);
    }
}

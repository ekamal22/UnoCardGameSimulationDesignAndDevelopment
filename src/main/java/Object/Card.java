package main.java.Object;

import java.io.Serializable;

public class Card implements Serializable {
    private String color;
    private String value;
    private String currentColor;  // Added to track the current active color

    public Card(String color, String value) {
        this.color = color;
        this.value = value;
        this.currentColor = color;  // Initialize with the default color
    }

    // Getters and setters
    public String getColor() {
        return color;
    }

    public String getValue() {
        return value;
    }

    public String getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(String currentColor) {
        this.currentColor = currentColor;
    }

    @Override
    public String toString() {
        return color + " " + value;
    }
}

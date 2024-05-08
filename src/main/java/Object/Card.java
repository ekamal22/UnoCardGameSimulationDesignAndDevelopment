package main.java.Object;

import java.io.Serializable;

public class Card implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L; // Serializable ID
    private String color;
    private String value;

    public Card(String color, String value) {
        this.color = color;
        this.value = value;
    }

    // Getters
    public String getColor() {
        return color;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return color + " " + value;
    }
}

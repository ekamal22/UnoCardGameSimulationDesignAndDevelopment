package main.java.Player;

import main.java.Object.Card;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L; // Serializable ID

    private String name;
    private List<Card> hand; // Ensure Card class is also Serializable
    private boolean isBot; // Primitive data type is naturally serializable

    public Player(String name, boolean isBot) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.isBot = isBot;
    }

    // Ensure all getters, setters, and methods do not affect serialization

    public String getName() {
        return name;
    }

    public void addCard(Card card) {
        hand.add(card);
    }

    public boolean removeCard(Card card) {
        return hand.remove(card);
    }

    public Card playCard(Card card) {
        if (removeCard(card)) {
            return card;
        }
        return null;
    }

    public int getCardCount() {
        return hand.size();
    }

    public boolean isBot() {
        return isBot;
    }

    public List<Card> getHand() {
        return hand;
    }
}

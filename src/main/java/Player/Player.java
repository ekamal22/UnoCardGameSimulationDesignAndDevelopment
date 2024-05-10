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
    private boolean calledUno; // Field to track if UNO has been called


    public Player(String name, boolean isBot) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.isBot = isBot;
        this.calledUno = false;
    }

    // Ensure all getters, setters, and methods do not affect serialization

    public String getName() {
        return name;
    }

    public void addCard(Card card) {
        hand.add(card);
        if (hand.size() != 1) {
            calledUno = false;  // Reset UNO call when card count changes
        }
    }

    public boolean removeCard(Card card) {
        boolean wasRemoved = hand.remove(card);
        if (hand.size() != 1) {
            calledUno = false;  // Reset UNO call when card count changes
        }
        return wasRemoved;
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
    
    public Card findPlayableCard(Card topCard) {
        for (Card card : hand) {
            if (card.getColor().equals(topCard.getColor()) || 
                card.getValue().equals(topCard.getValue()) ||
                card.getColor().equals("Wild")) {
                return card;
            }
        }
        return null; // Return null if no playable card is found
    }
    
    
    
 // Method to determine if the player has called UNO
    public boolean hasCalledUno() {
        return calledUno;
    }

    // Method to set the UNO call status
    public void callUno() {
        if (hand.size() == 1) {
            calledUno = true;
        }
    }

}

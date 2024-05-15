package main.java.Player;

import main.java.Object.Card;

import java.util.ArrayList;
import java.util.List;

public class Player{
    

    private String name;
    private List<Card> hand; 
    private boolean isBot; 
    private boolean hasCalledUno = false; // Field to track if UNO has been called


    public Player(String name, boolean isBot) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.isBot = isBot;
        this.hasCalledUno = false;
    }

    

    public String getName() {
        return name;
    }

    public void addCard(Card card) {
        hand.add(card);
        if (hand.size() != 1) {
            this.hasCalledUno = false;  // Reset UNO call when card count changes
        }
    }

    public boolean removeCard(Card card) {
        boolean wasRemoved = hand.remove(card);
        if (hand.size() != 1) {
            this.hasCalledUno = false;  // Reset UNO call when card count changes
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
        return this.hasCalledUno;
    }

    // Method to set the UNO call status
    public void callUno() {
        this.hasCalledUno = true;
    }

    
    public void clearUnoCall() {
        this.hasCalledUno = false;
    }

}

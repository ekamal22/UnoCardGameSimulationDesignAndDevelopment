package main.java.Player;

import main.java.Object.Card;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private List<Card> hand;
    private boolean isBot;

    public Player(String name, boolean isBot) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.isBot = isBot;
    }

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

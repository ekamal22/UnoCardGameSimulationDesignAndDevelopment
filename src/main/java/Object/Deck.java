package main.java.Object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
        initializeDeck();
        shuffleDeck();
    }

    private void initializeDeck() {
        // Number cards
        for (String color : new String[]{"Red", "Yellow", "Blue", "Green"}) {
            cards.add(new Card(color, "0"));
            for (int i = 1; i <= 9; i++) {
                cards.add(new Card(color, String.valueOf(i)));
                cards.add(new Card(color, String.valueOf(i)));
            }
        }

        // Action cards: Draw two, Reverse, Skip
        for (String color : new String[]{"Red", "Yellow", "Blue", "Green"}) {
            for (String action : new String[]{"Draw Two", "Reverse", "Skip"}) {
                cards.add(new Card(color, action));
                cards.add(new Card(color, action));
            }
        }

        // Wild cards
        for (String type : new String[]{"Wild", "Wild Draw Four"}) {
            for (int i = 0; i < 4; i++) {
                cards.add(new Card("Wild", type));
            }
        }
    }

    public void shuffleDeck() {
        Collections.shuffle(cards);
    }

    public Card drawCard() {
        return cards.isEmpty() ? null : cards.remove(0);
    }

    public int size() {
        return cards.size();
    }
}

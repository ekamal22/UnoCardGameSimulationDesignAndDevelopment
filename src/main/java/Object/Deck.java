package main.java.Object;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    private List<Card> cards;
    
    public Deck() {
        this.cards = new ArrayList<>();
        initializeDeck();  // Ensure this method is called to populate the deck
    }

    private void initializeDeck() {
        // Number cards
        for (String color : new String[]{"Red", "Yellow", "Blue", "Green"}) {
            cards.add(new Card(color, "0"));  // Zero cards
            for (int i = 1; i <= 9; i++) {
                cards.add(new Card(color, String.valueOf(i)));
                cards.add(new Card(color, String.valueOf(i)));  // Each card from 1-9 appears twice
            }
        }

        // Action cards
        for (String color : new String[]{"Red", "Yellow", "Blue", "Green"}) {
            for (String action : new String[]{"Draw Two", "Reverse", "Skip"}) {
                cards.add(new Card(color, action));
                cards.add(new Card(color, action));  // Each action card appears twice
            }
        }

        // Wild cards
        for (String type : new String[]{"Wild", "Wild Draw Four"}) {
            for (int i = 0; i < 4; i++) {
                cards.add(new Card("Wild", type));  // Four of each Wild card type
            }
        }

        shuffleDeck();  // Optionally shuffle the deck after initialization
    }
    

    

    public List<Card> getCards() {
        return cards;
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

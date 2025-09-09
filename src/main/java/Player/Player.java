package main.java.Player;

import main.java.Object.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Player
 * - Holds hand, identity (human/bot), and UNO state.
 * - removeCard(...) removes by value (uses Card.equals), not by object identity.
 * - findPlayableCard(...) offers a simple bot heuristic:
 *     prefer non-wild cards that match color or value; if none, play a wild.
 */
public class Player {

    private final String name;
    private final boolean bot;
    private final List<Card> hand = new ArrayList<>();
    private boolean calledUno = false;

    public Player(String name, boolean isBot) {
        this.name = name;
        this.bot = isBot;
    }

    // ---- identity ----
    public String getName() { return name; }
    public boolean isBot() { return bot; }

    // ---- hand management ----
    /** Unmodifiable view to avoid accidental external mutation. */
    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    public int getCardCount() {
        return hand.size();
    }

    /** Add a card to hand; if hand grows, UNO call (if any) is no longer valid. */
    public void addCard(Card c) {
        if (c != null) {
            hand.add(c);
            if (hand.size() > 1) {
                calledUno = false; // drawing usually cancels an existing UNO call
            }
        }
    }

    /**
     * Remove a card by value (color + value for non-wilds; value-only for wilds).
     * Returns true if any matching card was removed.
     */
    public boolean removeCard(Card target) {
        if (target == null) return false;
        Iterator<Card> it = hand.iterator();
        while (it.hasNext()) {
            Card c = it.next();
            if (target.equals(c)) {    // relies on Card.equals/hashCode you just added
                it.remove();
                return true;
            }
        }
        return false;
    }

    // ---- UNO state ----
    public boolean hasCalledUno() {
        return calledUno;
    }

    /** Mark UNO as called (Session checks the "one card left" rule before calling this). */
    public void callUno() {
        calledUno = true;
    }

    // ---- simple bot helper ----
    /**
     * Pick a playable card against a visible top card.
     * Heuristic: first try non-wild matches (color or value), then wilds.
     * (Session will still validate play using its own canPlayCard logic.)
     */
    public Card findPlayableCard(Card topCard) {
        if (topCard == null) return findAnyWild();

        // Prefer non-wild matches (color or value)
        for (Card c : hand) {
            if (!c.isWild()) {
                if (c.getColor() != null && c.getColor().equals(topCard.getColor())) {
                    return c;
                }
                if (c.getValue() != null && c.getValue().equals(topCard.getValue())) {
                    return c;
                }
            }
        }
        // If none, use a wild if available
        return findAnyWild();
    }

    private Card findAnyWild() {
        for (Card c : hand) {
            if (c.isWild()) return c;
        }
        return null;
    }

    @Override
    public String toString() {
        return (bot ? "[BOT] " : "") + name + " (" + hand.size() + " cards)";
    }
}

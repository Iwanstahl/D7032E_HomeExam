package game.player;

import game.card.Card;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/** The cards held by one player. Hides the underlying list so it can only be changed through this class. */
public class Hand {
    private final List<Card> cards = new ArrayList<>();

    /** Adds a card to the hand. */
    public void add(Card c) {
        cards.add(c);
    }

    /**
     * Removes a card from the hand.
     *
     * @throws IllegalArgumentException if the hand does not contain the card
     */
    public void remove(Card c) {
        if(!cards.remove(c)) {
            throw new IllegalArgumentException("Hand doesn't contain " + c);
        }
    }

    /** Returns true if the hand contains the card. */
    public boolean contains(Card c) {
        return cards.contains(c);
    }

    /** Returns the number of cards in the hand. */
    public int size() {
        return cards.size();
    }

    /** Returns a read-only view of the cards. */
    public List<Card> asList() {
        return Collections.unmodifiableList(cards);
    }

    /**
     * Returns true if the hand holds at least one card of the given type, e.g.
     * {@code containsType(DefuseCard.class)}. Unlike {@link #contains(Card)}, any card of
     * that type matches, not only one specific card object.
     */
    public boolean containsType(Class<? extends Card> type) {
        return countType(type) > 0;
    }

    /**
     * Returns the first card of the given type without removing it, e.g. the card a
     * 3-of-a-kind takes from its target.
     *
     * @throws IllegalArgumentException if the hand holds no card of that type
     */
    public Card firstOfType(Class<? extends Card> type) {
        for(int i = 0; i < cards.size(); i++) {
            if(type.isInstance(cards.get(i))) {
                return cards.get(i);
            }
        }
        throw new IllegalArgumentException("Hand doesn't contain a " + type.getSimpleName());
    }

    /**
     * Removes the first card of the given type from the hand and returns it, so the caller
     * can put it on the discard pile or give it to another player. Only one card is removed,
     * even if the hand holds several of that type.
     *
     * @throws IllegalArgumentException if the hand holds no card of that type
     */
    public Card removeType(Class<? extends Card> type) {
        Card card = firstOfType(type);
        cards.remove(card);
        return card;
    }

    /** Returns how many cards of the given type the hand holds, e.g. to offer a pair or a triple. */
    public int countType(Class<? extends Card> type) {
        int count = 0;
        for(int i = 0; i < cards.size(); i++) {
            if(type.isInstance(cards.get(i))) {
                count++;
            }
        }
        return count;
    }
}

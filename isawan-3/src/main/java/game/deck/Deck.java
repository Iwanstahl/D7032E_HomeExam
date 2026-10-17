package game.deck;

import game.card.Card;
import java.util.List;

/**
 * The draw pile. Position 0 is the top of the deck, the card drawn next.
 */
public interface Deck {

    /**
     * Removes and returns the top card.
     * @throws IllegalStateException if the deck is empty
     */
    public Card drawTop();

    /**
     * Inserts a card so that it ends up at position {@code pos} (0 = top).
     * @throws IndexOutOfBoundsException if {@code pos} is negative or larger than the deck size
     */
    public void insertAt(int pos, Card c);

    /** Adds the cards above the existing ones, the first card in the list becomes the new top. */
    public void insertOnTop(List<Card> cards);

    /** Returns a copy of the top {@code n} cards without removing them, or fewer if the deck is smaller. */
    public List<Card> peekTop(int n);

    /** Puts the cards in a random order. */
    public void shuffle();

    /** Returns the number of cards left in the deck. */
    public int size();
}

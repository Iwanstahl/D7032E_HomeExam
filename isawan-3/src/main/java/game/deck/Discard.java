package game.deck;

import game.card.Card;

/** The discard pile. The most recently played card is on top. */
public interface Discard {

    /** Puts a card on top of the pile. */
    public void addToTop(Card c);

    /**
     * Returns the top card without removing it.
     * @throws IllegalStateException if the discard pile is empty
     */
    public Card peekTop();
}

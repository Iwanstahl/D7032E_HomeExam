package game.deck;

import java.util.ArrayList;
import java.util.List;
import game.card.Card;

/** {@link Discard} pile kept in a list with the most recently added card first. */
public class ListDiscard implements Discard {
    private final List<Card> discardPile = new ArrayList<>();

    @Override 
    public void addToTop(Card c) {
        discardPile.add(0, c);
    }

    @Override 
    public Card peekTop() {
        if(discardPile.isEmpty()) {
            throw new IllegalStateException("Can't get top card, discard pile is empty");
        } 
        return discardPile.get(0);
    }
}
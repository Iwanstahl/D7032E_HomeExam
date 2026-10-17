package game.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import game.card.Card;

/**
 * Says which cards a game is set up with, in three groups: cards every player starts
 * with (a Defuse, rule 2), cards in the deck that hands are dealt from (rule 3), and
 * cards put into the deck only after dealing, so nobody is dealt them (the Exploding
 * Kittens, rule 6). Expansion modules fill it in. {@code GameSetup} decides when each
 * group is used, so it never needs to know which cards or how many.
 *
 * <p>Cards are given as a {@link Supplier} (e.g. {@code DefuseCard::new}), not as card
 * objects, because every card in the game must be its own object ({@code Hand} removes
 * cards by identity) and each player needs their own starting cards. Every getter
 * creates new cards each time it is called.
 */
public class DeckRecipe {
    private final List<Supplier<Card>> eachHand = new ArrayList<>();
    private final List<Supplier<Card>> deck = new ArrayList<>();
    private final List<Supplier<Card>> afterDeal = new ArrayList<>();

    /**
     * Adds {@code count} cards to the deck that hands are dealt from.
     *
     * @throws IllegalArgumentException if {@code card} is null or {@code count} is negative
     */
    public void addToDeck(Supplier<Card> card, int count) {
        add(deck, card, count);
    }

    /**
     * Gives every player one of this card before dealing, e.g. a Defuse.
     *
     * @throws IllegalArgumentException if {@code card} is null
     */
    public void addToEachHand(Supplier<Card> card) {
        add(eachHand, card, 1);
    }

    /**
     * Adds {@code count} cards that go into the deck after the hands are dealt, e.g. the
     * Exploding Kittens.
     *
     * @throws IllegalArgumentException if {@code card} is null or {@code count} is negative
     */
    public void addAfterDeal(Supplier<Card> card, int count) {
        add(afterDeal, card, count);
    }

    /** New cards for one player's starting hand. Call once per player. */
    public List<Card> eachHandCards() {
        return create(eachHand);
    }

    /** New cards for the deck that hands are dealt from. */
    public List<Card> deckCards() {
        return create(deck);
    }

    /** New cards to put into the deck after dealing. */
    public List<Card> afterDealCards() {
        return create(afterDeal);
    }

    private static void add(List<Supplier<Card>> group, Supplier<Card> card, int count) {
        if(card == null) {
            throw new IllegalArgumentException("card cannot be null.");
        }
        if(count < 0) {
            throw new IllegalArgumentException("count cannot be negative, was " + count + ".");
        }
        for(int i = 0; i < count; i++) {
            group.add(card);
        }
    }

    private static List<Card> create(List<Supplier<Card>> group) {
        List<Card> cards = new ArrayList<>();
        for(int i = 0; i < group.size(); i++) {
            cards.add(group.get(i).get());
        }
        return cards;
    }
}

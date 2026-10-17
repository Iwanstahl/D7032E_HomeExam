package game.deck;

import java.util.ArrayList;
import java.util.List;
import game.card.Card;
import game.random.RandomSource;

/**
 * {@link Deck} kept in a list with position 0 as the top. Shuffling uses the injected
 * {@link RandomSource} so tests can control or count it. The list is never handed out:
 * callers only get copies.
 */
public class ListDeck implements Deck {
    private final List<Card> drawPile = new ArrayList<>();
    private final RandomSource random;

    /**
     * Creates an empty deck that shuffles with {@code random}.
     * @throws IllegalArgumentException if {@code random} is null
     */
    public ListDeck(RandomSource random) {
        if(random == null) {
            throw new IllegalArgumentException("random cannot be null.");
        }
        this.random = random;
    }

    @Override
    public Card drawTop() {
        if(drawPile.isEmpty()) {
            throw new IllegalStateException("Can't draw, drawPile is empty");
        }
        return drawPile.remove(0);
    }

    @Override
    public void insertAt(int pos, Card c) {
        if(pos < 0 || pos > drawPile.size()) {
            throw new IndexOutOfBoundsException("pos cannot be negative or larger than the drawPile.");
        }
        drawPile.add(pos, c);
    }

    @Override
    public void insertOnTop(List<Card> cards) {
        drawPile.addAll(0, cards);
    }

    @Override
    public List<Card> peekTop(int n) {
        if(n < 1) {
            throw new IllegalArgumentException("n cannot be negative or zero.");
        }
        return new ArrayList<>(drawPile.subList(0, Math.min(n, drawPile.size())));
    }

    @Override
    public void shuffle() {
        random.shuffle(drawPile);
    }

    @Override
    public int size() {
        return drawPile.size();
    }
}

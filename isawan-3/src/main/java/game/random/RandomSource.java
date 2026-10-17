package game.random;

import java.util.List;

/**
 * Single source of randomness for the game. Implementations can be swapped,
 * for example for a scripted one in tests, without changing the classes that use it.
 */
public interface RandomSource {

    /** Reorders the list in place into a random order. */
    public void shuffle(List<?> list);

    /** Returns a random integer from 0 (inclusive) to {@code bound} (exclusive). */
    public int nextInt(int bound);
}

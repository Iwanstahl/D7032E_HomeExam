package game.random;

import java.util.Random;
import java.util.Collections;
import java.util.List;

/**
 * Production {@link RandomSource} backed by {@link java.util.Random}, so the rest of
 * the game never depends on {@code Random} directly.
 */
public class DefaultRandomSource implements RandomSource {
    private final Random random;

    /** Creates a source with a randomly seeded generator. */
    public DefaultRandomSource() {
        this(new Random());
    }

    /**
     * Creates a source using the given generator, e.g. one with a fixed seed so a game can
     * be repeated.
     *
     * @throws IllegalArgumentException if {@code random} is null
     */
    public DefaultRandomSource(Random random) {
        if(random == null) {
            throw new IllegalArgumentException("random cannot be null.");
        }
        this.random = random;
    }

    @Override
    public void shuffle(List<?> list) {
        Collections.shuffle(list, random);
    }

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }
}

package game.card;

import game.turn.TurnContext;

/**
 * Base class for the five cat cards. A cat card has no effect of its own. It can only be
 * played as a pair or triple (rule 11f/11g) which {@code CardComboAction} handles so
 * {@link #canPlayAlone()} is false and {@link #play} is never reached. Each cat is its own
 * subclass because a pair must be two cards of the same kind and kinds are compared by
 * class.
 */
public abstract class CatCard extends AbstractCard {
    /** Never called, since a cat card can't be played alone, throws if it is. */
    @Override
    public void play(TurnContext ctx) {
        throw new UnsupportedOperationException("cat cards can only be played as pair or triple.");
    }

    @Override
    public boolean canPlayAlone() {
        return false;
    }
}

package game.card;

import game.turn.TurnContext;

/**
 * Base class for cards, supplying the behaviour most cards share: playable alone,
 * can be Noped, no target, and no reaction when drawn.
 * Subclasses override only the parts where their card differs.
 */
public abstract class AbstractCard implements Card {

    /**
     * Applies this card's effect. Deliberately left abstract: there is no safe default,
     * and a card that silently did nothing would hide a bug.
     */
    @Override
    public abstract void play(TurnContext ctx);

    /* Does nothing by default. Only cards that react to being drawn override this. */
    @Override
    public void onDrawn(TurnContext ctx) {

    }

    /** Returns {@code true} by default. */
    @Override
    public boolean canPlayAlone() {
        return true;
    }

    /** Returns {@code true} by default. */
    @Override
    public boolean canBeNoped() {
        return true;
    }

    /** Returns {@code false} by default. */
    @Override
    public boolean needsTarget() {
        return false;
    }

    /**
     * Returns {@link #name()} so logs and test failures show "Skip" instead of an object
     * id. There is no default for {@code name()} itself so every concrete card must give
     * its own.
     */
    @Override
    public String toString() {
        return name();
    }

}

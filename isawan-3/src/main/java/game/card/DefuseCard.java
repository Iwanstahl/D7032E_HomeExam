package game.card;

import game.turn.TurnContext;

/**
 * Defuse: never played on its own. When its holder draws an Exploding Kitten, the kitten
 * uses up one Defuse from their hand automatically so they survive (rule 10). It can't be Noped.
 */
public class DefuseCard extends AbstractCard {

    /** Never played on its own. {@link ExplodingKittenCard} uses it when drawn. */
    @Override
    public void play(TurnContext ctx) {
        throw new UnsupportedOperationException("Defuse is used automatically when you draw an Exploding Kitten.");
    }

    @Override
    public boolean canPlayAlone() {
        return false;
    }

    /** A Defuse can't be Noped. */
    @Override
    public boolean canBeNoped() {
        return false;
    }

    @Override
    public String name() {
        return "Defuse";
    }
}

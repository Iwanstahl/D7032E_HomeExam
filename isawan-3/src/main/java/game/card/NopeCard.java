package game.card;

import game.turn.TurnContext;

/**
 * Nope: never played as a normal action. When another card is played the Nope window
 * ({@code NopeTimeWindow}) asks holders of a Nope whether to use it and spends it to
 * cancel that card. It's the only card that can be used on someone else's turn and a
 * Nope can itself be Noped (rule 12).
 */
public class NopeCard extends AbstractCard {

    @Override
    public void play(TurnContext ctx) {
        throw new UnsupportedOperationException("Nope is only used in the Nope window, to cancel another card.");
    }

    @Override
    public boolean canPlayAlone() {
        return false;
    }

    @Override
    public String name() {
        return "Nope";
    }
}

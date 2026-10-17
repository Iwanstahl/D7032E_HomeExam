package game.card;

import game.turn.TurnContext;

/** Shuffle: shuffles the deck (rule 11c). The turn goes on. */
public class ShuffleCard extends AbstractCard {

    @Override 
    public void play(TurnContext ctx) {
        ctx.deck().shuffle();
    }

    @Override
    public String name() {
        return "Shuffle";
    }
}

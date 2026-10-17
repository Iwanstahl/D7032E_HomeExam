package game.card;

import game.turn.TurnContext;

/**
 * Skip: ends the current turn without drawing a card. Only one turn ends so an
 * attacked player who plays Skip still has to take their other turn (rule 11d).
 */
public class SkipCard extends AbstractCard {
    
    @Override
    public void play(TurnContext ctx) {
        ctx.turns().endTurnWithoutDraw();
    }

    @Override
    public String name() {
        return "Skip";
    }
}
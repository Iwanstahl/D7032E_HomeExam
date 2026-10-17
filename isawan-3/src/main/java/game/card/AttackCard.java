package game.card;

import game.turn.TurnContext;
import game.player.Player;

/**
 * Attack: ends the current player's turns without drawing and gives the next player
 * 2 turns in a row (rule 11a). The card always asks for 2 turns, if the current player
 * was attacked themselves, {@code TurnControl.passTurnsTo} adds the turns they still owe.
 */
public class AttackCard extends AbstractCard {
    
    @Override
    public void play(TurnContext ctx) {
        Player next = ctx.turns().nextPlayer();
        ctx.turns().passTurnsTo(next, 2);
    }

    @Override
    public String name() {
        return "Attack";
    }
}
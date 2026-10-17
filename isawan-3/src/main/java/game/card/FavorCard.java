package game.card;

import game.io.PromptKind;
import game.player.Player;
import game.turn.TurnContext;

/**
 * Favor: the player who plays it targets another player who must give them one card of
 * the target's own choice (rule 11b). The target is chosen when the card is played,
 * before the Nope window ({@link #needsTarget()}), so everyone knows who is targeted.
 * Any player still in the game can be targeted, also one with no cards where nothing is
 * given and the Favor is simply used up.
 */
public class FavorCard extends AbstractCard {

    @Override
    public void play(TurnContext ctx) {
        Player target = ctx.target();
        if(target.hand().size() == 0) {
            return;
        }
        Card card = ctx.actions().chooseOwnCard(target, PromptKind.CHOOSE_CARD_TO_GIVE);
        ctx.actions().giveCard(target, ctx.actor(), card);
    }

    /** Favor targets another player. */
    @Override
    public boolean needsTarget() {
        return true;
    }

    @Override
    public String name() {
        return "Favor";
    }
}

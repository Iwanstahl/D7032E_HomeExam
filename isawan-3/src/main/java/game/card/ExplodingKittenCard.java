package game.card;

import java.util.Map;

import game.io.EventType;
import game.io.GameEvent;
import game.player.Player;
import game.turn.TurnContext;

/**
 * Exploding Kitten: never played, everything happens when it is drawn (rule 10). The
 * drawer has already put it in their hand. If they hold a Defuse, the Defuse is
 * discarded and they choose where in the deck the kitten goes back otherwise they are
 * eliminated and any turns they still owe are dropped so an exploded player gets no
 * more turns (rule 10b). It can't be Noped.
 */
public class ExplodingKittenCard extends AbstractCard {

    @Override
    public void onDrawn(TurnContext ctx) {
        Player drawer = ctx.actor();
        drawer.hand().remove(this);
        if(drawer.hand().containsType(DefuseCard.class)) {
            ctx.discard().addToTop(drawer.hand().removeType(DefuseCard.class));
            ctx.deck().insertAt(ctx.actions().chooseDeckPosition(drawer), this);
            ctx.actions().broadcast(new GameEvent(EventType.KITTEN_DEFUSED,
                    Map.of("player", String.valueOf(drawer.playerId()))));
        } else {
            ctx.actions().eliminate(drawer);
            ctx.discard().addToTop(this);
            ctx.turns().endAllTurns();
        }
    }

    /** Never played. It acts only when drawn, in {@link #onDrawn(TurnContext)}. */
    @Override
    public void play(TurnContext ctx) {
        throw new UnsupportedOperationException("An Exploding Kitten is never played, it acts when drawn.");
    }

    @Override
    public boolean canPlayAlone() {
        return false;
    }

    /** An Exploding Kitten can't be Noped. */
    @Override
    public boolean canBeNoped() {
        return false;
    }

    @Override
    public String name() {
        return "Exploding Kitten";
    }
}

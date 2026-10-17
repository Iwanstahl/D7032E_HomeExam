package game.card;

import java.util.List;
import java.util.Map;

import game.io.EventType;
import game.io.GameEvent;
import game.turn.TurnContext;

/**
 * See the Future: privately shows the player who played it the top 3 cards of the deck
 * without changing the deck or ending the turn (rule 11e). Only the actor is told. With
 * fewer than 3 cards left it shows what there is instead of crashing.
 */
public class SeeTheFutureCard extends AbstractCard {

    @Override
    public void play(TurnContext ctx) {
        List<Card> top = ctx.deck().peekTop(3);
        ctx.actor().notifier().onEvent(new GameEvent(EventType.CARDS_REVEALED, Map.of("cards", top.toString())));
    }

    @Override
    public String name() {
        return "See the Future";
    }
}

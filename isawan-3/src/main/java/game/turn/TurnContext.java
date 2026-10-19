package game.turn;

import game.deck.Deck;
import game.deck.Discard;
import game.player.Player;

/**
 * Everything a {@code Card} needs to act during one turn. That is the player acting, the
 * deck and discard pile, the turn control, the shared game actions and the target player
 * if the card needs one ({@code null} otherwise). It is immutable and built fresh for each
 * {@code play} or {@code onDrawn} call.
 */
public final class TurnContext {
    private final Player actor;
    private final Deck deck;
    private final Discard discard;
    private final TurnControl turns;
    private final GameActions actions;
    private final Player target;

    /** @throws IllegalArgumentException if any argument except {@code target} is null */
    public TurnContext(Player actor, Deck deck, Discard discard, TurnControl turns,
                       GameActions actions, Player target) {
        if(actor == null || deck == null || discard == null || turns == null || actions == null) {
            throw new IllegalArgumentException("Only target may be null in a TurnContext.");
        }
        this.actor = actor;
        this.deck = deck;
        this.discard = discard;
        this.turns = turns;
        this.actions = actions;
        this.target = target;
    }

    public Player actor() {
        return actor;
    }

    public Deck deck() {
        return deck;
    }

    public Discard discard() {
        return discard;
    }

    public TurnControl turns() {
        return turns;
    }

    public GameActions actions() {
        return actions;
    }

    /** The player this card targets, chosen before the Nope window. Null if it has no target. */
    public Player target() {
        return target;
    }
}

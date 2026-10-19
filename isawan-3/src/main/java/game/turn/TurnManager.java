package game.turn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.card.Card;
import game.deck.Deck;
import game.deck.Discard;
import game.io.EventType;
import game.io.GameEvent;
import game.io.Option;
import game.io.Prompt;
import game.io.PromptKind;
import game.player.Player;

/**
 * Plays one turn of the current player. It asks them for actions until the turn ends,
 * either by drawing a card (rule 9) or by a card that ends it (Skip, Attack, exploding).
 * Each action is playing one card they can play alone, playing 2 or 3 cards of a kind
 * (rule 11f/11g) or drawing.
 *
 * It never checks which card it is handling. It only asks a card's flags
 * ({@code canPlayAlone}, {@code needsTarget}, {@code canBeNoped}) and calls
 * {@code play}/{@code onDrawn}, so a new card type needs no change here. Whether the turn
 * is over is read from {@link RoundState#turnNumber()}, since the number changes whatever
 * ends the turn. An attacked player's second turn is a second call to {@link #playTurn()}.
 */
public class TurnManager {
    private final RoundState round;
    private final Deck deck;
    private final Discard discard;
    private final GameActions actions;
    private final NopeTimeWindow nope;
    private final CardComboAction combos;

    /** @throws IllegalArgumentException if any argument is null */
    public TurnManager(RoundState round, Deck deck, Discard discard,
                       GameActions actions, NopeTimeWindow nope, CardComboAction combos) {
        if(round == null || deck == null || discard == null || actions == null || nope == null || combos == null) {
            throw new IllegalArgumentException("No argument to TurnManager can be null.");
        }
        this.round = round;
        this.deck = deck;
        this.discard = discard;
        this.actions = actions;
        this.nope = nope;
        this.combos = combos;
    }

    /** One menu choice: play {@code count} cards of the same kind as {@code card}. A null card means draw. */
    private static final class Action {
        final Card card;
        final int count;

        Action(Card card, int count) {
            this.card = card;
            this.count = count;
        }
    }

    /** Plays one turn of {@code round.currentPlayer()}, until that turn has ended. */
    public void playTurn() {
        Player actor = round.currentPlayer();
        int start = round.turnNumber();
        actions.broadcast(new GameEvent(EventType.TURN_STARTED, Map.of(
                "player", String.valueOf(actor.playerId()),
                "turnsOwed", String.valueOf(round.turnsOwed()))));
        while(round.turnNumber() == start) {
            Action chosen = chooseAction(actor);
            if(chosen.card == null) {
                draw(actor);
            } else if(chosen.count == 1) {
                playCard(actor, chosen.card);
            } else {
                playCombo(actor, chosen.card, chosen.count);
            }
        }
    }

    /**
     * Asks the actor what to do. Offers one option per kind of card they can play alone
     * (three Attacks give one "Attack" option), "Two X" / "Three X" for every kind they
     * hold at least 2 / 3 of (any kind, as in the official rules), then "Draw a card".
     */
    private Action chooseAction(Player actor) {
        List<Card> kinds = Card.oneOfEachKind(actor.hand().asList());
        List<Action> menu = new ArrayList<>();
        for(int i = 0; i < kinds.size(); i++) {
            Card c = kinds.get(i);
            if(c.canPlayAlone()) {
                menu.add(new Action(c, 1));
            }
        }
        for(int i = 0; i < kinds.size(); i++) {
            Card c = kinds.get(i);
            int held = actor.hand().countType(c.getClass());
            if(held >= 2) {
                menu.add(new Action(c, 2));
            }
            if(held >= 3) {
                menu.add(new Action(c, 3));
            }
        }
        menu.add(new Action(null, 0));

        List<Option> options = new ArrayList<>();
        for(int i = 0; i < menu.size(); i++) {
            options.add(new Option(i, label(menu.get(i))));
        }
        Map<String, String> context = new HashMap<>();
        context.put("hand", actor.hand().asList().toString());
        context.put("turnsOwed", String.valueOf(round.turnsOwed()));
        int chosen = actor.responder().ask(new Prompt(PromptKind.CHOOSE_ACTION, options, context)).id();
        return menu.get(chosen);
    }

    /** Plays {@code card}: discards it, picks a target if needed, runs the Nope window, then applies it. */
    private void playCard(Player actor, Card card) {
        actor.hand().remove(card);
        discard.addToTop(card);
        Player target = card.needsTarget() ? actions.chooseOtherPlayer(actor) : null;
        announce(actor, card.name(), target);
        if(!card.canBeNoped() || nope.resolve(card.name(), actor)) {
            card.play(context(actor, target));
        }
    }

    /**
     * Plays {@code count} (2 or 3) cards of the same kind as {@code kind}: discards them,
     * picks a target before the Nope window, and if not Noped steals a random card (2) or
     * names a card to take (3). The Nope window is told what was played, e.g. "Two Taco Cat".
     */
    private void playCombo(Player actor, Card kind, int count) {
        for(int i = 0; i < count; i++) {
            discard.addToTop(actor.hand().removeType(kind.getClass()));
        }
        Player target = actions.chooseOtherPlayer(actor);
        String played = label(new Action(kind, count));
        announce(actor, played, target);
        if(nope.resolve(played, actor)) {
            if(count == 2) {
                combos.stealRandom(actor, target);
            } else {
                combos.nameAndTake(actor, target);
            }
        }
    }

    /**
     * Draws the top card into the actor's hand and lets it react. Ends the turn, unless
     * the card already ended it (an Exploding Kitten without a Defuse ends all turns).
     */
    private void draw(Player actor) {
        int before = round.turnNumber();
        Card card = deck.drawTop();
        actor.hand().add(card);
        actor.notifier().onEvent(new GameEvent(EventType.CARD_DRAWN, Map.of("card", card.name())));
        card.onDrawn(context(actor, null));
        if(round.turnNumber() == before) {
            round.endTurnAfterDraw();
        }
    }

    /** Tells everyone what was played, and against whom if there is a target. */
    private void announce(Player actor, String played, Player target) {
        Map<String, String> details = new HashMap<>();
        details.put("player", String.valueOf(actor.playerId()));
        details.put("card", played);
        if(target != null) {
            details.put("target", String.valueOf(target.playerId()));
        }
        actions.broadcast(new GameEvent(EventType.CARD_PLAYED, details));
    }

    private TurnContext context(Player actor, Player target) {
        return new TurnContext(actor, deck, discard, round, actions, target);
    }

    private static String label(Action a) {
        if(a.card == null) {
            return "Draw a card";
        }
        if(a.count == 2) {
            return "Two " + a.card.name();
        }
        if(a.count == 3) {
            return "Three " + a.card.name();
        }
        return a.card.name();
    }
}

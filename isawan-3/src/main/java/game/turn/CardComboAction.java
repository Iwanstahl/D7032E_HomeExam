package game.turn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import game.card.Card;
import game.deck.Deck;
import game.io.EventType;
import game.io.GameEvent;
import game.player.Player;
import game.random.RandomSource;

/**
 * The effects of playing 2 or 3 cards of the same kind (rule 11f/11g). They work the same
 * whatever kind of card was used (two Taco Cats or two Attacks), which is why this is a
 * separate class instead of code in each card. Discarding the cards, choosing the target
 * and the Nope window are done by {@link TurnManager} before these are called.
 */
public class CardComboAction {
    private final List<Player> players;
    private final Deck deck;
    private final GameActions actions;
    private final RandomSource random;

    /**
     * {@code deck} and the players' hands are where the names offered by a 3-of-a-kind come from.
     *
     * @throws IllegalArgumentException if any argument is null
     */
    public CardComboAction(List<Player> players, Deck deck, GameActions actions, RandomSource random) {
        if(players == null || deck == null || actions == null || random == null) {
            throw new IllegalArgumentException("No argument to CardComboAction can be null.");
        }
        this.players = List.copyOf(players);
        this.deck = deck;
        this.actions = actions;
        this.random = random;
    }

    /**
     * 2 of a kind: {@code actor} takes a random card from {@code target}'s hand (rule 11f).
     * The index is drawn from {@code [0, hand size)}, so every card is equally likely,
     * including the last one and a single card. If the target has no cards, nothing happens.
     */
    public void stealRandom(Player actor, Player target) {
        int size = target.hand().size();
        if(size == 0) {
            return;
        }
        Card stolen = target.hand().asList().get(random.nextInt(size));
        actions.stealCard(target, actor, stolen);
    }

    /**
     * 3 of a kind: {@code actor} names a kind of card and {@code target} must give one of
     * that kind if they have it (rule 11g). Otherwise the actor is told they had none.
     * The kinds offered are those in the deck and in players' hands, so the list itself
     * doesn't reveal what the target holds.
     */
    public void nameAndTake(Player actor, Player target) {
        List<Card> known = new ArrayList<>();
        if(deck.size() > 0) {
            known.addAll(deck.peekTop(deck.size()));
        }
        for(int i = 0; i < players.size(); i++) {
            known.addAll(players.get(i).hand().asList());
        }
        Card named = actions.chooseCardKind(actor, known);
        if(target.hand().containsType(named.getClass())) {
            actions.stealCard(target, actor, target.hand().firstOfType(named.getClass()));
        } else {
            actor.notifier().onEvent(new GameEvent(EventType.CARD_NOT_FOUND, Map.of(
                    "player", String.valueOf(target.playerId()),
                    "card", named.name())));
        }
    }
}

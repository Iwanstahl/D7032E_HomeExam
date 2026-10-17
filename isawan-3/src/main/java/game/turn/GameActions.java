package game.turn;

import java.util.ArrayList;
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
 * Game-level operations that several cards (and later the turn logic) need: telling
 * everyone something, asking a player to choose a player, a card or a deck position, and
 * eliminating a player. Keeping them here means each card only describes its own rule,
 * and the prompt-building and elimination steps exist exactly once.
 *
 * <p>It does not change turns. Callers use {@link TurnControl} for that, so the effect of
 * a card on turn order stays visible in the card itself.
 */
public class GameActions {
    private final List<Player> players;
    private final Deck deck;
    private final Discard discard;

    /** @throws IllegalArgumentException if {@code players} is null or empty, or any other argument is null */
    public GameActions(List<Player> players, Deck deck, Discard discard) {
        if(players == null || players.isEmpty()) {
            throw new IllegalArgumentException("players cannot be null or empty.");
        }
        if(deck == null || discard == null) {
            throw new IllegalArgumentException("deck and discard cannot be null.");
        }
        this.players = List.copyOf(players);
        this.deck = deck;
        this.discard = discard;
    }

    /** Sends the event to every player, including players who have exploded. */
    public void broadcast(GameEvent event) {
        for(int i = 0; i < players.size(); i++) {
            players.get(i).notifier().onEvent(event);
        }
    }

    /**
     * Asks {@code asker} to pick one of the other players still in the game. Each option's
     * id is that player's own number ("2: Player 2"), so a player types the number they see
     * in the game, not a position in the list.
     *
     * @throws IllegalStateException if there is no other player still in the game
     */
    public Player chooseOtherPlayer(Player asker) {
        List<Player> candidates = new ArrayList<>();
        for(int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if(p != asker && !p.isExploded()) {
                candidates.add(p);
            }
        }
        if(candidates.isEmpty()) {
            throw new IllegalStateException("No other player is still in the game.");
        }
        List<Option> options = new ArrayList<>();
        for(int i = 0; i < candidates.size(); i++) {
            int id = candidates.get(i).playerId();
            options.add(new Option(id, "Player " + id));
        }
        int chosenId = ask(asker, PromptKind.CHOOSE_TARGET_PLAYER, options);
        for(int i = 0; i < candidates.size(); i++) {
            if(candidates.get(i).playerId() == chosenId) {
                return candidates.get(i);
            }
        }
        throw new IllegalStateException("No player " + chosenId + " was offered.");
    }

    /**
     * Asks {@code chooser} to pick one card from their own hand, e.g. the card to give
     * for a Favor. The card stays in the hand. Moving it is up to the caller.
     *
     * @throws IllegalArgumentException if the chooser's hand is empty
     */
    public Card chooseOwnCard(Player chooser, PromptKind kind) {
        List<Card> hand = new ArrayList<>(chooser.hand().asList());
        if(hand.isEmpty()) {
            throw new IllegalArgumentException("Player " + chooser.playerId() + " has no cards to choose from.");
        }
        List<Option> options = new ArrayList<>();
        for(int i = 0; i < hand.size(); i++) {
            options.add(new Option(i, hand.get(i).name()));
        }
        return hand.get(ask(chooser, kind, options));
    }

    /**
     * Asks {@code chooser} to name one kind of card, e.g. for a 3-of-a-kind. One option
     * per kind among {@code candidates} (duplicates of a kind are merged), labelled with
     * the card's name.
     *
     * @return a card of the chosen kind, from {@code candidates}, use its class to look for that kind
     * @throws IllegalArgumentException if {@code candidates} is empty
     */
    public Card chooseCardKind(Player chooser, List<Card> candidates) {
        List<Card> kinds = Card.oneOfEachKind(candidates);
        if(kinds.isEmpty()) {
            throw new IllegalArgumentException("No card kinds to choose from.");
        }
        List<Option> options = new ArrayList<>();
        for(int i = 0; i < kinds.size(); i++) {
            options.add(new Option(i, kinds.get(i).name()));
        }
        return kinds.get(ask(chooser, PromptKind.NAME_CARD_TYPE, options));
    }

    /**
     * {@code from} gives {@code card} to {@code to} (Favor): moves it between their hands and
     * tells the two of them ({@code CARD_TRANSFERRED}). Nobody else learns which card it was.
     *
     * @throws IllegalArgumentException if {@code from} doesn't hold that card
     */
    public void giveCard(Player from, Player to, Card card) {
        moveCard(from, to, card, EventType.CARD_TRANSFERRED);
    }

    /**
     * {@code to} steals {@code card} from {@code from} (2-/3-of-a-kind): moves it between
     * their hands and tells the two of them ({@code CARD_STOLEN}). Nobody else learns which
     * card it was.
     *
     * @throws IllegalArgumentException if {@code from} doesn't hold that card
     */
    public void stealCard(Player from, Player to, Card card) {
        moveCard(from, to, card, EventType.CARD_STOLEN);
    }

    private static void moveCard(Player from, Player to, Card card, EventType type) {
        from.hand().remove(card);
        to.hand().add(card);
        GameEvent moved = new GameEvent(type, Map.of(
                "from", String.valueOf(from.playerId()),
                "to", String.valueOf(to.playerId()),
                "card", card.name()));
        from.notifier().onEvent(moved);
        to.notifier().onEvent(moved);
    }

    /**
     * Asks {@code chooser} where in the deck to put a card: 0 is the top, the deck's size
     * is the bottom. Labels say which end is which, e.g. "0 (top)" ... "3 (bottom)".
     */
    public int chooseDeckPosition(Player chooser) {
        int size = deck.size();
        List<Option> options = new ArrayList<>();
        for(int i = 0; i <= size; i++) {
            options.add(new Option(i, positionLabel(i, size)));
        }
        return ask(chooser, PromptKind.CHOOSE_DECK_POSITION, options);
    }

    /**
     * Takes {@code p} out of the game: marks them exploded, puts their whole hand on the
     * discard pile and tells everyone. Does not change turns. If {@code p} is the current
     * player, the caller ends their remaining turns (rule 10b).
     */
    public void eliminate(Player p) {
        p.explode();
        List<Card> cards = new ArrayList<>(p.hand().asList());
        for(int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            p.hand().remove(c);
            discard.addToTop(c);
        }
        broadcast(new GameEvent(EventType.PLAYER_EXPLODED, Map.of("player", String.valueOf(p.playerId()))));
    }

    /** Asks the player an untimed question and returns the id of the chosen option. */
    private static int ask(Player player, PromptKind kind, List<Option> options) {
        return player.responder().ask(new Prompt(kind, options, null)).id();
    }

    private static String positionLabel(int position, int deckSize) {
        if(position == 0) {
            return "0 (top)";
        }
        if(position == deckSize) {
            return position + " (bottom)";
        }
        return String.valueOf(position);
    }
}

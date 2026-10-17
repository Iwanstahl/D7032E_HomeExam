package game.setup;

import java.util.List;

import game.deck.Deck;
import game.deck.Discard;
import game.player.Player;
import game.turn.RoundState;

/**
 * Everything {@link GameSetup} produces, handed on to whoever wires the game together:
 * the players, the deck, the discard pile and the turn state. Only a holder. The objects
 * in it change as the game is played.
 */
public final class GameState {
    private final List<Player> players;
    private final Deck deck;
    private final Discard discard;
    private final RoundState round;

    /** @throws IllegalArgumentException if any argument is null */
    public GameState(List<Player> players, Deck deck, Discard discard, RoundState round) {
        if(players == null || deck == null || discard == null || round == null) {
            throw new IllegalArgumentException("No argument to GameState can be null.");
        }
        this.players = List.copyOf(players);
        this.deck = deck;
        this.discard = discard;
        this.round = round;
    }

    public List<Player> players() {
        return players;
    }

    public Deck deck() {
        return deck;
    }

    public Discard discard() {
        return discard;
    }

    public RoundState round() {
        return round;
    }
}

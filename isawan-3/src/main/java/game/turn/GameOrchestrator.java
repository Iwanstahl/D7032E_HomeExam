package game.turn;

import java.util.Map;

import game.io.EventType;
import game.io.GameEvent;
import game.player.Player;

/**
 * Runs a whole game: lets {@link TurnManager} play turns until only one player is left,
 * then announces the winner to everyone (rule 13). Kept apart from {@code TurnManager},
 * which plays a single turn, because when the game ends is a different reason to change
 * than how a turn works. {@link RoundState} reports which players are still in. This
 * class decides what that means.
 */
public class GameOrchestrator {
    private final RoundState round;
    private final TurnManager turnManager;
    private final GameActions actions;

    /** @throws IllegalArgumentException if any argument is null */
    public GameOrchestrator(RoundState round, TurnManager turnManager, GameActions actions) {
        if(round == null) {
            throw new IllegalArgumentException("round cannot be null.");
        }
        if(turnManager == null) {
            throw new IllegalArgumentException("turnManager cannot be null.");
        }
        if(actions == null) {
            throw new IllegalArgumentException("actions cannot be null.");
        }
        this.round = round;
        this.turnManager = turnManager;
        this.actions = actions;
    }

    /**
     * Plays turns until one player is left, tells everyone who won, and returns the
     * winner. It returns instead of ending the program, so tests can check the result.
     */
    public Player run() {
        while(round.activePlayers().size() > 1) {
            turnManager.playTurn();
        }
        Player winner = round.activePlayers().get(0);
        actions.broadcast(new GameEvent(EventType.PLAYER_WON,
                Map.of("player", String.valueOf(winner.playerId()))));
        return winner;
    }
}

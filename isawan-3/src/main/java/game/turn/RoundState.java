package game.turn;

import game.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns the turn state of a round: whose turn it is, which direction play goes, how
 * many turns the current player still owes, and whether those turns come from an
 * Attack. Every way a turn can end goes through one place, so turns owed after an
 * Attack never leak into the next player's turn (rule 11d), and exploded players are
 * always skipped (rule 10b). Attack stacking is based on this state, not on the
 * discard pile (rule 11a).
 */
public class RoundState implements TurnControl {
    private final List<Player> players;
    private boolean attacked;
    private int currentPlayerIdx;
    private int turnsOwed;
    private int direction;
    private int turnNumber;

    /**
     * Starts a round with {@code players} seated in list order and {@code firstIdx}
     * taking the first turn, playing in the normal direction. The list is copied, so
     * later changes to the caller's list don't affect the seating. The players
     * themselves are shared, so a player exploding is seen immediately.
     *
     * @throws IllegalArgumentException if {@code players} is null or empty or {@code firstIdx}
     *         is not a seat in it
     */
    public RoundState(List<Player> players, int firstIdx) {
        if(players == null || players.isEmpty()) {
            throw new IllegalArgumentException("players cannot be null or empty.");
        }
        if(firstIdx < 0 || firstIdx >= players.size()) {
            throw new IllegalArgumentException("firstIdx was " + firstIdx
                    + ", must be between 0 and " + (players.size() - 1) + ".");
        }
        this.players = new ArrayList<>(players);
        this.currentPlayerIdx = firstIdx;
        this.direction = 1;
        this.turnsOwed = 1;
        this.turnNumber = 0;
        this.attacked = false;
    }

    /** Returns the player whose turn it is. */
    public Player currentPlayer() {
        return players.get(currentPlayerIdx);
    }

    /** Turns the current player still has to take, including the one in progress. */
    public int turnsOwed() {
        return turnsOwed;
    }

    /** Goes up by 1 whenever a turn ends. TurnManager watches it to know when to stop. */
    public int turnNumber() {
        return turnNumber;
    }

    @Override
    public Player nextPlayer() {
        return players.get(nextPlayerIdx());
    }

    /** Index of the next player who isn't exploded, following the direction. Changes no state. */
    private int nextPlayerIdx() {
        int idx = currentPlayerIdx;
        for(int i = 0; i < players.size(); i++) {
            idx = Math.floorMod(idx + direction, players.size());
            if(!players.get(idx).isExploded()) {
                return idx;
            }
        }
        throw new IllegalStateException("Every player has exploded.");
    }

    @Override
    public void reverseDirection() {
        direction *= -1;
    }

    @Override
    public void endTurnWithoutDraw() {
        finishOneTurn();
    }

    /**
     * Ends the current turn after the player drew a card without exploding. Called by
     * TurnManager, not by cards, which is why it is not part of TurnControl.
     */
    public void endTurnAfterDraw() {
        finishOneTurn();
    }

    @Override
    public void endAllTurns() {
        turnsOwed = 1;
        finishOneTurn();
    }

    @Override
    public void passTurnsTo(Player target, int extra) {
        int targetIdx = players.indexOf(target);
        if(targetIdx == -1) {
            throw new IllegalArgumentException("target is not a player in this round.");
        }
        if(target.isExploded()) {
            throw new IllegalArgumentException("target has already exploded.");
        }
        if(extra < 1) {
            throw new IllegalArgumentException("extra must be at least 1, was " + extra + ".");
        }
        int passed = extra;
        if(attacked) {
            passed += turnsOwed;
        }
        currentPlayerIdx = targetIdx;
        turnsOwed = passed;
        attacked = true;
        turnNumber += 1;
    }

    /** The players who haven't exploded, in seat order. A new list on every call. */
    public List<Player> activePlayers() {
        List<Player> active = new ArrayList<>();
        for(int i = 0; i < players.size(); i++) {
            if(!players.get(i).isExploded()) {
                active.add(players.get(i));
            }
        }
        return active;
    }

    /**
     * The one place a single turn ends. If the player still owes turns (after an Attack),
     * they keep playing. Otherwise the turn passes on and the next player owes 1 (rule 11d).
     */
    private void finishOneTurn() {
        turnsOwed -= 1;
        turnNumber += 1;
        if(turnsOwed == 0) {
            currentPlayerIdx = nextPlayerIdx();
            turnsOwed = 1;
            attacked = false;
        }
    }

}

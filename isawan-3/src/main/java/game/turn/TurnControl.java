package game.turn;

import game.player.Player;

/**
 * The turn-flow operations a card may trigger. Cards call these instead of keeping
 * or computing turn state themselves. The implementation owns the turn order, the
 * play direction and how many turns each player still owes.
 */
public interface TurnControl {

    /**
     * Returns the player who would take the next turn, following the current play
     * direction and skipping exploded players (rule 10b). Changes no state.
     */
    public Player nextPlayer();

    /**
     * Ends the current player's current turn without drawing a card (Skip). Only one
     * turn ends: a player who owes several turns still has to take the rest (rule 11d).
     */
    public void endTurnWithoutDraw();

    /**
     * Ends all turns the current player still owes, without drawing a card
     * (e.g. Super Skip from Streaking Kittens).
     */
    public void endAllTurns();

    /**
     * Ends all of the current player's turns without drawing and gives {@code target}
     * {@code extra} turns in a row (Attack). If the current player is themselves the
     * victim of an earlier Attack, their current and remaining untaken turns are added
     * on top, so attacks stack (rule 11a). Following the official rules: a victim who
     * attacks on their first of 2 turns passes on 4, a victim who completes 1 turn and
     * then attacks on their second passes on 3, a player who was not attacked passes on 2.
     *
     * @param target the player who takes the turns next
     * @param extra  the number of turns the attack adds
     */
    public void passTurnsTo(Player target, int extra);

    /** Reverses the order in which players take turns (e.g. Reverse from Imploding Kittens). */
    public void reverseDirection();
}

package game.io;

/**
 * Tells one player what happens in the game. Implemented once per kind of player
 * (console, bot, network) so the game logic never knows how a player is reached.
 * Outgoing only, asking a player something is {@link PlayerResponder}'s job.
 */
public interface PlayerNotifier {

    /**
     * Reports an event to this player. Called by the game for every event this player
     * should know about, deciding who receives which event is the caller's job.
     */
    public void onEvent(GameEvent event);
}

package game.io;

/**
 * Asks one player a question and returns their answer. Implemented once per kind of
 * player (console, bot, network) so the game logic never knows how the answer was
 * given. Incoming only, reporting events is {@link PlayerNotifier}'s job.
 */
public interface PlayerResponder {

    /**
     * Asks the player {@code prompt} and waits for the answer. Every implementation
     * must keep this contract, so callers never need special cases.
     */
    public Option ask(Prompt prompt);
}

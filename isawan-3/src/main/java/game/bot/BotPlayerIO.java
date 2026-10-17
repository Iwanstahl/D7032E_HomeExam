package game.bot;

import java.util.List;

import game.io.Option;
import game.io.GameEvent;
import game.io.PlayerNotifier;
import game.io.PlayerResponder;
import game.io.Prompt;
import game.io.PromptKind;

/**
 * The simplest bot possible. It plugs into an ordinary {@code Player} like any 
 * other I/O, so the game never knows it is a bot.
 */
public class BotPlayerIO implements PlayerNotifier, PlayerResponder {

    /** Ignores events. */
    @Override
    public void onEvent(GameEvent event) {
    }

    /**
     * "Draw a card" on its turn (always the last action option, see {@code TurnManager}),
     * the default for a timed prompt, otherwise the first option.
     */
    @Override
    public Option ask(Prompt prompt) {
        List<Option> options = prompt.options();
        if(prompt.kind() == PromptKind.CHOOSE_ACTION) {
            return options.get(options.size() - 1);
        }
        if(prompt.hasTimeout()) {
            return prompt.defaultOption();
        }
        return options.get(0);
    }
}

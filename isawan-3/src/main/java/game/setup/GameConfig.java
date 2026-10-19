package game.setup;

import java.util.List;

/**
 * The settings a game is started with: how many players in total and which card sets
 * ({@link ExpansionModule}s) to use, base game first. Built from the command line today.
 * A menu (req. 15) would only need to build one of these. A config can't be
 * created with invalid values, so the rest of the game never checks them again. Which
 * players are bots doesn't matter here: whoever creates the players decides that.
 *
 * The allowed number of players comes from the modules and not from this class, so an
 * expansion that allows more players (e.g. 6 with Imploding Kittens) needs no change here.
 */
public final class GameConfig {
    private final int playerCount;
    private final List<ExpansionModule> modules;

    /**
     * @param playerCount all players, bots included
     * @param modules     the card sets to use, in order, copied
     * @throws IllegalArgumentException if {@code modules} is null or empty, or if
     *         {@code playerCount} is outside what the modules allow (2 to 5 for the
     *         base game, rule 1)
     */
    public GameConfig(int playerCount, List<ExpansionModule> modules) {
        if(modules == null || modules.isEmpty()) {
            throw new IllegalArgumentException("modules cannot be null or empty.");
        }
        int min = minPlayers(modules);
        int max = maxPlayers(modules);
        if(playerCount < min || playerCount > max) {
            throw new IllegalArgumentException("playerCount must be between " + min + " and " + max
                    + ", was " + playerCount + ".");
        }
        this.playerCount = playerCount;
        this.modules = List.copyOf(modules);
    }

    /** All players, bots included, within what the modules allow. */
    public int playerCount() {
        return playerCount;
    }

    /** The card sets to use, in order. Read-only. */
    public List<ExpansionModule> modules() {
        return modules;
    }

    /** The smallest minimum among the modules. */
    private static int minPlayers(List<ExpansionModule> modules) {
        int min = modules.get(0).minPlayers();
        for(int i = 1; i < modules.size(); i++) {
            min = Math.min(min, modules.get(i).minPlayers());
        }
        return min;
    }

    /** The largest maximum among the modules, so an expansion can raise the limit. */
    private static int maxPlayers(List<ExpansionModule> modules) {
        int max = modules.get(0).maxPlayers();
        for(int i = 1; i < modules.size(); i++) {
            max = Math.max(max, modules.get(i).maxPlayers());
        }
        return max;
    }
}

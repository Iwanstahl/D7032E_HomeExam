package rules;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import game.player.Player;
import game.setup.BaseGameModule;
import game.setup.DeckRecipe;
import game.setup.ExpansionModule;
import game.setup.GameConfig;
import game.setup.GameSetup;

class Rule01PlayerCountTest {
    private static final List<ExpansionModule> BASE_GAME = List.of(new BaseGameModule());

    @Test
    @DisplayName("Rule 1: 2, 3, 4 and 5 players are allowed")
    void twoToFivePlayersAreAllowed() {
        for(int count = 2; count <= 5; count++) {
            GameConfig config = new GameConfig(count, BASE_GAME);

            assertEquals(count, config.playerCount());
        }
    }

    @Test
    @DisplayName("Rule 1: fewer than 2 or more than 5 players are rejected")
    void tooFewOrTooManyPlayersAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new GameConfig(0, BASE_GAME));
        assertThrows(IllegalArgumentException.class, () -> new GameConfig(1, BASE_GAME));
        assertThrows(IllegalArgumentException.class, () -> new GameConfig(6, BASE_GAME));
    }

    @Test
    @DisplayName("Rule 1: a game can't be set up with a different number of players than configured")
    void setupNeedsTheConfiguredNumberOfPlayers() {
        GameConfig config = new GameConfig(2, BASE_GAME);
        List<Player> threePlayers = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            ScriptedPlayer io = new ScriptedPlayer();
            threePlayers.add(new Player(i + 1, io, io));
        }

        assertThrows(IllegalArgumentException.class, () -> new GameSetup(new FixedRandom()).setUp(config, threePlayers));
    }

    @Test
    @DisplayName("Rule 1 with req 14: an expansion can raise the limit to 6, the base game alone can't")
    void anExpansionCanRaiseTheLimit() {
        ExpansionModule sixPlayers = new ExpansionModule() {
            @Override
            public String name() {
                return "Expansion for up to 6 players";
            }

            @Override
            public int minPlayers() {
                return 2;
            }

            @Override
            public int maxPlayers() {
                return 6;
            }

            @Override
            public void applyTo(DeckRecipe recipe, int playerCount) {
            }
        };
        List<ExpansionModule> withExpansion = List.of(new BaseGameModule(), sixPlayers);

        assertEquals(6, new GameConfig(6, withExpansion).playerCount());
        assertThrows(IllegalArgumentException.class, () -> new GameConfig(7, withExpansion));
        assertThrows(IllegalArgumentException.class, () -> new GameConfig(6, BASE_GAME));
    }
}

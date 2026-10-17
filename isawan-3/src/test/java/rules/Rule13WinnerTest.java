package rules;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import game.card.*;
import game.io.EventType;
import game.io.GameEvent;
import game.player.Player;

class Rule13WinnerTest {
    @Test
    @DisplayName("Rule 13: with 2 players, when one explodes the other wins and the game ends")
    void lastPlayerLeftWins() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Draw a card");
        ScriptedPlayer second = new ScriptedPlayer();
        TestGame game = new TestGame(first, second);
        game.deckFromTop(new ExplodingKittenCard(), new SkipCard());

        Player winner = game.orchestrator().run();

        assertSame(game.player(1), winner);
        assertEquals(0, second.prompts().size());
    }

    @Test
    @DisplayName("Rule 13: the winner is announced to all players, also those who exploded")
    void winnerIsAnnouncedToEveryone() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Draw a card");
        ScriptedPlayer second = new ScriptedPlayer().thenChoose("Draw a card");
        ScriptedPlayer third = new ScriptedPlayer().thenChoose("Draw a card");
        TestGame game = new TestGame(first, second, third);

        game.deckFromTop(new ExplodingKittenCard(), new SkipCard(), new ExplodingKittenCard(), new SkipCard());

        Player winner = game.orchestrator().run();

        assertSame(game.player(1), winner);
        assertTrue(game.player(0).isExploded());
        assertTrue(game.player(2).isExploded());
        for(int seat = 0; seat < 3; seat++) {
            List<GameEvent> won = game.script(seat).events(EventType.PLAYER_WON);
            assertEquals(1, won.size());
            assertEquals("2", won.get(0).details().get("player"));
        }
    }
}

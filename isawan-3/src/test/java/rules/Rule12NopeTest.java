package rules;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import game.card.*;
import game.io.EventType;
import game.io.GameEvent;
import game.io.Prompt;
import game.io.PromptKind;

class Rule12NopeTest {
    @Test
    @DisplayName("Rule 12: a Noped card has no effect, and the Nope is used up")
    void nopeStopsTheCard() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Attack").thenChoose("Draw a card");
        ScriptedPlayer second = new ScriptedPlayer().thenChoose("Nope");
        TestGame game = new TestGame(first, second);
        Card nope = new NopeCard();
        game.give(0, new AttackCard());
        game.give(1, nope);
        game.deckFromTop(new SkipCard(), new SkipCard());

        game.turns().playTurn();

        assertSame(game.player(1), game.round().currentPlayer());
        assertEquals(1, game.round().turnsOwed());
        assertFalse(game.player(1).hand().contains(nope));
        assertTrue(game.discarded().contains(nope));
        assertEquals("true", lastWindow(first).details().get("cancelled"));
    }

    @Test
    @DisplayName("Rule 12: a pair can be Noped too: nothing is stolen, but the cats are used up")
    void nopeStopsAPair() {
        ScriptedPlayer first = new ScriptedPlayer()
                .thenChoose("Two Taco Cat")
                .thenChoose("Player 2")
                .thenChoose("Draw a card");
        ScriptedPlayer second = new ScriptedPlayer().thenChoose("Nope");
        TestGame game = new TestGame(first, second);
        Card catOne = new TacoCatCard();
        Card catTwo = new TacoCatCard();
        Card skip = new SkipCard();
        game.give(0, catOne, catTwo);
        game.give(1, new NopeCard(), skip);
        game.deckFromTop(new AttackCard());

        game.turns().playTurn();

        assertTrue(game.player(1).hand().contains(skip));
        assertEquals(1, game.player(1).hand().size());
        assertTrue(game.discarded().contains(catOne));
        assertTrue(game.discarded().contains(catTwo));
    }

    @Test
    @DisplayName("Rule 12: only players holding a Nope are asked, with a 5 second limit, and no answer means \"Don't Nope\"")
    void nopeQuestionIsTimedAndDefaultsToDontNope() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Attack");
        ScriptedPlayer second = new ScriptedPlayer();
        ScriptedPlayer third = new ScriptedPlayer();
        TestGame game = new TestGame(first, second, third);
        game.give(0, new AttackCard());
        game.give(1, new NopeCard());
        game.deckFromTop(new SkipCard());

        game.turns().playTurn();

        List<Prompt> asked = second.prompts(PromptKind.PLAY_NOPE);
        assertEquals(1, asked.size());
        assertEquals(5, asked.get(0).timeoutSeconds());
        assertEquals("Don't Nope", asked.get(0).defaultOption().label());
        assertEquals(0, third.prompts(PromptKind.PLAY_NOPE).size());
        assertEquals(0, first.prompts(PromptKind.PLAY_NOPE).size());
        assertSame(game.player(1), game.round().currentPlayer());
        assertEquals(2, game.round().turnsOwed());
        assertTrue(game.player(1).hand().containsType(NopeCard.class));
    }

    @Test
    @DisplayName("Rule 12a: a Nope on a Nope (Yup) lets the card go ahead")
    void nopeOnANopeLetsTheCardGoAhead() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Attack").thenChoose("Nope");
        ScriptedPlayer second = new ScriptedPlayer().thenChoose("Nope");
        TestGame game = new TestGame(first, second);
        game.give(0, new AttackCard(), new NopeCard());
        game.give(1, new NopeCard());
        game.deckFromTop(new SkipCard());

        game.turns().playTurn();

        assertSame(game.player(1), game.round().currentPlayer());
        assertEquals(2, game.round().turnsOwed());
        assertFalse(game.player(0).hand().containsType(NopeCard.class));
        assertFalse(game.player(1).hand().containsType(NopeCard.class));
        assertEquals("false", lastWindow(first).details().get("cancelled"));
    }

    @Test
    @DisplayName("Rule 12a: three Nopes in a row cancel the card again")
    void threeNopesCancelTheCard() {
        ScriptedPlayer first = new ScriptedPlayer()
                .thenChoose("Attack")
                .thenChoose("Nope")
                .thenChoose("Draw a card");
        ScriptedPlayer second = new ScriptedPlayer().thenChoose("Nope").thenChoose("Nope");
        TestGame game = new TestGame(first, second);
        game.give(0, new AttackCard(), new NopeCard());
        game.give(1, new NopeCard(), new NopeCard());
        game.deckFromTop(new SkipCard(), new SkipCard());

        game.turns().playTurn();

        assertSame(game.player(1), game.round().currentPlayer());
        assertEquals(1, game.round().turnsOwed());
        assertEquals(3, first.events(EventType.NOPE_PLAYED).size());
        assertEquals("true", lastWindow(first).details().get("cancelled"));
    }

    private static GameEvent lastWindow(ScriptedPlayer player) {
        List<GameEvent> windows = player.events(EventType.NOPE_WINDOW_CLOSED);
        return windows.get(windows.size() - 1);
    }
}

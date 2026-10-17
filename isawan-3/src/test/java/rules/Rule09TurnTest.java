package rules;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import game.card.*;

class Rule09TurnTest {
    @Test
    @DisplayName("Rule 9a: passing draws the top card into the hand and ends the turn")
    void passingDrawsAndEndsTheTurn() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Draw a card");
        TestGame game = new TestGame(first, new ScriptedPlayer());
        Card top = new SkipCard();
        game.deckFromTop(top, new ShuffleCard());

        game.turns().playTurn();

        assertTrue(game.player(0).hand().contains(top));
        assertEquals(1, game.deckCards().size());
        assertSame(game.player(1), game.round().currentPlayer());
    }

    @Test
    @DisplayName("Rule 9b: a player can pass without playing any of their cards")
    void playerCanPassWithoutPlaying() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Draw a card");
        TestGame game = new TestGame(first, new ScriptedPlayer());
        Card attack = new AttackCard();
        game.give(0, attack);
        game.deckFromTop(new SkipCard());

        game.turns().playTurn();

        assertTrue(game.player(0).hand().contains(attack));
        assertEquals(2, game.player(0).hand().size());
        assertSame(game.player(1), game.round().currentPlayer());
    }

    @Test
    @DisplayName("Rule 9b: a player can play several cards in one turn, then pass")
    void playerCanPlaySeveralCardsThenPass() {
        ScriptedPlayer first = new ScriptedPlayer()
                .thenChoose("Shuffle")
                .thenChoose("See the Future")
                .thenChoose("Draw a card");
        TestGame game = new TestGame(first, new ScriptedPlayer());
        Card shuffle = new ShuffleCard();
        Card seeTheFuture = new SeeTheFutureCard();
        Card top = new SkipCard();
        game.give(0, shuffle, seeTheFuture);
        game.deckFromTop(top);

        game.turns().playTurn();

        assertTrue(game.discarded().contains(shuffle));
        assertTrue(game.discarded().contains(seeTheFuture));
        assertTrue(game.player(0).hand().contains(top));
        assertEquals(1, game.player(0).hand().size());
        assertSame(game.player(1), game.round().currentPlayer());
        assertTrue(first.scriptUsedUp());
    }
}

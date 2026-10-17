package rules;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import game.card.*;
import game.io.EventType;

class Rule10ExplodingKittenTest {
    @Test
    @DisplayName("Rule 10: a player who draws an Exploding Kitten with a Defuse survives, and the Defuse is used up")
    void defuseSavesThePlayer() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Draw a card").thenChoose("2");
        TestGame game = new TestGame(first, new ScriptedPlayer());
        Card defuse = new DefuseCard();
        game.give(0, defuse);
        game.deckFromTop(new ExplodingKittenCard(), new SkipCard(), new SkipCard(), new SkipCard());

        game.turns().playTurn();

        assertFalse(game.player(0).isExploded());
        assertFalse(game.player(0).hand().containsType(DefuseCard.class));
        assertFalse(game.player(0).hand().containsType(ExplodingKittenCard.class));
        assertTrue(game.discarded().contains(defuse));
        assertEquals(1, game.script(1).events(EventType.KITTEN_DEFUSED).size());
        assertSame(game.player(1), game.round().currentPlayer());
    }

    @Test
    @DisplayName("Rule 10a: the defused kitten goes back exactly where the player chose")
    void kittenGoesBackWhereChosen() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Draw a card").thenChoose("2");
        TestGame game = new TestGame(first, new ScriptedPlayer());
        game.give(0, new DefuseCard());
        game.deckFromTop(new ExplodingKittenCard(), new SkipCard(), new SkipCard(), new SkipCard());

        game.turns().playTurn();

        List<Card> deck = game.deckCards();
        assertEquals(4, deck.size());
        assertInstanceOf(ExplodingKittenCard.class, deck.get(2));
    }

    @Test
    @DisplayName("Rule 10a: the defused kitten can be put on top or at the bottom of the deck")
    void kittenCanGoOnTopOrAtTheBottom() {
        ScriptedPlayer onTop = new ScriptedPlayer().thenChoose("Draw a card").thenChoose("0 (top)");
        TestGame topGame = new TestGame(onTop, new ScriptedPlayer());
        topGame.give(0, new DefuseCard());
        topGame.deckFromTop(new ExplodingKittenCard(), new SkipCard(), new SkipCard(), new SkipCard());

        ScriptedPlayer atBottom = new ScriptedPlayer().thenChoose("Draw a card").thenChoose("3 (bottom)");
        TestGame bottomGame = new TestGame(atBottom, new ScriptedPlayer());
        bottomGame.give(0, new DefuseCard());
        bottomGame.deckFromTop(new ExplodingKittenCard(), new SkipCard(), new SkipCard(), new SkipCard());

        topGame.turns().playTurn();
        bottomGame.turns().playTurn();

        assertInstanceOf(ExplodingKittenCard.class, topGame.deckCards().get(0));
        assertInstanceOf(ExplodingKittenCard.class, bottomGame.deckCards().get(3));
    }

    @Test
    @DisplayName("Rule 10b: without a Defuse the player explodes and discards their hand, including the kitten")
    void withoutDefuseThePlayerExplodes() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Draw a card");
        TestGame game = new TestGame(first, new ScriptedPlayer(), new ScriptedPlayer());
        Card attack = new AttackCard();
        Card tacoCat = new TacoCatCard();
        Card kitten = new ExplodingKittenCard();
        game.give(0, attack, tacoCat);
        game.deckFromTop(kitten, new SkipCard());

        game.turns().playTurn();

        assertTrue(game.player(0).isExploded());
        assertEquals(0, game.player(0).hand().size());
        assertTrue(game.discarded().contains(attack));
        assertTrue(game.discarded().contains(tacoCat));
        assertTrue(game.discarded().contains(kitten));
        for(int seat = 0; seat < 3; seat++) {
            assertEquals(1, game.script(seat).events(EventType.PLAYER_EXPLODED).size());
        }
    }

    @Test
    @DisplayName("Rule 10b: an exploded player takes no more turns, also turns they still owed after an Attack")
    void explodedPlayerTakesNoMoreTurns() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Attack");
        ScriptedPlayer second = new ScriptedPlayer().thenChoose("Draw a card");
        ScriptedPlayer third = new ScriptedPlayer().thenChoose("Draw a card");
        TestGame game = new TestGame(first, second, third);
        game.give(0, new AttackCard());
        game.deckFromTop(new ExplodingKittenCard(), new SkipCard(), new SkipCard());

        game.turns().playTurn();
        game.turns().playTurn();

        assertTrue(game.player(1).isExploded());
        assertSame(game.player(2), game.round().currentPlayer());
        assertEquals(1, game.round().turnsOwed());

        game.turns().playTurn();

        assertSame(game.player(0), game.round().currentPlayer());
    }
}

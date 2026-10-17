package rules;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import game.card.*;
import game.io.EventType;
import game.io.GameEvent;
import game.io.PromptKind;

class Rule11CardTest {
    @Test
    @DisplayName("Rule 11a: Attack ends the turn without drawing, and the next player takes 2 turns")
    void attackGivesTheNextPlayerTwoTurns() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Attack");
        TestGame game = new TestGame(first, new ScriptedPlayer(), new ScriptedPlayer());
        game.give(0, new AttackCard());
        game.deckFromTop(new SkipCard(), new SkipCard());

        game.turns().playTurn();

        assertEquals(0, game.player(0).hand().size());
        assertEquals(2, game.deckCards().size());
        assertSame(game.player(1), game.round().currentPlayer());
        assertEquals(2, game.round().turnsOwed());
    }

    @Test
    @DisplayName("Rule 11a: an attacked player who attacks right away passes on their 2 turns plus 2, so 4")
    void attackOnTheFirstAttackedTurnPassesOnFour() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Attack");
        ScriptedPlayer second = new ScriptedPlayer().thenChoose("Attack");
        TestGame game = new TestGame(first, second, new ScriptedPlayer());
        game.give(0, new AttackCard());
        game.give(1, new AttackCard());
        game.deckFromTop(new SkipCard(), new SkipCard());

        game.turns().playTurn();
        game.turns().playTurn();

        assertSame(game.player(2), game.round().currentPlayer());
        assertEquals(4, game.round().turnsOwed());
    }

    @Test
    @DisplayName("Rule 11a: an attacked player who completes one turn, then attacks, passes on 1 remaining turn plus 2, so 3")
    void attackOnTheSecondAttackedTurnPassesOnThree() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Attack");
        ScriptedPlayer second = new ScriptedPlayer().thenChoose("Draw a card").thenChoose("Attack");
        TestGame game = new TestGame(first, second, new ScriptedPlayer());
        game.give(0, new AttackCard());
        game.give(1, new AttackCard());
        game.deckFromTop(new SkipCard(), new SkipCard());

        game.turns().playTurn();
        game.turns().playTurn();
        assertSame(game.player(1), game.round().currentPlayer());
        assertEquals(1, game.round().turnsOwed());
        game.turns().playTurn();

        assertSame(game.player(2), game.round().currentPlayer());
        assertEquals(3, game.round().turnsOwed());
    }

    @Test
    @DisplayName("Rule 11a: attacks keep stacking: 2, then 4, then 6")
    void attacksKeepStacking() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Attack");
        ScriptedPlayer second = new ScriptedPlayer().thenChoose("Attack");
        ScriptedPlayer third = new ScriptedPlayer().thenChoose("Attack");
        TestGame game = new TestGame(first, second, third);
        game.give(0, new AttackCard());
        game.give(1, new AttackCard());
        game.give(2, new AttackCard());
        game.deckFromTop(new SkipCard());

        game.turns().playTurn();
        assertEquals(2, game.round().turnsOwed());
        game.turns().playTurn();
        assertEquals(4, game.round().turnsOwed());
        game.turns().playTurn();

        assertSame(game.player(0), game.round().currentPlayer());
        assertEquals(6, game.round().turnsOwed());
    }

    @Test
    @DisplayName("Rule 11b: Favor makes the target give a card of the target's own choice")
    void favorMakesTheTargetGiveACardOfTheirChoice() {
        ScriptedPlayer first = new ScriptedPlayer()
                .thenChoose("Favor")
                .thenChoose("Player 2")
                .thenChoose("Draw a card");
        ScriptedPlayer second = new ScriptedPlayer().thenChoose("Taco Cat");
        TestGame game = new TestGame(first, second);
        Card skip = new SkipCard();
        Card tacoCat = new TacoCatCard();
        game.give(0, new FavorCard());
        game.give(1, skip, tacoCat);
        game.deckFromTop(new ShuffleCard());

        game.turns().playTurn();

        assertEquals(1, second.prompts(PromptKind.CHOOSE_CARD_TO_GIVE).size());
        assertTrue(game.player(0).hand().contains(tacoCat));
        assertFalse(game.player(1).hand().contains(tacoCat));
        assertTrue(game.player(1).hand().contains(skip));
    }

    @Test
    @DisplayName("Rule 11c: Shuffle shuffles the deck")
    void shuffleShufflesTheDeck() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Shuffle").thenChoose("Draw a card");
        TestGame game = new TestGame(first, new ScriptedPlayer());
        game.give(0, new ShuffleCard());
        game.deckFromTop(new SkipCard(), new SkipCard());

        assertEquals(0, game.random().shuffleCount());
        game.turns().playTurn();

        assertEquals(1, game.random().shuffleCount());
    }

    @Test
    @DisplayName("Rule 11d: Skip ends the turn without drawing a card")
    void skipEndsTheTurnWithoutDrawing() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Skip");
        TestGame game = new TestGame(first, new ScriptedPlayer());
        game.give(0, new SkipCard());
        game.deckFromTop(new AttackCard());

        game.turns().playTurn();

        assertEquals(0, game.player(0).hand().size());
        assertEquals(1, game.deckCards().size());
        assertSame(game.player(1), game.round().currentPlayer());
    }

    @Test
    @DisplayName("Rule 11d: an attacked player's Skip ends only one of their 2 turns")
    void skipEndsOnlyOneOfSeveralTurns() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("Attack");
        ScriptedPlayer second = new ScriptedPlayer().thenChoose("Skip").thenChoose("Skip");
        TestGame game = new TestGame(first, second, new ScriptedPlayer());
        game.give(0, new AttackCard());
        game.give(1, new SkipCard(), new SkipCard());
        game.deckFromTop(new ShuffleCard());

        game.turns().playTurn();
        game.turns().playTurn();

        assertSame(game.player(1), game.round().currentPlayer());
        assertEquals(1, game.round().turnsOwed());

        game.turns().playTurn();

        assertSame(game.player(2), game.round().currentPlayer());
        assertEquals(1, game.round().turnsOwed());
    }

    @Test
    @DisplayName("Rule 11e: See the Future shows only the player the top 3 cards, and the deck is unchanged")
    void seeTheFutureShowsTheTopThreeCards() {
        ScriptedPlayer first = new ScriptedPlayer().thenChoose("See the Future").thenChoose("Draw a card");
        ScriptedPlayer second = new ScriptedPlayer();
        TestGame game = new TestGame(first, second);
        Card top = new AttackCard();
        game.give(0, new SeeTheFutureCard());
        game.deckFromTop(top, new SkipCard(), new ShuffleCard(), new FavorCard());

        game.turns().playTurn();

        List<GameEvent> shown = first.events(EventType.CARDS_REVEALED);
        assertEquals(1, shown.size());
        assertEquals("[Attack, Skip, Shuffle]", shown.get(0).details().get("cards"));
        assertEquals(0, second.events(EventType.CARDS_REVEALED).size());
        assertTrue(game.player(0).hand().contains(top));
    }

    @Test
    @DisplayName("Rule 11f: two cats of a kind steal a random card from the target")
    void twoOfAKindStealARandomCard() {
        ScriptedPlayer first = new ScriptedPlayer()
                .thenChoose("Two Taco Cat")
                .thenChoose("Player 2")
                .thenChoose("Draw a card");
        TestGame game = new TestGame(new FixedRandom(1), first, new ScriptedPlayer());
        Card catOne = new TacoCatCard();
        Card catTwo = new TacoCatCard();
        Card shuffle = new ShuffleCard();
        game.give(0, catOne, catTwo);
        game.give(1, new SkipCard(), shuffle, new FavorCard());
        game.deckFromTop(new AttackCard());

        game.turns().playTurn();

        assertTrue(game.player(0).hand().contains(shuffle));
        assertFalse(game.player(1).hand().contains(shuffle));
        assertEquals(2, game.player(1).hand().size());
        assertTrue(game.discarded().contains(catOne));
        assertTrue(game.discarded().contains(catTwo));
    }

    @Test
    @DisplayName("Rule 11f: the pair can be of any kind, e.g. two Skips")
    void pairCanBeOfAnyKind() {
        ScriptedPlayer first = new ScriptedPlayer()
                .thenChoose("Two Skip")
                .thenChoose("Player 2")
                .thenChoose("Draw a card");
        TestGame game = new TestGame(first, new ScriptedPlayer());
        Card favor = new FavorCard();
        game.give(0, new SkipCard(), new SkipCard());
        game.give(1, favor);
        game.deckFromTop(new AttackCard());

        game.turns().playTurn();

        assertTrue(game.player(0).hand().contains(favor));
        assertEquals(0, game.player(1).hand().size());
    }

    @Test
    @DisplayName("Rule 11g: three of a kind take the named card from the target when they have it")
    void threeOfAKindTakeTheNamedCard() {
        ScriptedPlayer first = new ScriptedPlayer()
                .thenChoose("Three Taco Cat")
                .thenChoose("Player 2")
                .thenChoose("Favor")
                .thenChoose("Draw a card");
        TestGame game = new TestGame(first, new ScriptedPlayer());
        Card skip = new SkipCard();
        Card favor = new FavorCard();
        game.give(0, new TacoCatCard(), new TacoCatCard(), new TacoCatCard());
        game.give(1, skip, favor);
        game.deckFromTop(new AttackCard());

        game.turns().playTurn();

        assertTrue(game.player(0).hand().contains(favor));
        assertFalse(game.player(1).hand().contains(favor));
        assertTrue(game.player(1).hand().contains(skip));
    }

    @Test
    @DisplayName("Rule 11g: naming a card the target doesn't have takes nothing, and the player is told")
    void threeOfAKindNamingAMissingCardTakeNothing() {
        ScriptedPlayer first = new ScriptedPlayer()
                .thenChoose("Three Taco Cat")
                .thenChoose("Player 2")
                .thenChoose("Shuffle")
                .thenChoose("Draw a card");
        TestGame game = new TestGame(first, new ScriptedPlayer());
        game.give(0, new TacoCatCard(), new TacoCatCard(), new TacoCatCard());
        game.give(1, new SkipCard());
        game.deckFromTop(new ShuffleCard(), new AttackCard());

        game.turns().playTurn();

        assertEquals(1, game.player(1).hand().size());
        assertEquals(1, game.player(0).hand().size());
        assertEquals(1, first.events(EventType.CARD_NOT_FOUND).size());
    }
}

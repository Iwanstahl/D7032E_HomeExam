package rules;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import game.card.*;
import game.player.Player;
import game.setup.BaseGameModule;
import game.setup.DeckRecipe;
import game.setup.GameConfig;
import game.setup.GameSetup;
import game.setup.GameState;

class Rule02to08SetupTest {
    @Test
    @DisplayName("Rule 2: the base game gives every player exactly one Defuse of their own")
    void everyPlayerIsGivenOneDefuse() {
        DeckRecipe recipe = new DeckRecipe();
        new BaseGameModule().applyTo(recipe, 4);

        List<Card> firstHand = recipe.eachHandCards();
        List<Card> secondHand = recipe.eachHandCards();

        assertEquals(1, firstHand.size());
        assertInstanceOf(DefuseCard.class, firstHand.get(0));
        assertNotSame(firstHand.get(0), secondHand.get(0));
    }

    @Test
    @DisplayName("Rule 2: after setup every player holds a Defuse")
    void everyPlayerHoldsADefuseAfterSetup() {
        for(int count = 2; count <= 5; count++) {
            GameState state = setUp(count, new FixedRandom());

            for(int i = 0; i < count; i++) {
                assertTrue(state.players().get(i).hand().containsType(DefuseCard.class));
            }
        }
    }

    @Test
    @DisplayName("Rule 3: with 2 to 4 players the deck before dealing holds exactly the cards of rule 3, with 2 Defuses")
    void deckBeforeDealingWithTwoToFourPlayers() {
        for(int count = 2; count <= 4; count++) {
            List<Card> deck = deckBeforeDealing(count);

            assertEquals(2, count(deck, DefuseCard.class));
            assertRestOfRuleThree(deck);
            assertEquals(48, deck.size());
        }
    }

    @Test
    @DisplayName("Rule 3a: with 5 players the deck before dealing holds 1 Defuse")
    void deckBeforeDealingWithFivePlayers() {
        List<Card> deck = deckBeforeDealing(5);

        assertEquals(1, count(deck, DefuseCard.class));
        assertRestOfRuleThree(deck);
        assertEquals(47, deck.size());
    }

    @Test
    @DisplayName("Rules 4 and 7: the deck is shuffled when full, before dealing, and again after the kittens are added")
    void deckIsShuffledBeforeDealingAndBeforeTheGameStarts() {
        FixedRandom twoPlayers = new FixedRandom();
        setUp(2, twoPlayers);
        FixedRandom fivePlayers = new FixedRandom();
        setUp(5, fivePlayers);

        assertEquals(List.of(48, 35), twoPlayers.shuffledSizes());

        assertEquals(List.of(47, 16), fivePlayers.shuffledSizes());
    }

    @Test
    @DisplayName("Rule 5: every player is dealt 7 cards, on top of their own Defuse")
    void everyPlayerIsDealtSevenCards() {
        for(int count = 2; count <= 5; count++) {
            GameState state = setUp(count, new FixedRandom());

            for(int i = 0; i < count; i++) {
                assertEquals(8, state.players().get(i).hand().size());
            }
        }
    }

    @Test
    @DisplayName("Rule 5: the dealt cards come from the deck")
    void dealtCardsComeFromTheDeck() {
        GameState state = setUp(4, new FixedRandom());

        assertEquals(23, state.deck().size());
    }

    @Test
    @DisplayName("Rule 6: the deck holds one Exploding Kitten fewer than players, and nobody is dealt one")
    void oneExplodingKittenFewerThanPlayers() {
        for(int count = 2; count <= 5; count++) {
            GameState state = setUp(count, new FixedRandom());
            List<Card> deck = state.deck().peekTop(state.deck().size());

            assertEquals(count - 1, count(deck, ExplodingKittenCard.class));
            for(int i = 0; i < count; i++) {
                assertFalse(state.players().get(i).hand().containsType(ExplodingKittenCard.class));
            }
        }
    }

    @Test
    @DisplayName("Rule 8: the player the random source picks goes first, and every seat can be picked")
    void randomPlayerGoesFirst() {
        for(int seat = 0; seat < 4; seat++) {
            GameState state = setUp(4, new FixedRandom(seat));

            assertSame(state.players().get(seat), state.round().currentPlayer());
        }
    }

    @Test
    @DisplayName("Rule 8: the start player is picked among all players, not a smaller range")
    void startPlayerIsPickedAmongAllPlayers() {
        assertThrows(IllegalStateException.class, () -> setUp(4, new FixedRandom(4)));
    }

    private static GameState setUp(int count, FixedRandom random) {
        List<Player> players = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            ScriptedPlayer io = new ScriptedPlayer();
            players.add(new Player(i + 1, io, io));
        }
        GameConfig config = new GameConfig(count, List.of(new BaseGameModule()));
        return new GameSetup(random).setUp(config, players);
    }

    private static List<Card> deckBeforeDealing(int count) {
        DeckRecipe recipe = new DeckRecipe();
        new BaseGameModule().applyTo(recipe, count);
        return recipe.deckCards();
    }

    private static void assertRestOfRuleThree(List<Card> deck) {
        assertEquals(4, count(deck, AttackCard.class));
        assertEquals(4, count(deck, FavorCard.class));
        assertEquals(5, count(deck, NopeCard.class));
        assertEquals(4, count(deck, ShuffleCard.class));
        assertEquals(4, count(deck, SkipCard.class));
        assertEquals(5, count(deck, SeeTheFutureCard.class));
        assertEquals(4, count(deck, HairyPotatoCatCard.class));
        assertEquals(4, count(deck, CattermelonCard.class));
        assertEquals(4, count(deck, RainbowRalphingCatCard.class));
        assertEquals(4, count(deck, TacoCatCard.class));
        assertEquals(4, count(deck, OverweightBikiniCatCard.class));
        assertEquals(0, count(deck, ExplodingKittenCard.class));
    }

    private static int count(List<Card> cards, Class<? extends Card> type) {
        int count = 0;
        for(int i = 0; i < cards.size(); i++) {
            if(type.isInstance(cards.get(i))) {
                count++;
            }
        }
        return count;
    }
}

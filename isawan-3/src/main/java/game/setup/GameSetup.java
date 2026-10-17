package game.setup;

import java.util.List;

import game.card.Card;
import game.deck.Deck;
import game.deck.Discard;
import game.deck.ListDeck;
import game.deck.ListDiscard;
import game.player.Player;
import game.random.RandomSource;
import game.turn.RoundState;

/**
 * Sets up a game from a {@link GameConfig}: decides <em>when</em> each group of cards is
 * used, while the {@link ExpansionModule}s decide <em>which</em> cards. It never names a
 * card class or a card count, so a new expansion needs no change here. All randomness
 * (shuffles, start player) comes from the injected {@link RandomSource}, so tests can
 * control it.
 */
public class GameSetup {
    /** Cards dealt to each player at the start (rule 5). A setup value, not a hand limit. */
    private static final int CARDS_DEALT_PER_PLAYER = 7;

    private final RandomSource random;

    /** @throws IllegalArgumentException if {@code random} is null */
    public GameSetup(RandomSource random) {
        if(random == null) {
            throw new IllegalArgumentException("random cannot be null.");
        }
        this.random = random;
    }

    /**
     * Sets up a game for {@code players}, in the order of rules 2 to 8: starting cards,
     * shuffle, deal, add the cards kept out of the deal (the Exploding Kittens), shuffle
     * again, pick a random start player.
     *
     * @param players the players, already created with their I/O, seated in this order
     * @return the dealt players, the deck, an empty discard pile and the turn state
     * @throws IllegalArgumentException if {@code players} is null or its size doesn't match
     *         the config, so rule 1 can't be bypassed
     */
    public GameState setUp(GameConfig config, List<Player> players) {
        if(players == null) {
            throw new IllegalArgumentException("players cannot be null.");
        }
        if(players.size() != config.playerCount()) {
            throw new IllegalArgumentException("Expected " + config.playerCount() + " players, got " + players.size() + ".");
        }
        DeckRecipe recipe = buildRecipe(config);
        giveStartingCards(recipe, players);
        Deck deck = buildDeck(recipe);
        deal(deck, players);
        addAfterDealCards(deck, recipe);

        Discard discard = new ListDiscard();
        RoundState round = new RoundState(players, random.nextInt(players.size()));
        return new GameState(players, deck, discard, round);
    }

    /** Lets every module add its cards to one recipe, in order. */
    private DeckRecipe buildRecipe(GameConfig config) {
        DeckRecipe recipe = new DeckRecipe();
        List<ExpansionModule> modules = config.modules();
        for(int i = 0; i < modules.size(); i++) {
            modules.get(i).applyTo(recipe, config.playerCount());
        }
        return recipe;
    }

    /** Gives each player their own new starting cards (rule 2). */
    private void giveStartingCards(DeckRecipe recipe, List<Player> players) {
        for(int i = 0; i < players.size(); i++) {
            List<Card> startingCards = recipe.eachHandCards();
            for(int j = 0; j < startingCards.size(); j++) {
                players.get(i).hand().add(startingCards.get(j));
            }
        }
    }

    /** Puts the recipe's deck cards in a new deck and shuffles it (rules 3 and 4). */
    private Deck buildDeck(DeckRecipe recipe) {
        Deck deck = new ListDeck(random);
        deck.insertOnTop(recipe.deckCards());
        deck.shuffle();
        return deck;
    }

    /** Deals {@link #CARDS_DEALT_PER_PLAYER} cards from the top to each player (rule 5). */
    private void deal(Deck deck, List<Player> players) {
        for(int i = 0; i < players.size(); i++) {
            for(int j = 0; j < CARDS_DEALT_PER_PLAYER; j++) {
                players.get(i).hand().add(deck.drawTop());
            }
        }
    }

    /** Adds the cards kept out of the deal and shuffles again (rules 6 and 7). */
    private void addAfterDealCards(Deck deck, DeckRecipe recipe) {
        deck.insertOnTop(recipe.afterDealCards());
        deck.shuffle();
    }
}

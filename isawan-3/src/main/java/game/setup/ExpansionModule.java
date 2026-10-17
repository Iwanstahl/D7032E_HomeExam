package game.setup;

/**
 * One set of cards the game can be played with: the base game, or an expansion such as
 * Imploding Kittens. A module only says which cards the game is set up with, by filling
 * in a {@link DeckRecipe}. When the cards are dealt and inserted is decided by
 * {@code GameSetup}. Adding an expansion therefore means a new module and its new card
 * classes, without changing {@code GameSetup}, the turn logic or the existing cards.
 */
public interface ExpansionModule {

    /** The module's name as shown to players, e.g. in the menu. */
    public String name();

    /**
     * The fewest players this module can be played with. With several modules, the
     * game allows the smallest minimum among them (see {@link GameConfig}).
     */
    public int minPlayers();

    /**
     * The most players this module can be played with, e.g. 5 for the base game and 6
     * for Imploding Kittens. With several modules, the game allows the largest maximum
     * among them, so an expansion can raise the limit without changing the base game.
     */
    public int maxPlayers();

    /**
     * Adds this module's cards to {@code recipe}. Modules run in order on the same recipe,
     * with the base game first. {@code playerCount} is given because some counts depend
     * on it, e.g. the Defuses in the deck (rule 3a) and the Exploding Kittens (rule 6).
     */
    public void applyTo(DeckRecipe recipe, int playerCount);
}

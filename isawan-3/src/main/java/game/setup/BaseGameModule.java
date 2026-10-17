package game.setup;

import game.card.AttackCard;
import game.card.CattermelonCard;
import game.card.DefuseCard;
import game.card.ExplodingKittenCard;
import game.card.FavorCard;
import game.card.HairyPotatoCatCard;
import game.card.NopeCard;
import game.card.OverweightBikiniCatCard;
import game.card.RainbowRalphingCatCard;
import game.card.SeeTheFutureCard;
import game.card.ShuffleCard;
import game.card.SkipCard;
import game.card.TacoCatCard;

/**
 * The cards of the base game: one Defuse for every player (rule 2), the deck of rule 3,
 * and one Exploding Kitten fewer than players, added after dealing (rule 6). Rule 3a, the
 * number of Defuses in the deck, depends on the player count and lives here, next to the
 * other card counts.
 */
public class BaseGameModule implements ExpansionModule {

    @Override
    public String name() {
        return "Exploding Kittens (base game)";
    }

    /** 2 players at least. */
    @Override
    public int minPlayers() {
        return 2;
    }

    /** 5 players at most. */
    @Override
    public int maxPlayers() {
        return 5;
    }

    /** Adds the base game's cards for {@code playerCount} players to {@code recipe}. */
    @Override
    public void applyTo(DeckRecipe recipe, int playerCount) {
        recipe.addToEachHand(DefuseCard::new);

        if(playerCount <= 4) {
            recipe.addToDeck(DefuseCard::new, 2);
        } else {
            recipe.addToDeck(DefuseCard::new, 1);
        }
        recipe.addToDeck(AttackCard::new, 4);
        recipe.addToDeck(FavorCard::new, 4);
        recipe.addToDeck(NopeCard::new, 5);
        recipe.addToDeck(ShuffleCard::new, 4);
        recipe.addToDeck(SkipCard::new, 4);
        recipe.addToDeck(SeeTheFutureCard::new, 5);
        recipe.addToDeck(HairyPotatoCatCard::new, 4);
        recipe.addToDeck(CattermelonCard::new, 4);
        recipe.addToDeck(RainbowRalphingCatCard::new, 4);
        recipe.addToDeck(TacoCatCard::new, 4);
        recipe.addToDeck(OverweightBikiniCatCard::new, 4);

        recipe.addAfterDeal(ExplodingKittenCard::new, playerCount - 1);
    }
}

package game.card;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import game.turn.TurnContext;

/**
 * One card in the game. Each kind of card is its own class, holding its own rule in
 * {@link #play} and/or {@link #onDrawn} and describing itself through the flags below.
 * The turn logic only asks these flags and calls these methods and never checks which
 * card it is handling. So a new card (e.g. from an expansion) is a new class and needs
 * no change to existing code. Most cards extend {@link AbstractCard} which supplies the
 * common defaults.
 */
public interface Card {

    /** The card's name as shown to players, e.g. "Attack" or "Taco Cat". */
    public String name();

    /** True if the card can be played on its own, false for e.g. cat cards, Defuse and Nope. */
    public boolean canPlayAlone();

    /** True if other players may play Nope on this card after it is played. */
    public boolean canBeNoped();

    /**
     * True if the player must pick a target player when playing this card. The target is
     * then chosen before the Nope window so everyone knows who is targeted and the card
     * reads it from {@link TurnContext#target()}.
     */
    public boolean needsTarget();

    /** Applies the card's effect when it is played and not Noped. */
    public void play(TurnContext ctx);

    /** Reacts to being drawn, the card is already in the drawer's hand (e.g. Exploding Kitten). */
    public void onDrawn(TurnContext ctx);

    /**
     * Returns one card of each kind in {@code cards}, in the order they first appear.
     * Cards of the same class count as the same kind so three Attacks give one Attack,
     * e.g. so a menu only offers "Attack" once.
     */
    public static List<Card> oneOfEachKind(List<Card> cards) {
        Map<Class<?>, Card> kinds = new LinkedHashMap<>();
        for(int i = 0; i < cards.size(); i++) {
            kinds.putIfAbsent(cards.get(i).getClass(), cards.get(i));
        }
        return new ArrayList<>(kinds.values());
    }
}

package game.io;

/**
 * The kinds of thing that can happen in a game and be reported to players. The
 * specifics (which player, which card) go in the {@link GameEvent}'s details.
 */
public enum EventType {

    /** A player joined the game. */
    PLAYER_JOINED,

    /** A player's turn started. */
    TURN_STARTED,

    /** A player played a card or a combo. */
    CARD_PLAYED,

    /** A player played Nope on the last action. */
    NOPE_PLAYED,

    /** The time to play Nope ran out. The action goes ahead or is cancelled. */
    NOPE_WINDOW_CLOSED,

    /** A player drew a card (only the drawer is told which). */
    CARD_DRAWN,

    /** A player gave a card to another player (Favor). */
    CARD_TRANSFERRED,

    /** A player stole a card from another player (2-/3-of-a-kind). */
    CARD_STOLEN,

    /** A 3-of-a-kind named a card the target didn't have. */
    CARD_NOT_FOUND,

    /** A player was shown cards from the deck (See the Future). */
    CARDS_REVEALED,

    /** A player defused an Exploding Kitten. */
    KITTEN_DEFUSED,

    /** A player exploded and is out. */
    PLAYER_EXPLODED,

    /** One player is left and has won (rule 13). */
    PLAYER_WON
}

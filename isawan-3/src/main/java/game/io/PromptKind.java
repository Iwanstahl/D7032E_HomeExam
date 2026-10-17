package game.io;

/**
 * The kinds of question the game can ask a player. An I/O implementation uses the kind,
 * not the wording, to decide how to ask and answer, so no client has to recognise
 * keywords in the text (req. 17).
 */
public enum PromptKind {

    /** It is your turn: play a card or a combo, or end the turn by drawing. */
    CHOOSE_ACTION,

    /** Pick a player, e.g. the target of a Favor or a 2-/3-of-a-kind. */
    CHOOSE_TARGET_PLAYER,

    /** Name a card type, for a 3-of-a-kind. */
    NAME_CARD_TYPE,

    /** Someone played Favor on you: choose a card from your hand to give them. */
    CHOOSE_CARD_TO_GIVE,

    /** You defused an Exploding Kitten: choose where in the deck to put it back. */
    CHOOSE_DECK_POSITION,

    /** A card was played that can be Noped: play a Nope or let it pass. Has a timeout. */
    PLAY_NOPE
}

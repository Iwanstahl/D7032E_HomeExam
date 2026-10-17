package game.player;

import game.io.PlayerNotifier;
import game.io.PlayerResponder;

/**
 * One player's state (id, hand, whether they have exploded) plus how to reach them. The
 * notifier and responder are given in the constructor, so the game never knows whether
 * this is a console, a bot or a network player.
 */
public class Player {
    private final int playerId;
    private final Hand hand = new Hand();
    private final PlayerNotifier notifier;
    private final PlayerResponder responder;
    private boolean exploded; //Set as false by default

    /**
     * @throws IllegalArgumentException if {@code notifier} or {@code responder} is null
     */
    public Player(int playerId, PlayerNotifier notifier, PlayerResponder responder) {
        if(notifier == null) {
            throw new IllegalArgumentException("notifier cannot be null.");
        }
        if(responder == null) {
            throw new IllegalArgumentException("responder cannot be null.");
        }
        this.playerId = playerId;
        this.notifier = notifier;
        this.responder = responder;
    }

    /** The cards this player holds. */
    public Hand hand() {
        return hand;
    }

    /** How this player is told what happens in the game. */
    public PlayerNotifier notifier() {
        return notifier;
    }

    /** How this player is asked questions. */
    public PlayerResponder responder() {
        return responder;
    }

    /** Marks this player as out of the game. */
    public void explode() {
        exploded = true;
    }

    /** Returns true if this player has exploded and is out of the game. */
    public boolean isExploded() {
        return exploded;
    }

    public int playerId() {
        return playerId;
    }

}

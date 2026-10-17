package game.io;

import java.io.Serializable;

import java.util.Map;

/**
 * Something that happened in the game, reported to players: what kind of event it is,
 * plus the details, e.g. which player exploded or which card was drawn. Plain data,
 * turning it into text is up to the I/O implementation.
 */
public final class GameEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final EventType type;
    private final Map<String, String> details;

    /**
     * Creates an event. A null {@code details} is treated as no details.
     *
     * @throws IllegalArgumentException if {@code type} is null
     */
    public GameEvent(EventType type, Map<String, String> details) {
        if(type == null) {
            throw new IllegalArgumentException("type cannot be null.");
        }
        this.type = type;
        if(details == null) {
            this.details = Map.of();
        } else {
            this.details = Map.copyOf(details);
        }
    }

    /** What kind of event this is. */
    public EventType type() {
        return type;
    }

    /** The event's details, e.g. {@code "player" -> "2"}. Read-only. */
    public Map<String, String> details() {
        return details;
    }

    /** Returns the type and details, e.g. "PLAYER_EXPLODED {player=2}", for test failures and logs. */
    @Override
    public String toString() {
        return type + " " + details;
    }
}

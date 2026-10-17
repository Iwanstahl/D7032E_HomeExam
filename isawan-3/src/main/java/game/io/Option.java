package game.io;

import java.io.Serializable;

/** One possible answer to a {@link Prompt}. */
public final class Option implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String label;

    /**
     * @throws IllegalArgumentException if {@code id} is negative or {@code label} is
     *         null or blank
     */
    public Option(int id, String label) {
        if(id < 0) {
            throw new IllegalArgumentException("id cannot be negative, was " + id + ".");
        }
        if(label == null || label.isBlank()) {
            throw new IllegalArgumentException("label cannot be null or blank.");
        }
        this.id = id;
        this.label = label;
    }

    /**
     * Identifies this option within its prompt, e.g. its position in the list. Chosen by
     * whoever builds the prompt, who uses it to look up what the option means.
     */
    public int id() {
        return id;
    }

    /** Short text to show for this option, e.g. "Attack" or "Player 2". */
    public String label() {
        return label;
    }

    /** Returns the label, so test failures and logs show which option it is. */
    @Override
    public String toString() {
        return label;
    }
}

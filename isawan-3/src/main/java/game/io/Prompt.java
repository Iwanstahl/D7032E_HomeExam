package game.io;

import java.io.Serializable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * One question to a player: what kind it is, the options they may pick from, context
 * to show with it, and an optional time limit. A timed prompt has a default option
 * which is the answer when time runs out, so an answer is never missing.
 */
public final class Prompt implements Serializable {
    private static final long serialVersionUID = 1L;

    private final PromptKind kind;
    private final List<Option> options;
    private final Map<String, String> context;
    private final int timeoutSeconds;
    private final Option defaultOption;

    /** Creates a prompt with no time limit. */
    public Prompt(PromptKind kind, List<Option> options, Map<String, String> context) {
        this(kind, options, context, 0, null);
    }

    /**
     * Creates a prompt, {@code timeoutSeconds} 0 means no time limit.
     */
    public Prompt(PromptKind kind, List<Option> options, Map<String, String> context,
                  int timeoutSeconds, Option defaultOption) {
        if(kind == null) {
            throw new IllegalArgumentException("kind cannot be null.");
        }
        checkOptions(options);
        checkTimeout(timeoutSeconds, defaultOption, options);

        this.kind = kind;
        this.options = List.copyOf(options);
        if(context == null) {
            this.context = Map.of();
        } else {
            this.context = Map.copyOf(context);
        }
        this.timeoutSeconds = timeoutSeconds;
        this.defaultOption = defaultOption;
    }

    public PromptKind kind() {
        return kind;
    }

    /** The options to pick from, in display order. Read-only. */
    public List<Option> options() {
        return options;
    }

    /** Extra information to show with the question, e.g. the player's hand. Read-only. */
    public Map<String, String> context() {
        return context;
    }

    /** Seconds to answer, or 0 for no time limit. */
    public int timeoutSeconds() {
        return timeoutSeconds;
    }

    /** Returns true if this prompt has a time limit. */
    public boolean hasTimeout() {
        return timeoutSeconds > 0;
    }

    /** The answer used when time runs out, or null if the prompt has no time limit. */
    public Option defaultOption() {
        return defaultOption;
    }

    /**
     * Returns the option with the given id, e.g. when a console player types a number.
     * @throws IllegalArgumentException if no option has that id
     */
    public Option option(int id) {
        for(int i = 0; i < options.size(); i++) {
            if(options.get(i).id() == id) {
                return options.get(i);
            }
        }
        throw new IllegalArgumentException("No option with id " + id + ".");
    }

    /** Options must exist, and no two may share an id, since answers are looked up by id. */
    private static void checkOptions(List<Option> options) {
        if(options == null || options.isEmpty()) {
            throw new IllegalArgumentException("options cannot be null or empty.");
        }
        Set<Integer> seenIds = new HashSet<>();
        for(int i = 0; i < options.size(); i++) {
            int id = options.get(i).id();
            if(!seenIds.add(id)) {
                throw new IllegalArgumentException("Two options share id " + id + ".");
            }
        }
    }

    /**
     * The time limit can't be negative, a time limit needs a default answer, and the
     * default must be one of the options. Assumes {@code options} was already checked.
     */
    private static void checkTimeout(int timeoutSeconds, Option defaultOption, List<Option> options) {
        if(timeoutSeconds < 0) {
            throw new IllegalArgumentException("timeoutSeconds cannot be negative, was " + timeoutSeconds + ".");
        }
        if(timeoutSeconds > 0 && defaultOption == null) {
            throw new IllegalArgumentException("A prompt with a time limit needs a defaultOption.");
        }
        if(defaultOption != null && !options.contains(defaultOption)) {
            throw new IllegalArgumentException("defaultOption must be one of the options.");
        }
    }
}

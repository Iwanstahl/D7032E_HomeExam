package game.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;

import game.io.Option;
import game.io.GameEvent;
import game.io.PlayerNotifier;
import game.io.PlayerResponder;
import game.io.Prompt;

/**
 * A player at a keyboard: prints events and prompts, and reads the chosen option's
 * number. Used for the host at the server and by the network client. The keyboard and
 * screen are passed in, so tests can use fake ones.
 *
 * A timed prompt (the Nope window) polls: until the deadline, it
 * reads a line if one has been typed, otherwise it sleeps briefly. When time is up it
 * returns the default option. A blocking read can't be interrupted, which is why it
 * doesn't read on a separate thread.
 */
public class ConsolePlayerIO implements PlayerNotifier, PlayerResponder {
    /** How often a timed prompt checks whether a line has been typed. */
    private static final int POLL_MILLIS = 100;

    private final BufferedReader in;
    private final PrintStream out;
    private final MessageFormatter formatter;

    /** @throws IllegalArgumentException if any argument is null */
    public ConsolePlayerIO(BufferedReader in, PrintStream out, MessageFormatter formatter) {
        if(in == null) {
            throw new IllegalArgumentException("in cannot be null.");
        }
        if(out == null) {
            throw new IllegalArgumentException("out cannot be null.");
        }
        if(formatter == null) {
            throw new IllegalArgumentException("formatter cannot be null.");
        }
        this.in = in;
        this.out = out;
        this.formatter = formatter;
    }

    /** Prints the event as a sentence. */
    @Override
    public void onEvent(GameEvent event) {
        out.println(formatter.format(event));
    }

    /**
     * Prints the prompt and reads the answer. A wrong answer is asked again.
     *
     * @throws IllegalStateException if the console is closed while waiting for an untimed answer
     * @throws java.io.UncheckedIOException if the console can't be read
     */
    @Override
    public Option ask(Prompt prompt) {
        out.println(formatter.format(prompt));
        if(prompt.hasTimeout()) {
            return askTimed(prompt);
        }
        return askUntimed(prompt);
    }

    /** Reads lines until one is a valid option. */
    private Option askUntimed(Prompt prompt) {
        while(true) {
            String line = readLine();
            if(line == null) {
                throw new IllegalStateException("The console input was closed.");
            }
            Option chosen = parse(line, prompt);
            if(chosen != null) {
                return chosen;
            }
            out.println("Not a valid option, try again.");
        }
    }

    /** Reads lines until one is a valid option or the time is up, then the default option. */
    private Option askTimed(Prompt prompt) {
        long deadline = System.nanoTime() + prompt.timeoutSeconds() * 1_000_000_000L;
        while(System.nanoTime() < deadline) {
            if(isLineReady()) {
                String line = readLine();
                if(line == null) {
                    return prompt.defaultOption();
                }
                Option chosen = parse(line, prompt);
                if(chosen != null) {
                    return chosen;
                }
                out.println("Not a valid option, try again.");
            } else {
                sleep(POLL_MILLIS);
            }
        }
        out.println("Time is up: " + prompt.defaultOption().label() + ".");
        return prompt.defaultOption();
    }

    /** The option whose id was typed, or null if the line isn't one of the ids. */
    private Option parse(String line, Prompt prompt) {
        int id;
        try {
            id = Integer.parseInt(line.trim());
        } catch(NumberFormatException e) {
            return null;
        }
        try {
            return prompt.option(id);
        } catch(IllegalArgumentException notAnOption) {
            return null;
        }
    }

    private String readLine() {
        try {
            return in.readLine();
        } catch(IOException e) {
            throw new UncheckedIOException("Could not read from the console.", e);
        }
    }

    private boolean isLineReady() {
        try {
            return in.ready();
        } catch(IOException e) {
            throw new UncheckedIOException("Could not read from the console.", e);
        }
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

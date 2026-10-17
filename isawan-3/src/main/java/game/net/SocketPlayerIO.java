package game.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;

import game.io.Option;
import game.io.GameEvent;
import game.io.PlayerNotifier;
import game.io.PlayerResponder;
import game.io.Prompt;

/**
 * A player connected over the network, as the server sees them. Events and prompts are
 * sent as objects and the answer comes back as the chosen {@link Option}, so the client
 * never has to search text for keywords (req. 17). The client enforces a
 * prompt's time limit.
 */
public class SocketPlayerIO implements PlayerNotifier, PlayerResponder, AutoCloseable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    /**
     * Opens the object streams on {@code socket}. The output stream is created and flushed
     * first: creating an input stream waits for the other side's stream header, so if both
     * sides started with input they would wait for each other forever.
     *
     * @throws IOException if the streams can't be opened
     */
    public SocketPlayerIO(Socket socket) throws IOException {
        if(socket == null) {
            throw new IllegalArgumentException("socket cannot be null.");
        }
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    /** @throws java.io.UncheckedIOException if the connection is lost */
    @Override
    public synchronized void onEvent(GameEvent event) {
        try {
            send(event);
        } catch(IOException e) {
            throw new UncheckedIOException("Lost the connection to a player.", e);
        }
    }

    /**
     * Sends the prompt and waits for the client's answer.
     *
     * @return the prompt's own option with the id the client chose
     * @throws java.io.UncheckedIOException if the connection is lost
     * @throws IllegalStateException if the client sends something other than an offered option
     */
    @Override
    public synchronized Option ask(Prompt prompt) {
        Object answer;
        try {
            send(prompt);
            answer = in.readObject();
        } catch(IOException e) {
            throw new UncheckedIOException("Lost the connection to a player.", e);
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException("A player sent something that is not an answer.", e);
        }
        if(!(answer instanceof Option)) {
            throw new IllegalStateException("A player sent something that is not an answer: " + answer);
        }
        int id = ((Option) answer).id();
        try {
            return prompt.option(id);
        } catch(IllegalArgumentException notOffered) {
            throw new IllegalStateException("A player answered with option " + id + ", which was not offered.", notOffered);
        }
    }

    /**
     * Writes one message. {@code reset()} makes the stream forget what it has sent.
     * Otherwise it keeps a reference to every object for the whole game, and an object
     * sent twice would arrive as the first copy.
     */
    private void send(Object message) throws IOException {
        out.writeObject(message);
        out.reset();
        out.flush();
    }

    /** Closes the connection. */
    @Override
    public void close() throws IOException {
        socket.close();
    }
}

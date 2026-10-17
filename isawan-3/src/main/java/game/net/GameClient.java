package game.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;

import game.console.ConsolePlayerIO;
import game.io.Option;
import game.io.EventType;
import game.io.GameEvent;
import game.io.Prompt;

/**
 * The client program: connects to the server and acts as a {@link ConsolePlayerIO} at the
 * other end of the socket. It decides what to do from the type of each object it receives
 * (an event is printed, a prompt is answered), never from keywords in text (req. 17),
 * and it contains no game rules.
 */
public class GameClient {
    private final String host;
    private final int port;
    private final ConsolePlayerIO console;
    private final PrintStream log;

    /** @throws IllegalArgumentException if {@code host} is blank or any other argument is null */
    public GameClient(String host, int port, ConsolePlayerIO console, PrintStream log) {
        if(host == null || host.isBlank()) {
            throw new IllegalArgumentException("host cannot be null or blank.");
        }
        if(console == null) {
            throw new IllegalArgumentException("console cannot be null.");
        }
        if(log == null) {
            throw new IllegalArgumentException("log cannot be null.");
        }
        this.host = host;
        this.port = port;
        this.console = console;
        this.log = log;
    }

    /**
     * Connects and plays until the game is won or the server closes the connection.
     * @throws IOException if the server can't be reached or sends something unreadable
     */
    public void run() throws IOException {
        try(Socket socket = new Socket(host, port)) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            while(true) {
                Object message = readMessage(in);
                if(message instanceof GameEvent) {
                    GameEvent event = (GameEvent) message;
                    console.onEvent(event);
                    if(event.type() == EventType.PLAYER_WON) {
                        return;
                    }
                } else if(message instanceof Prompt) {
                    Option answer = console.ask((Prompt) message);
                    out.writeObject(answer);
                    out.flush();
                } else {
                    log.println("Ignored an unknown message from the server: " + message);
                }
            }
        } catch(EOFException e) {
            log.println("The server closed the connection.");
        }
    }

    private static Object readMessage(ObjectInputStream in) throws IOException {
        try {
            return in.readObject();
        } catch(ClassNotFoundException e) {
            throw new IOException("The server sent a message this client does not understand.", e);
        }
    }
}

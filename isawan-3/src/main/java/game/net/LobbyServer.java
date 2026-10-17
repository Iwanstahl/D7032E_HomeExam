package game.net;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import game.io.EventType;
import game.io.GameEvent;

/**
 * Waits for the network players to connect before a game starts: opens a server
 * socket, accepts one connection per player, tells each client its
 * player number, and logs who connected.
 */
public class LobbyServer {
    /** The port the server listens on and the client connects to. */
    public static final int PORT = 2048;

    private final int port;
    private final PrintStream log;

    /** @throws IllegalArgumentException if {@code log} is null */
    public LobbyServer(int port, PrintStream log) {
        if(log == null) {
            throw new IllegalArgumentException("log cannot be null.");
        }
        this.port = port;
        this.log = log;
    }

    /**
     * Waits until {@code count} clients have connected. With 0 clients no port is opened.
     */
    public List<SocketPlayerIO> acceptClients(int count, int firstPlayerId) throws IOException {
        List<SocketPlayerIO> clients = new ArrayList<>();
        if(count == 0) {
            return clients;
        }
        try(ServerSocket server = new ServerSocket(port)) {
            for(int i = 0; i < count; i++) {
                int playerId = firstPlayerId + i;
                log.println("Waiting for player " + playerId + " to connect on port " + port + "...");
                Socket socket = server.accept();
                SocketPlayerIO client = new SocketPlayerIO(socket);
                client.onEvent(new GameEvent(EventType.PLAYER_JOINED, Map.of("player", String.valueOf(playerId))));
                clients.add(client);
                log.println("Connected to player " + playerId + ".");
            }
        }
        return clients;
    }
}

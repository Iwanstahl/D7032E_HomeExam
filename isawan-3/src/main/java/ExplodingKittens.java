import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import game.bot.BotPlayerIO;
import game.console.ConsolePlayerIO;
import game.console.MessageFormatter;
import game.net.GameClient;
import game.net.LobbyServer;
import game.net.SocketPlayerIO;
import game.player.Player;
import game.random.DefaultRandomSource;
import game.random.RandomSource;
import game.setup.BaseGameModule;
import game.setup.ExpansionModule;
import game.setup.GameConfig;
import game.setup.GameSetup;
import game.setup.GameState;
import game.turn.CardComboAction;
import game.turn.GameActions;
import game.turn.GameOrchestrator;
import game.turn.NopeTimeWindow;
import game.turn.TurnManager;

/**
 * Starts the program as a server or a client, depending on the command line.
 * It is the composition root: the only place that picks the concrete classes and wires
 * them together. Everything else receives what it needs through its constructor.
 */
public class ExplodingKittens {
    /** Time to play a Nope, in seconds. */
    private static final int NOPE_SECONDS = 5;

    /** Two arguments start the server, one starts the client. Anything else prints how to use it. */
    public static void main(String[] args) {
        if(args.length == 2) {
            int humans;
            int bots;
            try {
                humans = Integer.parseInt(args[0]);
                bots = Integer.parseInt(args[1]);
            } catch(NumberFormatException e) {
                printUsage();
                return;
            }
            runServer(humans, bots);
        } else if(args.length == 1) {
            runClient(args[0]);
        } else {
            printUsage();
        }
    }

    /**
     * Seats the players (you as player 1, then the bots, then the
     * network players as they connect), plays the game and closes the connections.
     */
    private static void runServer(int humans, int bots) {
        if(humans < 1) {
            System.out.println("There must be at least 1 human player: you, at this computer.");
            printUsage();
            return;
        }
        if(bots < 0) {
            System.out.println("The number of bots can't be negative.");
            printUsage();
            return;
        }
        GameConfig config;
        try {
            List<ExpansionModule> modules = List.of(new BaseGameModule());
            config = new GameConfig(humans + bots, modules);
        } catch(IllegalArgumentException e) {
            System.out.println(e.getMessage());
            printUsage();
            return;
        }

        MessageFormatter formatter = new MessageFormatter();
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        ConsolePlayerIO host = new ConsolePlayerIO(keyboard, System.out, formatter);

        List<Player> players = new ArrayList<>();
        players.add(new Player(1, host, host));
        for(int i = 0; i < bots; i++) {
            BotPlayerIO bot = new BotPlayerIO();
            players.add(new Player(2 + i, bot, bot));
        }

        List<SocketPlayerIO> clients;
        try {
            LobbyServer lobby = new LobbyServer(LobbyServer.PORT, System.out);
            clients = lobby.acceptClients(humans - 1, 2 + bots);
        } catch(IOException e) {
            System.out.println("Could not start the server: " + e.getMessage());
            return;
        }
        for(int i = 0; i < clients.size(); i++) {
            players.add(new Player(2 + bots + i, clients.get(i), clients.get(i)));
        }

        try {
            playGame(config, players);
        } catch(RuntimeException e) {
            System.out.println("The game stopped: " + e.getMessage());
        } finally {
            closeAll(clients);
        }
    }

    /** Sets up the game, wires the turn logic together and plays until someone wins. */
    private static Player playGame(GameConfig config, List<Player> players) {
        RandomSource random = new DefaultRandomSource();
        GameState state = new GameSetup(random).setUp(config, players);
        GameActions actions = new GameActions(players, state.deck(), state.discard());
        CardComboAction combos = new CardComboAction(players, state.deck(), actions, random);
        NopeTimeWindow nope = new NopeTimeWindow(NOPE_SECONDS, players, state.discard(), actions);
        TurnManager turnManager = new TurnManager(state.round(), state.deck(), state.discard(),
                actions, nope, combos);
        return new GameOrchestrator(state.round(), turnManager, actions).run();
    }

    /** Connects to the server at {@code host} and plays from this keyboard. */
    private static void runClient(String host) {
        MessageFormatter formatter = new MessageFormatter();
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        ConsolePlayerIO console = new ConsolePlayerIO(keyboard, System.out, formatter);
        GameClient client = new GameClient(host, LobbyServer.PORT, console, System.out);
        try {
            client.run();
        } catch(IOException e) {
            System.out.println("Could not connect to the server at " + host + ": " + e.getMessage());
        } catch(RuntimeException e) {
            System.out.println("The client stopped: " + e.getMessage());
        }
    }

    private static void closeAll(List<SocketPlayerIO> clients) {
        for(int i = 0; i < clients.size(); i++) {
            try {
                clients.get(i).close();
            } catch(IOException e) {
                System.out.println("Could not close a connection: " + e.getMessage());
            }
        }
    }

    private static void printUsage() {
        System.out.println("Server: java ExplodingKittens <humans> <bots>");
        System.out.println("        e.g. java ExplodingKittens 2 1 (you, 1 online player and 1 bot)");
        System.out.println("Client: java ExplodingKittens <server IP>");
        System.out.println("        e.g. java ExplodingKittens 127.0.0.1");
        System.out.println("The base game has 2 to 5 players in total; you play at the server as player 1.");
    }
}

package game.turn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import game.card.NopeCard;
import game.deck.Discard;
import game.io.Option;
import game.io.EventType;
import game.io.GameEvent;
import game.io.Prompt;
import game.io.PromptKind;
import game.player.Player;

/**
 * The short window after a card is played in which other players may Nope it (rule 12).
 * A Nope can itself be Noped ("Yup"), so the window runs in rounds. In each round every
 * player who is still in the game, holds a Nope and didn't act last is asked once. They
 * are all asked at the same time, so a round takes one time limit however many players
 * are asked. If anyone answers "Nope", one Nope card is spent by the first such player in
 * seat order. The outcome then flips and a new round starts so the others can answer
 * back. The window closes when a round passes with no Nope. An even number of Nopes lets
 * the card go ahead and an odd number cancels it.
 *
 * The window only decides whether the card goes ahead. It doesn't know what the card
 * does. The time limit is passed to players in each {@link Prompt}. Waiting is up to the
 * I/O implementation, and an unanswered prompt counts as "Don't Nope". Each round uses
 * its own thread pool, which is always closed before the round ends. The outcome is
 * tracked in a plain boolean and never inferred from the discard pile.
 */
public class NopeTimeWindow {
    private static final int NOPE = 0;
    private static final int DONT_NOPE = 1;

    private final int seconds;
    private final List<Player> players;
    private final Discard discard;
    private final GameActions actions;

    /**
     * @param seconds how long each player has to answer, e.g. 5
     * @param players everyone at the table, in seat order, copied
     * @param discard where spent Nope cards go
     * @param actions used to tell everyone about each Nope and the outcome
     * @throws IllegalArgumentException if {@code seconds} is less than 1 or any other
     *         argument is null
     */
    public NopeTimeWindow(int seconds, List<Player> players, Discard discard, GameActions actions) {
        if(seconds < 1) {
            throw new IllegalArgumentException("seconds must be at least 1, was " + seconds + ".");
        }
        if(players == null || discard == null || actions == null) {
            throw new IllegalArgumentException("players, discard and actions cannot be null.");
        }
        this.seconds = seconds;
        this.players = List.copyOf(players);
        this.discard = discard;
        this.actions = actions;
    }

    /**
     * Runs the window for what {@code playedBy} just played, described for players by
     * {@code played}, e.g. "Favor" or "Two Taco Cat".
     *
     * @return true if it goes ahead, false if it was Noped
     */
    public boolean resolve(String played, Player playedBy) {
        boolean cancelled = false;
        Player lastToAct = playedBy;
        Player noper = askRound(played, playedBy, lastToAct, cancelled);
        while(noper != null) {
            discard.addToTop(noper.hand().removeType(NopeCard.class));
            cancelled = !cancelled;
            lastToAct = noper;
            actions.broadcast(new GameEvent(EventType.NOPE_PLAYED, Map.of(
                    "player", String.valueOf(noper.playerId()),
                    "cancelled", String.valueOf(cancelled))));
            noper = askRound(played, playedBy, lastToAct, cancelled);
        }
        actions.broadcast(new GameEvent(EventType.NOPE_WINDOW_CLOSED, Map.of(
                "card", played,
                "cancelled", String.valueOf(cancelled))));
        return !cancelled;
    }

    /**
     * Asks every eligible player at the same time, each on its own thread, and waits until
     * all have answered (or their time ran out). Returns the first player in seat order who
     * answered "Nope", or null if nobody did.
     */
    private Player askRound(String played, Player playedBy, Player lastToAct, boolean cancelled) {
        List<Player> eligible = new ArrayList<>();
        for(int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if(p != lastToAct && !p.isExploded() && p.hand().containsType(NopeCard.class)) {
                eligible.add(p);
            }
        }
        if(eligible.isEmpty()) {
            return null;
        }

        Prompt prompt = nopePrompt(played, playedBy, cancelled);
        List<Future<Option>> answers = new ArrayList<>();
        // close() at the end of the try waits for every answer, then shuts the pool down.
        try(ExecutorService pool = Executors.newFixedThreadPool(eligible.size())) {
            for(int i = 0; i < eligible.size(); i++) {
                Player p = eligible.get(i);
                answers.add(pool.submit(() -> p.responder().ask(prompt)));
            }
        }

        for(int i = 0; i < answers.size(); i++) {
            if(answerOf(answers.get(i)).id() == NOPE) {
                return eligible.get(i);
            }
        }
        return null;
    }

    /**
     * The answer of a finished prompt. If the player's I/O threw, the same exception is
     * thrown here, as if they had been asked on this thread.
     */
    private static Option answerOf(Future<Option> answer) {
        try {
            return answer.get();
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for a Nope answer.", e);
        } catch(ExecutionException e) {
            if(e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new IllegalStateException("A player could not answer the Nope prompt.", e.getCause());
        }
    }

    private Prompt nopePrompt(String played, Player playedBy, boolean cancelled) {
        Option nope = new Option(NOPE, "Nope");
        Option dontNope = new Option(DONT_NOPE, "Don't Nope");
        Map<String, String> context = Map.of(
                "card", played,
                "by", String.valueOf(playedBy.playerId()),
                "cancelled", String.valueOf(cancelled));
        return new Prompt(PromptKind.PLAY_NOPE, List.of(nope, dontNope), context, seconds, dontNope);
    }
}

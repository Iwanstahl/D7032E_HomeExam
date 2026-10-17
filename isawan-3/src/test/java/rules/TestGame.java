package rules;

import java.util.ArrayList;
import java.util.List;

import game.card.Card;
import game.deck.Deck;
import game.deck.Discard;
import game.deck.ListDeck;
import game.player.Player;
import game.turn.CardComboAction;
import game.turn.GameActions;
import game.turn.GameOrchestrator;
import game.turn.NopeTimeWindow;
import game.turn.RoundState;
import game.turn.TurnManager;

public class TestGame {
    public static final int NOPE_SECONDS = 5;

    private final FixedRandom random;
    private final List<ScriptedPlayer> scripts = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();
    private final Deck deck;
    private final RecordingDiscard discard = new RecordingDiscard();
    private final RoundState round;
    private final TurnManager turns;
    private final GameOrchestrator orchestrator;

    public TestGame(ScriptedPlayer... scripts) {
        this(new FixedRandom(), scripts);
    }

    public TestGame(FixedRandom random, ScriptedPlayer... scripts) {
        this.random = random;
        for(int i = 0; i < scripts.length; i++) {
            this.scripts.add(scripts[i]);
            this.players.add(new Player(i + 1, scripts[i], scripts[i]));
        }
        this.deck = new ListDeck(random);
        this.round = new RoundState(players, 0);
        GameActions actions = new GameActions(players, deck, discard);
        NopeTimeWindow nope = new NopeTimeWindow(NOPE_SECONDS, players, discard, actions);
        CardComboAction combos = new CardComboAction(players, deck, actions, random);
        this.turns = new TurnManager(round, deck, discard, actions, nope, combos);
        this.orchestrator = new GameOrchestrator(round, turns, actions);
    }

    public void deckFromTop(Card... cards) {
        deck.insertOnTop(List.of(cards));
    }

    public void give(int seat, Card... cards) {
        for(int i = 0; i < cards.length; i++) {
            players.get(seat).hand().add(cards[i]);
        }
    }

    public Player player(int seat) {
        return players.get(seat);
    }

    public ScriptedPlayer script(int seat) {
        return scripts.get(seat);
    }

    public List<Card> deckCards() {
        if(deck.size() == 0) {
            return new ArrayList<>();
        }
        return deck.peekTop(deck.size());
    }

    public List<Card> discarded() {
        return discard.cards();
    }

    public FixedRandom random() {
        return random;
    }

    public RoundState round() {
        return round;
    }

    public TurnManager turns() {
        return turns;
    }

    public GameOrchestrator orchestrator() {
        return orchestrator;
    }

    private static class RecordingDiscard implements Discard {
        private final List<Card> cards = new ArrayList<>();

        @Override
        public void addToTop(Card c) {
            cards.add(0, c);
        }

        @Override
        public Card peekTop() {
            if(cards.isEmpty()) {
                throw new IllegalStateException("The discard pile is empty.");
            }
            return cards.get(0);
        }

        List<Card> cards() {
            return new ArrayList<>(cards);
        }
    }
}

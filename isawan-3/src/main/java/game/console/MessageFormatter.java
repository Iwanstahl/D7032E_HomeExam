package game.console;

import java.util.List;
import java.util.Map;

import game.io.GameEvent;
import game.io.Option;
import game.io.Prompt;

/**
 * Turns events and prompts into the text players read. It is the only class with
 * player-facing sentences so changing a message never touches the game logic and the
 * network client doesn't depend on any wording (req. 17). Both {@code switch}es
 * list every enum constant and have no {@code default} so a new event type or prompt
 * kind doesn't compile until it gets a sentence here.
 */
public class MessageFormatter {

    /** One sentence describing {@code event}, e.g. "Player 2 exploded!". */
    public String format(GameEvent event) {
        Map<String, String> d = event.details();
        return switch(event.type()) {
            case PLAYER_JOINED -> "You joined the game as player " + d.get("player") + ".";
            case TURN_STARTED -> turnStarted(d);
            case CARD_PLAYED -> cardPlayed(d);
            case NOPE_PLAYED -> nopePlayed(d);
            case NOPE_WINDOW_CLOSED -> nopeWindowClosed(d);
            case CARD_DRAWN -> "You drew: " + d.get("card");
            case CARD_TRANSFERRED -> "Player " + d.get("from") + " gave " + d.get("card")
                    + " to player " + d.get("to") + ".";
            case CARD_STOLEN -> "Player " + d.get("to") + " stole " + d.get("card")
                    + " from player " + d.get("from") + ".";
            case CARD_NOT_FOUND -> "Player " + d.get("player") + " did not have any " + d.get("card") + ".";
            case CARDS_REVEALED -> "The top cards of the deck are: " + d.get("cards");
            case KITTEN_DEFUSED -> "Player " + d.get("player") + " defused an Exploding Kitten!";
            case PLAYER_EXPLODED -> "Player " + d.get("player") + " exploded!";
            case PLAYER_WON -> "Player " + d.get("player") + " has won the game!";
        };
    }

    /**
     * The question, the options as numbered lines ("  0: Attack") and what to type. A timed
     * prompt also shows the time limit and the default answer.
     */
    public String format(Prompt prompt) {
        StringBuilder text = new StringBuilder();
        text.append(question(prompt)).append("\n");
        List<Option> options = prompt.options();
        for(int i = 0; i < options.size(); i++) {
            Option o = options.get(i);
            text.append("  ").append(o.id()).append(": ").append(o.label()).append("\n");
        }
        if(prompt.hasTimeout()) {
            text.append("Type a number within ").append(prompt.timeoutSeconds())
                    .append(" seconds (no answer = ").append(prompt.defaultOption().label()).append(").");
        } else {
            text.append("Type a number:");
        }
        return text.toString();
    }

    private String question(Prompt prompt) {
        Map<String, String> c = prompt.context();
        return switch(prompt.kind()) {
            case CHOOSE_ACTION -> "Your hand: " + c.get("hand") + "\n"
                    + "Turns left to take: " + c.get("turnsOwed") + "\n"
                    + "What do you want to do?";
            case CHOOSE_TARGET_PLAYER -> "Choose a player:";
            case NAME_CARD_TYPE -> "Name a card to take:";
            case CHOOSE_CARD_TO_GIVE -> "Choose a card to give away:";
            case CHOOSE_DECK_POSITION -> "Where in the deck do you want to put the Exploding Kitten?";
            case PLAY_NOPE -> nopeQuestion(c);
        };
    }

    private String nopeQuestion(Map<String, String> c) {
        if(c.get("cancelled").equals("true")) {
            return "The " + c.get("card") + " played by player " + c.get("by")
                    + " is cancelled. Play a Nope to let it go ahead again?";
        }
        return "Player " + c.get("by") + " played " + c.get("card") + ". Play a Nope to stop it?";
    }

    private String turnStarted(Map<String, String> d) {
        String text = "It is now the turn of player " + d.get("player");
        if(!d.get("turnsOwed").equals("1")) {
            text += " (" + d.get("turnsOwed") + " turns to take)";
        }
        return text + ".";
    }

    private String cardPlayed(Map<String, String> d) {
        String text = "Player " + d.get("player") + " played " + d.get("card");
        if(d.containsKey("target")) {
            text += " against player " + d.get("target");
        }
        return text + ".";
    }

    private String nopePlayed(Map<String, String> d) {
        if(d.get("cancelled").equals("true")) {
            return "Player " + d.get("player") + " played Nope!";
        }
        return "Player " + d.get("player") + " played Nope on the Nope (Yup)!";
    }

    private String nopeWindowClosed(Map<String, String> d) {
        if(d.get("cancelled").equals("true")) {
            return "The " + d.get("card") + " was Noped and has no effect.";
        }
        return "The time to play Nope is over: the " + d.get("card") + " goes ahead.";
    }
}

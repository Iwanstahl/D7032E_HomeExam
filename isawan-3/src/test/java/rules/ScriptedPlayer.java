package rules;

import java.util.ArrayList;
import java.util.List;

import game.io.EventType;
import game.io.GameEvent;
import game.io.Option;
import game.io.PlayerNotifier;
import game.io.PlayerResponder;
import game.io.Prompt;
import game.io.PromptKind;

public class ScriptedPlayer implements PlayerNotifier, PlayerResponder {
    private final List<String> answers = new ArrayList<>();
    private final List<GameEvent> events = new ArrayList<>();
    private final List<Prompt> prompts = new ArrayList<>();

    public ScriptedPlayer thenChoose(String label) {
        answers.add(label);
        return this;
    }

    public ScriptedPlayer thenChoose(String label, int times) {
        for(int i = 0; i < times; i++) {
            answers.add(label);
        }
        return this;
    }

    @Override
    public synchronized void onEvent(GameEvent event) {
        events.add(event);
    }

    @Override
    public synchronized Option ask(Prompt prompt) {
        prompts.add(prompt);
        if(answers.isEmpty()) {
            if(prompt.hasTimeout()) {
                return prompt.defaultOption();
            }
            throw new IllegalStateException("No scripted answer left for " + prompt.kind()
                    + ", options were " + prompt.options());
        }
        String label = answers.remove(0);
        List<Option> options = prompt.options();
        for(int i = 0; i < options.size(); i++) {
            if(options.get(i).label().equals(label)) {
                return options.get(i);
            }
        }
        throw new IllegalStateException("Scripted answer \"" + label + "\" was not offered for "
                + prompt.kind() + ", options were " + options);
    }

    public synchronized List<GameEvent> events() {
        return new ArrayList<>(events);
    }

    public synchronized List<GameEvent> events(EventType type) {
        List<GameEvent> matching = new ArrayList<>();
        for(int i = 0; i < events.size(); i++) {
            if(events.get(i).type() == type) {
                matching.add(events.get(i));
            }
        }
        return matching;
    }

    public synchronized List<Prompt> prompts() {
        return new ArrayList<>(prompts);
    }

    public synchronized List<Prompt> prompts(PromptKind kind) {
        List<Prompt> matching = new ArrayList<>();
        for(int i = 0; i < prompts.size(); i++) {
            if(prompts.get(i).kind() == kind) {
                matching.add(prompts.get(i));
            }
        }
        return matching;
    }

    public synchronized boolean scriptUsedUp() {
        return answers.isEmpty();
    }
}

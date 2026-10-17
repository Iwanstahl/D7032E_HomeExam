package rules;

import java.util.ArrayList;
import java.util.List;

import game.random.RandomSource;

public class FixedRandom implements RandomSource {
    private final List<Integer> numbers = new ArrayList<>();
    private final List<Integer> shuffledSizes = new ArrayList<>();

    public FixedRandom(int... numbers) {
        for(int i = 0; i < numbers.length; i++) {
            this.numbers.add(numbers[i]);
        }
    }

    @Override
    public int nextInt(int bound) {
        if(numbers.isEmpty()) {
            return 0;
        }
        int next = numbers.remove(0);
        if(next < 0 || next >= bound) {
            throw new IllegalStateException("FixedRandom was given " + next
                    + ", but the caller asked for a number from 0 to " + (bound - 1) + ".");
        }
        return next;
    }

    @Override
    public void shuffle(List<?> list) {
        shuffledSizes.add(list.size());
    }

    public int shuffleCount() {
        return shuffledSizes.size();
    }

    public List<Integer> shuffledSizes() {
        return new ArrayList<>(shuffledSizes);
    }
}

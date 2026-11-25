package net.gazeplay.games.math101;

import net.gazeplay.commons.random.ReplayablePseudoRandom;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The operations of a game of "Apprends les Maths", without repetition (tester's feedback of 23/11/2026, point 62): an
 * operation does not come back before all those of the topic and level have been asked (3 + 4 and 4 + 3 are the same
 * one), and the operations around the same numbers (3 + 4, 7 - 3, 7 - 4) do not follow each other closely. The
 * operations are drawn as before ({@link MathQuestion#draw}), so that each kind and size keeps its part.
 */
final class MathDeck {

    /**
     * How many operations are asked before one around the same numbers.
     */
    static final int RECENT = 3;

    private static final int TRIES = 100;

    private final MathTopic topic;
    private final MathLevel level;
    private final List<MathQuestion> all;
    private final int different;
    private final Set<String> asked = new HashSet<>();
    private final Deque<String> recent = new ArrayDeque<>();

    MathDeck(final MathTopic topic, final MathLevel level) {
        this.topic = topic;
        this.level = level;
        all = MathQuestion.all(topic, level);
        different = (int) all.stream().map(MathQuestion::key).distinct().count();
    }

    /**
     * @return how many different operations the topic and level have
     */
    int getDifferent() {
        return different;
    }

    /**
     * @return the next operation
     */
    MathQuestion next(final ReplayablePseudoRandom random) {
        if (asked.size() >= different) {
            asked.clear();
        }
        MathQuestion next = null;
        for (int i = 0; i < TRIES && next == null; i++) {
            final MathQuestion drawn = MathQuestion.draw(topic, level, random);
            if (isNew(drawn) && !recent.contains(drawn.family())) {
                next = drawn;
            }
        }
        if (next == null) {
            // Few are left: one of them
            final List<MathQuestion> left = new ArrayList<>(all.stream().filter(this::isNew).toList());
            final List<MathQuestion> others = left.stream().filter(one -> !recent.contains(one.family())).toList();
            final List<MathQuestion> candidates = others.isEmpty() ? left : others;
            next = candidates.get(random.nextInt(candidates.size()));
        }
        asked.add(next.key());
        recent.addLast(next.family());
        if (recent.size() > RECENT) {
            recent.removeFirst();
        }
        return next;
    }

    private boolean isNew(final MathQuestion one) {
        return !asked.contains(one.key());
    }
}

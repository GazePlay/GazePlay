package net.gazeplay.games.math101;

import lombok.Getter;
import net.gazeplay.commons.random.ReplayablePseudoRandom;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An operation of "Apprends les Maths": two numbers, an operator, the answer, and wrong answers close to it (review of
 * 10/11/2026; before, numbers from 0 and wrong answers drawn up to twice the largest number, often too far to be
 * mistaken).
 */
@Getter
public final class MathQuestion {

    private final int first;
    private final MathOperation operation;
    private final int second;
    private final int answer;
    /**
     * The table of a multiplication or a division (7 for 7 × 4, 4 × 7 and 28 ÷ 7), 0 for the others.
     */
    private final int table;

    MathQuestion(final int first, final MathOperation operation, final int second, final int answer, final int table) {
        this.first = first;
        this.operation = operation;
        this.second = second;
        this.answer = answer;
        this.table = table;
    }

    /**
     * @return an operation of a topic at a level, drawn at random: the additions with a sum up to the largest number of
     * the level, the subtractions from a number up to it, the multiplications of a table by 1 to 10 (in both orders),
     * the divisions of a product of a table by this table; never a 0
     */
    static MathQuestion draw(final MathTopic topic, final MathLevel level, final ReplayablePseudoRandom random) {
        final List<MathOperation> operations = topic.getOperations();
        final MathOperation operation = operations.get(operations.size() == 1 ? 0 : random.nextInt(operations.size()));
        switch (operation) {
            case PLUS -> {
                final int sum = 2 + random.nextInt(level.getUpTo() - 1);
                final int first = 1 + random.nextInt(sum - 1);
                return new MathQuestion(first, operation, sum - first, sum, 0);
            }
            case MINUS -> {
                final int first = 2 + random.nextInt(level.getUpTo() - 1);
                final int second = 1 + random.nextInt(first - 1);
                return new MathQuestion(first, operation, second, first - second, 0);
            }
            case MULTIPLY -> {
                final int table = level.getTables()[random.nextInt(level.getTables().length)];
                final int factor = 1 + random.nextInt(MathLevel.FACTORS);
                return random.nextBoolean()
                    ? new MathQuestion(table, operation, factor, table * factor, table)
                    : new MathQuestion(factor, operation, table, table * factor, table);
            }
            default -> {
                final int table = level.getTables()[random.nextInt(level.getTables().length)];
                final int quotient = 1 + random.nextInt(MathLevel.FACTORS);
                return new MathQuestion(table * quotient, operation, table, quotient, table);
            }
        }
    }

    /**
     * @return the operation as written on the board, without its answer: "7 + 5 ="
     */
    public String text() {
        return first + " " + operation.getText() + " " + second + " =";
    }

    /**
     * @return two different wrong answers drawn among those close to the right one, all from 1: 1 to 3 more or less
     * (and 10 more or less from 15) for an addition or a subtraction; the products next to it in the tables, or 1 more
     * or less, for a multiplication; 1 or 2 more or less, or the divisor, for a division. Never one more and one less
     * by as much (as 16, 17 and 18): the right answer would be the one in the middle, found without counting.
     */
    List<Integer> wrongAnswers(final ReplayablePseudoRandom random) {
        final Set<Integer> close = new LinkedHashSet<>();
        switch (operation) {
            case PLUS, MINUS -> {
                for (int difference = 1; difference <= 3; difference++) {
                    close.add(answer - difference);
                    close.add(answer + difference);
                }
                if (answer >= 15) {
                    close.add(answer - 10);
                    close.add(answer + 10);
                }
            }
            case MULTIPLY -> {
                final int factor = answer / table;
                close.add(table * (factor - 1));
                close.add(table * (factor + 1));
                close.add((table - 1) * factor);
                close.add((table + 1) * factor);
                close.add(answer - 1);
                close.add(answer + 1);
            }
            default -> {
                close.add(answer - 2);
                close.add(answer - 1);
                close.add(answer + 1);
                close.add(answer + 2);
                close.add(table);
            }
        }
        final List<Integer> candidates = new ArrayList<>();
        for (final int value : close) {
            if (value >= 1 && value != answer) {
                candidates.add(value);
            }
        }
        final int first = candidates.remove(random.nextInt(candidates.size()));
        candidates.remove(Integer.valueOf(2 * answer - first));
        return List.of(first, candidates.get(random.nextInt(candidates.size())));
    }
}

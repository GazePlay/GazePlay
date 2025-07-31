package net.gazeplay.games.cups2.strategy;

import net.gazeplay.games.cups2.CupsAndBalls;
import net.gazeplay.games.cups2.action.*;
import net.gazeplay.games.cups2.utils.Cup;

import java.util.List;

public interface StrategyBuilder {
    enum Type {
        SIMPLE, ADAPTIVE
    }

    Type getType();

    void computeActions(List<Action> actions, List<Cup> cups, int ballIndex);

    static StrategyBuilder newInstanceOf(Type type) {
        return switch (type) {
            case SIMPLE -> new SimpleStrategy();
            case ADAPTIVE -> new AdaptiveStrategy();
        };
    }

    static Action newActionOfType(Action.Type type, List<Cup> cups, int ballIndex) {
        int a, b;
        switch (type) {
            case TRICK -> {
                do {
                    b = CupsAndBalls.random.nextBoolean() ? ballIndex - 1 : ballIndex + 1;
                } while (!(0 <= b && b < cups.size()));
                if (CupsAndBalls.random.nextBoolean())
                    return new Trick(cups, ballIndex, b);
                else
                    return new Trick(cups, b, ballIndex);
            }
            case FAKE_TRICK -> {
                a = CupsAndBalls.random.nextInt(cups.size());
                do {
                    b = CupsAndBalls.random.nextInt(cups.size());
                } while (Math.abs(a - b) != 1);
                return new FakeTrick(cups, a, b);
            }
            case EXCHANGE -> {
                a = CupsAndBalls.random.nextInt(cups.size());
                do {
                    b = CupsAndBalls.random.nextInt(cups.size());
                } while (a == b);
                return new Exchange(cups, a, b);
            }
            case FAKE_EXCHANGE -> {
                a = CupsAndBalls.random.nextInt(cups.size());
                do {
                    b = CupsAndBalls.random.nextInt(cups.size());
                } while (a == b);
                return new FakeExchange(cups, a, b);
            }
            case CYCLE -> {
                do {
                    a = CupsAndBalls.random.nextInt(cups.size());
                    b = CupsAndBalls.random.nextInt(cups.size());
                } while (Math.abs(a - b) < 2);
                return new Cycle(cups, a, b, CupsAndBalls.random.nextBoolean());
            }
            case FAKE_CYCLE -> {  // Not relevant when nbCups < 4
                do {
                    a = CupsAndBalls.random.nextInt(cups.size());
                    b = CupsAndBalls.random.nextInt(cups.size());
                } while (Math.abs(a - b) < 2);
                return new FakeCycle(cups, a, b, CupsAndBalls.random.nextBoolean());
            }
            default -> throw new IllegalArgumentException("Unknown action type: " + type);
        }
    }
}

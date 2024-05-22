package net.gazeplay.games.cups2.strategy;

import net.gazeplay.games.cups2.CupsAndBalls;
import net.gazeplay.games.cups2.action.*;
import net.gazeplay.games.cups2.utils.Cup;

import java.util.List;

public class SimpleStrategy implements StrategyBuilder {
    @Override
    public Type getType() {
        return Type.SIMPLE;
    }

    @Override
    public void computeActions(List<Action> actions, List<Cup> cups, int ballIndex) {
        actions.add(new RevealAll(cups));
        final int nbRounds = CupsAndBalls.random.nextInt(10) + 6;
        for (int i = 0; i < nbRounds; i++) {
            int a, b;
            ballIndex = actions.get(actions.size() - 1).simulate(ballIndex);
            switch (CupsAndBalls.random.nextInt(6)) {
                case 0 -> {
                    do {
                        b = CupsAndBalls.random.nextBoolean() ? ballIndex - 1 : ballIndex + 1;
                    } while (0 <= b && b < cups.size());
                    actions.add(new Trick(cups, ballIndex, b));
                }
                case 1 -> {
                    a = CupsAndBalls.random.nextInt(cups.size());
                    do {
                        b = CupsAndBalls.random.nextInt(cups.size());
                    } while (a == b);
                    actions.add(new FakeTrick(cups, a, b));
                }
                case 2 -> {
                    a = CupsAndBalls.random.nextInt(cups.size());
                    do {
                        b = CupsAndBalls.random.nextInt(cups.size());
                    } while (a == b);
                    actions.add(new Exchange(cups, a, b));
                }
                case 3 -> {
                    a = CupsAndBalls.random.nextInt(cups.size());
                    do {
                        b = CupsAndBalls.random.nextInt(cups.size());
                    } while (a == b);
                    actions.add(new FakeExchange(cups, a, b));
                }
                case 4 -> {
                    a = CupsAndBalls.random.nextInt(cups.size());
                    do {
                        b = CupsAndBalls.random.nextInt(cups.size());
                    } while (Math.abs(a - b) < 2);
                    actions.add(new Cycle(cups, a, b, CupsAndBalls.random.nextBoolean()));
                }
                case 5 -> {
                    a = CupsAndBalls.random.nextInt(cups.size());
                    do {
                        b = CupsAndBalls.random.nextInt(cups.size());
                    } while (Math.abs(a - b) < 2);
                    actions.add(new FakeCycle(cups, a, b, CupsAndBalls.random.nextBoolean()));
                }
            }
        }
    }
}

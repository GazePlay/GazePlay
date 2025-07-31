package net.gazeplay.games.cups2.strategy;

import net.gazeplay.games.cups2.Config;
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
        int nbRounds = CupsAndBalls.random.nextInt(10) + 6;
        for (int i = 0; i < nbRounds; i++) {
            if (!actions.isEmpty())
                ballIndex = actions.get(actions.size() - 1).simulate(ballIndex);
            int type = CupsAndBalls.random.nextInt(Config.getNbCups() > 3 ? 6 : 4);
            actions.add(StrategyBuilder.newActionOfType(Action.Type.values()[type], cups, ballIndex));
        }
    }
}

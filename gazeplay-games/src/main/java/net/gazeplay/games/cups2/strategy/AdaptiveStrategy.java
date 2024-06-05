package net.gazeplay.games.cups2.strategy;

import net.gazeplay.games.cups2.Config;
import net.gazeplay.games.cups2.CupsAndBalls;
import net.gazeplay.games.cups2.action.*;
import net.gazeplay.games.cups2.model.*;
import net.gazeplay.games.cups2.utils.Cup;

import java.util.ArrayList;
import java.util.List;

public class AdaptiveStrategy implements StrategyBuilder {

    private static final List<Action.Type> actionSuccession = List.of(
        Action.Type.EXCHANGE,
        Action.Type.CYCLE,
        Action.Type.TRICK
    );

    private final List<Action.Type> actionPool = new ArrayList<>(List.of(Action.Type.EXCHANGE));

    @Override
    public Type getType() {
        return Type.ADAPTIVE;
    }

    @Override
    public void computeActions(List<Action> actions, List<Cup> cups, int ballIndex) {
        List<Performance> performances = CupsAndBalls.getPlayerModel().getPerformanceHistory();

        // Calibration phase if not true
        if (performances.size() >= 2) {
            Performance prevPerf1 = performances.get(performances.size() - 2);
            Performance prevPerf2 = performances.get(performances.size() - 1);

            // Compute the different factors of the next round with linear extrapolation
            int predNbCups = prevPerf2.getRound().getNbCups() + (int) Math.round(
                (
                    prevPerf2.getRound().getNbCups() * prevPerf2.getNbCupsPerf() -
                        prevPerf1.getRound().getNbCups() * prevPerf1.getNbCupsPerf()
                ) /
                    Math.max(Math.abs(prevPerf1.getNbCupsPerf()), Math.abs(prevPerf2.getNbCupsPerf()))
            );
            Config.setNbCups(predNbCups);

            double predSpeedFactor = prevPerf2.getRound().getSpeedFactor() + (
                prevPerf2.getRound().getSpeedFactor() * prevPerf2.getSpeedPerf() -
                    prevPerf1.getRound().getSpeedFactor() * prevPerf1.getSpeedPerf()
            ) /
                Math.max(Math.abs(prevPerf1.getSpeedPerf()), Math.abs(prevPerf2.getSpeedPerf()));
            Config.setSpeedFactor(predSpeedFactor);

            for (Action.Type type : prevPerf2.getRound().getActionPool()) {
                if (prevPerf2.getActionsPerf().get(type) < Config.ADAPTIVE_REMOVE_FAKENESS_THRESHOLD)
                    actionPool.remove(type);
                if (prevPerf2.getActionsPerf().get(type) > Config.ADAPTIVE_INTRODUCE_FAKENESS_THRESHOLD)
                    actionPool.add(Action.getFakeTypeOf(type));
            }

            if (prevPerf2.getActionsPerf().values().stream().reduce(0.0, Double::sum) /
                prevPerf2.getActionsPerf().size() > Config.ADAPTIVE_INTRODUCE_FEATURE_THRESHOLD &&
                !actionPool.containsAll(actionSuccession)) {
                List<Action.Type> candidates = new ArrayList<>(List.copyOf(actionSuccession));
                candidates.removeAll(actionPool);
                actionPool.add(candidates.get(CupsAndBalls.random.nextInt(candidates.size())));
            }
        }

        int nbActions = Config.MIN_ACTIONS_PER_ROUND + CupsAndBalls.random.nextInt(Config.MAX_ACTIONS_PER_ROUND + 1 - Config.MIN_ACTIONS_PER_ROUND);
        for (int i = 0; i < nbActions; i++) {
            if (!actions.isEmpty())
                ballIndex = actions.get(actions.size() - 1).simulate(ballIndex);

            Action.Type type = actionPool.get(CupsAndBalls.random.nextInt(actionPool.size()));
            actions.add(StrategyBuilder.newActionOfType(type, cups, ballIndex));
        }
        CupsAndBalls.getPlayerModel().newRound(new RoundInstance(actionPool, actions.size()));
    }

    public static double computeDifficulty(List<Action> actions) {
        return actions.stream().mapToDouble(Action::getDifficulty).sum() * Math.sqrt(Config.getNbCups()) * Config.getSpeedFactor();
    }
}

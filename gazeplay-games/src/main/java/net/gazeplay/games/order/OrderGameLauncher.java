package net.gazeplay.games.order;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class OrderGameLauncher implements IGameLauncher<Stats, IntGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new OrderStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new OrderStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IntGameVariant gameVariant,
                                       Stats stats) {
        return new Order(gameContext, gameVariant.getNumber(), stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IntGameVariant gameVariant,
                                    Stats stats, double gameSeed) {
        return new Order(gameContext, gameVariant.getNumber(), stats, gameSeed);
    }
}

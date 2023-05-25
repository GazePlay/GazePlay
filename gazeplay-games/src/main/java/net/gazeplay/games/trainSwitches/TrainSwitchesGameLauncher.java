package net.gazeplay.games.trainSwitches;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

public class TrainSwitchesGameLauncher implements IGameLauncher<Stats, IntStringGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new TrainSwitchesStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IntStringGameVariant gameVariant, Stats stats) {
        return new TrainSwitches(gameContext, stats, gameVariant.getNumber(), gameVariant.getStringValue());
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return null;
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IntStringGameVariant gameVariant, Stats stats, double gameSeed) {
        return new TrainSwitches(gameContext, stats, gameVariant.getNumber(), gameVariant.getStringValue());
    }
}

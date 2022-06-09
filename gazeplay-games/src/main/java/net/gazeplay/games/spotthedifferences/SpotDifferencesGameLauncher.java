package net.gazeplay.games.spotthedifferences;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class SpotDifferencesGameLauncher implements IGameLauncher<SpotTheDifferencesStats, IGameVariant> {

    @Override
    public SpotTheDifferencesStats createNewStats(Scene scene) {
        return new SpotTheDifferencesStats(scene);
    }

    @Override
    public SpotTheDifferencesStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new SpotTheDifferencesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant,
                                       SpotTheDifferencesStats stats) {
        return new SpotTheDifferences(gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant,
                                    SpotTheDifferencesStats stats, double gameSeed) {
        return new SpotTheDifferences(gameContext, stats);
    }
}

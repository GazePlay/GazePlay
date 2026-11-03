package net.gazeplay.games.surviveAgainstRobots;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates the game "SurviveAgainstRobots" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). Without a variant (or with one of before the review of 3/11/2026): normal level, automatic
 * shots, without end.
 */
public class SurviveAgainstRobotsLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new SurviveAgainstRobotsStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return new SurviveAgainstRobots(gameContext, stats, levelOf(gameVariant), shotOf(gameVariant),
            endOf(gameVariant));
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new SurviveAgainstRobotsStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return new SurviveAgainstRobots(gameContext, stats, levelOf(gameVariant), shotOf(gameVariant),
            endOf(gameVariant), gameSeed);
    }

    static SurviveLevel levelOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? SurviveLevel.of(grouped.getGroup())
            : SurviveLevel.NORMAL;
    }

    static SurviveShot shotOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? SurviveShot.of(grouped.getSubGroup())
            : SurviveShot.AUTOMATIC;
    }

    static SurviveEnd endOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? SurviveEnd.of(grouped.getValue())
            : SurviveEnd.ENDLESS;
    }
}

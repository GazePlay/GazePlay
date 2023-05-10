package net.gazeplay.games.surviveAgainstRobots;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

public class SurviveAgainstRobotsMouseLauncher implements IGameLauncher<Stats, EnumGameVariant<SurviveAgainstRobotsVariant>> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new SurviveAgainstRobotsStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<SurviveAgainstRobotsVariant> gameVariant, Stats stats) {
        return new SurviveAgainstRobots(gameContext,gameVariant.getEnumValue(),stats,true);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new SurviveAgainstRobotsStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<SurviveAgainstRobotsVariant> gameVariant, Stats stats, double gameSeed) {
        return new SurviveAgainstRobots(gameContext,gameVariant.getEnumValue(),stats,true);
    }

}

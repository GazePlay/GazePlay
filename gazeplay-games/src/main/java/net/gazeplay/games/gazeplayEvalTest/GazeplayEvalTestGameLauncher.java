package net.gazeplay.games.gazeplayEvalTest;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;

import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

public class GazeplayEvalTestGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    private GazeplayEvalTestGameStats gameStats;

    @Override
    public Stats createNewStats(Scene scene){

        gameStats = new GazeplayEvalTestGameStats(scene);
        return gameStats;
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo){
        gameStats = new GazeplayEvalTestGameStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        return gameStats;
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant,
                                       Stats stats) {
        return new GazePlayEvalTest(gameContext, gameStats, gameContext.getTranslator());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant,
                                    Stats stats, double gameSeed) {
        return new GazePlayEvalTest(gameContext, gameStats, gameContext.getTranslator());
    }
}

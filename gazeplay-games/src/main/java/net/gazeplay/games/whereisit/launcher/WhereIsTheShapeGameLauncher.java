package net.gazeplay.games.whereisit.launcher;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.whereisit.WhereIsIt;
import net.gazeplay.games.whereisit.WhereIsItGameType;
import net.gazeplay.games.whereisit.WhereIsItStats;

import java.util.ArrayList;
import java.util.LinkedList;


public class WhereIsTheShapeGameLauncher implements IGameLauncher<Stats, DimensionDifficultyGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new WhereIsItStats(scene, WhereIsItGameType.SHAPE_NAME.getGameName());
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new WhereIsItStats(scene, WhereIsItGameType.SHAPE_NAME.getGameName(), nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       DimensionDifficultyGameVariant gameVariant, Stats stats) {
        if (gameVariant.getDifficulty().equals("easy")) {
            return new WhereIsIt(WhereIsItGameType.SHAPE_NAME_EASY, gameVariant.getWidth(),
                gameVariant.getHeight(), false, gameContext, stats);
        } else {
            return new WhereIsIt(WhereIsItGameType.SHAPE_NAME, gameVariant.getWidth(),
                gameVariant.getHeight(), false, gameContext, stats);
        }
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext,
                                       DimensionDifficultyGameVariant gameVariant, Stats stats, double gameSeed) {
        if (gameVariant.getDifficulty().equals("easy")) {
            return new WhereIsIt(WhereIsItGameType.SHAPE_NAME_EASY, gameVariant.getWidth(),
                gameVariant.getHeight(), false, gameContext, stats, gameSeed);
        } else {
            return new WhereIsIt(WhereIsItGameType.SHAPE_NAME, gameVariant.getWidth(),
                gameVariant.getHeight(), false, gameContext, stats, gameSeed);
        }
    }
}

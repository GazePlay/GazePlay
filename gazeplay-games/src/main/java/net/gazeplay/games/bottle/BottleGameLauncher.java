package net.gazeplay.games.bottle;

import javafx.scene.Scene;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class BottleGameLauncher implements IGameLauncher<BottleGameStats, IntStringGameVariant> {

    @Override
    public BottleGameStats createNewStats(Scene scene) {
        return new BottleGameStats(scene);
    }

    @Override
    public BottleGame createNewGame(IGameContext gameContext, IntStringGameVariant gameVariant, BottleGameStats stats) {
        return new BottleGame(gameContext, stats, gameVariant.getNumber(), gameVariant.getStringValue());
    }

    @Override
    public BottleGameStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new BottleGameStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public BottleGame replayGame(IGameContext gameContext, IntStringGameVariant gameVariant, BottleGameStats stats, double gameSeed) {
        return new BottleGame(gameContext, stats, gameVariant.getNumber(), gameVariant.getStringValue(), gameSeed);
    }
}

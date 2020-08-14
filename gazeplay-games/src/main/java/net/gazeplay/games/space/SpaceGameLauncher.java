package net.gazeplay.games.space;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.LinkedList;

public class SpaceGameLauncher implements GameSpec.GameLauncher<SpaceGameStats, GameSpec.GameVariant> {

    @Override
    public SpaceGameStats createNewStats(Scene scene) {
        return new SpaceGameStats(scene);
    }

    @Override
    public SpaceGameStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, LinkedList<FixationPoint> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new SpaceGameStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.GameVariant gameVariant, SpaceGameStats stats) {
        return new SpaceGame(gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, GameSpec.GameVariant gameVariant, SpaceGameStats stats, double gameSeed) {
        return new SpaceGame(gameContext, stats, gameSeed);
    }

}

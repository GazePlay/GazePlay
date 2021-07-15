package net.gazeplay.games.pianosight;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.games.shooter.ShooterGamesStats;

import java.util.ArrayList;
import java.util.LinkedList;

public class PianoGameLauncher implements IGameLauncher<ShooterGamesStats, DimensionGameVariant> {

    @Override
    public ShooterGamesStats createNewStats(Scene scene) {
        return new ShooterGamesStats(scene, "Piano");
    }

    @Override
    public ShooterGamesStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new ShooterGamesStats(scene, "Piano", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(
        IGameContext gameContext,
        DimensionGameVariant gameVariant,
        ShooterGamesStats stats
    ) {
        return new Piano(gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(
        IGameContext gameContext,
        DimensionGameVariant gameVariant,
        ShooterGamesStats stats, double gameSeed
    ) {
        return new Piano(gameContext, stats, gameSeed);
    }
}

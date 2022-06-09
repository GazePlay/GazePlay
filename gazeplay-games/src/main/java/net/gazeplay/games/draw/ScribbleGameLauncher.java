package net.gazeplay.games.draw;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class ScribbleGameLauncher implements IGameLauncher<Stats, DimensionGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new Stats(scene, "Scribble");
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new Stats(scene, "Scribble", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       DimensionGameVariant gameVariant, Stats stats) {
        return new DrawApplication(gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext,
                                    DimensionGameVariant gameVariant, Stats stats, double gameSeed) {
        return new DrawApplication(gameContext, stats, gameSeed);
    }
}

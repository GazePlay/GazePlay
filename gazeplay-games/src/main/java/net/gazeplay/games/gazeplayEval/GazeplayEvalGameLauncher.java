package net.gazeplay.games.gazeplayEval;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Launcher of GazePlay Eval, and of GazePlay Eval Test: the variant is the imported evaluation to play (null when none
 * is imported: the game then says how to import one).
 */
public class GazeplayEvalGameLauncher implements IGameLauncher<Stats, GazeplayEvalGameVariant> {

    /**
     * GazePlay Eval Test: no participant form, nothing recorded (see {@link GazeplayEval#test}).
     */
    private final boolean test;

    public GazeplayEvalGameLauncher() {
        this(false);
    }

    public GazeplayEvalGameLauncher(final boolean test) {
        this.test = test;
    }

    @Override
    public Stats createNewStats(Scene scene) {
        final Stats stats = new GazeplayEvalGameStats(scene);
        if (test) {
            stats.disableSaving();
        }
        return stats;
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new GazeplayEvalGameStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GazeplayEvalGameVariant gameVariant, Stats stats) {
        return test ? GazeplayEval.test(gameContext, gameVariant, stats)
            : new GazeplayEval(gameContext, gameVariant, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, GazeplayEvalGameVariant gameVariant, Stats stats, double gameSeed) {
        return new GazeplayEval(gameContext, gameVariant, stats, gameSeed);
    }
}

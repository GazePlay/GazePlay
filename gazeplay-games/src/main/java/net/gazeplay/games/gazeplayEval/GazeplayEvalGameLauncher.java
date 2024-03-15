package net.gazeplay.games.gazeplayEval;

import javafx.scene.Scene;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RequiredArgsConstructor
public class GazeplayEvalGameLauncher implements IGameLauncher<Stats, GazeplayEvalGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new GazeplayEvalGameStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new GazeplayEvalGameStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GazeplayEvalGameVariant gameVariant, Stats stats) {
        return replayGame(gameContext, gameVariant, stats, Math.random());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, GazeplayEvalGameVariant gameVariant, Stats stats, double gameSeed) {
        return GameState.setup(gameContext, gameVariant, stats, gameSeed);
    }
}

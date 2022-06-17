package net.gazeplay.games.slidingpuzzle;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.commons.utils.FixationPoint;

import java.util.List;

public class PuzzleGameLauncher implements IGameLauncher<Stats, EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant>> {

    @Override
    public Stats createNewStats(final Scene scene) {
        return new SlidingPuzzleStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene,
                                  int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                  LifeCycle lifeCycle,
                                  RoundsDurationReport roundsDurationReport,
                                  List<List<FixationPoint>> fixationSequence,
                                  List<CoordinatesTracker> movementHistory,
                                  double[][] heatMap,
                                  List<AreaOfInterest> aoiList,
                                  SavedStatsInfo savedStatsInfo
    ) {
        return new SlidingPuzzleStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant> gameVariant, Stats stats) {
        return new SlidingPuzzle(stats, gameContext, 3, 3, gameVariant);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<PuzzleGameVariantGenerator.PuzzleGameVariant> gameVariant, Stats stats, double gameSeed) {
        return new SlidingPuzzle(stats, gameContext, 3, 3, gameVariant, gameSeed);
    }
}

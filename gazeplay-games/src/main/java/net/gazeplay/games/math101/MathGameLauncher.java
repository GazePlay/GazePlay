package net.gazeplay.games.math101;

import javafx.scene.Scene;
import lombok.RequiredArgsConstructor;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.MathGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

@RequiredArgsConstructor
public final class MathGameLauncher implements IGameLauncher<Stats, MathGameVariant> {

    private final MathGameType mathGameType;

    @Override
    public Stats createNewStats(final Scene scene) {
        return new MathGamesStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene,
                                  int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                  LifeCycle lifeCycle,
                                  RoundsDurationReport roundsDurationReport,
                                  List<List<FixationPoint>> fixationSequence,
                                  List<CoordinatesTracker> movementHistory,
                                  int[][] heatMap,
                                  List<AreaOfInterest> aoiList,
                                  SavedStatsInfo savedStatsInfo
    ) {
        return new MathGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final MathGameVariant gameVariant, final Stats stats) {
        return new Math101(mathGameType, gameContext, gameVariant, stats);
    }

    @Override
    public GameLifeCycle replayGame(final IGameContext gameContext, final MathGameVariant gameVariant, final Stats stats, double gameSeed) {
        return new Math101(mathGameType, gameContext, gameVariant, stats, gameSeed);
    }
}

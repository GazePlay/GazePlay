package net.gazeplay.games.paperScissorsStone;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class PaperScissorsStoneLauncher implements IGameLauncher<PaperScissorsStoneStats, IGameVariant> {

    @Override
    public PaperScissorsStoneStats createNewStats(Scene scene) {
        return new PaperScissorsStoneStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, PaperScissorsStoneStats stats) {
        return new PaperScissorsStoneGame(gameContext, stats);
    }

    @Override
    public PaperScissorsStoneStats createSavedStats(Scene scene,
                                                    int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                                    LifeCycle lifeCycle,
                                                    RoundsDurationReport roundsDurationReport,
                                                    List<List<FixationPoint>> fixationSequence,
                                                    List<CoordinatesTracker> movementHistory,
                                                    int[][] heatMap,
                                                    List<AreaOfInterest> aoiList,
                                                    SavedStatsInfo savedStatsInfo
    ) {
        return new PaperScissorsStoneStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, PaperScissorsStoneStats stats, double gameSeed) {
        return new PaperScissorsStoneGame(gameContext, stats, gameSeed);
    }
}

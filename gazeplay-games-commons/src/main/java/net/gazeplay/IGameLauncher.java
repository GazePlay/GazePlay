package net.gazeplay;

import javafx.scene.Scene;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.CoordinatesTracker;
import net.gazeplay.commons.utils.stats.AreaOfInterest;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.List;

public interface IGameLauncher<T extends Stats, V extends IGameVariant> {

    T createNewStats(Scene scene);

    GameLifeCycle createNewGame(IGameContext gameContext, V gameVariant, T stats);

    T createSavedStats(Scene scene,
                       int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                       LifeCycle lifeCycle,
                       RoundsDurationReport roundsDurationReport,
                       List<List<FixationPoint>> fixationSequence,
                       List<CoordinatesTracker> movementHistory,
                       double[][] heatMap,
                       List<AreaOfInterest> aoiList,
                       SavedStatsInfo savedStatsInfo
    );

    GameLifeCycle replayGame(IGameContext gameContext, V gameVariant, T stats, double gameSeed);
}

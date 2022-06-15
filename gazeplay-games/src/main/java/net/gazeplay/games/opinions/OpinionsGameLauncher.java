package net.gazeplay.games.opinions;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class OpinionsGameLauncher implements IGameLauncher<OpinionsGameStats, EnumGameVariant<OpinionsGameVariant>> {

    @Override
    public OpinionsGameStats createNewStats(Scene scene) {
        return new OpinionsGameStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<OpinionsGameVariant> gameVariant, OpinionsGameStats stats) {
        return new OpinionsGame(gameContext, stats, gameVariant.getEnumValue());
    }

    @Override
    public OpinionsGameStats createSavedStats(Scene scene,
                                              int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                              LifeCycle lifeCycle,
                                              RoundsDurationReport roundsDurationReport,
                                              List<List<FixationPoint>> fixationSequence,
                                              List<CoordinatesTracker> movementHistory,
                                              double[][] heatMap,
                                              List<AreaOfInterest> AOIList,
                                              SavedStatsInfo savedStatsInfo
    ) {
        return new OpinionsGameStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, AOIList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<OpinionsGameVariant> gameVariant, OpinionsGameStats stats, double gameSeed) {
        return new OpinionsGame(gameContext, stats, gameVariant.getEnumValue(), gameSeed);
    }
}

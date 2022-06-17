package net.gazeplay.games.biboulejump;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class BibouleJumpGameLauncher implements IGameLauncher<BibouleJumpStats, EnumGameVariant<BibouleJumpVariant>> {

    @Override
    public BibouleJumpStats createNewStats(Scene scene) {
        return new BibouleJumpStats(scene);
    }

    @Override
    public BibouleJumpStats createSavedStats(Scene scene,
                                             int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                             LifeCycle lifeCycle,
                                             RoundsDurationReport roundsDurationReport,
                                             List<List<FixationPoint>> fixationSequence,
                                             List<CoordinatesTracker> movementHistory,
                                             double[][] heatMap,
                                             List<AreaOfInterest> aoiList,
                                             SavedStatsInfo savedStatsInfo
    ) {
        return new BibouleJumpStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached,
            lifeCycle, roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<BibouleJumpVariant> gameVariant, BibouleJumpStats stats) {
        return new BibouleJump(gameContext, stats, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<BibouleJumpVariant> gameVariant, BibouleJumpStats stats, double gameSeed) {
        return new BibouleJump(gameContext, stats, gameVariant.getEnumValue(), gameSeed);
    }
}

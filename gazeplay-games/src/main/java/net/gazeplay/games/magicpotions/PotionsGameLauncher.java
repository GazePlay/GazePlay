package net.gazeplay.games.magicpotions;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.List;

public class PotionsGameLauncher implements IGameLauncher<MagicPotionsStats, IGameVariant> {

    @Override
    public MagicPotionsStats createNewStats(Scene scene) {
        return new MagicPotionsStats(scene);
    }

    @Override
    public MagicPotionsStats createSavedStats(Scene scene,
                                              int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                              LifeCycle lifeCycle,
                                              RoundsDurationReport roundsDurationReport,
                                              List<List<FixationPoint>> fixationSequence,
                                              List<CoordinatesTracker> movementHistory,
                                              double[][] heatMap,
                                              List<AreaOfInterest> aoiList,
                                              SavedStatsInfo savedStatsInfo
    ) {
        return new MagicPotionsStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, lifeCycle,
            roundsDurationReport, fixationSequence, movementHistory, heatMap, aoiList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, MagicPotionsStats stats) {
        return new MagicPotions(gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, MagicPotionsStats stats, double gameSeed) {
        return new MagicPotions(gameContext, stats, gameSeed);
    }
}

package net.gazeplay.games.beraPreTest;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;

import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BeraPreTestGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    private BeraPreTestGameStats gameStats;

    @Override
    public Stats createNewStats(Scene scene) {

        gameStats = new BeraPreTestGameStats(scene);
        return gameStats;
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, List<AreaOfInterest> AOIList, SavedStatsInfo savedStatsInfo) {
        gameStats = new BeraPreTestGameStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, AOIList, savedStatsInfo);
        return gameStats;
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant,
                                       Stats stats) {
        return new BeraPreTest(gameContext, gameStats, gameContext.getTranslator());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant,
                                    Stats stats, double gameSeed) {
        return new BeraPreTest(gameContext, gameStats, gameContext.getTranslator());
    }
}

package net.gazeplay.games.findTheOddOneOut;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

public class FindTheOddOneOutEmmanuelGameLauncher implements IGameLauncher<Stats, DimensionGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new FindTheOddOneOutEmmanuelStats(scene, FindTheOddOneOutEmmanuelGameType.FIND_ODD.getGameName());
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new FindTheOddOneOutEmmanuelStats(scene, FindTheOddOneOutEmmanuelGameType.FIND_ODD.getGameName(), nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       DimensionGameVariant gameVariant, Stats stats) {
        return new FindTheOddOneOutEmmanuel(FindTheOddOneOutEmmanuelGameType.FIND_ODD, gameVariant.getWidth(),
            gameVariant.getHeight(), false, gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext,
                                    DimensionGameVariant gameVariant, Stats stats, double gameSeed) {
        return new FindTheOddOneOutEmmanuel(FindTheOddOneOutEmmanuelGameType.FIND_ODD, gameVariant.getWidth(),
            gameVariant.getHeight(), false, gameContext, stats, gameSeed);
    }
}

package net.gazeplay.games.whereisit.launcher;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.whereisit.WhereIsIt;
import net.gazeplay.games.whereisit.WhereIsItGameType;
import net.gazeplay.games.whereisit.WhereIsItStats;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class WhereIsTheLetterGameLauncher implements IGameLauncher<Stats, DimensionGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new WhereIsItStats(scene, WhereIsItGameType.LETTERS.getGameName());
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new WhereIsItStats(scene, WhereIsItGameType.LETTERS.getGameName(), nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       DimensionGameVariant gameVariant, Stats stats) {
        return new WhereIsIt(WhereIsItGameType.LETTERS, gameVariant.getWidth(),
            gameVariant.getHeight(), false, gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext,
                                    DimensionGameVariant gameVariant, Stats stats, double gameSeed) {
        return new WhereIsIt(WhereIsItGameType.LETTERS, gameVariant.getWidth(),
            gameVariant.getHeight(), false, gameContext, stats, gameSeed);
    }

}

package net.gazeplay.games.rushhour;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.pet.PetStats;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class RushHourGameLauncher implements IGameLauncher<Stats, IntStringGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new RushHourStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new RushHourStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       IntStringGameVariant gameVariant, Stats stats) {
        return new RushHour(gameContext, stats, gameVariant.getNumber());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext,
                                    IntStringGameVariant gameVariant, Stats stats, double gameSeed) {
        return new RushHour(gameContext, stats, gameVariant.getNumber());
    }

}

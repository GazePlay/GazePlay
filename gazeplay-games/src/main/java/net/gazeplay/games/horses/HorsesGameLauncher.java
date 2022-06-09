package net.gazeplay.games.horses;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class HorsesGameLauncher implements IGameLauncher<Stats, IntGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new Stats(scene, "horses");
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new Stats(scene, "horses", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IntGameVariant gameVariant,
                                       Stats stats) {
        return new Horses(gameContext, stats, 0, gameVariant.getNumber());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IntGameVariant gameVariant,
                                    Stats stats, double gameSeed) {
        return new Horses(gameContext, stats, 0, gameVariant.getNumber(), gameSeed);
    }

}

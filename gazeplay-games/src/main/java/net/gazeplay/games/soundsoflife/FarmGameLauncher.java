package net.gazeplay.games.soundsoflife;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class FarmGameLauncher implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new SoundsOfLifeStats(scene, "Farm");
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new SoundsOfLifeStats(scene, "Farm", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant,
                                       Stats stats) {
        return new SoundsOfLife(gameContext, stats, 0);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant,
                                    Stats stats, double gameSeed) {
        return new SoundsOfLife(gameContext, stats, 0, gameSeed);
    }
}

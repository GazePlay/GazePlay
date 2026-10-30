package net.gazeplay.games.soundsoflife;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates the game "SoundsOfLife" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}).
 */
public class SoundsOfLifeGameLauncher implements IGameLauncher<Stats, EnumGameVariant<SoundsOfLifeGameVariant>> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new SoundsOfLifeStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new SoundsOfLifeStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<SoundsOfLifeGameVariant> gameVariant, Stats stats) {
        return new SoundsOfLife(gameContext, stats, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<SoundsOfLifeGameVariant> gameVariant, Stats stats, double gameSeed) {
        // With the seed of the saved game (30/10/2026: a new one used to be drawn, so the sounds changed)
        return new SoundsOfLife(gameContext, stats, gameVariant.getEnumValue(), gameSeed);
    }
}

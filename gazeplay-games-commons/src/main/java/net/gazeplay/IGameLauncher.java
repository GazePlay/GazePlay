package net.gazeplay;

import javafx.scene.Scene;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates a game and its statistics, for a new game or for the replay of a saved one.
 *
 * @param <T> the statistics of the game
 * @param <V> the type of the variants of the game
 */
public interface IGameLauncher<T extends Stats, V extends IGameVariant> {

    /**
     * @return new statistics for a game played on this scene
     */
    T createNewStats(Scene scene);

    /**
     * @return a new game of the given variant, with a random seed
     */
    GameLifeCycle createNewGame(IGameContext gameContext, V gameVariant, T stats);

    /**
     * @return statistics rebuilt from a replayData.json file, for a replay
     */
    T createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo);

    /**
     * @return a game of the given variant whose random draws are those of the saved seed, for a replay
     */
    GameLifeCycle replayGame(IGameContext gameContext, V gameVariant, T stats, double gameSeed);
}

package net.gazeplay.games.spotthedifferences;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates the game "SpotDifference" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}).
 */
public class SpotDifferencesGameLauncher
    implements IGameLauncher<SpotTheDifferencesStats, GroupedGameVariant> {

    @Override
    public SpotTheDifferencesStats createNewStats(Scene scene) {
        return new SpotTheDifferencesStats(scene);
    }

    @Override
    public SpotTheDifferencesStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new SpotTheDifferencesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GroupedGameVariant gameVariant,
                                       SpotTheDifferencesStats stats) {
        return new SpotTheDifferences(gameContext, stats, levelOf(gameVariant), ringShown(gameVariant), -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, GroupedGameVariant gameVariant,
                                    SpotTheDifferencesStats stats, double gameSeed) {
        return new SpotTheDifferences(gameContext, stats, levelOf(gameVariant), ringShown(gameVariant), gameSeed);
    }

    /**
     * @return the level chosen, normal when there is none
     */
    static SpotTheDifferencesVariant levelOf(final GroupedGameVariant gameVariant) {
        if (gameVariant != null) {
            for (final SpotTheDifferencesVariant level : SpotTheDifferencesVariant.values()) {
                if (level.getLabel().equals(gameVariant.getValue())) {
                    return level;
                }
            }
        }
        return SpotTheDifferencesVariant.NORMAL;
    }

    /**
     * @return whether the ring of the fixation is shown (not when there is no variant)
     */
    static boolean ringShown(final GroupedGameVariant gameVariant) {
        return gameVariant != null && SpotTheDifferencesVariantGenerator.RING_SHOWN.equals(gameVariant.getSubGroup());
    }
}

package net.gazeplay.games.spotthedifferences;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
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
    implements IGameLauncher<SpotTheDifferencesStats, EnumGameVariant<SpotTheDifferencesVariant>> {

    @Override
    public SpotTheDifferencesStats createNewStats(Scene scene) {
        return new SpotTheDifferencesStats(scene);
    }

    @Override
    public SpotTheDifferencesStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new SpotTheDifferencesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<SpotTheDifferencesVariant> gameVariant,
                                       SpotTheDifferencesStats stats) {
        return new SpotTheDifferences(gameContext, stats, variantOf(gameVariant), -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<SpotTheDifferencesVariant> gameVariant,
                                    SpotTheDifferencesStats stats, double gameSeed) {
        return new SpotTheDifferences(gameContext, stats, variantOf(gameVariant), gameSeed);
    }

    /**
     * @return the variant chosen, normal for a game saved before the variants (30/10/2026)
     */
    static SpotTheDifferencesVariant variantOf(final EnumGameVariant<SpotTheDifferencesVariant> gameVariant) {
        return gameVariant == null ? SpotTheDifferencesVariant.NORMAL : gameVariant.getEnumValue();
    }
}

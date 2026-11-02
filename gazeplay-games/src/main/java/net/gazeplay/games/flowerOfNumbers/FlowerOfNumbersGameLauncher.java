package net.gazeplay.games.flowerOfNumbers;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates the game "FlowerOfNumbers" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). A game saved before the variants (2/11/2026) has none: it is replayed guided, up to 20 (its
 * rules were others: each petal made the number).
 */
public class FlowerOfNumbersGameLauncher implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new Stats(scene, "FlowerOfNumbers");
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new Stats(scene, "FlowerOfNumbers", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return new FlowerOfNumbersGame(gameContext, stats, maxNumberOf(gameVariant), modeOf(gameVariant));
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return new FlowerOfNumbersGame(gameContext, stats, maxNumberOf(gameVariant), modeOf(gameVariant), gameSeed);
    }

    /**
     * @return the largest number of the heart of the variant, 20 without one
     */
    static int maxNumberOf(final IGameVariant gameVariant) {
        if (gameVariant instanceof GroupedGameVariant grouped
            && grouped.getValue().startsWith(FlowerOfNumbersVariantGenerator.UP_TO)) {
            try {
                return Integer.parseInt(grouped.getValue().substring(FlowerOfNumbersVariantGenerator.UP_TO.length()));
            } catch (final NumberFormatException e) {
                return 20;
            }
        }
        return 20;
    }

    /**
     * @return the mode of the variant, guided without one
     */
    static FlowerMode modeOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? FlowerMode.of(grouped.getSubGroup())
            : FlowerMode.GUIDED;
    }
}

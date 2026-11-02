package net.gazeplay.games.egg;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates the game "EggGame" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). A variant of before the review (2/11/2026: a number of steps and "Classic" or "ImageShrink")
 * is played with its number of steps, with the animals.
 */
public class EggGameGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new EggGameStats(scene, "egg");
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new EggGameStats(scene, "egg", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return new EggGame(gameContext, stats, stepsOf(gameVariant), contentOf(gameVariant));
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats,
                                    double gameSeed) {
        return new EggGame(gameContext, stats, stepsOf(gameVariant), contentOf(gameVariant), gameSeed);
    }

    /**
     * @return the number of fixations hatching an egg, 3 for an unknown variant
     */
    static int stepsOf(final IGameVariant gameVariant) {
        if (gameVariant instanceof GroupedGameVariant grouped
            && grouped.getValue().startsWith(EggGameVariantGenerator.STEPS)) {
            try {
                return Integer.parseInt(grouped.getValue().substring(EggGameVariantGenerator.STEPS.length()));
            } catch (final NumberFormatException e) {
                return 3;
            }
        }
        if (gameVariant instanceof IntStringGameVariant former) {
            return Math.max(1, former.getNumber());
        }
        return 3;
    }

    static EggContent contentOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? EggContent.of(grouped.getSubGroup())
            : EggContent.ANIMALS;
    }
}

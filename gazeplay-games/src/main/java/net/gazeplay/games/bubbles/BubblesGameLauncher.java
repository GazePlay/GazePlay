package net.gazeplay.games.bubbles;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
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
 * Creates the game "Bubbles" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). A variant of before the review (2/11/2026: only a direction) plays colors, bursting at once.
 */
public class BubblesGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(final Scene scene) {
        return new BubblesGamesStats(scene);
    }

    @Override
    public Stats createSavedStats(final Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new BubblesGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final IGameVariant gameVariant,
                                       final Stats stats) {
        return new BubblesGame(gameContext, stats, contentOf(gameVariant), directionOf(gameVariant),
            burstOf(gameVariant));
    }

    @Override
    public GameLifeCycle replayGame(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                                    double gameSeed) {
        return new BubblesGame(gameContext, stats, contentOf(gameVariant), directionOf(gameVariant),
            burstOf(gameVariant), gameSeed);
    }

    static BubblesContent contentOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? BubblesContent.of(grouped.getSubGroup())
            : BubblesContent.COLORS;
    }

    static BubblesBurst burstOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? BubblesBurst.of(grouped.getGroup())
            : BubblesBurst.AT_ONCE;
    }

    static BubblesGameVariant directionOf(final IGameVariant gameVariant) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return BubblesGameVariant.of(grouped.getValue());
        }
        if (gameVariant instanceof EnumGameVariant<?> former
            && former.getEnumValue() instanceof BubblesGameVariant direction) {
            return direction;
        }
        return BubblesGameVariant.TOP;
    }
}

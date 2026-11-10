package net.gazeplay.games.rockPaperScissors;

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

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates "Pierre papier ciseaux" (name code RockPaperScissors) and its statistics, for a new game or for the replay of
 * a saved one (see {@code IGameLauncher}). A variant of before the review of 10/11/2026 is read as told in
 * {@link RockPaperScissorsGameVariant}.
 */
public class RockPaperScissorsLauncher implements IGameLauncher<RockPaperScissorsStats, IGameVariant> {

    @Override
    public RockPaperScissorsStats createNewStats(final Scene scene) {
        return new RockPaperScissorsStats(scene);
    }

    @Override
    public RockPaperScissorsStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach,
                                                   int nbUnCountedGoalsReached,
                                                   ArrayList<LinkedList<FixationPoint>> fixationSequence,
                                                   LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport,
                                                   SavedStatsInfo savedStatsInfo) {
        return new RockPaperScissorsStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached,
            fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final IGameVariant gameVariant,
                                       final RockPaperScissorsStats stats) {
        return new RockPaperScissorsGame(gameContext, stats, modeOf(gameVariant), endOf(gameVariant), -1);
    }

    @Override
    public GameLifeCycle replayGame(final IGameContext gameContext, final IGameVariant gameVariant,
                                    final RockPaperScissorsStats stats, final double gameSeed) {
        return new RockPaperScissorsGame(gameContext, stats, modeOf(gameVariant), endOf(gameVariant), gameSeed);
    }

    static RockPaperScissorsMode modeOf(final IGameVariant gameVariant) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return RockPaperScissorsMode.of(grouped.getSubGroup());
        }
        if (gameVariant instanceof EnumGameVariant<?> old
            && old.getEnumValue() instanceof RockPaperScissorsGameVariant former) {
            return former.getMode();
        }
        return RockPaperScissorsMode.VERSUS;
    }

    static RockPaperScissorsEnd endOf(final IGameVariant gameVariant) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return RockPaperScissorsEnd.of(grouped.getValue());
        }
        if (gameVariant instanceof EnumGameVariant<?> old
            && old.getEnumValue() instanceof RockPaperScissorsGameVariant former) {
            return former.getEnd();
        }
        return RockPaperScissorsEnd.WINS_3;
    }
}

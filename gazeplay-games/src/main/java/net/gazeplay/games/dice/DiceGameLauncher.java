package net.gazeplay.games.dice;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates the game "Dés" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). A variant of before the review of 12/11/2026 (a number of dice, "IntGameVariant") throws that
 * many dice; without a variant, counting with 1 die, without end, looking again after a wrong total.
 */
public class DiceGameLauncher implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new Stats(scene, "dice");
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new Stats(scene, "dice", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return create(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return create(gameContext, gameVariant, stats, gameSeed);
    }

    static Dice create(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                       final double gameSeed) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            final DiceMode mode = DiceMode.of(grouped.getGroup());
            final DiceMistakes mistakes = DiceMistakes.of(grouped.getExtras().isEmpty() ? null
                : grouped.getExtras().get(0));
            if (mode == DiceMode.THROW) {
                return new Dice(gameContext, stats, mode, throwDice(grouped.getValue()), DiceEnd.ENDLESS, mistakes,
                    gameSeed);
            }
            return new Dice(gameContext, stats, mode, DiceLevel.of(grouped.getValue()).getDice(),
                DiceEnd.of(grouped.getSubGroup()), mistakes, gameSeed);
        }
        if (gameVariant instanceof IntGameVariant number) {
            return new Dice(gameContext, stats, DiceMode.THROW, clamp(number.getNumber()), DiceEnd.ENDLESS,
                DiceMistakes.SEARCH_AGAIN, gameSeed);
        }
        return new Dice(gameContext, stats, DiceMode.COUNT, DiceLevel.EASY.getDice(), DiceEnd.ENDLESS,
            DiceMistakes.SEARCH_AGAIN, gameSeed);
    }

    /**
     * @return the dice of a button of the throwing mode ("Dice4": 4), 2 for an unknown one
     */
    static int throwDice(final String key) {
        for (int dice = 1; dice <= DiceGameVariantGenerator.MOST_DICE; dice++) {
            if (DiceGameVariantGenerator.throwKey(dice).equals(key)) {
                return dice;
            }
        }
        return 2;
    }

    private static int clamp(final int dice) {
        return Math.max(1, Math.min(DiceGameVariantGenerator.MOST_DICE, dice));
    }
}

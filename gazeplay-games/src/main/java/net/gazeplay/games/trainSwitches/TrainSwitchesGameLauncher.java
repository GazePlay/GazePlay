package net.gazeplay.games.trainSwitches;

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
 * Creates "Aiguillages" (name code TrainSwitches) and its statistics, for a new game or for the replay of a saved one
 * (see {@code IGameLauncher}). With a variant of before the review of 10/11/2026 (a number of stations, a mode and a
 * number of trains): 3 stations are played easy, 8 normal, 13 hard; "Pause" with a pause before each switch, "Unique"
 * one at a time, the others at a steady pace ("Infinite" without end); the number of trains gives the closest end.
 */
public class TrainSwitchesGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(final Scene scene) {
        return new TrainSwitchesStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                                  ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle,
                                  RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new TrainSwitchesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached,
            fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final IGameVariant gameVariant,
                                       final Stats stats) {
        return new TrainSwitches(gameContext, stats, levelOf(gameVariant), rhythmOf(gameVariant), endOf(gameVariant),
            -1);
    }

    @Override
    public GameLifeCycle replayGame(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                                    final double gameSeed) {
        return new TrainSwitches(gameContext, stats, levelOf(gameVariant), rhythmOf(gameVariant), endOf(gameVariant),
            gameSeed);
    }

    static TrainSwitchesLevel levelOf(final IGameVariant gameVariant) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return TrainSwitchesLevel.of(grouped.getSubGroup());
        }
        if (gameVariant instanceof IntStringGameVariant old) {
            return old.getNumber() <= 3 ? TrainSwitchesLevel.EASY
                : old.getNumber() <= 8 ? TrainSwitchesLevel.NORMAL : TrainSwitchesLevel.HARD;
        }
        return TrainSwitchesLevel.NORMAL;
    }

    static TrainSwitchesRhythm rhythmOf(final IGameVariant gameVariant) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return grouped.getExtras().isEmpty() ? TrainSwitchesRhythm.ONE_AT_A_TIME
                : TrainSwitchesRhythm.of(grouped.getExtras().get(0));
        }
        if (gameVariant instanceof IntStringGameVariant old) {
            return switch (String.valueOf(old.getStringValue())) {
                case "PauseTrain" -> TrainSwitchesRhythm.PAUSE;
                case "UniqueTrain" -> TrainSwitchesRhythm.ONE_AT_A_TIME;
                default -> TrainSwitchesRhythm.CONTINUOUS;
            };
        }
        return TrainSwitchesRhythm.ONE_AT_A_TIME;
    }

    static TrainSwitchesEnd endOf(final IGameVariant gameVariant) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return TrainSwitchesEnd.of(grouped.getValue());
        }
        if (gameVariant instanceof IntStringGameVariant old) {
            return "InfiniteTrain".equals(old.getStringValue()) ? TrainSwitchesEnd.ENDLESS
                : TrainSwitchesEnd.closestTo(old.getNumber2());
        }
        return TrainSwitchesEnd.TRAINS_10;
    }
}

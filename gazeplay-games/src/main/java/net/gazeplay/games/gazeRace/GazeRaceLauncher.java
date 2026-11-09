package net.gazeplay.games.gazeRace;

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
 * Creates the game "GazeRace" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). With a variant of before the review of 9/11/2026 (horizontal or vertical only): that road, the
 * normal level, without end.
 */
public class GazeRaceLauncher implements IGameLauncher<GazeRaceStats, IGameVariant> {

    @Override
    public GazeRaceStats createNewStats(Scene scene) {
        return new GazeRaceStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, GazeRaceStats stats) {
        return new GazeRace(gameContext, stats, roadOf(gameVariant), levelOf(gameVariant), endOf(gameVariant), -1);
    }

    @Override
    public GazeRaceStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new GazeRaceStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, GazeRaceStats stats, double gameSeed) {
        return new GazeRace(gameContext, stats, roadOf(gameVariant), levelOf(gameVariant), endOf(gameVariant), gameSeed);
    }

    static GazeRaceVariant roadOf(final IGameVariant gameVariant) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return GazeRaceVariant.of(grouped.getGroup());
        }
        if (gameVariant instanceof EnumGameVariant<?> old && old.getEnumValue() instanceof GazeRaceVariant road) {
            return road;
        }
        return GazeRaceVariant.HORIZONTAL;
    }

    static GazeRaceLevel levelOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? GazeRaceLevel.of(grouped.getSubGroup())
            : GazeRaceLevel.NORMAL;
    }

    static GazeRaceEnd endOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? GazeRaceEnd.of(grouped.getValue())
            : GazeRaceEnd.ENDLESS;
    }
}

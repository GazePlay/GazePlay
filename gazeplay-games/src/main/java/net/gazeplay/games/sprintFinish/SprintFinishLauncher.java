package net.gazeplay.games.sprintFinish;

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
 * Creates the game "SprintFinish" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). A variant of before the review (a level number, 2/11/2026) is played alone with the gaze, on
 * the normal difficulty.
 */
public class SprintFinishLauncher implements IGameLauncher<Stats, IGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new SprintFinishStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return new SprintFinish(gameContext, stats, modeOf(gameVariant), difficultyOf(gameVariant));
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new SprintFinishStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return new SprintFinish(gameContext, stats, modeOf(gameVariant), difficultyOf(gameVariant), gameSeed);
    }

    static SprintMode modeOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? SprintMode.of(grouped.getSubGroup())
            : SprintMode.SOLO_GAZE;
    }

    static SprintDifficulty difficultyOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? SprintDifficulty.of(grouped.getValue())
            : SprintDifficulty.NORMAL;
    }
}

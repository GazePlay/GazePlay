package net.gazeplay.games.shooter;

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
 * Creates the shooting game and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}). Without a variant: the Biboules, shot at once, without end (the game of before).
 */
public class ShooterGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    /**
     * The scene of the game being created, for its statistics (named as before: "biboule" or "robot").
     */
    private ShooterTheme theme = ShooterTheme.BIBOULES;

    @Override
    public Stats createNewStats(Scene scene) {
        return new ShooterGamesStats(scene, theme.getFolder());
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new ShooterGamesStats(scene, theme.getFolder(), nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        theme = themeOf(gameVariant);
        renameStats(stats);
        return new ShooterGame(gameContext, stats, theme, shotOf(gameVariant), endOf(gameVariant));
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats,
                                    double gameSeed) {
        theme = themeOf(gameVariant);
        renameStats(stats);
        return new ShooterGame(gameContext, stats, theme, shotOf(gameVariant), endOf(gameVariant), gameSeed);
    }

    /**
     * The statistics are made before the game: they take the name of its scene here.
     */
    private void renameStats(final Stats stats) {
        if (stats instanceof ShooterGamesStats shooterStats) {
            shooterStats.setName(theme.getFolder());
        }
    }

    static ShooterTheme themeOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? ShooterTheme.of(grouped.getSubGroup())
            : ShooterTheme.BIBOULES;
    }

    static ShooterShot shotOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? ShooterShot.of(grouped.getGroup())
            : ShooterShot.AT_ONCE;
    }

    static ShooterEnd endOf(final IGameVariant gameVariant) {
        return gameVariant instanceof GroupedGameVariant grouped ? ShooterEnd.of(grouped.getValue())
            : ShooterEnd.ENDLESS;
    }
}

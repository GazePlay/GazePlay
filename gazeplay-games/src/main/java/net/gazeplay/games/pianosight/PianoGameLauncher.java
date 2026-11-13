package net.gazeplay.games.pianosight;

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
import net.gazeplay.games.shooter.ShooterGamesStats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates the Piano and its statistics (folder "Piano", as before), for a new game or for the replay of a saved one
 * (see {@code IGameLauncher}). The variants of before the review are read as the free mode ("Jeu libre") and the songs
 * without end ("Jouer en suivant les instructions").
 */
public class PianoGameLauncher implements IGameLauncher<ShooterGamesStats, IGameVariant> {

    @Override
    public ShooterGamesStats createNewStats(Scene scene) {
        return new ShooterGamesStats(scene, "Piano");
    }

    @Override
    public ShooterGamesStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new ShooterGamesStats(scene, "Piano", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, ShooterGamesStats stats) {
        return create(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, ShooterGamesStats stats,
                                    double gameSeed) {
        return create(gameContext, gameVariant, stats, gameSeed);
    }

    static Piano create(final IGameContext gameContext, final IGameVariant gameVariant,
                        final net.gazeplay.commons.utils.stats.Stats stats, final double gameSeed) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return new Piano(gameContext, stats, PianoMode.of(grouped.getGroup()), PianoEnd.of(grouped.getValue()),
                gameSeed);
        }
        if (gameVariant instanceof EnumGameVariant<?> former && former.getEnumValue() == PianoGameVariant.FREEPLAY) {
            return new Piano(gameContext, stats, PianoMode.FREE, PianoEnd.FREE, gameSeed);
        }
        return new Piano(gameContext, stats, PianoMode.SONG, PianoEnd.ENDLESS, gameSeed);
    }
}

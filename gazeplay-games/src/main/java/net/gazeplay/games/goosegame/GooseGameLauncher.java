package net.gazeplay.games.goosegame;

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
 * Creates "Jeu de l'oie" and its statistics (folder "goosegame", as before), for a new game or for the replay of a saved
 * one (see {@code IGameLauncher}). A variant of before the review of 12/11/2026 (2 to 5 persons, "IntGameVariant"):
 * that many persons, 4 at most, with the classic rules; without a variant, one person against one computer.
 */
public class GooseGameLauncher implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new Stats(scene, "goosegame");
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new Stats(scene, "goosegame", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        return create(gameContext, gameVariant, stats, -1);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        return create(gameContext, gameVariant, stats, gameSeed);
    }

    static GooseGame create(final IGameContext gameContext, final IGameVariant gameVariant, final Stats stats,
                            final double gameSeed) {
        if (gameVariant instanceof GroupedGameVariant grouped) {
            return new GooseGame(gameContext, stats, GooseRules.of(grouped.getGroup()),
                GoosePlayers.of(grouped.getValue()), gameSeed);
        }
        if (gameVariant instanceof IntGameVariant number) {
            return new GooseGame(gameContext, stats, GooseRules.CLASSIC, GoosePlayers.persons(number.getNumber()),
                gameSeed);
        }
        return new GooseGame(gameContext, stats, GooseRules.CLASSIC, GoosePlayers.VS_COMPUTER_1, gameSeed);
    }
}

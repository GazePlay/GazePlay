package net.gazeplay.games.whereisit.launcher;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.CustomWhereIsItVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.whereisit.CustomGame;
import net.gazeplay.commons.whereisit.CustomGames;
import net.gazeplay.games.whereisit.WhereIsIt;
import net.gazeplay.games.whereisit.WhereIsItGameType;
import net.gazeplay.games.whereisit.WhereIsItStats;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Creates the game "WhereIsIt" (a game made by the user, see {@code CustomGames}) and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}).
 */
public class WhereIsItGameLauncher implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new WhereIsItStats(scene, WhereIsItGameType.CUSTOMIZED.getGameName());
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new WhereIsItStats(scene, WhereIsItGameType.CUSTOMIZED.getGameName(), nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       IGameVariant gameVariant, Stats stats) {
        return new WhereIsIt(gameContext, stats, gameOf(gameVariant), hasLives(gameVariant));
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext,
                                       IGameVariant gameVariant, Stats stats, double gameSeed) {
        return new WhereIsIt(gameContext, stats, gameOf(gameVariant), hasLives(gameVariant), gameSeed);
    }

    /**
     * @return the game made by the user of a variant, null when it no longer exists (or for a variant of before
     * 25/11/2026, a folder of pictures): the game then says so
     */
    private static CustomGame gameOf(final IGameVariant gameVariant) {
        return gameVariant instanceof CustomWhereIsItVariant custom ? CustomGames.load(custom.getFolder()) : null;
    }

    private static boolean hasLives(final IGameVariant gameVariant) {
        return gameVariant instanceof CustomWhereIsItVariant custom && custom.hasLives();
    }
}

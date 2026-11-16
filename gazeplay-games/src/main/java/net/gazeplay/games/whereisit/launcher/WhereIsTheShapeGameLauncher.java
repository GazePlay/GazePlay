package net.gazeplay.games.whereisit.launcher;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.whereisit.WhereIsIt;
import net.gazeplay.games.whereisit.WhereIsItVariants;
import net.gazeplay.games.whereisit.WhereIsItGameType;
import net.gazeplay.games.whereisit.WhereIsItStats;

import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Creates the game "WhereIsTheShape" and its statistics, for a new game or for the replay of a saved one (see
 * {@code IGameLauncher}).
 */
public class WhereIsTheShapeGameLauncher implements IGameLauncher<Stats, IGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new WhereIsItStats(scene, WhereIsItGameType.SHAPES_NORMAL.getGameName());
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new WhereIsItStats(scene, WhereIsItGameType.SHAPES_NORMAL.getGameName(), nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats) {
        WhereIsItGameType gameType = switch (WhereIsItVariants.theme(gameVariant, "")) {
            case "Easy" -> WhereIsItGameType.SHAPES_EASY;
            case "Hard" -> WhereIsItGameType.SHAPES_HARD;
            default -> WhereIsItGameType.SHAPES_NORMAL;
        };
        return new WhereIsIt(gameType, WhereIsItVariants.lines(gameVariant),
            WhereIsItVariants.columns(gameVariant), gameContext, stats,
            WhereIsItVariants.end(gameVariant));
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, Stats stats, double gameSeed) {
        WhereIsItGameType gameType = switch (WhereIsItVariants.theme(gameVariant, "")) {
            case "Easy" -> WhereIsItGameType.SHAPES_EASY;
            case "Hard" -> WhereIsItGameType.SHAPES_HARD;
            default -> WhereIsItGameType.SHAPES_NORMAL;
        };
        return new WhereIsIt(gameType, WhereIsItVariants.lines(gameVariant),
            WhereIsItVariants.columns(gameVariant), gameContext, stats,
            WhereIsItVariants.end(gameVariant), gameSeed);
    }
}

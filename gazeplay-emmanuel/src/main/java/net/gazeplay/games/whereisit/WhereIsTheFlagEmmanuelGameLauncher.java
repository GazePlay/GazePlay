package net.gazeplay.games.whereisit;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionDifficultyGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

public class WhereIsTheFlagEmmanuelGameLauncher implements IGameLauncher<Stats, DimensionDifficultyGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new WhereIsItEmmanuelStats(scene, WhereIsItEmmanuelGameType.FLAGS_ALL.getGameName());
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new WhereIsItEmmanuelStats(scene, WhereIsItEmmanuelGameType.FLAGS_ALL.getGameName(), nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, DimensionDifficultyGameVariant gameVariant, Stats stats) {
        WhereIsItEmmanuelGameType gameType = switch (gameVariant.getVariant()) {
            case "Europe" -> WhereIsItEmmanuelGameType.FLAGS_EUROPE;
            case "EuropeAmerica" -> WhereIsItEmmanuelGameType.FLAGS_EUROPE_AMERICA;
            default -> WhereIsItEmmanuelGameType.FLAGS_ALL;
        };
        return new WhereIsItEmmanuel(gameType, gameVariant.getWidth(), gameVariant.getHeight(), false, gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, DimensionDifficultyGameVariant gameVariant, Stats stats, double gameSeed) {
        WhereIsItEmmanuelGameType gameType = switch (gameVariant.getVariant()) {
            case "Europe" -> WhereIsItEmmanuelGameType.FLAGS_EUROPE;
            case "EuropeAmerica" -> WhereIsItEmmanuelGameType.FLAGS_EUROPE_AMERICA;
            default -> WhereIsItEmmanuelGameType.FLAGS_ALL;
        };
        return new WhereIsItEmmanuel(gameType, gameVariant.getWidth(), gameVariant.getHeight(), false, gameContext, stats, gameSeed);
    }
}

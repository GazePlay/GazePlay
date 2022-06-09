package net.gazeplay.games.colors;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class ColorsGameLauncher implements IGameLauncher<Stats, IGameVariant> {

    private ColorsGamesStats gameStat;

    @Override
    public Stats createNewStats(Scene scene) {

        gameStat = new ColorsGamesStats(scene);
        return gameStat;
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        gameStat = new ColorsGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        return gameStat;
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant,
                                       Stats stats) {
        return new ColorsGame(gameContext, gameStat, gameContext.getTranslator());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant,
                                    Stats stats, double gameSeed) {
        return new ColorsGame(gameContext, gameStat, gameContext.getTranslator());
    }
}

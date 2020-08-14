package net.gazeplay.games.goosegame;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.LinkedList;

public class GooseGameLauncher implements GameSpec.GameLauncher<GooseGameStats, GameSpec.IntGameVariant> {
    @Override
    public GooseGameStats createNewStats(Scene scene) {
        return new GooseGameStats(scene, "goosegame");
    }

    @Override
    public GooseGameStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, LinkedList<FixationPoint> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new GooseGameStats(scene, "goosegame", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.IntGameVariant gameVariant,
                                       GooseGameStats stats) {
        return new GooseGame(gameContext, stats, gameVariant.getNumber());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, GameSpec.IntGameVariant gameVariant,
                                       GooseGameStats stats, double gameSeed) {
        return new GooseGame(gameContext, stats, gameVariant.getNumber(), gameSeed);
    }

}

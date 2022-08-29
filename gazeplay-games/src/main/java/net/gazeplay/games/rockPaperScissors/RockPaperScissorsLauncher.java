package net.gazeplay.games.rockPaperScissors;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class RockPaperScissorsLauncher implements IGameLauncher<RockPaperScissorsStats, EnumGameVariant<RockPaperScissorsGameVariant>> {

    @Override
    public RockPaperScissorsStats createNewStats(Scene scene) {
        return new RockPaperScissorsStats(scene);
    }

    @Override
    public RockPaperScissorsStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new RockPaperScissorsStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<RockPaperScissorsGameVariant> gameVariant, RockPaperScissorsStats stats) {
        return new RockPaperScissorsGame(gameContext, stats, gameVariant);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<RockPaperScissorsGameVariant> gameVariant, RockPaperScissorsStats stats, double gameSeed) {
        return new RockPaperScissorsGame(gameContext, stats, gameVariant, gameSeed);
    }
}

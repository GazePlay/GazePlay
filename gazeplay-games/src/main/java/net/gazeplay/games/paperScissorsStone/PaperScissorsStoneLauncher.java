package net.gazeplay.games.paperScissorsStone;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class PaperScissorsStoneLauncher implements IGameLauncher<PaperScissorsStoneStats, IGameVariant> {

    @Override
    public PaperScissorsStoneStats createNewStats(Scene scene) {
        return new PaperScissorsStoneStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IGameVariant gameVariant, PaperScissorsStoneStats stats) {
        return new PaperScissorsStoneGame(gameContext, stats);
    }

    @Override
    public PaperScissorsStoneStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new PaperScissorsStoneStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IGameVariant gameVariant, PaperScissorsStoneStats stats, double gameSeed) {
        return new PaperScissorsStoneGame(gameContext, stats, gameSeed);
    }
}

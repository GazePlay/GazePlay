package net.gazeplay.games.magicpotions;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.LinkedList;

public class PotionsGameLauncher implements GameSpec.GameLauncher<MagicPotionsStats, GameSpec.GameVariant> {

    @Override
    public MagicPotionsStats createNewStats(Scene scene) {
        return new MagicPotionsStats(scene);
    }

    @Override
    public MagicPotionsStats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, LinkedList<FixationPoint> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new MagicPotionsStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, GameSpec.GameVariant gameVariant, MagicPotionsStats stats) {
        return new MagicPotions(gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, GameSpec.GameVariant gameVariant, MagicPotionsStats stats, double gameSeed) {
        return new MagicPotions(gameContext, stats, gameSeed);
    }

}

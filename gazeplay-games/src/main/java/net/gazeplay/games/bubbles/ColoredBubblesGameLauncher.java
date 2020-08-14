package net.gazeplay.games.bubbles;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.LinkedList;

public class ColoredBubblesGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.EnumGameVariant<BubblesGameVariant>> {
    @Override
    public Stats createNewStats(final Scene scene) {
        return new BubblesGamesStats(scene);
    }
    @Override
    public Stats createSavedStats(final Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, LinkedList<FixationPoint> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new BubblesGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final GameSpec.EnumGameVariant<BubblesGameVariant> gameVariant, final Stats stats) {
        return new Bubble(gameContext, BubbleType.COLOR, stats, true, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(final IGameContext gameContext, final GameSpec.EnumGameVariant<BubblesGameVariant> gameVariant, final Stats stats, double gameSeed) {
        return new Bubble(gameContext, BubbleType.COLOR, stats, true, gameVariant.getEnumValue(), gameSeed);
    }
}

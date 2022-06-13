package net.gazeplay.games.bubbles;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ColoredBubblesGameLauncher implements IGameLauncher<Stats, EnumGameVariant<BubblesGameVariant>> {
    @Override
    public Stats createNewStats(final Scene scene) {
        return new BubblesGamesStats(scene);
    }

    @Override
    public Stats createSavedStats(final Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, List<AreaOfInterest> AOIList, SavedStatsInfo savedStatsInfo) {
        return new BubblesGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, AOIList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(final IGameContext gameContext, final EnumGameVariant<BubblesGameVariant> gameVariant, final Stats stats) {
        return new Bubble(gameContext, BubbleType.COLOR, stats, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(final IGameContext gameContext, final EnumGameVariant<BubblesGameVariant> gameVariant, final Stats stats, double gameSeed) {
        return new Bubble(gameContext, BubbleType.COLOR, stats, gameVariant.getEnumValue(), gameSeed);
    }
}

package net.gazeplay.games.magiccards;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.commons.utils.FixationPoint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MagicCardsGameLauncher implements IGameLauncher<Stats, DimensionGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new MagicCardsGamesStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, List<AreaOfInterest> AOIList, SavedStatsInfo savedStatsInfo) {
        return new MagicCardsGamesStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, AOIList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       DimensionGameVariant gameVariant, Stats stats) {
        return new MagicCards(gameContext, gameVariant.getWidth(), gameVariant.getHeight(), stats);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext,
                                    DimensionGameVariant gameVariant, Stats stats, double gameSeed) {
        return new MagicCards(gameContext, gameVariant.getWidth(), gameVariant.getHeight(), stats, gameSeed);
    }
}

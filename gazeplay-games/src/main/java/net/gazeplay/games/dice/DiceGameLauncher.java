package net.gazeplay.games.dice;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.utils.stats.*;
import net.gazeplay.commons.utils.FixationPoint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DiceGameLauncher implements IGameLauncher<Stats, IntGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new Stats(scene, "dice");
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, List<AreaOfInterest> AOIList, SavedStatsInfo savedStatsInfo) {
        return new Stats(scene, "dice", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, AOIList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       IntGameVariant gameVariant, Stats stats) {
        return new Dice(gameContext, stats, gameVariant.getNumber());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext,
                                    IntGameVariant gameVariant, Stats stats, double gameSeed) {
        return new Dice(gameContext, stats, gameVariant.getNumber(), gameSeed);
    }

}

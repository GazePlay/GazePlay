package net.gazeplay.games.pet;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class PetGameLauncher implements IGameLauncher<Stats, DimensionGameVariant> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new PetStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new PetStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(
        IGameContext gameContext,
        DimensionGameVariant gameVariant,
        Stats stats
    ) {
        return new PetHouse(gameContext, stats);
    }

    @Override
    public GameLifeCycle replayGame(
        IGameContext gameContext,
        DimensionGameVariant gameVariant,
        Stats stats, double gameSeed
    ) {
        return new PetHouse(gameContext, stats, gameSeed);
    }

}

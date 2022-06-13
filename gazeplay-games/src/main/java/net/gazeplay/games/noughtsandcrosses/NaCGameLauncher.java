package net.gazeplay.games.noughtsandcrosses;

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

public class NaCGameLauncher implements IGameLauncher<Stats, EnumGameVariant<NaCGameVariant>> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new NaCStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, List<AreaOfInterest> AOIList, SavedStatsInfo savedStatsInfo) {
        return new NaCStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, AOIList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(
        IGameContext gameContext,
        EnumGameVariant<NaCGameVariant> gameVariant,
        Stats stats
    ) {
        return new NaC(gameContext, stats, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(
        IGameContext gameContext,
        EnumGameVariant<NaCGameVariant> gameVariant,
        Stats stats, double gameSeed
    ) {
        return new NaC(gameContext, stats, gameVariant.getEnumValue());
    }

}

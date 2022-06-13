package net.gazeplay.games.dottodot;

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

public class DotToDotGameLauncher implements IGameLauncher<Stats, EnumGameVariant<DotToDotGameVariant>> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new DotToDotGameStats(scene);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<DotToDotGameVariant> gameVariant, Stats stats) {
        return new DotToDot(gameContext, gameVariant.getEnumValue(), stats);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, List<AreaOfInterest> AOIList, SavedStatsInfo savedStatsInfo) {
        return new DotToDotGameStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, AOIList, savedStatsInfo);
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<DotToDotGameVariant> gameVariant, Stats stats, double gameSeed) {
        return new DotToDot(gameContext, gameVariant.getEnumValue(), stats);
    }
}

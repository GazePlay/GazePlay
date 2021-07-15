package net.gazeplay.games.ninja;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

public class NinjaGameLauncher implements IGameLauncher<Stats, EnumGameVariant<NinjaGameVariant>> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new NinjaStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new NinjaStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<NinjaGameVariant> gameVariant, Stats stats) {
        return new Ninja(gameContext, stats, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<NinjaGameVariant> gameVariant, Stats stats, double gameSeed) {
        return new Ninja(gameContext, stats, gameVariant.getEnumValue(), gameSeed);
    }

}

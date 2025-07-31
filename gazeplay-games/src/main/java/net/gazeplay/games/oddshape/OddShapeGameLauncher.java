package net.gazeplay.games.oddshape;


import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;
import org.apache.poi.ss.formula.functions.Odd;

import java.util.ArrayList;
import java.util.LinkedList;

public class OddShapeGameLauncher implements IGameLauncher<Stats, EnumGameVariant<OddShapeVariant>> {

    @Override
    public Stats createNewStats(Scene scene) {
        return new OddShapeStats(scene);
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new OddShapeStats(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, EnumGameVariant<OddShapeVariant> gameVariant,
                                       Stats stats) {
        return new OddShapeGame(gameContext, stats, gameVariant.getEnumValue());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, EnumGameVariant<OddShapeVariant> gameVariant,
                                    Stats stats, double gameSeed) {
        return new OddShapeGame(gameContext, stats, gameVariant.getEnumValue());
    }
}

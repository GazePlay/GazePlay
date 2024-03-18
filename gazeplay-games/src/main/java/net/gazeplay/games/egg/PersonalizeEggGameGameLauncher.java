package net.gazeplay.games.egg;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

public class PersonalizeEggGameGameLauncher implements IGameLauncher<Stats, IntStringGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new EggGameStats(scene, "personalize");
    }

    @Override
    public Stats createSavedStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        return new EggGameStats(scene, "personalize", nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext, IntStringGameVariant gameVariant,
                                       Stats stats) {
        return new EggGame(gameContext, stats, gameVariant.getNumber(), "personalize", gameVariant.getStringValue());
    }

    @Override
    public GameLifeCycle replayGame(IGameContext gameContext, IntStringGameVariant gameVariant,
                                    Stats stats, double gameSeed) {
        return new EggGame(gameContext, stats, gameVariant.getNumber(), "personalize", gameSeed,gameVariant.getStringValue());
    }
}

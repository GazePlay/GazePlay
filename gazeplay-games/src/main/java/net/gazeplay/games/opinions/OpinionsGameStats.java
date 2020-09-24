package net.gazeplay.games.opinions;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.LinkedList;

public class OpinionsGameStats extends Stats {

    public OpinionsGameStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "Opinions-game";
    }

    public OpinionsGameStats(Scene gameContextScene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(gameContextScene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        this.gameName = "Opinions-game";
    }

    public void incrementNumberOfGoalsReached(int increment) {
        nbGoalsReached = increment;
    }
}

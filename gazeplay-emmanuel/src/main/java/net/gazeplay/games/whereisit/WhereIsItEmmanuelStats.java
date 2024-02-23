package net.gazeplay.games.whereisit;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.stats.HiddenItemsGamesStats;

import java.util.ArrayList;
import java.util.LinkedList;

public class WhereIsItEmmanuelStats extends HiddenItemsGamesStats {

    public WhereIsItEmmanuelStats(Scene scene, String gameName) {
        super(scene);
        this.gameName = gameName;
    }

    public WhereIsItEmmanuelStats(Scene scene, String gameName, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        this.gameName = gameName;
    }

    protected void setName(String gameName) {
        this.gameName = gameName;
    }

}

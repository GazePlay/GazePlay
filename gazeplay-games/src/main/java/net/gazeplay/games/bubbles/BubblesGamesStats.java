package net.gazeplay.games.bubbles;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.stats.ShootGamesStats;

import java.util.ArrayList;
import java.util.LinkedList;

public class BubblesGamesStats extends ShootGamesStats {

    public BubblesGamesStats(Scene scene) {
        super(scene);
        this.gameName = "bubbles";
        setAccidentalShotPreventionPeriod(0);
    }

    public BubblesGamesStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        this.gameName = "bubbles";
        setAccidentalShotPreventionPeriod(0);
    }

}

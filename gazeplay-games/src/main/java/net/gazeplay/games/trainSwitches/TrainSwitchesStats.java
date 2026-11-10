package net.gazeplay.games.trainSwitches;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.stats.ShootGamesStats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Statistics of the game "TrainSwitches", with the info-game.csv file of the games where targets are hit (see
 * {@code ShootGamesStats}): a train at the right station is a goal, at a wrong one a mistake.
 */
public class TrainSwitchesStats extends ShootGamesStats {

    public TrainSwitchesStats(Scene scene) {
        super(scene);
        this.gameName = "TrainSwitches";
        setAccidentalShotPreventionPeriod(0);
    }

    public TrainSwitchesStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                              ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle,
                              RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle,
            roundsDurationReport, savedStatsInfo);
        this.gameName = "TrainSwitches";
        setAccidentalShotPreventionPeriod(0);
    }
}

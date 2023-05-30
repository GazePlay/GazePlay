package net.gazeplay.games.surviveAgainstRobots;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.stats.SelectionGamesStats;

import java.util.ArrayList;
import java.util.LinkedList;

public class SurviveAgainstRobotsStats extends SelectionGamesStats {

    public SurviveAgainstRobotsStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "SurviveAgainstRobots";
        setAccidentalShotPreventionPeriod(0);
    }

    public SurviveAgainstRobotsStats(Scene gameContextScene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(gameContextScene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        this.gameName = "SurviveAgainstRobots";
        setAccidentalShotPreventionPeriod(0);
    }
}

package net.gazeplay.games.rushhour;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.stats.SelectionGamesStats;

import java.util.ArrayList;
import java.util.LinkedList;

public class RushHourEmmanuelStats extends SelectionGamesStats {

    public RushHourEmmanuelStats(Scene scene)  {
        super(scene);
        this.gameName = "rushHour";
        setAccidentalShotPreventionPeriod(0);
    }

    public RushHourEmmanuelStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        this.gameName = "rushHour";
        setAccidentalShotPreventionPeriod(0);
    }

}

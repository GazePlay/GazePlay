package net.gazeplay.games.goosegame;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GooseGameStats extends Stats {


    public GooseGameStats(Scene gameContextScene, String gameName) {
        super(gameContextScene, gameName);
    }

    public GooseGameStats(Scene gameContextScene, String gameName, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, List<AreaOfInterest> AOIList, SavedStatsInfo savedStatsInfo) {
        super(gameContextScene, gameName, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, AOIList, savedStatsInfo);
    }

    public void incrementNumberOfGoalsReached(int i) {
        nbGoalsReached += i;
        this.notifyNextRound();
    }
}

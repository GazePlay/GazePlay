package net.gazeplay.games.biboulejump;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BibouleJumpStats extends Stats {

    public BibouleJumpStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "biboule-jump";
    }

    public BibouleJumpStats(Scene gameContextScene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, List<AreaOfInterest> AOIList, SavedStatsInfo savedStatsInfo) {
        super(gameContextScene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, AOIList, savedStatsInfo);
        this.gameName = "biboule-jump";
    }

    public void incrementNumberOfGoalsReached(int increment) {
        nbGoalsReached += increment;
    }
}

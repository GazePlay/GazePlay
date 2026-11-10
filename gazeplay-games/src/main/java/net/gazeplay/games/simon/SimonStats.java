package net.gazeplay.games.simon;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.games.shooter.ShooterGamesStats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Statistics of "Répète après moi" (see {@code ShooterGamesStats}, the same files as before): a melody to repeat is a
 * goal to reach, a melody repeated without a mistake a goal reached (in free mode, each note added). Saved in the
 * folder "simon" since the review of 10/11/2026: they were saved before in the folder "Piano", with those of the Piano
 * game (those saved files stay there).
 */
public class SimonStats extends ShooterGamesStats {

    static final String NAME = "simon";

    public SimonStats(Scene scene) {
        super(scene, NAME);
    }

    public SimonStats(Scene scene, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached,
                      ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle,
                      RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(scene, NAME, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle,
            roundsDurationReport, savedStatsInfo);
    }
}

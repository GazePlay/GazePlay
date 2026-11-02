package net.gazeplay.games.shooter;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.LifeCycle;
import net.gazeplay.commons.utils.stats.RoundsDurationReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.stats.ShootGamesStats;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Statistics of the shooting game ("biboule" or "robot", its scene), with the info-game.csv file of the games where
 * targets are hit (see {@code ShootGamesStats}).
 */
public class ShooterGamesStats extends ShootGamesStats {

    public ShooterGamesStats(Scene scene, String gameType) {
        super(scene);
        this.gameName = gameType;
        setAccidentalShotPreventionPeriod(0);
    }

    public ShooterGamesStats(Scene scene, String gameType, int nbGoalsReached, int nbGoalsToReach, int nbUnCountedGoalsReached, ArrayList<LinkedList<FixationPoint>> fixationSequence, LifeCycle lifeCycle, RoundsDurationReport roundsDurationReport, SavedStatsInfo savedStatsInfo) {
        super(scene, nbGoalsReached, nbGoalsToReach, nbUnCountedGoalsReached, fixationSequence, lifeCycle, roundsDurationReport, savedStatsInfo);
        this.gameName = gameType;
        setAccidentalShotPreventionPeriod(0);
    }


    /**
     * Names the statistics after the scene of the game (they are made before the game).
     */
    void setName(final String name) {
        this.gameName = name;
    }
}

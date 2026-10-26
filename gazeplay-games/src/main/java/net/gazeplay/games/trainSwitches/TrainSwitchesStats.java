package net.gazeplay.games.trainSwitches;

import javafx.scene.Scene;
import net.gazeplay.stats.ShootGamesStats;

/**
 * Statistics of the game "TrainSwitches", with the info-game.csv file of the games where targets are hit (see
 * {@code ShootGamesStats}).
 */
public class TrainSwitchesStats extends ShootGamesStats {

    public TrainSwitchesStats(Scene scene){
        super(scene);
        this.gameName = "TrainSwitches";
        setAccidentalShotPreventionPeriod(0);
    }
}

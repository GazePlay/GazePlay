package net.gazeplay.games.trainSwitches;

import javafx.scene.Scene;
import net.gazeplay.stats.ShootGamesStats;

public class TrainSwitchesStats extends ShootGamesStats {

    public TrainSwitchesStats(Scene scene){
        super(scene);
        this.gameName = "TrainSwitches";
        setAccidentalShotPreventionPeriod(0);
    }
}

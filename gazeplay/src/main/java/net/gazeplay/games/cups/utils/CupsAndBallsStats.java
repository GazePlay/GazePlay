package net.gazeplay.games.cups.utils;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.stats.HiddenItemsGamesStats;

@Slf4j
public class CupsAndBallsStats extends HiddenItemsGamesStats {

    public CupsAndBallsStats(Scene scene) {
        super(scene);
        this.gameName = "cups";
    }

}

package net.gazeplay.games.cups.utils;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.HiddenItemsGamesStats;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CupsAndBallsStats extends HiddenItemsGamesStats {

    public CupsAndBallsStats(Scene scene) {
        super(scene);
        this.gameName = "cups";
    }

}

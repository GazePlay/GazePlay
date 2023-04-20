package net.gazeplay.games.CooperativeGame;

import javafx.scene.Scene;
import net.gazeplay.stats.SelectionGamesStats;

public class CooperativeGameStats extends SelectionGamesStats {

    public CooperativeGameStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "CooperativeGame";
        setAccidentalShotPreventionPeriod(0);
    }
}

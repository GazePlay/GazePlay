package net.gazeplay.games.whereisit;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.HiddenItemsGamesStats;

public class WhereIsItStats extends HiddenItemsGamesStats {

    public WhereIsItStats(Scene scene, String gameName) {
        super(scene);
        this.gameName = gameName;
    }

    protected void setName(String gameName) {
        this.gameName = gameName;
    }

}

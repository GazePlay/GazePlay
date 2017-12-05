package net.gazeplay.games.whereisit;

import javafx.scene.Scene;
import net.gazeplay.utils.stats.HiddenItemsGamesStats;

public class WhereIsItStats extends HiddenItemsGamesStats {

    public WhereIsItStats(Scene scene) {

        super(scene);
    }

    protected void setName(String gameName) {

        this.gameName = gameName;
    }
}
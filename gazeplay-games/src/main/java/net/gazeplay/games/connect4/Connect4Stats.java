package net.gazeplay.games.connect4;

import javafx.scene.Scene;
import net.gazeplay.stats.SelectionGamesStats;

public class Connect4Stats extends SelectionGamesStats {

    public Connect4Stats(Scene scene){
        super(scene);
        this.gameName = "connect4";
    }

}

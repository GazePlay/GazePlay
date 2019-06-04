package net.gazeplay.games.space;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.Stats;

public class SpaceGameStats extends Stats {
    public SpaceGameStats(Scene gameContextScene){
        super(gameContextScene);
        this.gameName = "space-game";
    }

    public void incNbGoals(int increment){
        nbGoals += increment;
    }
}

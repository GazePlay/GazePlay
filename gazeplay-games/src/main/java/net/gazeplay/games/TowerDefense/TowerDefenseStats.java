package net.gazeplay.games.TowerDefense;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.Stats;

/**
 * Statistics of the Tower Defense game.
 */
public class TowerDefenseStats extends Stats {

    public TowerDefenseStats(Scene gameContextScene) {
        super(gameContextScene);
        this.gameName = "TowerDefense";
    }
}

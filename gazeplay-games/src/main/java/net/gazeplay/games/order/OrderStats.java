package net.gazeplay.games.order;

import javafx.scene.Scene;
import net.gazeplay.stats.ShootGamesStats;

/**
 * @author vincent
 */
public class OrderStats extends ShootGamesStats {
    public OrderStats(final Scene scene) {
        super(scene);
        this.gameName = "order";
    }
}

package net.gazeplay.games.order;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.ShootGamesStats;

/**
 *
 * @author vincent
 */
public class OrderStats extends ShootGamesStats {
    public OrderStats(Scene scene) {
        super(scene);
        this.gameName = "order";
    }
}

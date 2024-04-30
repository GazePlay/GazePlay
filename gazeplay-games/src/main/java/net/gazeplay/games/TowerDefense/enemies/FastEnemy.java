package net.gazeplay.games.TowerDefense.enemies;

import javafx.scene.image.Image;
import net.gazeplay.games.TowerDefense.maps.Map;

public class FastEnemy extends Enemy {
    public FastEnemy(Map map, double x, double y) {
        super(map, x, y, 50, 10, 1.5/60, new Image("data/towerDefense/images/fastEnemy.png"));
    }
}

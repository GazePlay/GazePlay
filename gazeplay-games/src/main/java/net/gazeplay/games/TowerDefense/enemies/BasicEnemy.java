package net.gazeplay.games.TowerDefense.enemies;

import javafx.scene.image.Image;
import net.gazeplay.games.TowerDefense.maps.Map;

public class BasicEnemy extends Enemy {

    public BasicEnemy(Map map, double x, double y) {
        super(map, x, y,50,5,1.0/60, new Image("data/TowerDefense/images/basicEnemy.png"));
    }
}

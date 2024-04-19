package net.gazeplay.games.TowerDefense;

import javafx.scene.image.Image;

public class TankEnemy extends Enemy{
    public TankEnemy(Map map, double x, double y) {
        super(map, x, y,100,8,1.0/60, new Image("data/TowerDefense/images/tankEnemy.png"));

    }
}

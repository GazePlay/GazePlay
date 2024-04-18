package net.gazeplay.games.TowerDefense;

import javafx.scene.image.Image;

public class FastEnemy extends Enemy{
    public FastEnemy(Map map, double x, double y) {
        super(map, x, y, 50, 10, 1.5/60, new Image("data/TowerDefense/images/fastEnemy.png"));
    }
}

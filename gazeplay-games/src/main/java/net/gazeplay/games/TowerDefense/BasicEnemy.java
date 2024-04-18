package net.gazeplay.games.TowerDefense;

import javafx.scene.image.Image;

public class BasicEnemy extends Enemy{

    public BasicEnemy(Map map, double x, double y) {
        super(map, x, y,50,5,1.0/60, new Image("data/TowerDefense/images/basicEnemy.png"));
    }
}

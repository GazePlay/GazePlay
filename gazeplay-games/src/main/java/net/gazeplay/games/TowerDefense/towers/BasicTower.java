package net.gazeplay.games.TowerDefense.towers;

import net.gazeplay.games.TowerDefense.enemies.Enemy;
import net.gazeplay.games.TowerDefense.Projectile;

import java.util.ArrayList;

/**
 * Tower shooting bullets at the enemies in its range.
 */
public class BasicTower extends Tower {

    public static final String SOUNDS_FIRE = "data/towerDefense/sounds/shot.wav";
    private static final String SOUNDS_BUILD = "data/towerDefense/sounds/build.wav";

    public BasicTower(int col, int row, ArrayList<Projectile> projectiles, ArrayList<Enemy> enemies) {
        super(col, row, projectiles, enemies);
    }

    @Override
    public String getSoundsFire() {
        return SOUNDS_FIRE;
    }

    @Override
    public String getSoundsConstruction() {
        return SOUNDS_BUILD;
    }
}

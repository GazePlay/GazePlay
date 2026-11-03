package net.gazeplay.games.TowerDefense.towers;

import net.gazeplay.games.TowerDefense.enemies.Enemy;
import net.gazeplay.games.TowerDefense.Projectile;

import java.util.ArrayList;

/**
 * The cannon: slow but strong shots, from farther, a bigger and slower dark ball (maintainer's request of 3/11/2026:
 * a role of its own; it used to be the basic tower with another look).
 */
public class CanonTower extends Tower {

    public static final String SOUNDS_FIRE = "data/towerDefense/sounds/canon.wav";
    private static final String SOUNDS_BUILD = "data/towerDefense/sounds/build.wav";

    /**
     * The size of its balls: the game draws them dark (the others are small and white).
     */
    public static final double BALL_SIZE = 0.32;

    public CanonTower(int col, int row, ArrayList<Projectile> projectiles, ArrayList<Enemy> enemies) {
        super(col, row, projectiles, enemies);
        fireTickLimit = 90;
        damage = 18;
        range = 4;
        projSpeed = 3.5 / 60;
        projSize = BALL_SIZE;
        cost = 35;
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

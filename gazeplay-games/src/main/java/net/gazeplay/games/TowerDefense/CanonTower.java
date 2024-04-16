package net.gazeplay.games.TowerDefense;

import java.util.ArrayList;

public class CanonTower extends Tower{

    public static final String SOUNDS_FIRE = "data/towerDefense/sounds/piou.mp3";
    private static final String SOUNDS_BUILD = "data/towerDefense/sounds/craft.mp3";

    public CanonTower(int col, int row, ArrayList<Projectile> projectiles, ArrayList<Enemy> enemies) {
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

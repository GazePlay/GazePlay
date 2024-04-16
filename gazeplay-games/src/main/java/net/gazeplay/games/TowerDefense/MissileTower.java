package net.gazeplay.games.TowerDefense;

import javafx.geometry.Point2D;

import java.util.ArrayList;

public class MissileTower extends Tower{

    public static final String SOUNDS_FIRE = "data/towerDefense/sounds/piou.mp3";
    private static final String SOUNDS_BUILD = "data/towerDefense/sounds/craft.mp3";

    public MissileTower(int col, int row, ArrayList<Projectile> projectiles, ArrayList<Enemy> enemies) {
        super(col, row, projectiles, enemies);
        fireTickLimit = 120;
        damage = 5;
        range = 5;
    }

    @Override
    public void createProjectile(double x, double y, double speedX, double speedY, double size, double damage) {
        projectiles.add(new Missile(x, y, speedX, speedY, rotation, damage));
    }

    @Override
    public String getSoundsFire() {
        return SOUNDS_FIRE;
    }

    @Override
    public String getSoundsConstruction() {
        return SOUNDS_BUILD;
    }

    @Override
    public Point2D getProjectileStart() {
        return new Point2D(col, row);
    }
}

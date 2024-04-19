package net.gazeplay.games.TowerDefense.towers;

import javafx.geometry.Point2D;
import net.gazeplay.games.TowerDefense.enemies.Enemy;
import net.gazeplay.games.TowerDefense.Projectile;

import java.util.ArrayList;

public class DoubleTower extends Tower {

    private boolean shootLeft;

    public static final String SOUNDS_FIRE = "data/towerDefense/sounds/piou.mp3";
    private static final String SOUNDS_BUILD = "data/towerDefense/sounds/craftdouble.mp3";

    public DoubleTower(int col, int row, ArrayList<Projectile> projectiles, ArrayList<Enemy> enemies) {
        super(col, row, projectiles, enemies);
        fireTickLimit = 20;
        shootLeft = true;
        cost = 50;
    }

    @Override
    public Point2D getProjectileStart() {
        // To alternate between 2 barrels
        double centerX = col+0.5;
        double centerY = row+0.5;

        double projX = centerX + (shootLeft ? 0.15:-0.15);
        double projY = centerY;

        double rota = Math.toRadians(rotation);
        double newX = centerX + (projX-centerX)*Math.cos(rota) - (projY-centerY)*Math.sin(rota);
        double newY = centerY + (projX-centerX)*Math.sin(rota) + (projY-centerY)*Math.cos(rota);

        return  new Point2D(newX, newY);
    }

    @Override
    public void resetTick() {
        tick = 0;
        shootLeft = !shootLeft;
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

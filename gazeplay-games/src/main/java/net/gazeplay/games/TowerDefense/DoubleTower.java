package net.gazeplay.games.TowerDefense;

import javafx.geometry.Point2D;

import java.util.ArrayList;

public class DoubleTower extends Tower {

    private boolean shootLeft;

    public DoubleTower(int col, int row, ArrayList<Projectile> projectiles, ArrayList<Enemy> enemies) {
        super(col, row, projectiles, enemies);
        fireTickLimit = 20;
        shootLeft = true;
        cost = 50;
    }

    @Override
    public Point2D getProjectileStart() {
        double centerX = col+0.5;
        double centerY = row+0.5;

        double projX = centerX + (shootLeft ? 0.1:-0.1);
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
}

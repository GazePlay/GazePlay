package net.gazeplay.games.TowerDefense;

import javafx.geometry.Point2D;
import javafx.scene.shape.Ellipse;

import java.util.ArrayList;

public abstract class Tower {

    protected double fireTickLimit;
    protected double damage;
    protected double projSpeed;
    protected double projSize;
    protected int range;
    protected double cost;
    protected final ArrayList<Projectile> projectiles;
    private final ArrayList<Enemy> enemies;
    protected int tick;
    protected final int col;
    protected final int row;
    protected double rotation;

    public Tower(int col, int row, ArrayList<Projectile> projectiles, ArrayList<Enemy> enemies){
        this.col = col;
        this.row = row;
        this.projectiles = projectiles;
        this.enemies = enemies;
        tick = 0;
        rotation = 0;

        fireTickLimit = 40;
        damage = 5;
        cost = 25;
        // In Tile/tick
        projSpeed = 5.0/60;
        projSize = 0.2;
        range = 3;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public void fire(){
        Enemy target = findEnemyInRange();
        if(target!=null){
            double towerCenterX = col + 0.5;
            double towerCenterY = row + 0.5;

            Point2D targetCenter = target.getCenter();

            double tx = targetCenter.getX() - towerCenterX;
            double ty = targetCenter.getY() - towerCenterY;

            double estimatedTicksToReachTarget = Math.sqrt(Math.pow(tx, 2)+Math.pow(ty, 2))/projSpeed;
            double estimatedTargetX = targetCenter.getX() + target.getSpeedX()*estimatedTicksToReachTarget;
            double estimatedTargetY = targetCenter.getY() + target.getSpeedY()*estimatedTicksToReachTarget;

            // Correct translation based on predicted position
            tx = estimatedTargetX - towerCenterX;
            ty = estimatedTargetY - towerCenterY;

            // Aim at enemy
            rotation = Math.toDegrees(Math.atan2(ty, tx)) + 90;

            // Fire a projectile
            if(tick++>= fireTickLimit){
                resetTick();

                double xratio = tx / (Math.abs(tx)+Math.abs(ty));
                double yratio = ty / (Math.abs(tx)+Math.abs(ty));

               createProjectile(getProjectileStart().getX(), getProjectileStart().getY(), xratio*projSpeed, yratio*projSpeed, projSize, damage);
            }
        }
    }

    private Enemy findEnemyInRange(){
        Ellipse ellipse = new Ellipse(col+0.5, row+0.5, range, range);
        for (Enemy enemy : enemies) {
            if(ellipse.contains(enemy.getCenter())){
                return enemy;
            }
        }
        return null;
    }

    public double getRotation() {
        return rotation;
    }
    public Point2D getProjectileStart(){
        return new Point2D(col+0.5, row+0.5);
    }

    public void resetTick(){
        tick = 0;
    }

    public void createProjectile(double x, double y, double speedX, double speedY, double size, double damage){
        projectiles.add(new Projectile(x, y, speedX, speedY, size, damage));
    }

}

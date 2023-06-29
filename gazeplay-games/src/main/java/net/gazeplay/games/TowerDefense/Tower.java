package net.gazeplay.games.TowerDefense;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;

import java.util.ArrayList;

public abstract class Tower {

    protected double fireTickLimit;
    protected int tick;
    protected final int col;
    protected final int row;

    protected double damage;
    protected double projSpeed;
    protected double projSize;
    protected double rotation;
    protected double cost;
    protected int range;

    protected final ArrayList<Projectile> projectiles;
    private final ArrayList<Enemy> enemies;

    public Tower(int col, int row, ArrayList<Projectile> projectiles, ArrayList<Enemy> enemies){
        this.col = col;
        this.row = row;
        this.projectiles = projectiles;
        this.enemies = enemies;
        tick = 0;
        rotation = 0;

        // Tower characteristics
        fireTickLimit = 40;
        damage = 5;
        cost = 25;
        projSpeed = 5.0/60; // In Tile/tick
        projSize = 0.2;
        range = 3;
    }

    public void fire(){
        Enemy target = findEnemyInRange();
        if(target!=null){
            double towerCenterX = getCenter().getX();
            double towerCenterY = getCenter().getY();

            Point2D targetCenter = target.getCenter();

            double tx = targetCenter.getX() - towerCenterX;
            double ty = targetCenter.getY() - towerCenterY;

            // If target isn't frozen, tower needs to shoot ahead of the target
            if(!target.isFrozen()){
                double estimatedTicksToReachTarget = Math.sqrt(Math.pow(tx, 2)+Math.pow(ty, 2))/projSpeed;
                double estimatedTargetX = targetCenter.getX() + target.getSpeedX()*estimatedTicksToReachTarget;
                double estimatedTargetY = targetCenter.getY() + target.getSpeedY()*estimatedTicksToReachTarget;

                // Correct translation based on predicted position
                tx = estimatedTargetX - towerCenterX;
                ty = estimatedTargetY - towerCenterY;
            }

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
        Circle circle = new Circle(getCenter().getX(), getCenter().getY(), range);
        for (Enemy enemy : enemies) {
            if(circle.contains(enemy.getCenter())){
                return enemy;
            }
        }
        return null;
    }

    public double getRotation() {
        return rotation;
    }

    public Point2D getProjectileStart(){
        return getCenter();
    }

    public void resetTick(){
        tick = 0;
    }

    public void createProjectile(double x, double y, double speedX, double speedY, double size, double damage){
        projectiles.add(new Projectile(x, y, speedX, speedY, size, damage));
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public Point2D getCenter(){
        return new Point2D(col + 0.5, row + 0.5);
    }

}

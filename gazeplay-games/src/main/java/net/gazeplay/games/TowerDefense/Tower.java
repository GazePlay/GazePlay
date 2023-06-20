package net.gazeplay.games.TowerDefense;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;

import java.util.ArrayList;

public class Tower {

    private double x;
    private double y;
    private double fireRate;
    private ArrayList<Projectile> projectiles;
    private ArrayList<Enemy> enemies;
    private int tick;
    private Circle range;

    public Tower(double x, double y, ArrayList<Projectile> projectiles, ArrayList<Enemy> enemies){
        this.x = x;
        this.y = y;
        this.projectiles = projectiles;
        this.enemies = enemies;
        tick = 0;

        fireRate = 30;
        range = new Circle(x,y,200);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void fire(){
        tick++;

        if(tick>=fireRate){
            Enemy target = findEnemyInRange();
            if(target!=null){
                tick = 0;
                Point2D tc = target.getCenter();
                double speed = 5;

                double tx = (tc.getX() - x);
                double ty = (tc.getY() - y);

                double xratio = tx/(Math.abs(tx)+Math.abs(ty));
                double yratio = ty/(Math.abs(tx)+Math.abs(ty));

                projectiles.add(new Projectile(x, y, xratio*speed, yratio*speed, 10));
            }
        }
    }

    public Enemy findEnemyInRange(){
        for (Enemy enemy : enemies) {
            if(range.contains(enemy.getCenter())){
                return enemy;
            }
        }
        return null;
    }

}

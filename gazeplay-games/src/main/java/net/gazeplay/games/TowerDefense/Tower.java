package net.gazeplay.games.TowerDefense;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Ellipse;

import java.util.ArrayList;

public class Tower {

    private final DoubleProperty tileWidth;
    private final DoubleProperty tileHeight;
    private double x;
    private double y;
    private double fireRate;
    private double damage;
    private double projSpeed;
    private double projSize;
    private int range;
    private final ArrayList<Projectile> projectiles;
    private final ArrayList<Enemy> enemies;
    private int tick;

    public Tower(double x, double y, DoubleProperty tileWidth, DoubleProperty tileHeight, ArrayList<Projectile> projectiles, ArrayList<Enemy> enemies){
        this.x = x;
        this.y = y;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.projectiles = projectiles;
        this.enemies = enemies;
        tick = 0;

        fireRate = 30;
        damage = 5;
        // In tile unit
        projSpeed = 0.1;
        projSize = 0.2;
        range = 3;
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
                Point2D targetCoord = target.getCenter();

                double startX = x+tileWidth.get()/2;
                double startY = y+tileHeight.get()/2;
                double tx = (targetCoord.getX() - startX);
                double ty = (targetCoord.getY() -startY);

                double xratio = tx/(Math.abs(tx)+Math.abs(ty));
                double yratio = ty/(Math.abs(tx)+Math.abs(ty));

                projectiles.add(new Projectile(startX, startY, xratio*projSpeed*tileWidth.get(), yratio*projSpeed*tileHeight.get(), projSize*tileWidth.get(), damage));
            }
        }
    }

    public Enemy findEnemyInRange(){
        for (Enemy enemy : enemies) {
            Ellipse ellipse = new Ellipse(x+tileWidth.get()/2, y+tileHeight.get()/2, range*tileWidth.get(), range*tileHeight.get());
            if(ellipse.contains(enemy.getCenter())){
                return enemy;
            }
        }
        return null;
    }

}

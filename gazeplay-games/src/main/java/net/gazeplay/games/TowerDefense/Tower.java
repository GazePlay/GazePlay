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
    private int col;
    private int row;

    public Tower(double x, double y, DoubleProperty tileWidth, DoubleProperty tileHeight, ArrayList<Projectile> projectiles, ArrayList<Enemy> enemies){
        this.x = x;
        this.y = y;
        col = (int) (x/tileWidth.get());
        row = (int) (y/tileHeight.get());
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

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public void fire(){
        tick++;

        if(tick>=fireRate){
            Enemy target = findEnemyInRange();
            if(target!=null){
                tick = 0;
                Point2D targetCoord = target.getCenter();

                double startX = col*tileWidth.get() + tileWidth.get()/2;
                double startY = row*tileHeight.get() + tileHeight.get()/2;
                double tx = (targetCoord.getX() - startX);
                double ty = (targetCoord.getY() -startY);

                double xratio = tx/(Math.abs(tx)+Math.abs(ty));
                double yratio = ty/(Math.abs(tx)+Math.abs(ty));

                projectiles.add(new Projectile(col*tileWidth.get()+tileWidth.get()/2, row*tileHeight.get()+tileHeight.get()/2, xratio*projSpeed*tileWidth.get(), yratio*projSpeed*tileHeight.get(), projSize*tileWidth.get(), damage));
            }
        }
    }

    public Enemy findEnemyInRange(){
        Ellipse ellipse = new Ellipse(col*tileWidth.get()+tileWidth.get()/2, row*tileHeight.get()+tileHeight.get()/2, range*tileWidth.get(), range*tileHeight.get());
        for (Enemy enemy : enemies) {
            if(ellipse.contains(enemy.getCenter())){
                return enemy;
            }
        }
        return null;
    }

}

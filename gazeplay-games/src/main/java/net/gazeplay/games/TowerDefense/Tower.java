package net.gazeplay.games.TowerDefense;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Ellipse;

import java.util.ArrayList;

public class Tower {

    private final DoubleProperty tileWidth;
    private final DoubleProperty tileHeight;
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

    public Tower(int col, int row, DoubleProperty tileWidth, DoubleProperty tileHeight, ArrayList<Projectile> projectiles, ArrayList<Enemy> enemies){
        this.col = col;
        this.row = row;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.projectiles = projectiles;
        this.enemies = enemies;
        tick = 0;

        fireRate = 30;
        damage = 5;
        // In tile unit
        projSpeed = 3.0/60;
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
        tick++;

        if(tick>=fireRate){
            Enemy target = findEnemyInRange();
            if(target!=null){
                tick = 0;
                Point2D targetCoord = target.getCenter();

                double startX = col + 0.5;
                double startY = row + 0.5;
                double tx = targetCoord.getX() - startX;
                double ty = targetCoord.getY() - startY;

                double xratio = tx/(Math.abs(tx)+Math.abs(ty));
                double yratio = ty/(Math.abs(tx)+Math.abs(ty));

                projectiles.add(new Projectile(startX, startY, xratio*projSpeed, yratio*projSpeed, projSize, damage));
            }
        }
    }

    public Enemy findEnemyInRange(){
        Ellipse ellipse = new Ellipse(col+0.5, row+0.5, range, range);
        for (Enemy enemy : enemies) {
            if(ellipse.contains(enemy.getCenter())){
                return enemy;
            }
        }
        return null;
    }

}

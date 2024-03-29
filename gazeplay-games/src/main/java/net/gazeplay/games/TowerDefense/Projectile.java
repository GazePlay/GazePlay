package net.gazeplay.games.TowerDefense;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class Projectile {

    private double x;
    private double y;
    private final double size;
    private double speedX;
    private double speedY;
    private double damage;

    public Projectile(double x, double y, double speedX, double speedY, double size, double damage){
        this.x = x;
        this.y = y;
        this.size = size;
        this.speedX = speedX;
        this.speedY = speedY;
        this.damage = damage;
    }

    public void move(){
        x += speedX;
        y += speedY;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSize() {
        return size;
    }

    public double getDamage() {
        return damage;
    }

    public Shape getHitbox(){
        return new Circle(x, y, size);
    }
}

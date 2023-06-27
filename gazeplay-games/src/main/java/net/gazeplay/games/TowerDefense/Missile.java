package net.gazeplay.games.TowerDefense;

import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class Missile extends Projectile {

    private int frameIndex;
    private int tick;
    private final int animationTickLimit = 5;
    private boolean isActive;
    private double rotation;
    Missile(double x, double y, double speedX, double speedY, double rotation, double damage) {
        super(x, y, speedX, speedY, 1, damage);
        frameIndex = 0;
        isActive = true;
        this.rotation = rotation;
    }

    @Override
    public void move() {
        if(isActive){
            super.move();
        }else{
            if(tick++>=animationTickLimit){
                frameIndex++;
                tick = 0;
            }
        }
    }

    public int getFrameIndex(){
        return frameIndex;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public double getRotation() {
        return rotation;
    }

    @Override
    public Shape getHitbox() {
        Rectangle rect = new Rectangle(getX()+0.4, getY()+0.15, 0.2, 0.7);
        rect.setRotate(rotation);
        return rect;
    }
}

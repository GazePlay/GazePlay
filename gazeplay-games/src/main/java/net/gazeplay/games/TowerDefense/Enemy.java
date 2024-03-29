package net.gazeplay.games.TowerDefense;

import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;

import static net.gazeplay.games.TowerDefense.Map.*;

public class Enemy {

    private final Map map;
    private final double reward;
    private double x;
    private double y;
    private double speedX;
    private double speedY;
    private double maxHealth;
    private double currentHealth;
    private boolean reachedEnd;
    private boolean isFrozen;

    public Enemy(Map map, double x, double y){
        this.map = map;
        this.x = x;
        this.y = y;
        reachedEnd = false;
        isFrozen = false;

        maxHealth = 50;
        currentHealth = maxHealth;
        reward = 5;

        // In Tile/tick
        speedX = 1.0/60;
        speedY = 0;
    }

    public void move(){
        double newX = x + speedX;
        if(speedX > 0){
            newX += 1;
        }
        double newY = y + speedY;
        if(speedY > 0){
            newY += 1;
        }

        int col = (int) newX;
        int row = (int) newY;
        // Check if next tile is a ROAD
        if(map.getTile(col, row)==ROAD){
            // Continue in the same direction
            x += speedX;
            y += speedY;
        } else if (map.getTile(col, row)==END){
            // Reached the end
            reachedEnd = true;
        } else {
            //// Change direction
            // Need to finish moving to the tile entirely
            if(speedX>0){
                x = Math.ceil(x);
            }
            if(speedX<0){
                x = Math.floor(x);
            }
            if(speedY>0){
                y =  Math.ceil(y);
            }
            if(speedY<0){
                y = Math.floor(y);
            }

            // Choose new direction
            if(speedX!=0){
                speedY = Math.abs(speedX);
                speedX = 0;
                if(map.getTileAbove((int) x, (int) y)==ROAD){
                    speedY = -speedY;
                }
            } else if (speedY!=0){
                speedX = Math.abs(speedY);
                speedY = 0;
                if(map.getTileLeft((int) x, (int) y)==ROAD){
                    speedX = -speedX;
                }
            }
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Rectangle getHitbox() {
        return new Rectangle(x + 0.3, y + 0.3, 0.4, 0.4);
    }

    public Point2D getCenter(){
        return new Point2D(x + 0.5, y + 0.5);
    }

    public double getRelativeHeath(){
        return currentHealth/maxHealth;
    }

    public void loseHP(double value){
        currentHealth -= value;
    }

    public double getHealth(){
        return currentHealth;
    }

    public double getReward() {
        return reward;
    }

    public boolean reachedEnd() {
        return reachedEnd;
    }

    public void multiplyHealth(double multiplier){
        maxHealth = maxHealth*multiplier;
        currentHealth = maxHealth;
    }

    public double getRotation() {
        double rotation = 0;
        if(speedX>0){
            rotation = 0;
        }else if (speedX<0){
            rotation = 180;
        }else if (speedY<0){
            rotation = -90;
        }else if (speedY>0){
            rotation = 90;
        }
        return rotation;
    }

    public double getSpeedX() {
        return speedX;
    }

    public double getSpeedY() {
        return speedY;
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    public void setFrozen(boolean frozen) {
        isFrozen = frozen;
    }
}

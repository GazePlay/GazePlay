package net.gazeplay.games.TowerDefense;

import static net.gazeplay.games.TowerDefense.Map.END;
import static net.gazeplay.games.TowerDefense.Map.ROAD;

public class Enemy {

    private final Map map;
    private double x;
    private double y;
    private double speedX;
    private double speedY;

    public Enemy(Map map){
        this.map = map;
        this.x = map.getStartX();
        this.y = map.getStartY();
        speedX = 1;
        speedY = 0;
    }

    public void move(){

        double newX = x + speedX;
        if(speedX > 0){
            newX += map.getTileWidth();
        }
        double newY = y + speedY;
        if(speedY > 0){
            newY += map.getTileHeight();
        }

        if(map.getTile(newX, newY)==ROAD){
            // Continue in the same direction
            x += speedX;
            y += speedY;
        } else if (map.getTile(newX, newY)==END){
            // Reached the end
        } else {
            // Change direction
            if(speedX>0){
                x = Math.ceil(x/map.getTileWidth())*map.getTileWidth();
            }
            if(speedX<0){
                x = Math.floor(x/map.getTileWidth())*map.getTileWidth();
            }
            if(speedY>0){
                y =  Math.ceil(y/map.getTileHeight())*map.getTileHeight();
            }
            if(speedY<0){
                y = Math.floor(y/map.getTileHeight())*map.getTileHeight();
            }

            if(speedX!=0){
                speedY = Math.abs(speedX);
                speedX = 0;
                if(map.getTileAbove(x, y)==ROAD){
                    speedY = -speedY;
                }
            } else if (speedY!=0){
                speedX = Math.abs(speedY);
                speedY = 0;
                if(map.getTileLeft(x, y)==ROAD){
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
}

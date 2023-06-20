package net.gazeplay.games.TowerDefense;

public class Projectile {

    private double x;
    private double y;
    private double size;
    private double speedX;
    private double speedY;

    Projectile(double x, double y, double speedX, double speedY, double size){
        this.x = x;
        this.y = y;
        this.size = size;
        this.speedX = speedX;
        this.speedY = speedY;
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
}

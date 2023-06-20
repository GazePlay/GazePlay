package net.gazeplay.games.TowerDefense;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

import static net.gazeplay.games.TowerDefense.Map.*;

public class ResizableCanvas extends Canvas {

    private final GraphicsContext gc;
    private final int[][] map;
    private final ArrayList<Enemy> enemies;
    private final ArrayList<Tower> towers;
    private final ArrayList<Projectile> projectiles;
    private int tileWidth;
    private int tileHeight;

    public ResizableCanvas(int[][] map, ArrayList<Enemy> enemies, ArrayList<Tower> towers, ArrayList<Projectile> projectiles) {
        gc = getGraphicsContext2D();
        this.map = map;
        this.enemies = enemies;
        this.towers = towers;
        this.projectiles = projectiles;
        widthProperty().addListener(evt -> draw());
        heightProperty().addListener(evt -> draw());
    }

    public void draw() {
        gc.clearRect(0,0, getWidth(), getHeight());

        tileWidth = (int) (getWidth()/map[0].length);
        tileHeight = (int) (getHeight()/map.length);

        drawTerrain();
        drawEnemies();
        drawTowers();
        drawProjectiles();
    }
    private void drawTerrain(){
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[0].length; col++) {
                switch (map[row][col]){
                    case GRASS:
                        gc.setFill(Color.LIME);
                        break;
                    case ROAD:
                        gc.setFill(Color.SIENNA);
                        break;
                    case TURRET:
                        gc.setFill(Color.GREY);
                        break;
                    case START:
                        gc.setFill(Color.BLUE);
                        break;
                    case END:
                        gc.setFill(Color.YELLOW);
                        break;
                    default:
                        gc.setFill(Color.LIME);
                        break;
                }
                gc.fillRect(tileWidth*col,tileHeight*row,tileWidth, tileHeight);
            }
        }
    }

    private void drawEnemies(){
        gc.setFill(Color.RED);
        for (Enemy enemy : enemies) {
            gc.fillRect(enemy.getX(), enemy.getY(), tileWidth, tileHeight);
        }

    }

    private void drawTowers(){
        gc.setFill(Color.AQUA);
        for (Tower tower : towers) {
            gc.fillOval(tower.getX(),tower.getY(), tileWidth, tileHeight);
        }
    }

    private void drawProjectiles(){
        gc.setFill(Color.WHITE);
        for (Projectile projectile : projectiles) {
            gc.fillOval(projectile.getX(), projectile.getY(), projectile.getSize(), projectile.getSize());
        }
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }
    
}

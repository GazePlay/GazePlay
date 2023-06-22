package net.gazeplay.games.TowerDefense;

import javafx.beans.property.DoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

import static net.gazeplay.games.TowerDefense.Map.*;

public class ResizableCanvas extends Canvas {

    private final DoubleProperty tileWidth;
    private final DoubleProperty tileHeight;
    private final GraphicsContext gc;
    private final int[][] map;
    private final ArrayList<Enemy> enemies;
    private final ArrayList<Tower> towers;
    private final ArrayList<Projectile> projectiles;

    public ResizableCanvas(int[][] map, DoubleProperty tileWidth, DoubleProperty tileHeight, ArrayList<Enemy> enemies, ArrayList<Tower> towers, ArrayList<Projectile> projectiles) {
        gc = getGraphicsContext2D();
        this.map = map;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.enemies = enemies;
        this.towers = towers;
        this.projectiles = projectiles;
        widthProperty().addListener(evt -> draw());
        heightProperty().addListener(evt -> draw());
    }

    public void draw() {
        gc.clearRect(0,0, getWidth(), getHeight());

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
                gc.fillRect(tileWidth.get()*col,tileHeight.get()*row, tileWidth.get(), tileHeight.get());
            }
        }
    }

    private void drawEnemies(){
        for (Enemy enemy : enemies) {
            gc.setFill(Color.RED);
            gc.fillRect(enemy.getX(), enemy.getY(), tileWidth.get(), tileHeight.get());

            // Draw Health bar
            gc.setFill(Color.WHITE);
            double height = tileHeight.get()/5;
            double offset = 0;
            gc.fillRect(enemy.getX(), enemy.getY() - (height+offset), tileWidth.get(), height);

            gc.setFill(Color.GREEN);
            gc.fillRect(enemy.getX(), enemy.getY() - (height+offset), tileWidth.get()*enemy.getRelativeHeath(), height);
        }
    }

    private void drawTowers(){
        gc.setFill(Color.AQUA);
        for (Tower tower : towers) {
            //gc.fillOval(tower.getX(),tower.getY(), tileWidth.get(), tileHeight.get());
            gc.fillOval(tower.getCol()*tileWidth.get(),tower.getRow()*tileHeight.get(), tileWidth.get(), tileHeight.get());
        }
    }

    private void drawProjectiles(){
        gc.setFill(Color.WHITE);
        for (Projectile projectile : projectiles) {
            gc.fillOval(projectile.getX(), projectile.getY(), projectile.getSize(), projectile.getSize());
        }
    }

}

package net.gazeplay.games.TowerDefense;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

import static net.gazeplay.games.TowerDefense.Map.*;

public class ResizableCanvas extends Canvas {

    private final int[][] map;
    private final ArrayList<Enemy> enemies;
    private int tileWidth;
    private int tileHeight;

    public ResizableCanvas(int[][] map, ArrayList<Enemy> enemies) {
        this.map = map;
        this.enemies = enemies;
        widthProperty().addListener(evt -> draw());
        heightProperty().addListener(evt -> draw());
    }

    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0,0, getWidth(), getHeight());

        tileWidth = (int) (getWidth()/map[0].length);
        tileHeight = (int) (getHeight()/map.length);

        drawTerrain();
        drawEnemies();
    }
    private void drawTerrain(){
        GraphicsContext gc = getGraphicsContext2D();
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
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.RED);
        for (Enemy enemy : enemies) {
            gc.fillRect(enemy.getX(), enemy.getY(), tileWidth, tileHeight);
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

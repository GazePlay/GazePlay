package net.gazeplay.games.TowerDefense;

import javafx.beans.property.DoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
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
    private final Image basicTowerImage;
    private final Image basicEnemyImage;
    private final Image dirtImage;
    private final Image grassImage;
    private final Image towerBaseImage;
    private final Image castleImage;
    private final Image arrow1Image;
    private final Image arrow2Image;
    private int arrowTick;
    public ResizableCanvas(int[][] map, DoubleProperty tileWidth, DoubleProperty tileHeight, ArrayList<Enemy> enemies, ArrayList<Tower> towers, ArrayList<Projectile> projectiles) {
        gc = getGraphicsContext2D();
        this.map = map;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.enemies = enemies;
        this.towers = towers;
        arrowTick = 0;
        this.projectiles = projectiles;
        basicTowerImage = new Image("data/TowerDefense/basicTower.png");
        basicEnemyImage = new Image("data/TowerDefense/basicEnemy.png");
        dirtImage = new Image("data/TowerDefense/dirt.png");
        grassImage = new Image("data/TowerDefense/grass.png");
        towerBaseImage = new Image("data/TowerDefense/towerBase.png");
        castleImage = new Image("data/TowerDefense/castle.png");
        arrow1Image = new Image("data/TowerDefense/arrow1.png");
        arrow2Image = new Image("data/TowerDefense/arrow2.png");
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
                        gc.drawImage(grassImage, tileWidth.get()*col,tileHeight.get()*row, tileWidth.get(), tileHeight.get());
                        break;
                    case ROAD:
                        gc.drawImage(dirtImage, tileWidth.get()*col,tileHeight.get()*row, tileWidth.get(), tileHeight.get());
                        break;
                    case TURRET:
                        gc.drawImage(towerBaseImage, tileWidth.get()*col,tileHeight.get()*row, tileWidth.get(), tileHeight.get());
                        break;
                    case START:
                        gc.drawImage(arrowTick++%60 > 30 ? arrow1Image:arrow2Image, tileWidth.get()*col,tileHeight.get()*row, tileWidth.get(), tileHeight.get());
                        break;
                    case END:
                        gc.drawImage(castleImage, tileWidth.get()*col,tileHeight.get()*row, tileWidth.get(), tileHeight.get());
                        break;
                    default:
                        gc.drawImage(grassImage, tileWidth.get()*col,tileHeight.get()*row, tileWidth.get(), tileHeight.get());
                        break;
                }

            }
        }
    }

    private void drawEnemies(){
        for (Enemy enemy : enemies) {
            double x = enemy.getX()*tileWidth.get();
            double y = enemy.getY()*tileHeight.get();

            gc.save();
            gc.translate(enemy.getCenter().getX()*tileWidth.get(),enemy.getCenter().getY()*tileHeight.get());
            gc.rotate(enemy.getRotation());
            gc.drawImage(basicEnemyImage, -tileWidth.get()/2, -tileHeight.get()/2, tileWidth.get(), tileHeight.get());
            gc.restore();
//            gc.setFill(Color.RED);
//            gc.fillRect(x, y, tileWidth.get(), tileHeight.get());

            // Draw Health bar
            gc.setFill(Color.WHITE);
            double height = tileHeight.get()/10;
            gc.fillRect(x, y - height, tileWidth.get(), height);

            gc.setFill(Color.GREEN);
            gc.fillRect(x, y - height, tileWidth.get()*enemy.getRelativeHeath(), height);
        }
    }

    private void drawTowers(){
        gc.setFill(Color.AQUA);
        for (Tower tower : towers) {
            //gc.fillOval(tower.getCol()*tileWidth.get(),tower.getRow()*tileHeight.get(), tileWidth.get(), tileHeight.get());
            gc.save();
            gc.translate((tower.getCol()+0.5)*tileWidth.get(),(tower.getRow()+0.5)*tileHeight.get());
            gc.rotate(tower.getRotation());
            gc.drawImage(basicTowerImage, -tileWidth.get()/2, -tileHeight.get()/2,tileWidth.get(), tileHeight.get());
            gc.restore();
        }
    }

    private void drawProjectiles(){
        gc.setFill(Color.WHITE);
        for (Projectile projectile : projectiles) {
            gc.fillOval(projectile.getX()*tileWidth.get(), projectile.getY()*tileHeight.get(), projectile.getSize()*tileHeight.get(), projectile.getSize()*tileWidth.get());
        }
    }

}

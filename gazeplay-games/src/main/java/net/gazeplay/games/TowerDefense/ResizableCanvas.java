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
    private final Image doubleTowerImage;
    private final Image missileTowerImage;
    private final Image canonTowerImage;
    private final Image missileImage;
    private final Image basicEnemyImage;
    private final Image dirtImage;
    private final Image grassImage;
    private final Image towerBaseImage;
    private final Image castleImage;
    private final Image arrow1Image;
    private final Image arrow2Image;
    private final Image explosionImage;
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
        doubleTowerImage = new Image("data/TowerDefense/doubleTower.png");
        missileTowerImage = new Image("data/TowerDefense/missileTower.png");
        canonTowerImage = new Image("data/TowerDefense/canonTower.png");
        basicEnemyImage = new Image("data/TowerDefense/basicEnemy.png");
        explosionImage = new Image("data/TowerDefense/explosion.png");
        missileImage = new Image("data/TowerDefense/missile.png");
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

            // Show Hitbox
//            Rectangle rect = enemy.getHitbox();
//            gc.setStroke(Color.RED);
//            gc.strokeRect(rect.getX()*tileWidth.get(), rect.getY()*tileHeight.get(), rect.getWidth()*tileWidth.get(), rect.getHeight()*tileHeight.get());

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
            gc.save();
            gc.translate((tower.getCol()+0.5)*tileWidth.get(),(tower.getRow()+0.5)*tileHeight.get());
            gc.rotate(tower.getRotation());
            Image towerImage = basicTowerImage;
            if(tower instanceof BasicTower){
                towerImage = basicTowerImage;
            } else if (tower instanceof DoubleTower) {
                towerImage = doubleTowerImage;
            } else if (tower instanceof MissileTower) {
                towerImage = missileTowerImage;
            } else if (tower instanceof CanonTower) {
                towerImage = canonTowerImage;
            }
            gc.drawImage(towerImage, -tileWidth.get()/2, -tileHeight.get()/2,tileWidth.get(), tileHeight.get());
            gc.restore();
        }
    }

    private void drawProjectiles(){
        gc.setFill(Color.WHITE);
        for (Projectile projectile : projectiles) {
            if(projectile instanceof Missile){
                if(((Missile) projectile).isActive()){
                    // Show Hitbox
//                    Bounds rect = projectile.getHitbox().getBoundsInLocal();
//                    gc.setStroke(Color.RED);
//                    gc.save();
//                    gc.translate((rect.getMinX()+rect.getWidth()/2)*tileWidth.get(), (rect.getMinY()+rect.getHeight()/2)*tileHeight.get());
//                    gc.rotate(((Missile) projectile).getRotation());
//                    gc.strokeRect(-rect.getWidth()*tileWidth.get()/2, -rect.getHeight()*tileHeight.get()/2, rect.getWidth()*tileWidth.get(), rect.getHeight()*tileHeight.get());
//                    gc.restore();

                    gc.save();
                    gc.translate((projectile.getX()+0.5)*tileWidth.get(), (projectile.getY()+0.5)*tileHeight.get());
                    gc.rotate(((Missile) projectile).getRotation());
                    gc.drawImage(missileImage, -tileWidth.get()/2, -tileHeight.get()/2, tileWidth.get(), tileHeight.get());
                    gc.restore();
                }else{
                    gc.drawImage(explosionImage, 96*((Missile) projectile).getFrameIndex(), 0, 96,96, projectile.getX()*tileWidth.get(), projectile.getY()*tileHeight.get(), tileHeight.get(), tileWidth.get());
                }
            }else{
                gc.fillOval(projectile.getX()*tileWidth.get(), projectile.getY()*tileHeight.get(), projectile.getSize()*tileHeight.get(), projectile.getSize()*tileWidth.get());
            }
        }
    }

}

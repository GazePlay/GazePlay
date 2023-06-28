package net.gazeplay.games.TowerDefense;

import javafx.beans.property.DoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;

import static net.gazeplay.games.TowerDefense.Map.*;

public class GameCanvas extends Canvas {

    // UI
    private final DoubleProperty tileWidth;
    private final DoubleProperty tileHeight;
    private final GraphicsContext gc;
    private int arrowTick;

    // GAME
    private final Map map;
    private final ArrayList<Enemy> enemies;
    private final ArrayList<Tower> towers;
    private final ArrayList<Projectile> projectiles;

    // IMAGES
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

    public GameCanvas(DoubleProperty tileWidth, DoubleProperty tileHeight, Map map, ArrayList<Enemy> enemies, ArrayList<Tower> towers, ArrayList<Projectile> projectiles) {
        gc = getGraphicsContext2D();
        this.map = map;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.enemies = enemies;
        this.towers = towers;
        this.projectiles = projectiles;
        arrowTick = 0;

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
        for (int row = 0; row < map.getNbRows(); row++) {
            for (int col = 0; col < map.getNbCols(); col++) {
                Image image = switch (map.getTile(col, row)) {
                    case GRASS -> grassImage;
                    case ROAD -> dirtImage;
                    case TURRET -> towerBaseImage;
                    case START -> arrowTick++ % 60 > 30 ? arrow1Image : arrow2Image;
                    case END -> castleImage;
                    default -> grassImage;
                };
                gc.drawImage(image, tileWidth.get()*col,tileHeight.get()*row, tileWidth.get(), tileHeight.get());
            }
        }
    }

    private void drawEnemies(){
        for (Enemy enemy : enemies) {
            double x = enemy.getX()*tileWidth.get();
            double y = enemy.getY()*tileHeight.get();

            // Draw the enemy
            gc.save();
            gc.translate(enemy.getCenter().getX()*tileWidth.get(),enemy.getCenter().getY()*tileHeight.get());
            gc.rotate(enemy.getRotation());
            gc.drawImage(basicEnemyImage, -tileWidth.get()/2, -tileHeight.get()/2, tileWidth.get(), tileHeight.get());
            gc.restore();

            // Draw Health bar
            gc.setFill(Color.WHITE);
            double healthBarHeight = tileHeight.get()/10;
            gc.fillRect(x, y - healthBarHeight, tileWidth.get(), healthBarHeight);

            gc.setFill(Color.GREEN);
            gc.fillRect(x, y - healthBarHeight, tileWidth.get()*enemy.getRelativeHeath(), healthBarHeight);

            // Show Hitbox
//            Rectangle rect = enemy.getHitbox();
//            gc.setStroke(Color.RED);
//            gc.strokeRect(rect.getX()*tileWidth.get(), rect.getY()*tileHeight.get(), rect.getWidth()*tileWidth.get(), rect.getHeight()*tileHeight.get());
        }
    }

    private void drawTowers(){
        for (Tower tower : towers) {
            Image towerImage = basicTowerImage;
            if (tower instanceof DoubleTower) {
                towerImage = doubleTowerImage;
            } else if (tower instanceof MissileTower) {
                towerImage = missileTowerImage;
            } else if (tower instanceof CanonTower) {
                towerImage = canonTowerImage;
            }

            gc.save();
            gc.translate(tower.getCenter().getX()*tileWidth.get(),tower.getCenter().getY()*tileHeight.get());
            gc.rotate(tower.getRotation());
            gc.drawImage(towerImage, -tileWidth.get()/2, -tileHeight.get()/2, tileWidth.get(), tileHeight.get());
            gc.restore();
        }
    }

    private void drawProjectiles(){
        for (Projectile projectile : projectiles) {
            if(projectile instanceof Missile missile){
                if(missile.isActive()){
                    gc.save();
                    gc.translate((projectile.getX()+0.5)*tileWidth.get(), (projectile.getY()+0.5)*tileHeight.get());
                    gc.rotate(((Missile) projectile).getRotation());
                    gc.drawImage(missileImage, -tileWidth.get()/2, -tileHeight.get()/2, tileWidth.get(), tileHeight.get());
                    gc.restore();

                    // Show Hitbox
//                    Bounds rect = missile.getHitbox().getBoundsInLocal();
//                    gc.setStroke(Color.RED);
//                    gc.save();
//                    gc.translate((rect.getMinX()+rect.getWidth()/2)*tileWidth.get(), (rect.getMinY()+rect.getHeight()/2)*tileHeight.get());
//                    gc.rotate(missile.getRotation());
//                    gc.strokeRect(-rect.getWidth()*tileWidth.get()/2, -rect.getHeight()*tileHeight.get()/2, rect.getWidth()*tileWidth.get(), rect.getHeight()*tileHeight.get());
//                    gc.restore();
                }else{
                    gc.drawImage(explosionImage, 96*missile.getFrameIndex(), 0, 96,96, projectile.getX()*tileWidth.get(), projectile.getY()*tileHeight.get(), tileHeight.get()*1.5, tileWidth.get()*1.5);
                }
            }else{
                gc.setFill(Color.WHITE);
                gc.fillOval(projectile.getX()*tileWidth.get(), projectile.getY()*tileHeight.get(), projectile.getSize()*tileHeight.get(), projectile.getSize()*tileWidth.get());
            }
        }
    }

}

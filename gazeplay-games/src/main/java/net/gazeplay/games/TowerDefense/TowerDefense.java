package net.gazeplay.games.TowerDefense;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;
import java.util.ArrayList;
import java.util.ListIterator;

public class TowerDefense implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;

    private ArrayList<Enemy> enemies;
    private ArrayList<Tower> towers;
    private ArrayList<Projectile> projectiles;
    private ResizableCanvas canvas;
    private Map map;

    private ProgressIndicator progressIndicator;
    private Timeline progressTimeline;

    private DoubleProperty tileWidth;
    private DoubleProperty tileHeight;

    TowerDefense(final IGameContext gameContext, final Stats stats){
        this.gameContext = gameContext;
        this.stats = stats;

        enemies = new ArrayList<>();
        towers = new ArrayList<>();
        projectiles = new ArrayList<>();
        map = new Map(1);

        tileWidth = new SimpleDoubleProperty();
        tileHeight = new SimpleDoubleProperty();
        tileWidth.bind(gameContext.getRoot().widthProperty().divide(map.getNbCols()));
        tileHeight.bind(gameContext.getRoot().heightProperty().divide(map.getNbRows()));

        canvas = new ResizableCanvas(map.getMap(),tileWidth, tileHeight, enemies, towers, projectiles);
        canvas.heightProperty().bind(gameContext.getRoot().heightProperty());
        canvas.widthProperty().bind(gameContext.getRoot().widthProperty());
        gameContext.getRoot().getChildren().add(canvas);

        gameContext.getRoot().addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            createEnemy();
        });

        progressIndicator = new ProgressIndicator(0);
        progressIndicator.setVisible(false);
        gameContext.getRoot().getChildren().add(progressIndicator);

        progressTimeline = new Timeline();
        progressTimeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(progressIndicator.progressProperty(), 1)));

        gameContext.getRoot().addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            boolean inside = false;
            for (Point2D turretsTile : map.getTurretsTiles()) {
                double turretX = turretsTile.getX()*tileWidth.get();
                double turretY = turretsTile.getY()*tileHeight.get();
                if(event.getX() >= turretX && event.getX() <= (turretsTile.getX()+1)*tileWidth.get() && event.getY() >= turretY && event.getY() <= (turretsTile.getY()+1)*tileHeight.get()){
                    inside = true;
                    if(progressTimeline.getStatus()!= Animation.Status.RUNNING){
                        progressIndicator.setMinSize(tileWidth.get(), tileHeight.get());
                        progressIndicator.relocate(turretX, turretY);
                        progressIndicator.setProgress(0);
                        progressIndicator.setVisible(true);

                        progressTimeline.setOnFinished(actionEvent -> {
                            progressIndicator.setVisible(false);
                            int index = getTower(turretX, turretY);
                            if(index==-1){
                                createTower(turretX, turretY);
                            }else{
                                towers.remove(index);
                            }
                        });
                        progressTimeline.play();
                    }
                }
            }
            if(!inside){
                progressTimeline.stop();
                progressIndicator.setVisible(false);
            }
        });

    }

    @Override
    public void launch() {

        gameContext.start();
        stats.notifyNewRoundReady();

        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long l) {

                //// UPDATE

                // Spawn enemies
                if(enemies.isEmpty()){
                    createEnemy();
                }

                // Enemies movement
                for (Enemy enemy : enemies) {
                    enemy.move();
                }

                // Fire projectiles
                for (Tower tower : towers) {
                    tower.fire();
                }

                // Projectile movement
                for (Projectile projectile : projectiles) {
                    projectile.move();
                }

                // Check projectiles colisions
                checkColisions();

                //// RENDER
                canvas.draw();
            }
        };

        gameLoop.start();

    }

    @Override
    public void dispose() {
        gameContext.clear();
    }

    private void createTower(double x, double y){
        Tower tower = new Tower(x, y, tileWidth, tileHeight, projectiles, enemies);
        towers.add(tower);
    }

    private void createEnemy(){
        Enemy enemy = new Enemy(map,map.getStartCol()*tileWidth.get(), map.getStartRow()*tileHeight.get(), tileWidth, tileHeight);
        enemies.add(enemy);
    }

    private int getTower(double x, double y){
        int index = -1;
        for (int i = 0; i < towers.size(); i++) {
            if(Math.abs(towers.get(i).getX()-x)<=10 && Math.abs(towers.get(i).getY()-y)<=10){
                index = i;
            }
        }
        return index;
    }

    private void checkColisions() {
        ListIterator<Enemy> enemyIter = enemies.listIterator();
        while (enemyIter.hasNext()) {
            Enemy enemy = enemyIter.next();
            ListIterator<Projectile> projIter = projectiles.listIterator();
            while (projIter.hasNext()) {
                Projectile proj = projIter.next();
                if (enemy.getHitbox().contains(proj.getX(), proj.getY())) {
                    projIter.remove();
                    // Enemy lose HP
                    enemy.loseHP(proj.getDamage());
                    if (enemy.getHealth() <= 0) {
                        enemyIter.remove();
                    }
                }
                if (proj.getX() < 0 || proj.getX() > canvas.getWidth() || proj.getY() < 0 || proj.getY() > canvas.getHeight()) {
                    //Projectil out of screen
                    projIter.remove();
                }
            }
        }
    }

}

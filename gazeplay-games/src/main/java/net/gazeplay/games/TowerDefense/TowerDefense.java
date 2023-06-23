package net.gazeplay.games.TowerDefense;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.util.Duration;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.ui.I18NButton;
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
    private Label lifeLabel;
    private Label moneyLabel;
    private Label waveLabel;
    private I18NButton waveButton;
    private DoubleProperty money;
    private final double TOWER_COST = 25;
    private final double START_MONEY = 50;
    private final int START_LIFE = 10;
    private int life;
    private int waveCount;
    private int enemyCount;
    private double enemyHealthMultiplier;
    private int enemiesSent;
    private int enemySpawnTickLimit = 120;
    private int enemySpawnTick;

    TowerDefense(final IGameContext gameContext, final Stats stats){
        this.gameContext = gameContext;
        this.stats = stats;

        enemies = new ArrayList<>();
        towers = new ArrayList<>();
        projectiles = new ArrayList<>();
        map = new Map(1);
        money = new SimpleDoubleProperty(START_MONEY);
        life = START_LIFE;
        waveCount = 0;

        tileWidth = new SimpleDoubleProperty(gameContext.getRoot().getWidth());
        tileHeight = new SimpleDoubleProperty(gameContext.getRoot().getHeight());
        tileWidth.bind(gameContext.getRoot().widthProperty().divide(map.getNbCols()));
        tileHeight.bind(gameContext.getRoot().heightProperty().divide(map.getNbRows()));

        canvas = new ResizableCanvas(map.getMap(),tileWidth, tileHeight, enemies, towers, projectiles);
        canvas.heightProperty().bind(gameContext.getRoot().heightProperty());
        canvas.widthProperty().bind(gameContext.getRoot().widthProperty());
        gameContext.getChildren().add(canvas);

        HBox topBar = new HBox();
        topBar.setSpacing(30);
        topBar.setPadding(new Insets(10,0,0,15));
        gameContext.getChildren().add(topBar);

        lifeLabel = createLabel(life+"/"+START_LIFE, "data/TowerDefense/heart.png");
        topBar.getChildren().add(lifeLabel);

        moneyLabel = createLabel(""+START_MONEY, "data/TowerDefense/money.png");
        money.addListener(observable -> moneyLabel.setText(""+money.get()));
        topBar.getChildren().add(moneyLabel);

        waveLabel = createLabel(""+waveCount, "data/TowerDefense/wave.png");
        topBar.getChildren().add(waveLabel);

        ProgressIndicator waveButtonPi = new ProgressIndicator(0);
        waveButtonPi.setMouseTransparent(true);
        waveButtonPi.setVisible(false);
        gameContext.getChildren().add(waveButtonPi);

        Timeline sendWaveTimeline = new Timeline();
        sendWaveTimeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(waveButtonPi.progressProperty(), 1)));
        sendWaveTimeline.setOnFinished(event -> createWave());

        waveButton = new I18NButton(gameContext.getTranslator(), "SendWave");
        tileWidth.addListener(observable -> waveButton.setStyle("-fx-font-size: "+tileWidth.get()/2+";-fx-font-family: 'Agency FB'"));
        topBar.getChildren().add(waveButton);

        waveButton.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
            waveButtonPi.setMinSize(tileWidth.get(), tileHeight.get());
            waveButtonPi.relocate(waveButton.getLayoutX()+waveButton.getWidth()/2-waveButtonPi.getWidth()/2, waveButton.getLayoutY());
            waveButtonPi.setProgress(0);
            waveButtonPi.setVisible(true);
            sendWaveTimeline.play();
        });

        waveButton.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            waveButtonPi.setVisible(false);
            sendWaveTimeline.stop();
        });

        progressIndicator = new ProgressIndicator(0);
        progressIndicator.setVisible(false);
        gameContext.getRoot().getChildren().add(progressIndicator);

        progressTimeline = new Timeline();
        progressTimeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(progressIndicator.progressProperty(), 1)));

        gameContext.getRoot().addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            if(money.get()>=TOWER_COST) {
                boolean inside = false;
                for (Point2D turretsTile : map.getTurretsTiles()) {
                    double turretX = turretsTile.getX() * tileWidth.get();
                    double turretY = turretsTile.getY() * tileHeight.get();
                    if (event.getX() >= turretX && event.getX() <= (turretsTile.getX() + 1) * tileWidth.get() && event.getY() >= turretY && event.getY() <= (turretsTile.getY() + 1) * tileHeight.get()) {
                        inside = true;
                        if (progressTimeline.getStatus() != Animation.Status.RUNNING) {
                            progressIndicator.setMinSize(tileWidth.get(), tileHeight.get());
                            progressIndicator.relocate(turretX, turretY);
                            progressIndicator.setProgress(0);
                            progressIndicator.setVisible(true);

                            progressTimeline.setOnFinished(actionEvent -> {
                                progressIndicator.setVisible(false);
                                int index = getTower(turretX, turretY);
                                if (index == -1) {
                                    createTower((int) turretsTile.getX(), (int) turretsTile.getY());
                                } else {
                                    towers.remove(index);
                                }
                            });
                            progressTimeline.play();
                        }
                    }
                }
                if (!inside) {
                    progressTimeline.stop();
                    progressIndicator.setVisible(false);
                }
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
                spawnEnemies();

                // Move enemies
                moveEnemies();

                // Fire projectiles
                for (Tower tower : towers) {
                    tower.fire();
                }

                // Move projectiles
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

    private void createTower(int col, int row){
        Tower tower = new Tower(col, row, tileWidth, tileHeight, projectiles, enemies);
        money.set(money.get() - TOWER_COST);

        // Play little animation
        ImageView image = new ImageView(new Image("data/TowerDefense/moneyLoss.png"));
        image.setFitWidth(tileWidth.get()/2);
        image.setFitHeight(tileHeight.get()/2);
        image.setTranslateX(tower.getCol()*tileWidth.get());
        image.setTranslateY(tower.getRow()*tileHeight.get());

        FadeTransition ft = new FadeTransition(Duration.millis(1500), image);
        ft.setFromValue(1);
        ft.setToValue(0);

        TranslateTransition tt = new TranslateTransition(Duration.millis(1500), image);
        tt.setFromY(tower.getRow()*tileHeight.get());
        tt.setToY(tower.getRow()*tileHeight.get() + tileHeight.get()/2);

        ft.play();
        tt.play();
        gameContext.getChildren().add(image);

        towers.add(tower);
    }

    private int getTower(double x, double y){
        int index = -1;
        for (int i = 0; i < towers.size(); i++) {
            if(Math.abs(towers.get(i).getCol()*tileWidth.get()-x)<=10 && Math.abs(towers.get(i).getRow()*tileWidth.get()-y)<=10){
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
                if(enemy.getHitbox().intersects(proj.getHitbox().getBoundsInLocal())) {
                    // Enemy lose HP, projectile disappear
                    projIter.remove();
                    enemy.loseHP(proj.getDamage());
                    if (enemy.getHealth() <= 0) {
                        // Enemy dies
                        enemyIter.remove();
                        money.set(money.get() + enemy.getReward());

                        // Play little animation
                        ImageView image = new ImageView(new Image("data/TowerDefense/moneyGain.png"));
                        image.setFitWidth(tileWidth.get()/2);
                        image.setFitHeight(tileHeight.get()/2);
                        image.setTranslateX(enemy.getX()*tileWidth.get());
                        image.setTranslateY(enemy.getCenter().getY()*tileHeight.get());

                        FadeTransition ft = new FadeTransition(Duration.millis(1500), image);
                        ft.setFromValue(1);
                        ft.setToValue(0);

                        TranslateTransition tt = new TranslateTransition(Duration.millis(1500), image);
                        tt.setFromY(enemy.getCenter().getY()*tileHeight.get());
                        tt.setToY(enemy.getY()*tileHeight.get());

                        ft.play();
                        tt.play();
                        gameContext.getChildren().add(image);
                    }
                }
                if (proj.getX() < 0 || proj.getX()*tileWidth.get() > canvas.getWidth() || proj.getY() < 0 || proj.getY()*tileHeight.get() > canvas.getHeight()) {
                    //Projectile out of screen
                    projIter.remove();
                }
            }
        }
    }

    private void moveEnemies(){
        ListIterator<Enemy> enemyIter = enemies.listIterator();
        while (enemyIter.hasNext()) {
            Enemy enemy = enemyIter.next();
            enemy.move();
            if(enemy.reachedEnd()){
                enemyIter.remove();
                loseLife();
            }
        }
    }

    private Label createLabel(String text, String imagePath){
        Label label = new Label(text);
        System.out.println(tileWidth.get());
        tileWidth.addListener(observable -> label.setFont(new Font("Agency FB", tileWidth.divide(2).get())));
        ImageView image = new ImageView(new Image(imagePath));
        image.fitWidthProperty().bind(tileWidth);
        image.fitHeightProperty().bind(tileHeight);
        label.setGraphic(image);
        return label;
    }

    private void loseLife(){
        life--;
        if(life<=0){
            // Game Lost
        }
        lifeLabel.setText(life+"/"+START_LIFE);
    }

    private void createWave(){
        // Alternate between 5 and 10 enemies
        enemiesSent = 0;
        enemyCount = (waveCount%2 + 1)*5;

        // Enemies gains 0.25 health multiplier every 2 waves
        enemyHealthMultiplier = 1 + waveCount/2*0.25;

        waveCount++;
        waveLabel.setText(""+waveCount);
        waveButton.setVisible(false);
    }

    private void spawnEnemies(){
        if (enemiesSent < enemyCount && enemySpawnTick >= enemySpawnTickLimit){
            enemySpawnTick = 0;
            enemiesSent++;
            Enemy enemy = new Enemy(map, map.getStartCol(), map.getStartRow(), tileWidth, tileHeight);
            enemy.multiplyHealth(enemyHealthMultiplier);
            enemies.add(enemy);
        }else{
            enemySpawnTick++;
        }
        if(enemiesSent >= enemyCount){
            waveButton.setVisible(true);
        }
    }

}

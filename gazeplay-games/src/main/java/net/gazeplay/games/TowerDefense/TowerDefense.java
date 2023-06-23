package net.gazeplay.games.TowerDefense;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
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

        for (Point2D turretsTile : map.getTurretsTiles()) {
            Rectangle rectangle = new Rectangle();
            rectangle.xProperty().bind(tileWidth.multiply(turretsTile.getX()));
            rectangle.yProperty().bind(tileHeight.multiply(turretsTile.getY()));
            rectangle.widthProperty().bind(tileWidth);
            rectangle.heightProperty().bind(tileHeight);

            rectangle.setOpacity(0);
            rectangle.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
                if(money.get()>=TOWER_COST){
                    double turretX = turretsTile.getX() * tileWidth.get();
                    double turretY = turretsTile.getY() * tileHeight.get();
                    progressIndicator.setMinSize(tileWidth.get(), tileHeight.get());
                    progressIndicator.relocate(turretX, turretY);
                    progressIndicator.setProgress(0);
                    progressIndicator.setVisible(true);

                    progressTimeline.setOnFinished(actionEvent -> {
                        progressIndicator.setVisible(false);
                        createTowerSelection((int) turretsTile.getX(), (int) turretsTile.getY());
                    });
                    progressTimeline.play();
                }

            });
            rectangle.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
                progressTimeline.stop();
                progressIndicator.setVisible(false);
            });
            gameContext.getChildren().add(rectangle);
        }
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

    private void createTowerSelection(int col, int row){

        Group group = new Group();
        gameContext.getChildren().add(group);

        double MainCircleRadius = 2;
        double TowerIconRadius = (MainCircleRadius-0.5)/2;

        Ellipse ellipse = new Ellipse((col+0.5)*tileWidth.get(), (row+0.5)*tileHeight.get(), MainCircleRadius*tileWidth.get(), MainCircleRadius*tileHeight.get());
        ellipse.setFill(Color.MEDIUMSLATEBLUE);
        ellipse.setOpacity(0.75);
        group.getChildren().add(ellipse);

        ProgressIndicator towerPi = new ProgressIndicator(0);
        towerPi.setVisible(false);
        towerPi.setMouseTransparent(true);
        towerPi.setMinSize(TowerIconRadius*2*tileWidth.get(), TowerIconRadius*2*tileHeight.get());

        Timeline placeTowerTimeline = new Timeline();
        placeTowerTimeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(towerPi.progressProperty(), 1)));
        placeTowerTimeline.setOnFinished(event -> {
            createTower(col, row);
            group.getChildren().clear();
            gameContext.getChildren().remove(group);
        });

        // Above
        Ellipse t1 = new Ellipse((col+0.5)*tileWidth.get(), (row + 0.5 - 2*TowerIconRadius)*tileHeight.get(), TowerIconRadius*tileWidth.get(), TowerIconRadius*tileHeight.get());
        t1.setFill(Color.AZURE);
        group.getChildren().add(t1);
        t1.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
            towerPi.relocate((col+0.5-TowerIconRadius)*tileWidth.get(), (row + 0.5 - 3*TowerIconRadius)*tileHeight.get());
            towerPi.setProgress(0);
            towerPi.setVisible(true);
            placeTowerTimeline.play();
        });
        t1.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            towerPi.setVisible(false);
            placeTowerTimeline.stop();
        });

        // Under
        Ellipse t2 = new Ellipse((col+0.5)*tileWidth.get(), (row + 0.5 + 2*TowerIconRadius)*tileHeight.get(), TowerIconRadius*tileWidth.get(), TowerIconRadius*tileHeight.get());
        t2.setFill(Color.RED);
        group.getChildren().add(t2);
        t2.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
            towerPi.relocate((col+0.5-TowerIconRadius)*tileWidth.get(), (row + 0.5 + TowerIconRadius)*tileHeight.get());
            towerPi.setProgress(0);
            towerPi.setVisible(true);
            placeTowerTimeline.play();
        });
        t2.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            towerPi.setVisible(false);
            placeTowerTimeline.stop();
        });

        // Left
        Ellipse t3 = new Ellipse((col + 0.5 - 2*TowerIconRadius)*tileWidth.get(), (row+0.5)*tileHeight.get(), TowerIconRadius*tileWidth.get(), TowerIconRadius*tileHeight.get());
        t3.setFill(Color.GREEN);
        group.getChildren().add(t3);
        t3.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
            towerPi.relocate((col + 0.5 - 3*TowerIconRadius)*tileWidth.get(), (row+0.5-TowerIconRadius)*tileHeight.get());
            towerPi.setProgress(0);
            towerPi.setVisible(true);
            placeTowerTimeline.play();
        });
        t3.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            towerPi.setVisible(false);
            placeTowerTimeline.stop();
        });

        // Right
        Ellipse t4 = new Ellipse((col + 0.5 + 2*TowerIconRadius)*tileWidth.get(), (row+0.5)*tileHeight.get(), TowerIconRadius*tileWidth.get(), TowerIconRadius*tileHeight.get());
        t4.setFill(Color.PURPLE);
        group.getChildren().add(t4);
        t4.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
            towerPi.relocate((col + 0.5 + TowerIconRadius)*tileWidth.get(), (row+0.5-TowerIconRadius)*tileHeight.get());
            towerPi.setProgress(0);
            towerPi.setVisible(true);
            placeTowerTimeline.play();
        });
        t4.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            towerPi.setVisible(false);
            placeTowerTimeline.stop();
        });

        ProgressIndicator exitPi = new ProgressIndicator(0);
        exitPi.setMouseTransparent(true);
        exitPi.setMinSize(tileWidth.get(), tileHeight.get());
        exitPi.relocate(col*tileWidth.get(), row*tileHeight.get());

        Timeline exitTl = new Timeline();
        exitTl.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(exitPi.progressProperty(), 1)));
        exitTl.setOnFinished(event1 -> {
            group.getChildren().clear();
            gameContext.getChildren().remove(group);
        });

        group.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            exitPi.setProgress(0);
            exitPi.setVisible(true);
            exitTl.play();
        });

        group.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
            exitPi.setVisible(false);
            exitTl.stop();
        });

        group.getChildren().add(towerPi);
        group.getChildren().add(exitPi);

    }

}

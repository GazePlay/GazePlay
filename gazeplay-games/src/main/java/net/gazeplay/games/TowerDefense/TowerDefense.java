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
import javafx.scene.shape.Circle;
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
    private int enemySpawnTickLimit = 60;
    private int enemySpawnTick;
    private Image basicTowerImage;
    private Image doubleTowerImage;
    private Image missileTowerImage;
    private Image canonTowerImage;
    private static final int BASIC_TOWER = 0;
    private static final int DOUBLE_TOWER = 1;
    private static final int MISSILE_TOWER = 2;
    private static final int CANON_TOWER = 3;

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
        basicTowerImage = new Image("data/TowerDefense/basicTower.png");
        doubleTowerImage = new Image("data/TowerDefense/doubleTower.png");
        missileTowerImage = new Image("data/TowerDefense/missileTower.png");
        canonTowerImage = new Image("data/TowerDefense/canonTower.png");

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
        gameContext.getChildren().add(progressIndicator);

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

    private void createTower(int col, int row, int type){
        Tower tower;
        switch (type){
            case BASIC_TOWER -> tower = new BasicTower(col, row, projectiles, enemies);
            case DOUBLE_TOWER -> tower = new DoubleTower(col, row, projectiles, enemies);
            case MISSILE_TOWER -> tower = new MissileTower(col, row, projectiles, enemies);
            case CANON_TOWER -> tower = new CanonTower(col, row, projectiles, enemies);
            default -> tower = new BasicTower(col, row, projectiles, enemies);
        }
        if(money.get()>=tower.cost && getTower(col, row)==null){
            money.set(money.get() - tower.cost);

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
    }

    private Tower getTower(double col, double row){
        for (Tower tower : towers) {
            if(tower.getCol()==col && tower.getRow()==row){
                return tower;
            }
        }
        return null;
    }

    private void checkColisions() {
        ArrayList<Circle> explosions = new ArrayList<>();
        ListIterator<Enemy> enemyIter = enemies.listIterator();
        while (enemyIter.hasNext()) {
            Enemy enemy = enemyIter.next();
            ListIterator<Projectile> projIter = projectiles.listIterator();
            while (projIter.hasNext()) {
                Projectile proj = projIter.next();
                if(enemy.getHitbox().intersects(proj.getHitbox().getBoundsInLocal())) {
                    if(proj instanceof Missile){
                        if(((Missile) proj).getFrameIndex()>=8){
                            projIter.remove();
                        }else{
                            if(((Missile) proj).isActive()){
                                ((Missile) proj).setActive(false);
                                Circle explosionRadius = new Circle(proj.getX()+proj.getSize()/2, proj.getY()+proj.getSize()/2, 1);
                                explosions.add(explosionRadius);
                            }
                        }
                    }else{
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
                }
                if (proj.getX() < 0 || proj.getX()*tileWidth.get() > canvas.getWidth() || proj.getY() < 0 || proj.getY()*tileHeight.get() > canvas.getHeight()) {
                    //Projectile out of screen
                    projIter.remove();
                }
            }
        }

        for (Circle explosion : explosions) {
            ListIterator<Enemy> enemyIter2 = enemies.listIterator();
            while (enemyIter2.hasNext()) {
                Enemy enemy2 = enemyIter2.next();
                if (explosion.intersects(enemy2.getHitbox().getBoundsInLocal())) {
                    // Enemy lose HP
                    enemy2.loseHP(5);
                    if (enemy2.getHealth() <= 0) {
                        // Enemy dies
                        enemyIter2.remove();
                        money.set(money.get() + enemy2.getReward());

                        // Play little animation
                        ImageView image = new ImageView(new Image("data/TowerDefense/moneyGain.png"));
                        image.setFitWidth(tileWidth.get()/2);
                        image.setFitHeight(tileHeight.get()/2);
                        image.setTranslateX(enemy2.getX()*tileWidth.get());
                        image.setTranslateY(enemy2.getCenter().getY()*tileHeight.get());

                        FadeTransition ft = new FadeTransition(Duration.millis(1500), image);
                        ft.setFromValue(1);
                        ft.setToValue(0);

                        TranslateTransition tt = new TranslateTransition(Duration.millis(1500), image);
                        tt.setFromY(enemy2.getCenter().getY()*tileHeight.get());
                        tt.setToY(enemy2.getY()*tileHeight.get());

                        ft.play();
                        tt.play();
                        gameContext.getChildren().add(image);
                    }
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

        double mainCircleRadius = 2;

        Ellipse ellipse = new Ellipse((col+0.5)*tileWidth.get(), (row+0.5)*tileHeight.get(), mainCircleRadius*tileWidth.get(), mainCircleRadius*tileHeight.get());
        ellipse.setFill(Color.MEDIUMSLATEBLUE);
        ellipse.setOpacity(0.75);
        group.getChildren().add(ellipse);

        ProgressIndicator towerPi = new ProgressIndicator(0);
        towerPi.setVisible(false);
        towerPi.setMouseTransparent(true);
        towerPi.setMinSize((mainCircleRadius - 0.5)*tileWidth.get(), (mainCircleRadius - 0.5)*tileHeight.get());

        Timeline placeTowerTimeline = new Timeline();
        placeTowerTimeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(towerPi.progressProperty(), 1)));

        // Above
        ImageView topTower = new ImageView(basicTowerImage);
        topTower.setFitWidth((mainCircleRadius - 0.5)*tileWidth.get());
        topTower.setFitHeight((mainCircleRadius - 0.5)*tileHeight.get());
        topTower.setX((col + 0.5) * tileWidth.get() - topTower.getFitWidth()/2);
        topTower.setY((row + 0.5 - mainCircleRadius) * tileHeight.get());
        group.getChildren().add(topTower);
        topTower.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
            towerPi.relocate(topTower.getX(), topTower.getY());
            towerPi.setProgress(0);
            towerPi.setVisible(true);
            placeTowerTimeline.setOnFinished(eve -> {
                createTower(col, row, BASIC_TOWER);
                group.getChildren().clear();
                gameContext.getChildren().remove(group);
            });
            placeTowerTimeline.play();
        });
        topTower.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            towerPi.setVisible(false);
            placeTowerTimeline.stop();
        });

        // Under
        ImageView downTower = new ImageView(doubleTowerImage);
        downTower.setFitWidth((mainCircleRadius - 0.5)*tileWidth.get());
        downTower.setFitHeight((mainCircleRadius - 0.5)*tileHeight.get());
        downTower.setX((col + 0.5) * tileWidth.get() - downTower.getFitWidth()/2);
        downTower.setY((row + 1) * tileHeight.get());
        group.getChildren().add(downTower);
        downTower.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
            towerPi.relocate(downTower.getX(), downTower.getY());
            towerPi.setProgress(0);
            towerPi.setVisible(true);
            placeTowerTimeline.setOnFinished(eve -> {
                createTower(col, row, DOUBLE_TOWER);
                group.getChildren().clear();
                gameContext.getChildren().remove(group);
            });
            placeTowerTimeline.play();
        });
        downTower.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            towerPi.setVisible(false);
            placeTowerTimeline.stop();
        });

        // Left
        ImageView leftTower = new ImageView(missileTowerImage);
        leftTower.setFitWidth((mainCircleRadius - 0.5)*tileWidth.get());
        leftTower.setFitHeight((mainCircleRadius - 0.5)*tileHeight.get());
        leftTower.setX((col + 0.5 - mainCircleRadius) * tileWidth.get());
        leftTower.setY((row + 0.5)* tileHeight.get() - leftTower.getFitHeight()/2);
        group.getChildren().add(leftTower);
        leftTower.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
            towerPi.relocate(leftTower.getX(), leftTower.getY());
            towerPi.setProgress(0);
            towerPi.setVisible(true);
            placeTowerTimeline.setOnFinished(eve -> {
                createTower(col, row, MISSILE_TOWER);
                group.getChildren().clear();
                gameContext.getChildren().remove(group);
            });
            placeTowerTimeline.play();
        });
        leftTower.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            towerPi.setVisible(false);
            placeTowerTimeline.stop();
        });

        // Right
        ImageView rightTower = new ImageView(canonTowerImage);
        rightTower.setFitWidth((mainCircleRadius - 0.5)*tileWidth.get());
        rightTower.setFitHeight((mainCircleRadius - 0.5)*tileHeight.get());
        rightTower.setX((col + 1) * tileWidth.get());
        rightTower.setY((row + 0.5)* tileHeight.get() - rightTower.getFitHeight()/2);
        group.getChildren().add(rightTower);
        rightTower.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
            towerPi.relocate(rightTower.getX(), rightTower.getY());
            towerPi.setProgress(0);
            towerPi.setVisible(true);
            placeTowerTimeline.setOnFinished(eve -> {
                createTower(col, row, CANON_TOWER);
                group.getChildren().clear();
                gameContext.getChildren().remove(group);
            });
            placeTowerTimeline.play();
        });
        rightTower.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
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

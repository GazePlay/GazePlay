package net.gazeplay.games.TowerDefense;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
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
import javafx.util.Duration;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.utils.stats.Stats;
import java.util.ArrayList;
import java.util.ListIterator;

public class TowerDefense implements GameLifeCycle {

    // GAZEPLAY
    private final IGameContext gameContext;
    private final Stats stats;

    // UI ELEMENTS
    private final GameCanvas canvas;
    private I18NButton sendWaveButton;
    private final DoubleProperty tileWidth;
    private final DoubleProperty tileHeight;
    private Label lifeLabel;
    private Label moneyLabel;
    private Label waveCountLabel;
    private ProgressIndicator towerPi;
    private Timeline placeTowerTimeline;
    private Timeline createTowerSelectionTimeline;
    private AnimationTimer gameLoop;

    // GAME VARIABLES
    private final ArrayList<Enemy> enemies;
    private final ArrayList<Tower> towers;
    private final ArrayList<Projectile> projectiles;
    private final Map map;
    private final DoubleProperty money;
    private final IntegerProperty life;
    private final IntegerProperty waveCount;
    private double enemyHealthMultiplier;
    private int enemiesToSend;
    private int enemiesSent;
    private int enemySpawnTick;

    // IMAGES
    private final Image basicTowerImage;
    private final Image doubleTowerImage;
    private final Image missileTowerImage;
    private final Image canonTowerImage;
    private final Image moneyGainImage;
    private final Image moneyLossImage;

    // CONSTANTS
    private final static int EXPLOSION_DAMAGE = 5;
    private final static double EXPLOSION_RADIUS = 1.5;
    private final static double START_MONEY = 50;
    private final static int START_LIFE = 10;
    private final static int ENEMY_SPAWN_TICK_LIMIT = 60;
    private final static int BASIC_TOWER = 0;
    private final static int DOUBLE_TOWER = 1;
    private final static int MISSILE_TOWER = 2;
    private final static int CANON_TOWER = 3;

    TowerDefense(final IGameContext gameContext, final Stats stats){

        this.gameContext = gameContext;
        this.stats = stats;
        gameContext.getGazeDeviceManager().addStats(stats);

        enemies = new ArrayList<>();
        towers = new ArrayList<>();
        projectiles = new ArrayList<>();
        map = new Map(1);

        money = new SimpleDoubleProperty(START_MONEY);
        life = new SimpleIntegerProperty(START_LIFE);
        waveCount = new SimpleIntegerProperty(0);

        basicTowerImage = new Image("data/TowerDefense/basicTower.png");
        doubleTowerImage = new Image("data/TowerDefense/doubleTower.png");
        missileTowerImage = new Image("data/TowerDefense/missileTower.png");
        canonTowerImage = new Image("data/TowerDefense/canonTower.png");
        moneyLossImage = new Image("data/TowerDefense/moneyLoss.png");
        moneyGainImage = new Image("data/TowerDefense/moneyGain.png");

        tileWidth = new SimpleDoubleProperty();
        tileHeight = new SimpleDoubleProperty();
        tileWidth.bind(gameContext.getRoot().widthProperty().divide(map.getNbCols()));
        tileHeight.bind(gameContext.getRoot().heightProperty().divide(map.getNbRows()));

        canvas = new GameCanvas(tileWidth, tileHeight, map, enemies, towers, projectiles);
        canvas.heightProperty().bind(gameContext.getRoot().heightProperty());
        canvas.widthProperty().bind(gameContext.getRoot().widthProperty());

        gameContext.getGazeDeviceManager().addStats(stats);
    }

    @Override
    public void launch() {
        money.set(START_MONEY);
        life.set(START_LIFE);
        waveCount.set(0);
        enemyHealthMultiplier = 0;
        enemiesToSend = 0;
        enemiesSent = 0;
        enemySpawnTick = 0;

        gameContext.getChildren().add(canvas);

        // TOPBAR

        HBox topBar = new HBox();
        topBar.setSpacing(30);
        topBar.setPadding(new Insets(10,0,0,15));
        gameContext.getChildren().add(topBar);

        lifeLabel = createLabel(life.get()+"/"+START_LIFE, "data/TowerDefense/heart.png");
        life.addListener(observable -> lifeLabel.setText(life.get()+"/"+START_LIFE));
        topBar.getChildren().add(lifeLabel);

        moneyLabel = createLabel(""+START_MONEY, "data/TowerDefense/money.png");
        money.addListener(observable -> moneyLabel.setText(""+money.get()));
        topBar.getChildren().add(moneyLabel);

        waveCountLabel = createLabel(""+waveCount.get(), "data/TowerDefense/wave.png");
        waveCount.addListener(observable -> waveCountLabel.setText(""+waveCount.getValue()));
        topBar.getChildren().add(waveCountLabel);

        ProgressIndicator waveButtonPi = new ProgressIndicator(0);
        waveButtonPi.setMouseTransparent(true);
        waveButtonPi.setVisible(false);
        gameContext.getChildren().add(waveButtonPi);

        Timeline sendWaveTimeline = new Timeline();
        sendWaveTimeline.setOnFinished(event -> createWave());

        sendWaveButton = new I18NButton(gameContext.getTranslator(), "SendWave");
        sendWaveButton.setStyle("-fx-font-size: "+tileWidth.get()/2+";-fx-font-family: 'Agency FB'");
        tileWidth.addListener(observable -> sendWaveButton.setStyle("-fx-font-size: "+tileWidth.get()/2+";-fx-font-family: 'Agency FB'"));
        topBar.getChildren().add(sendWaveButton);

        EventHandler<Event> enterWaveButtonHandler = event -> {
            waveButtonPi.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
            waveButtonPi.setMinSize(tileWidth.get(), tileHeight.get());
            waveButtonPi.relocate(sendWaveButton.getLayoutX()+ sendWaveButton.getWidth()/2-waveButtonPi.getWidth()/2, sendWaveButton.getLayoutY());
            waveButtonPi.setProgress(0);
            waveButtonPi.setVisible(true);

            sendWaveTimeline.getKeyFrames().clear();
            sendWaveTimeline.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(waveButtonPi.progressProperty(), 1)));
            sendWaveTimeline.play();
        };

        sendWaveButton.addEventFilter(MouseEvent.MOUSE_ENTERED, enterWaveButtonHandler);
        sendWaveButton.addEventFilter(GazeEvent.GAZE_ENTERED, enterWaveButtonHandler);

        EventHandler<Event> exitWaveButtonHandler = event -> {
            waveButtonPi.setVisible(false);
            sendWaveTimeline.stop();
        };

        sendWaveButton.addEventFilter(MouseEvent.MOUSE_EXITED, exitWaveButtonHandler);
        sendWaveButton.addEventFilter(GazeEvent.GAZE_EXITED, exitWaveButtonHandler);
        gameContext.getGazeDeviceManager().addEventFilter(sendWaveButton);

        // TOWER SELECTION

        ProgressIndicator createTowerSelectionPI = new ProgressIndicator(0);
        createTowerSelectionPI.setVisible(false);
        gameContext.getChildren().add(createTowerSelectionPI);

        EventHandler<Event> exitTurretTileHandler = event -> {
            createTowerSelectionTimeline.stop();
            createTowerSelectionPI.setVisible(false);
        };

        for (Point2D turretsTile : map.getTurretsTiles()) {
            Rectangle rectangle = new Rectangle();
            rectangle.xProperty().bind(tileWidth.multiply(turretsTile.getX()-0.5));
            rectangle.yProperty().bind(tileHeight.multiply(turretsTile.getY()-0.5));
            rectangle.widthProperty().bind(tileWidth.multiply(2));
            rectangle.heightProperty().bind(tileHeight.multiply(2));
            rectangle.setOpacity(0);

            EventHandler<Event> enterTurretTileHandler = event -> {
                double turretX = turretsTile.getX() * tileWidth.get();
                double turretY = turretsTile.getY() * tileHeight.get();
                createTowerSelectionPI.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
                createTowerSelectionPI.setMinSize(tileWidth.get(), tileHeight.get());
                createTowerSelectionPI.relocate(turretX, turretY);
                createTowerSelectionPI.setProgress(0);
                createTowerSelectionPI.setVisible(true);

                if(createTowerSelectionTimeline!=null){
                    createTowerSelectionTimeline.stop();
                }
                createTowerSelectionTimeline = new Timeline();
                createTowerSelectionTimeline.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(createTowerSelectionPI.progressProperty(), 1)));
                createTowerSelectionTimeline.setOnFinished(actionEvent -> {
                    createTowerSelectionPI.setVisible(false);
                    createTowerSelection((int) turretsTile.getX(), (int) turretsTile.getY());
                });
                createTowerSelectionTimeline.play();
            };

            rectangle.addEventFilter(MouseEvent.MOUSE_ENTERED, enterTurretTileHandler);
            rectangle.addEventFilter(GazeEvent.GAZE_ENTERED, enterTurretTileHandler);

            rectangle.addEventFilter(MouseEvent.MOUSE_EXITED, exitTurretTileHandler);
            rectangle.addEventFilter(GazeEvent.GAZE_EXITED, exitTurretTileHandler);

            gameContext.getGazeDeviceManager().addEventFilter(rectangle);
            gameContext.getChildren().add(rectangle);
        }

        gameLoop = new AnimationTimer() {
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

        gameContext.start();
        stats.notifyNewRoundReady();
        gameLoop.start();

    }

    @Override
    public void dispose() {
        gameLoop.stop();
        gameContext.clear();
        enemies.clear();
        towers.clear();
        projectiles.clear();
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
            ImageView image = new ImageView(moneyLossImage);
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

        // Check projectiles
        ListIterator<Enemy> enemyIter = enemies.listIterator();
        while (enemyIter.hasNext()) {
            Enemy enemy = enemyIter.next();
            ListIterator<Projectile> projIter = projectiles.listIterator();
            while (projIter.hasNext()) {
                Projectile proj = projIter.next();

                // If Projectile collides with an enemy
                if(enemy.getHitbox().intersects(proj.getHitbox().getBoundsInLocal())) {
                    if(proj instanceof Missile missile){
                        // If Explosion animation is over
                        if(missile.getFrameIndex()>=8){
                            projIter.remove();
                        }else{
                            if(missile.isActive()){
                                missile.setActive(false);
                                Circle explosionRadius = new Circle(proj.getX()+0.5, proj.getY()+proj.getSize()+0.5, EXPLOSION_RADIUS);
                                explosions.add(explosionRadius);
                            }
                        }
                    }else{
                        // Enemy loses HP, projectile disappears
                        projIter.remove();
                        enemy.loseHP(proj.getDamage());
                        if (enemy.getHealth() <= 0) {
                            // Enemy dies
                            enemyIter.remove();
                            money.set(money.get() + enemy.getReward());
                            playEnemyDeathAnimation(enemy);
                        }
                    }
                }
                // Remove projectile when it goes outside the screen
                if (proj.getX() < 0 || proj.getX()*tileWidth.get() > canvas.getWidth() || proj.getY() < 0 || proj.getY()*tileHeight.get() > canvas.getHeight()) {
                    projIter.remove();
                }
            }
        }

        // Check Explosions
        for (Circle explosion : explosions) {
            ListIterator<Enemy> enemyIter2 = enemies.listIterator();
            while (enemyIter2.hasNext()) {
                Enemy enemy2 = enemyIter2.next();
                if (explosion.intersects(enemy2.getHitbox().getBoundsInLocal())) {
                    // Enemy lose HP
                    enemy2.loseHP(EXPLOSION_DAMAGE);
                    if (enemy2.getHealth() <= 0) {
                        // Enemy dies
                        enemyIter2.remove();
                        money.set(money.get() + enemy2.getReward());
                        playEnemyDeathAnimation(enemy2);
                    }
                }
            }
        }
    }

    private void playEnemyDeathAnimation(Enemy enemy){
            ImageView image = new ImageView(moneyGainImage);
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
        tileWidth.addListener(observable -> label.setStyle("-fx-font-size: "+tileWidth.get()/2+";-fx-font-family: 'Agency FB'"));
        label.setStyle("-fx-font-size: "+tileWidth.get()/2+";-fx-font-family: 'Agency FB'");
        ImageView image = new ImageView(new Image(imagePath));
        image.fitWidthProperty().bind(tileWidth);
        image.fitHeightProperty().bind(tileHeight);
        label.setGraphic(image);
        return label;
    }

    private void loseLife(){
        life.set(life.get()-1);
        if(life.get()<=0){
            // Game Lost
            gameLoop.stop();
            sendWaveButton.setVisible(false);
            gameContext.playWinTransition(0, actionEvent -> gameContext.endWinTransition());
        }
    }

    private void createWave(){
        // Alternate between 5 and 10 enemies
        enemiesSent = 0;
        enemiesToSend = (waveCount.get() % 2 + 1) * 5;

        // Enemies gains 0.25 health multiplier every 2 waves
        int i = waveCount.get()/2;
        enemyHealthMultiplier = 1 + i * 0.25;

        waveCount.set(waveCount.get() + 1);
        sendWaveButton.setVisible(false);
    }

    private void spawnEnemies(){
        if (enemiesSent < enemiesToSend && enemySpawnTick >= ENEMY_SPAWN_TICK_LIMIT){
            enemySpawnTick = 0;
            enemiesSent++;
            Enemy enemy = new Enemy(map, map.getStartCol(), map.getStartRow());
            enemy.multiplyHealth(enemyHealthMultiplier);
            enemies.add(enemy);
        }else{
            enemySpawnTick++;
        }
        if(enemiesSent >= enemiesToSend){
            sendWaveButton.setVisible(true);
        }
    }

    private void createTowerSelection(int col, int row){

        Group group = new Group();
        gameContext.getChildren().add(group);

        double mainCircleRadius = 2.5;
        Ellipse ellipse = new Ellipse((col+0.5)*tileWidth.get(), (row+0.5)*tileHeight.get(), mainCircleRadius*tileWidth.get(), mainCircleRadius*tileHeight.get());
        ellipse.setFill(Color.MEDIUMSLATEBLUE);
        ellipse.setOpacity(0.75);
        group.getChildren().add(ellipse);

        towerPi = new ProgressIndicator(0);
        towerPi.setVisible(false);
        towerPi.setMouseTransparent(true);
        towerPi.setMinSize((mainCircleRadius - 0.5)*tileWidth.get(), (mainCircleRadius - 0.5)*tileHeight.get());

        placeTowerTimeline = new Timeline();

        createTowerIcon(BASIC_TOWER, col, row, mainCircleRadius, group);
        createTowerIcon(MISSILE_TOWER, col, row, mainCircleRadius, group);
        createTowerIcon(CANON_TOWER, col, row, mainCircleRadius, group);
        createTowerIcon(DOUBLE_TOWER, col, row, mainCircleRadius, group);

        //// Close the tower selection when looking away
        ProgressIndicator exitPi = new ProgressIndicator(0);
        exitPi.setVisible(false);
        exitPi.setMouseTransparent(true);
        exitPi.setMinSize(tileWidth.get(), tileHeight.get());
        exitPi.relocate(col*tileWidth.get(), row*tileHeight.get());

        Timeline exitTl = new Timeline();
        exitTl.setOnFinished(event1 -> {
            group.getChildren().clear();
            gameContext.getChildren().remove(group);
        });

        EventHandler<Event> exitGroupHandler = event -> {
            exitPi.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
            exitPi.setProgress(0);
            exitPi.setVisible(true);

            exitTl.getKeyFrames().clear();
            exitTl.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(exitPi.progressProperty(), 1)));
            exitTl.play();
        };

        EventHandler<Event> enterGroupHandler = event -> {
            exitPi.setVisible(false);
            exitTl.stop();
        };

        // Bigger rectangle to work better with eye-tracking
        Rectangle rect = new Rectangle((col-2)*tileWidth.get(),(row-2)*tileHeight.get(),tileWidth.get()*5, tileHeight.get()*5);
        rect.setOpacity(0);
        rect.setMouseTransparent(true);
        gameContext.getChildren().add(rect);

        group.addEventFilter(MouseEvent.MOUSE_ENTERED, enterGroupHandler);
        rect.addEventFilter(GazeEvent.GAZE_ENTERED, enterGroupHandler);

        group.addEventFilter(MouseEvent.MOUSE_EXITED, exitGroupHandler);
        rect.addEventFilter(GazeEvent.GAZE_EXITED, exitGroupHandler);

        gameContext.getGazeDeviceManager().addEventFilter(rect);
        gameContext.getGazeDeviceManager().addEventFilter(group);

        group.getChildren().add(towerPi);
        group.getChildren().add(exitPi);

    }

    private void createTowerIcon(int towerType, int col, int row, double mainCircleRadius, Group group){
        ImageView tower;
        switch (towerType) {
            case MISSILE_TOWER -> {
                tower = new ImageView(missileTowerImage);
                tower.setFitWidth((mainCircleRadius - 0.5) * tileWidth.get());
                tower.setFitHeight((mainCircleRadius - 0.5) * tileHeight.get());
                tower.setX((col + 0.5 - mainCircleRadius) * tileWidth.get());
                tower.setY((row + 0.5) * tileHeight.get() - tower.getFitHeight() / 2);
            }
            case DOUBLE_TOWER -> {
                tower = new ImageView(doubleTowerImage);
                tower.setFitWidth((mainCircleRadius - 0.5) * tileWidth.get());
                tower.setFitHeight((mainCircleRadius - 0.5) * tileHeight.get());
                tower.setX((col + 0.5) * tileWidth.get() - tower.getFitWidth() / 2);
                tower.setY((row + 1) * tileHeight.get());
            }
            case CANON_TOWER -> {
                tower = new ImageView(canonTowerImage);
                tower.setFitWidth((mainCircleRadius - 0.5) * tileWidth.get());
                tower.setFitHeight((mainCircleRadius - 0.5) * tileHeight.get());
                tower.setX((col + 1) * tileWidth.get());
                tower.setY((row + 0.5) * tileHeight.get() - tower.getFitHeight() / 2);
            }
            default -> {
                tower = new ImageView(basicTowerImage);
                tower.setFitWidth((mainCircleRadius - 0.5) * tileWidth.get());
                tower.setFitHeight((mainCircleRadius - 0.5) * tileHeight.get());
                tower.setX((col + 0.5) * tileWidth.get() - tower.getFitWidth() / 2);
                tower.setY((row + 0.5 - mainCircleRadius) * tileHeight.get());
            }
        }

        group.getChildren().add(tower);

        EventHandler<Event> enterRightTowerHandler = event -> {
            towerPi.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
            towerPi.relocate(tower.getX(), tower.getY());
            towerPi.setProgress(0);
            towerPi.setVisible(true);

            placeTowerTimeline = new Timeline();
            placeTowerTimeline.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(towerPi.progressProperty(), 1)));
            placeTowerTimeline.setOnFinished(eve -> {
                createTower(col, row, towerType);
                group.getChildren().clear();
                gameContext.getChildren().remove(group);
            });
            placeTowerTimeline.play();
        };

        EventHandler<Event> exitTowerHandler = event -> {
            towerPi.setVisible(false);
            placeTowerTimeline.stop();
        };

        tower.addEventFilter(MouseEvent.MOUSE_ENTERED, enterRightTowerHandler);
        tower.addEventFilter(GazeEvent.GAZE_ENTERED, enterRightTowerHandler);
        tower.addEventFilter(MouseEvent.MOUSE_EXITED, exitTowerHandler);
        tower.addEventFilter(GazeEvent.GAZE_EXITED, exitTowerHandler);
        gameContext.getGazeDeviceManager().addEventFilter(tower);
    }

}

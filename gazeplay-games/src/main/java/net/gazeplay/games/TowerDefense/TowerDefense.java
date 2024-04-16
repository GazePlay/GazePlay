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
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.utils.stats.Stats;

import java.time.Instant;
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
    private Instant lastBlizzardInstant;

    // IMAGES
    private final Image basicTowerImage;
    private final Image doubleTowerImage;
    private final Image missileTowerImage;
    private final Image canonTowerImage;
    private final Image moneyGainImage;
    private final Image moneyLossImage;
    private final Image sellTowerImage;
    private final ImageView blizzardGif;

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
    private final static double BLIZZARD_COOLDOWN = 30;


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

        basicTowerImage = new Image("data/TowerDefense/images/basicTower.png");
        doubleTowerImage = new Image("data/TowerDefense/images/doubleTower.png");
        missileTowerImage = new Image("data/TowerDefense/images/missileTower.png");
        canonTowerImage = new Image("data/TowerDefense/images/canonTower.png");
        moneyLossImage = new Image("data/TowerDefense/images/moneyLoss.png");
        moneyGainImage = new Image("data/TowerDefense/images/moneyGain.png");
        sellTowerImage = new Image("data/TowerDefense/images/sellTower.png");
        blizzardGif = new ImageView(new Image("data/TowerDefense/images/blizzard.gif"));
        blizzardGif.fitWidthProperty().bind(gameContext.getRoot().widthProperty());
        blizzardGif.fitHeightProperty().bind(gameContext.getRoot().heightProperty());

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
        lastBlizzardInstant = Instant.MIN;

        gameContext.getChildren().add(canvas);

        // TOPBAR

        HBox topBar = new HBox();
        topBar.setSpacing(30);
        topBar.setPadding(new Insets(10,0,0,15));
        gameContext.getChildren().add(topBar);

        lifeLabel = createLabel(life.get()+"/"+START_LIFE, "data/TowerDefense/images/heart.png");
        life.addListener(observable -> lifeLabel.setText(life.get()+"/"+START_LIFE));
        topBar.getChildren().add(lifeLabel);

        moneyLabel = createLabel(""+START_MONEY, "data/TowerDefense/images/money.png");
        money.addListener(observable -> moneyLabel.setText(""+money.get()));
        topBar.getChildren().add(moneyLabel);

        waveCountLabel = createLabel(""+waveCount.get(), "data/TowerDefense/images/wave.png");
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

        //// Blizzard Power
        Label blizzardLabel = createLabel("", "data/TowerDefense/images/blizzardIcon.png");
        ImageView blizzarIcon = new ImageView(new Image("data/TowerDefense/images/blizzardIcon.png"));
        blizzarIcon.fitWidthProperty().bind(tileWidth.multiply(1.5));
        blizzarIcon.fitHeightProperty().bind(tileHeight.multiply(1.5));
        blizzardLabel.setGraphic(blizzarIcon);
        topBar.getChildren().add(blizzardLabel);

        ProgressIndicator blizzardPi = new ProgressIndicator(0);
        blizzardPi.setVisible(false);
        blizzardPi.setMouseTransparent(true);
        gameContext.getChildren().add(blizzardPi);

        Timeline blizzardTl = new Timeline();
        blizzardTl.setOnFinished(actionEvent -> {
            lastBlizzardInstant = Instant.now();
            blizzardPi.setVisible(false);
            gameContext.getChildren().add(blizzardGif);
            enemies.forEach(enemy -> enemy.setFrozen(true));

            Arc arc = new Arc(blizzardLabel.getLayoutX()+blizzardLabel.getWidth()/2, blizzardLabel.getLayoutY()+blizzardLabel.getHeight()/2, blizzardLabel.getWidth()/2, blizzardLabel.getHeight()/2, 90, 360);
            arc.centerXProperty().bind(blizzardLabel.layoutXProperty().add(blizzardLabel.getWidth()/2));
            arc.centerYProperty().bind(blizzardLabel.layoutYProperty().add(blizzardLabel.getHeight()/2));
            arc.radiusXProperty().bind(blizzardLabel.widthProperty().divide(2));
            arc.radiusYProperty().bind(blizzardLabel.heightProperty().divide(2));
            arc.setType(ArcType.ROUND);
            arc.setOpacity(0.75);
            arc.setFill(Color.DARKGREY);
            gameContext.getChildren().add(arc);

            // Update cooldown
            AnimationTimer cooldownTimer = new AnimationTimer() {
                @Override
                public void handle(long l) {
                    double rate = 1 - java.time.Duration.between(lastBlizzardInstant, Instant.now()).toSeconds()/BLIZZARD_COOLDOWN;
                    if(rate<0){
                        gameContext.getChildren().remove(arc);
                        stop();
                    }
                    arc.setLength(rate*360);
                }
            };

            cooldownTimer.start();

            // End the blizzard
            Timeline endBlizzardTl = new Timeline(new KeyFrame(Duration.seconds(5), actionEvent1 -> {
                gameContext.getChildren().remove(blizzardGif);
                enemies.forEach(enemy -> enemy.setFrozen(false));
            }));
            endBlizzardTl.play();

        });

        EventHandler<Event> enterBlizzardHandler = event -> {
            if(java.time.Duration.between(lastBlizzardInstant, Instant.now()).toSeconds() >= BLIZZARD_COOLDOWN){
                blizzardPi.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
                blizzardPi.setMinSize(blizzardLabel.getWidth(), blizzardLabel.getHeight());
                blizzardPi.relocate(blizzardLabel.getLayoutX(), blizzardLabel.getLayoutY());
                blizzardPi.setProgress(0);
                blizzardPi.setVisible(true);
                blizzardTl.getKeyFrames().clear();
                blizzardTl.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(blizzardPi.progressProperty(), 1)));
                blizzardTl.play();
            }
        };

        EventHandler<Event> exitBlizzardHandler = event -> {
            blizzardTl.stop();
            blizzardPi.setVisible(false);
        };

        blizzardLabel.addEventFilter(MouseEvent.MOUSE_ENTERED, enterBlizzardHandler);
        blizzardLabel.addEventFilter(GazeEvent.GAZE_ENTERED, enterBlizzardHandler);

        blizzardLabel.addEventFilter(MouseEvent.MOUSE_EXITED, exitBlizzardHandler);
        blizzardLabel.addEventFilter(GazeEvent.GAZE_EXITED, exitBlizzardHandler);

        gameContext.getGazeDeviceManager().addEventFilter(blizzardLabel);

        // TOWER SELECTION

        ProgressIndicator createTowerSelectionPI = new ProgressIndicator(0);
        createTowerSelectionPI.setVisible(false);
        createTowerSelectionPI.setMouseTransparent(true);
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
                    tower.fire(gameContext);
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
            gameContext.getSoundManager().add(tower.getSoundsConstruction());
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
            if(!enemy.isFrozen()){
                enemy.move();
            }
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

        if(getTower(col, row)!=null){
            createSellIcon(col, row, mainCircleRadius, group);
            //
            Tower existingTower = getTower(col, row);
            Text towerInfo = new Text("Dégâts : " + existingTower.damage + "\n"
            + "Portée : " + existingTower.range + " unités" + "\n"
            + "Vitesse : " + existingTower.projSpeed);
            towerInfo.setLayoutX(existingTower.getCenter().getX());
            towerInfo.setLayoutY(existingTower.getCenter().getY());
            towerInfo.setId("info");
            gameContext.getChildren().add(towerInfo);


        }else{
            createTowerIcon(BASIC_TOWER, col, row, mainCircleRadius, group);
            createTowerIcon(MISSILE_TOWER, col, row, mainCircleRadius, group);
            createTowerIcon(CANON_TOWER, col, row, mainCircleRadius, group);
            createTowerIcon(DOUBLE_TOWER, col, row, mainCircleRadius, group);
        }

        // Bigger rectangle to work better with eye-tracking
        Rectangle rect = new Rectangle((col-mainCircleRadius)*tileWidth.get(),(row-mainCircleRadius)*tileHeight.get(),(mainCircleRadius*2+1)*tileWidth.get(), (mainCircleRadius*2+1)*tileHeight.get());
        rect.setOpacity(0);
        rect.setViewOrder(10);
        group.getChildren().add(rect);
        rect.addEventFilter(GazeEvent.GAZE_EXITED, gazeEvent -> {
            group.getChildren().clear();
            gameContext.getChildren().remove(group);
        });
        rect.addEventFilter(MouseEvent.MOUSE_EXITED, mouseEvent -> {
            group.getChildren().clear();
            gameContext.getChildren().remove(group);
        });

        gameContext.getGazeDeviceManager().addEventFilter(rect);

        group.getChildren().add(towerPi);

    }

    private void createTowerIcon(int towerType, int col, int row, double mainCircleRadius, Group group){
        StackPane stackPane = new StackPane();

        ImageView tower;
        switch (towerType) {
            case MISSILE_TOWER -> {
                tower = new ImageView(missileTowerImage);
                tower.setFitWidth((mainCircleRadius - 0.5) * tileWidth.get());
                tower.setFitHeight((mainCircleRadius - 0.5) * tileHeight.get());
                stackPane.setTranslateX((col + 0.5 - mainCircleRadius) * tileWidth.get());
                stackPane.setTranslateY((row + 0.5) * tileHeight.get() - tower.getFitHeight() / 2);
            }
            case DOUBLE_TOWER -> {
                tower = new ImageView(doubleTowerImage);
                tower.setFitWidth((mainCircleRadius - 0.5) * tileWidth.get());
                tower.setFitHeight((mainCircleRadius - 0.5) * tileHeight.get());
                stackPane.setTranslateX((col + 0.5) * tileWidth.get() - tower.getFitWidth() / 2);
                stackPane.setTranslateY((row + 1) * tileHeight.get());
            }
            case CANON_TOWER -> {
                tower = new ImageView(canonTowerImage);
                tower.setFitWidth((mainCircleRadius - 0.5) * tileWidth.get());
                tower.setFitHeight((mainCircleRadius - 0.5) * tileHeight.get());
                stackPane.setTranslateX((col + 1) * tileWidth.get());
                stackPane.setTranslateY((row + 0.5) * tileHeight.get() - tower.getFitHeight() / 2);
            }
            default -> {
                tower = new ImageView(basicTowerImage);
                tower.setFitWidth((mainCircleRadius - 0.5) * tileWidth.get());
                tower.setFitHeight((mainCircleRadius - 0.5) * tileHeight.get());
                stackPane.setTranslateX((col + 0.5) * tileWidth.get() - tower.getFitWidth() / 2);
                stackPane.setTranslateY((row + 0.5 - mainCircleRadius) * tileHeight.get());
            }
        }


        Label costLabel = new Label();
        costLabel.setMouseTransparent(true);
        costLabel.setFont(Font.font("Agency FB", tower.getFitHeight()/2));
        switch (towerType){
            case BASIC_TOWER -> costLabel.setText("25");
            case DOUBLE_TOWER -> costLabel.setText("50");
            case MISSILE_TOWER -> costLabel.setText("25");
            case CANON_TOWER -> costLabel.setText("25");
            default -> costLabel.setText("25");
        }
        stackPane.getChildren().addAll(tower, costLabel);

        tower.setPickOnBounds(true);
        group.getChildren().add(stackPane);

        EventHandler<Event> enterRightTowerHandler = event -> {
            towerPi.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
            towerPi.relocate(stackPane.getTranslateX(), stackPane.getTranslateY());
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

    private void createSellIcon(int col, int row, double mainCircleRadius, Group group){
        ImageView sellIcon = new ImageView(sellTowerImage);
        sellIcon.setPickOnBounds(true);
        sellIcon.setFitWidth((mainCircleRadius - 0.5) * tileWidth.get());
        sellIcon.setFitHeight((mainCircleRadius - 0.5) * tileHeight.get());
        sellIcon.setX((col + 0.5) * tileWidth.get() - sellIcon.getFitWidth() / 2);
        sellIcon.setY((row + 0.5 - mainCircleRadius) * tileHeight.get());

        group.getChildren().add(sellIcon);

        EventHandler<Event> enterRightTowerHandler = event -> {
            towerPi.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
            towerPi.relocate(sellIcon.getX(), sellIcon.getY());
            towerPi.setProgress(0);
            towerPi.setVisible(true);

            placeTowerTimeline = new Timeline();
            placeTowerTimeline.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(towerPi.progressProperty(), 1)));
            placeTowerTimeline.setOnFinished(eve -> {
                Tower tower = getTower(col, row);
                if(tower!=null){
                    money.set(money.get()+tower.cost);
                    towers.remove(tower);
                }
                group.getChildren().clear();
                gameContext.getChildren().remove(group);
            });
            placeTowerTimeline.play();
        };

        EventHandler<Event> exitTowerHandler = event -> {
            towerPi.setVisible(false);
            placeTowerTimeline.stop();
            gameContext.getRoot().lookupAll("#info").forEach(node -> gameContext.getChildren().remove(node));

        };

        sellIcon.addEventFilter(MouseEvent.MOUSE_ENTERED, enterRightTowerHandler);
        sellIcon.addEventFilter(GazeEvent.GAZE_ENTERED, enterRightTowerHandler);
        sellIcon.addEventFilter(MouseEvent.MOUSE_EXITED, exitTowerHandler);
        sellIcon.addEventFilter(GazeEvent.GAZE_EXITED, exitTowerHandler);
        gameContext.getGazeDeviceManager().addEventFilter(sellIcon);
    }

}

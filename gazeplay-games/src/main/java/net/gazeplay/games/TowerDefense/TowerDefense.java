package net.gazeplay.games.TowerDefense;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;
import net.gazeplay.games.TowerDefense.enemies.BasicEnemy;
import net.gazeplay.games.TowerDefense.enemies.Enemy;
import net.gazeplay.games.TowerDefense.enemies.FastEnemy;
import net.gazeplay.games.TowerDefense.enemies.TankEnemy;
import net.gazeplay.games.TowerDefense.maps.Map;
import net.gazeplay.games.TowerDefense.maps.MapOne;
import net.gazeplay.games.TowerDefense.maps.MapTwo;
import net.gazeplay.games.TowerDefense.towers.BasicTower;
import net.gazeplay.games.TowerDefense.towers.CanonTower;
import net.gazeplay.games.TowerDefense.towers.DoubleTower;
import net.gazeplay.games.TowerDefense.towers.MissileTower;
import net.gazeplay.games.TowerDefense.towers.Tower;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.function.DoubleSupplier;

/**
 * Tower Defense: waves of enemies follow the road towards the castle; the player builds towers on the tower places
 * (chosen by a fixation), paid with the money won by killing the enemies, and can freeze the enemies for
 * {@value #BLIZZARD_SECONDS} seconds with a blizzard. Each enemy reaching the castle costs a life.
 * <p>
 * Reviewed on 3/11/2026 (maintainer's rules): the waves start on demand (the button, as before: the player is never
 * overwhelmed) or by themselves {@value #AUTOMATIC_WAVE_DELAY} s after the last one, as chosen; the level gives the
 * money, the lives and how much stronger the enemies get; the game is won after 10 or 20 waves held, or never ("Bravo"
 * then the statistics), and lost when the lives are over ("Lost!" then the statistics; it used to show the animation
 * of a win). The map is drawn at random. The game runs in ticks of 1/60 s of game time, at the animation speed of the
 * settings, and stops while the menu of the game is open (the blizzard too).
 *
 * @author HugolinBouhineau, Yaniissou
 */
public class TowerDefense implements GameLifeCycle {

    static final int TICKS_PER_SECOND = 60;
    static final int AUTOMATIC_WAVE_DELAY = 10;
    static final int BLIZZARD_SECONDS = 5;
    static final int BLIZZARD_COOLDOWN = 30;

    enum Phase { PLAY, OVER }

    // GAZEPLAY
    private final IGameContext gameContext;
    private final Stats stats;
    @Getter
    private final TowerDefenseWaves waves;
    @Getter
    private final TowerDefenseLevel level;
    @Getter
    private final TowerDefenseEnd end;
    private final ReplayablePseudoRandom random;

    // UI ELEMENTS
    private final GameCanvas canvas;
    private I18NButton sendWaveButton;
    private final DoubleProperty tileWidth;
    private final DoubleProperty tileHeight;
    private Group lifeView;
    private Label moneyLabel;
    private Label waveCountLabel;
    private Label nextWaveLabel;
    /**
     * The ring of the fixations (the one of the reviewed games; it replaces the yellow discs of before).
     */
    private final FixationRing ring = new FixationRing();
    /**
     * The node whose fixation the ring shows, null when none.
     */
    private Node ringOwner;
    /**
     * The shop open around a tower place, null when none.
     */
    @Getter
    private Group shop;
    private Group shopCard;
    private final Dimension2D dimensions;
    private final VBox messages;
    private final Group snow = new Group();
    private Arc blizzardCooldownArc;

    // GAME VARIABLES
    @Getter
    private final ArrayList<Enemy> enemies;
    @Getter
    private final ArrayList<Tower> towers;
    private final ArrayList<Projectile> projectiles;
    @Getter
    private final Map map;
    private final DoubleProperty money;
    private final IntegerProperty life;
    private final IntegerProperty waveCount;
    private double enemyHealthMultiplier;
    private int enemiesToSend;
    private int enemiesSent;
    private int enemySpawnTick;
    @Getter
    private Phase phase = Phase.OVER;
    /**
     * Ticks left before the next automatic wave, -1 when none is waiting.
     */
    @Getter
    private int nextWaveTicks = -1;
    @Getter
    private int blizzardTicks;
    private int blizzardCooldownTicks;
    private double tickDebt;
    private PauseTransition lostPause;

    // IMAGES
    private final Image basicTowerImage;
    private final Image doubleTowerImage;
    private final Image missileTowerImage;
    private final Image canonTowerImage;
    private final Image moneyGainImage;
    private final Image moneyLossImage;
    /**
     * The pictures selling a tower, one for each tower (that tower, an arrow, coins: 3/11/2026, so that the player
     * sees which tower is sold).
     */
    private final java.util.Map<Class<? extends Tower>, Image> sellImages = new java.util.HashMap<>();

    // CONSTANTS
    private static final double MESSAGE_PADDING = 160;
    private final static int EXPLOSION_DAMAGE = 5;
    private final static double EXPLOSION_RADIUS = 1.5;
    private final static int ENEMY_SPAWN_TICK_LIMIT = 60;
    static final int BASIC_TOWER = 0;
    static final int DOUBLE_TOWER = 1;
    static final int MISSILE_TOWER = 2;
    static final int CANON_TOWER = 3;

    private final AnimationTimer gameLoop = new AnimationTimer() {
        private long last = -1;

        @Override
        public void handle(final long now) {
            final double seconds = last < 0 ? 0 : Math.min(0.1, (now - last) / 1e9);
            last = now;
            if (!Stats.isConfigMenuOpen()) {
                step(seconds);
            }
        }

        @Override
        public void stop() {
            super.stop();
            last = -1;
        }
    };
    private boolean manual;

    TowerDefense(final IGameContext gameContext, final Stats stats, final TowerDefenseWaves waves,
                 final TowerDefenseLevel level, final TowerDefenseEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.waves = waves;
        this.level = level;
        this.end = end;
        if (gameSeed < 0) {
            random = new ReplayablePseudoRandom();
            stats.setGameSeed(random.getSeed());
        } else {
            random = new ReplayablePseudoRandom(gameSeed);
        }

        enemies = new ArrayList<>();
        towers = new ArrayList<>();
        projectiles = new ArrayList<>();
        // The map, drawn at random (3/11/2026: the variants hold the waves, the level and the end)
        this.map = random.nextInt(2) == 0 ? new MapOne() : new MapTwo();

        money = new SimpleDoubleProperty(level.getMoney());
        life = new SimpleIntegerProperty(level.getLives());
        waveCount = new SimpleIntegerProperty(0);

        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();

        // The message queue (allows to display messages for the user)
        messages = new VBox();
        messages.setAlignment(Pos.CENTER);
        messages.setPadding(new Insets(MESSAGE_PADDING));
        messages.setMouseTransparent(true);

        basicTowerImage = new Image("data/towerDefense/images/basicTower.png");
        doubleTowerImage = new Image("data/towerDefense/images/doubleTower.png");
        missileTowerImage = new Image("data/towerDefense/images/missileTower.png");
        canonTowerImage = new Image("data/towerDefense/images/canonTower.png");
        moneyLossImage = new Image("data/towerDefense/images/moneyLoss.png");
        moneyGainImage = new Image("data/towerDefense/images/moneyGain.png");
        sellImages.put(BasicTower.class, new Image("data/towerDefense/images/sellBasic.png"));
        sellImages.put(DoubleTower.class, new Image("data/towerDefense/images/sellDouble.png"));
        sellImages.put(MissileTower.class, new Image("data/towerDefense/images/sellMissile.png"));
        sellImages.put(CanonTower.class, new Image("data/towerDefense/images/sellCanon.png"));

        tileWidth = new SimpleDoubleProperty();
        tileHeight = new SimpleDoubleProperty();
        tileWidth.bind(gameContext.getRoot().widthProperty().divide(map.getNbCols()));
        tileHeight.bind(gameContext.getRoot().heightProperty().divide(map.getNbRows()));

        canvas = new GameCanvas(tileWidth, tileHeight, map, enemies, towers, projectiles);
        canvas.heightProperty().bind(gameContext.getRoot().heightProperty());
        canvas.widthProperty().bind(gameContext.getRoot().widthProperty());
    }

    @Override
    public void launch() {
        money.set(level.getMoney());
        life.set(level.getLives());
        waveCount.set(0);
        enemyHealthMultiplier = 0;
        enemiesToSend = 0;
        enemiesSent = 0;
        enemySpawnTick = 0;
        blizzardTicks = 0;
        blizzardCooldownTicks = 0;
        nextWaveTicks = waves.isAutomatic() ? AUTOMATIC_WAVE_DELAY * TICKS_PER_SECOND : -1;

        gameContext.setLimiterAvailable();
        gameContext.getChildren().add(canvas);
        gameContext.getChildren().add(messages);

        // TOPBAR: the lives, the money, the waves, the button of the next wave, the blizzard
        final HBox topBar = new HBox();
        topBar.setSpacing(30);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 0, 0, 15));
        gameContext.getChildren().add(topBar);

        // The lives as in the other reviewed games (maintainer's request of 10/11/2026): hearts up to 5 lives (hard
        // level), else the lives left on the lives at the start and a green bar shorter at each life lost
        lifeView = new Group();
        life.addListener(observable -> showLife());
        tileHeight.addListener(observable -> showLife());
        topBar.getChildren().add(lifeView);

        moneyLabel = createLabel("", "data/towerDefense/images/money.png");
        money.addListener(observable -> showMoney());
        topBar.getChildren().add(moneyLabel);

        waveCountLabel = createLabel("", "data/towerDefense/images/wave.png");
        waveCount.addListener(observable -> showWaves());
        topBar.getChildren().add(waveCountLabel);
        showLife();
        showMoney();
        showWaves();

        sendWaveButton = new I18NButton(gameContext.getTranslator(), "SendWave");
        sendWaveButton.setStyle(GameStyle.titleFontStyle(fontSize()));
        tileWidth.addListener(observable -> sendWaveButton.setStyle(GameStyle.titleFontStyle(fontSize())));
        topBar.getChildren().add(sendWaveButton);
        fixate(sendWaveButton, () -> tileHeight.get() * 1.2, this::sendWave);

        nextWaveLabel = new Label();
        final String nextWaveColor = " -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 4, 0.5, 0, 1);";
        nextWaveLabel.setStyle(GameStyle.titleFontStyle(fontSize() * 0.8) + nextWaveColor);
        tileWidth.addListener(observable -> nextWaveLabel.setStyle(GameStyle.titleFontStyle(fontSize() * 0.8)
            + nextWaveColor));
        nextWaveLabel.setMouseTransparent(true);
        topBar.getChildren().add(nextWaveLabel);

        //// Blizzard Power
        final Label blizzardLabel = createLabel("", "data/towerDefense/images/blizzardIcon.png");
        final ImageView blizzardIcon = new ImageView(new Image("data/towerDefense/images/blizzardIcon.png"));
        blizzardIcon.fitWidthProperty().bind(tileWidth.multiply(1.5));
        blizzardIcon.fitHeightProperty().bind(tileHeight.multiply(1.5));
        blizzardLabel.setGraphic(blizzardIcon);
        topBar.getChildren().add(blizzardLabel);

        blizzardCooldownArc = new Arc();
        blizzardCooldownArc.centerXProperty().bind(blizzardLabel.layoutXProperty().add(blizzardLabel.widthProperty().divide(2)));
        blizzardCooldownArc.centerYProperty().bind(blizzardLabel.layoutYProperty().add(topBar.layoutYProperty()).add(blizzardLabel.heightProperty().divide(2)).add(10));
        blizzardCooldownArc.radiusXProperty().bind(blizzardLabel.widthProperty().divide(2));
        blizzardCooldownArc.radiusYProperty().bind(blizzardLabel.heightProperty().divide(2));
        blizzardCooldownArc.setStartAngle(90);
        blizzardCooldownArc.setType(ArcType.ROUND);
        blizzardCooldownArc.setOpacity(0.75);
        blizzardCooldownArc.setFill(Color.DARKGREY);
        blizzardCooldownArc.setMouseTransparent(true);
        blizzardCooldownArc.setVisible(false);
        gameContext.getChildren().add(blizzardCooldownArc);

        // A fixation on the snowflake starts the blizzard, when it is ready
        fixate(blizzardLabel, () -> blizzardCooldownTicks <= 0 ? tileHeight.get() * 1.8 : 0, this::startBlizzard);

        // The snow of the blizzard, drawn by the game (it was an animated picture of unknown source)
        snow.setMouseTransparent(true);
        snow.setVisible(false);
        final Rectangle frost = new Rectangle();
        frost.widthProperty().bind(gameContext.getRoot().widthProperty());
        frost.heightProperty().bind(gameContext.getRoot().heightProperty());
        frost.setFill(Color.rgb(200, 230, 255, 0.25));
        snow.getChildren().add(frost);
        for (int i = 0; i < 90; i++) {
            final Circle flake = new Circle(2 + 4 * random.nextDouble(), Color.rgb(255, 255, 255, 0.85));
            flake.setCenterX(dimensions.getWidth() * random.nextDouble());
            flake.setCenterY(dimensions.getHeight() * random.nextDouble());
            snow.getChildren().add(flake);
        }
        gameContext.getChildren().add(snow);

        // TOWER SELECTION: a fixation on a tower place opens the shop around it
        for (final Point2D turretsTile : map.getTurretsTiles()) {
            final Rectangle rectangle = new Rectangle();
            rectangle.xProperty().bind(tileWidth.multiply(turretsTile.getX() - 0.5));
            rectangle.yProperty().bind(tileHeight.multiply(turretsTile.getY() - 0.5));
            rectangle.widthProperty().bind(tileWidth.multiply(2));
            rectangle.heightProperty().bind(tileHeight.multiply(2));
            rectangle.setOpacity(0);
            gameContext.getChildren().add(rectangle);
            fixate(rectangle, () -> shop == null ? tileHeight.get() * 1.1 : 0,
                () -> createTowerSelection((int) turretsTile.getX(), (int) turretsTile.getY()));
        }

        // The ring of the fixations, above everything
        ring.setMouseTransparent(true);
        gameContext.getChildren().add(ring);

        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();
        gameContext.firstStart();
        phase = Phase.PLAY;
        showNextWave();
        canvas.draw();
        if (!manual) {
            gameLoop.start();
        }
    }

    /**
     * Game time passing, in seconds: as many ticks of 1/60 s as it holds (at the animation speed of the settings),
     * then the drawing.
     */
    void step(final double seconds) {
        if (phase != Phase.PLAY || seconds <= 0) {
            return;
        }
        final double ratio = Math.max(0.1, gameContext.getConfiguration().getAnimationSpeedRatio());
        tickDebt += seconds * TICKS_PER_SECOND * ratio;
        int ticks = 0;
        while (tickDebt >= 1 && phase == Phase.PLAY && ticks < 30) {
            tickDebt--;
            ticks++;
            tick();
        }
        if (ticks == 30) {
            tickDebt = 0;
        }
        canvas.draw();
    }

    /**
     * One tick of the game: the enemies come and move, the towers shoot, the projectiles fly and hit, the waves and
     * the blizzard go on.
     */
    void tick() {
        spawnEnemies();
        moveEnemies();
        if (phase != Phase.PLAY) {
            return;
        }
        for (final Tower tower : towers) {
            tower.fire(gameContext);
        }
        for (final Projectile projectile : projectiles) {
            projectile.move();
        }
        checkColisions();
        // A missile whose explosion is over goes, whether it touched an enemy or not
        projectiles.removeIf(projectile -> projectile instanceof Missile missile && missile.getFrameIndex() >= 8);

        tickBlizzard();
        tickWaves();
    }

    private void tickBlizzard() {
        if (blizzardTicks > 0) {
            blizzardTicks--;
            for (int i = 1; i < snow.getChildren().size(); i++) {
                final Circle flake = (Circle) snow.getChildren().get(i);
                flake.setCenterY(flake.getCenterY() + 1 + flake.getRadius() * 0.4);
                flake.setCenterX(flake.getCenterX() + Math.sin((blizzardTicks + i * 7) / 15.0) * 0.6);
                if (flake.getCenterY() > dimensions.getHeight()) {
                    flake.setCenterY(-5);
                }
            }
            if (blizzardTicks == 0) {
                snow.setVisible(false);
                enemies.forEach(enemy -> enemy.setFrozen(false));
            }
        }
        if (blizzardCooldownTicks > 0) {
            blizzardCooldownTicks--;
            blizzardCooldownArc.setLength(360.0 * blizzardCooldownTicks / (BLIZZARD_COOLDOWN * TICKS_PER_SECOND));
            blizzardCooldownArc.setVisible(blizzardCooldownTicks > 0);
        }
    }

    /**
     * The blizzard: the enemies stop for {@value #BLIZZARD_SECONDS} s; it comes back after
     * {@value #BLIZZARD_COOLDOWN} s (game time).
     */
    void startBlizzard() {
        if (phase != Phase.PLAY || blizzardCooldownTicks > 0) {
            return;
        }
        blizzardTicks = BLIZZARD_SECONDS * TICKS_PER_SECOND;
        blizzardCooldownTicks = BLIZZARD_COOLDOWN * TICKS_PER_SECOND;
        snow.setVisible(true);
        snow.toFront();
        enemies.forEach(enemy -> enemy.setFrozen(true));
    }

    private void tickWaves() {
        final boolean waveOver = enemiesSent >= enemiesToSend && enemies.isEmpty();
        if (waveOver && end.getWaves() > 0 && waveCount.get() >= end.getWaves()) {
            win();
            return;
        }
        if (waves.isAutomatic() && waveOver && !lastWaveSent()) {
            if (nextWaveTicks < 0) {
                nextWaveTicks = AUTOMATIC_WAVE_DELAY * TICKS_PER_SECOND;
            }
            nextWaveTicks--;
            if (nextWaveTicks % TICKS_PER_SECOND == 0) {
                showNextWave();
            }
            if (nextWaveTicks <= 0) {
                sendWave();
            }
        }
    }

    private boolean lastWaveSent() {
        return end.getWaves() > 0 && waveCount.get() >= end.getWaves();
    }

    private void showNextWave() {
        if (nextWaveLabel == null) {
            return;
        }
        if (nextWaveTicks > 0 && phase == Phase.PLAY) {
            nextWaveLabel.setText(gameContext.getTranslator().translate("TowerDefenseNextWave") + " "
                + (int) Math.ceil(nextWaveTicks / (double) TICKS_PER_SECOND) + " s");
        } else {
            nextWaveLabel.setText("");
        }
    }

    /**
     * A fixation on a node (gaze or mouse) runs an action: the ring fills on the node during the fixation length of
     * the settings. The size of the ring is asked when the fixation starts; 0: nothing to do now.
     */
    private void fixate(final Node node, final DoubleSupplier ringSize, final Runnable action) {
        final EventHandler<Event> enter = event -> {
            final double size = ringSize.getAsDouble();
            if (phase != Phase.PLAY || size <= 0) {
                return;
            }
            final Bounds bounds = node.localToScene(node.getBoundsInLocal());
            final Point2D center = ring.getParent() == null ? new Point2D(bounds.getCenterX(), bounds.getCenterY())
                : ring.getParent().sceneToLocal(bounds.getCenterX(), bounds.getCenterY());
            ring.setSize(size);
            ringOwner = node;
            ring.start(center.getX(), center.getY(), gameContext.getConfiguration().getFixationLength(), () -> {
                ringOwner = null;
                if (phase == Phase.PLAY) {
                    action.run();
                }
            });
        };
        // Only the node whose fixation runs stops the ring: the gaze is tested on each node apart, and the place of a
        // tower, under its shop, used to stop the fixation of a tower of the shop as soon as the gaze (which moves a
        // little) left it (found with the Tobii by the maintainer, 3/11/2026)
        final EventHandler<Event> exit = event -> {
            if (ringOwner == node) {
                ringOwner = null;
                ring.stop();
            }
        };
        node.addEventFilter(MouseEvent.MOUSE_ENTERED, enter);
        node.addEventFilter(GazeEvent.GAZE_ENTERED, enter);
        node.addEventFilter(MouseEvent.MOUSE_EXITED, exit);
        node.addEventFilter(GazeEvent.GAZE_EXITED, exit);
        gameContext.getGazeDeviceManager().addEventFilter(node);
    }

    private double fontSize() {
        return Math.max(14, tileWidth.get() / 2.4);
    }

    /**
     * Builds a tower of a type on a tower place, if the player has the money.
     */
    void createTower(final int col, final int row, final int type) {
        final Tower tower = newTower(col, row, type);
        if (money.get() >= tower.cost && getTower(col, row) == null) {
            money.set(money.get() - tower.cost);

            // Play little animation
            final ImageView image = new ImageView(moneyLossImage);
            image.setFitWidth(tileWidth.get() / 2);
            image.setFitHeight(tileHeight.get() / 2);
            image.setTranslateX(tower.getCol() * tileWidth.get());
            image.setTranslateY(tower.getRow() * tileHeight.get());
            image.setMouseTransparent(true);

            final FadeTransition ft = new FadeTransition(Duration.millis(1500), image);
            ft.setFromValue(1);
            ft.setToValue(0);
            ft.setOnFinished(event -> gameContext.getChildren().remove(image));

            final TranslateTransition tt = new TranslateTransition(Duration.millis(1500), image);
            tt.setFromY(tower.getRow() * tileHeight.get());
            tt.setToY(tower.getRow() * tileHeight.get() + tileHeight.get() / 2);

            ft.play();
            tt.play();
            gameContext.getChildren().add(image);

            towers.add(tower);
            //fire a sound when a tower is created
            gameContext.getSoundManager().add(tower.getSoundsConstruction());
        } else {
            // Let the user know why the tower is not built
            showMessage(Color.rgb(255, 120, 120), gameContext.getTranslator().translate("TowerDefenseNoMoney"));
        }
    }

    private Tower newTower(final int col, final int row, final int type) {
        return switch (type) {
            case DOUBLE_TOWER -> new DoubleTower(col, row, projectiles, enemies);
            case MISSILE_TOWER -> new MissileTower(col, row, projectiles, enemies);
            case CANON_TOWER -> new CanonTower(col, row, projectiles, enemies);
            default -> new BasicTower(col, row, projectiles, enemies);
        };
    }

    /**
     * @return the tower on a tile, null when there is none
     */
    Tower getTower(final double col, final double row) {
        for (final Tower tower : towers) {
            if (tower.getCol() == col && tower.getRow() == row) {
                return tower;
            }
        }
        return null;
    }

    /**
     * Sells the tower of a tile: its price back.
     */
    void sellTower(final int col, final int row) {
        final Tower tower = getTower(col, row);
        if (tower != null) {
            money.set(money.get() + tower.cost);
            towers.remove(tower);
        }
    }

    /**
     * Applies the hits of the projectiles (and the explosions of the missiles) to the enemies, and gives the reward of
     * the enemies killed.
     */
    private void checkColisions() {
        final ArrayList<Circle> explosions = new ArrayList<>();

        // Check projectiles
        final ListIterator<Enemy> enemyIter = enemies.listIterator();
        while (enemyIter.hasNext()) {
            final Enemy enemy = enemyIter.next();
            final ListIterator<Projectile> projIter = projectiles.listIterator();
            boolean dead = false;
            while (projIter.hasNext() && !dead) {
                final Projectile proj = projIter.next();

                // If Projectile collides with an enemy
                if (enemy.getHitbox().intersects(proj.getHitbox().getBoundsInLocal())) {
                    if (proj instanceof Missile missile) {
                        if (missile.isActive()) {
                            missile.setActive(false);
                            explosions.add(new Circle(proj.getX() + 0.5, proj.getY() + proj.getSize() + 0.5, EXPLOSION_RADIUS));
                        }
                    } else {
                        // Enemy loses HP, projectile disappears
                        projIter.remove();
                        enemy.loseHP(proj.getDamage());
                        if (enemy.getHealth() <= 0) {
                            enemyIter.remove();
                            killed(enemy);
                            dead = true;
                        }
                    }
                }
            }
        }
        // Remove the projectiles gone outside the screen
        projectiles.removeIf(proj -> proj.getX() < -1 || proj.getX() * tileWidth.get() > canvas.getWidth() || proj.getY() < -1 || proj.getY() * tileHeight.get() > canvas.getHeight());

        // Check Explosions
        for (final Circle explosion : explosions) {
            final ListIterator<Enemy> enemyIter2 = enemies.listIterator();
            while (enemyIter2.hasNext()) {
                final Enemy enemy2 = enemyIter2.next();
                if (explosion.intersects(enemy2.getHitbox().getBoundsInLocal())) {
                    enemy2.loseHP(EXPLOSION_DAMAGE);
                    if (enemy2.getHealth() <= 0) {
                        enemyIter2.remove();
                        killed(enemy2);
                    }
                }
            }
        }
    }

    /**
     * An enemy killed: its reward, one more goal reached.
     */
    private void killed(final Enemy enemy) {
        money.set(money.get() + enemy.getReward());
        stats.incrementNumberOfGoalsReached();
        gameContext.updateScore(stats, this);
        playEnemyDeathAnimation(enemy);
    }

    /**
     * Shows the money won where an enemy died.
     */
    private void playEnemyDeathAnimation(final Enemy enemy) {
        final ImageView image = new ImageView(moneyGainImage);
        image.setFitWidth(tileWidth.get() / 2);
        image.setFitHeight(tileHeight.get() / 2);
        image.setTranslateX(enemy.getX() * tileWidth.get());
        image.setTranslateY(enemy.getCenter().getY() * tileHeight.get());
        image.setMouseTransparent(true);

        final FadeTransition ft = new FadeTransition(Duration.millis(1500), image);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(event -> gameContext.getChildren().remove(image));

        final TranslateTransition tt = new TranslateTransition(Duration.millis(1500), image);
        tt.setFromY(enemy.getCenter().getY() * tileHeight.get());
        tt.setToY(enemy.getY() * tileHeight.get());

        ft.play();
        tt.play();
        gameContext.getChildren().add(image);
    }

    /**
     * Moves the enemies along the road; an enemy reaching the end costs a life.
     */
    private void moveEnemies() {
        final ListIterator<Enemy> enemyIter = enemies.listIterator();
        while (enemyIter.hasNext()) {
            final Enemy enemy = enemyIter.next();
            if (!enemy.isFrozen()) {
                enemy.move();
            }
            if (enemy.reachedEnd()) {
                enemyIter.remove();
                loseLife();
                if (phase != Phase.PLAY) {
                    return;
                }
            }
        }
    }

    /**
     * @return a label with an icon (life, money, wave), in the font of the games
     */
    private Label createLabel(final String text, final String imagePath) {
        final Label label = new Label(text);
        final String color = " -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 4, 0.5, 0, 1);";
        tileWidth.addListener(observable -> label.setStyle(GameStyle.titleFontStyle(fontSize()) + color));
        label.setStyle(GameStyle.titleFontStyle(fontSize()) + color);
        final ImageView image = new ImageView(new Image(imagePath));
        image.fitWidthProperty().bind(tileWidth);
        image.fitHeightProperty().bind(tileHeight);
        label.setGraphic(image);
        return label;
    }

    private void showLife() {
        // As big as the icons of the money and the waves (a tile); the map is the background: dark enough for white
        lifeView.getChildren().setAll(GameStyle.lives(life.get(), level.getLives(), tileHeight.get(),
            gameContext.getRoot().getWidth() * 0.35, true));
    }

    private void showMoney() {
        moneyLabel.setText(String.valueOf((int) Math.round(money.get())));
    }

    private void showWaves() {
        waveCountLabel.setText(waveCount.get() + (end.getWaves() > 0 ? " / " + end.getWaves() : ""));
    }

    /**
     * Removes a life; the game is lost at zero.
     */
    private void loseLife() {
        life.set(life.get() - 1);
        if (life.get() <= 0) {
            lose();
        }
    }

    /**
     * The next wave: alternately 5 and 10 enemies, stronger every two waves (by the level).
     */
    void sendWave() {
        if (phase != Phase.PLAY || lastWaveSent()) {
            return;
        }
        enemiesSent = 0;
        enemiesToSend = (waveCount.get() % 2 + 1) * 5;
        // Stronger every two waves (a whole number of pairs of waves)
        final int pairsOfWaves = waveCount.get() / 2;
        enemyHealthMultiplier = 1 + pairsOfWaves * level.getHealthGrowth();
        waveCount.set(waveCount.get() + 1);
        nextWaveTicks = -1;
        showNextWave();
        sendWaveButton.setVisible(false);
    }

    /**
     * Sends the next enemy of the wave when its delay has passed.
     */
    private void spawnEnemies() {
        if (enemiesSent < enemiesToSend && enemySpawnTick >= ENEMY_SPAWN_TICK_LIMIT) {
            enemySpawnTick = 0;
            enemiesSent++;
            final Enemy enemy;
            // Fast enemies every 10 waves, tanks every 5 (but the 5th)
            if (waveCount.get() % 10 == 0) {
                enemy = new FastEnemy(map, map.getStartCol(), map.getStartRow());
            } else if (waveCount.get() % 5 == 0 && waveCount.get() != 5) {
                enemy = new TankEnemy(map, map.getStartCol(), map.getStartRow());
            } else {
                enemy = new BasicEnemy(map, map.getStartCol(), map.getStartRow());
            }
            enemy.multiplyHealth(enemyHealthMultiplier);
            enemy.setFrozen(blizzardTicks > 0);
            enemies.add(enemy);
            stats.incrementNumberOfGoalsToReach();
        } else {
            enemySpawnTick++;
        }
        if (enemiesSent >= enemiesToSend && !lastWaveSent()) {
            sendWaveButton.setVisible(true);
        }
    }

    private void win() {
        if (phase != Phase.PLAY) {
            return;
        }
        phase = Phase.OVER;
        gameLoop.stop();
        sendWaveButton.setVisible(false);
        stats.takeScreenShot();
        gameContext.updateScore(stats, this);
        gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
    }

    /**
     * The lives over: "Lost!", then the statistics (it used to show the animation of a win).
     */
    private void lose() {
        if (phase != Phase.PLAY) {
            return;
        }
        phase = Phase.OVER;
        gameLoop.stop();
        sendWaveButton.setVisible(false);
        nextWaveTicks = -1;
        showNextWave();
        stats.takeScreenShot();
        gameContext.updateScore(stats, this);
        final Group card = GameStyle.card(gameContext.getTranslator().translate("TowerDefenseLost"),
            dimensions.getHeight() * 0.06, dimensions.getWidth() * 0.6);
        card.setLayoutX(dimensions.getWidth() / 2 - card.getBoundsInLocal().getWidth() / 2);
        card.setLayoutY(dimensions.getHeight() / 2 - card.getBoundsInLocal().getHeight() / 2);
        gameContext.getChildren().add(card);
        lostPause = new PauseTransition(Duration.seconds(2));
        lostPause.setOnFinished(event -> gameContext.showRoundStats(stats, this));
        lostPause.play();
    }

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        gameLoop.stop();
    }

    int getLife() {
        return life.get();
    }

    double getMoney() {
        return money.get();
    }

    int getWaveCount() {
        return waveCount.get();
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        gameLoop.stop();
        if (lostPause != null) {
            lostPause.stop();
        }
        gameContext.clear();
        enemies.clear();
        towers.clear();
        projectiles.clear();
    }


    /**
     * The radius of the shop, in tiles.
     */
    private static final double SHOP_RADIUS = 2.5;

    /**
     * Opens, around a tower place, the shop: the four towers that can be built there with their price (or the button
     * selling the tower already there), each chosen by a fixation, and beside it a card saying what the tower looked
     * at does. The shop stays inside the screen (it used to go out of it at the edges), the card too.
     */
    void createTowerSelection(final int col, final int row) {
        closeShop();
        final double width = tileWidth.get() * map.getNbCols();
        final double height = tileHeight.get() * map.getNbRows();
        final double rx = SHOP_RADIUS * tileWidth.get();
        final double ry = SHOP_RADIUS * tileHeight.get();
        final double cx = Math.max(rx, Math.min(width - rx, (col + 0.5) * tileWidth.get()));
        final double cy = Math.max(ry, Math.min(height - ry, (row + 0.5) * tileHeight.get()));

        final Group group = new Group();
        shop = group;
        gameContext.getChildren().add(group);

        final Ellipse ellipse = new Ellipse(cx, cy, rx, ry);
        ellipse.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-accent; -fx-stroke-width: 4;");
        ellipse.setOpacity(0.9);
        ellipse.setMouseTransparent(true);
        group.getChildren().add(ellipse);

        // Bigger rectangle to work better with eye-tracking: leaving it closes the shop
        final double left = Math.max(0, cx - rx - tileWidth.get() / 2);
        final double up = Math.max(0, cy - ry - tileHeight.get() / 2);
        final Rectangle area = new Rectangle(left, up,
            Math.min(width, cx + rx + tileWidth.get() / 2) - left, Math.min(height, cy + ry + tileHeight.get() / 2) - up);
        area.setOpacity(0);
        group.getChildren().add(area);
        area.addEventFilter(GazeEvent.GAZE_EXITED, event -> closeShop(group));
        // The mouse "leaves" the area as soon as it is over a tower of the shop (another node, above it): the shop
        // used to close then, no tower could be chosen; it closes only when the mouse is out of the area
        area.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            if (!area.contains(area.sceneToLocal(event.getSceneX(), event.getSceneY()))) {
                closeShop(group);
            }
        });
        gameContext.getGazeDeviceManager().addEventFilter(area);

        // The towers and the card, above the area, let the mouse go out of the shop through them as well
        group.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            if (event.getTarget() != area && !area.contains(area.sceneToLocal(event.getSceneX(), event.getSceneY()))) {
                closeShop(group);
            }
        });

        final Tower existing = getTower(col, row);
        if (existing != null) {
            createSellIcon(col, row, cx, cy, group);
            showShopCard(describe(existing) + "\n" + translate("TowerDefenseSellHint") + " " + (int) existing.cost
                + " " + translate("TowerDefenseCoins"), cx, cy, width, height);
        } else {
            createTowerIcon(BASIC_TOWER, col, row, cx, cy, group);
            createTowerIcon(MISSILE_TOWER, col, row, cx, cy, group);
            createTowerIcon(CANON_TOWER, col, row, cx, cy, group);
            createTowerIcon(DOUBLE_TOWER, col, row, cx, cy, group);
            showShopCard(translate("TowerDefenseChooseHint"), cx, cy, width, height);
        }
    }

    private void closeShop(final Group group) {
        if (shop == group) {
            closeShop();
        }
    }

    private void closeShop() {
        if (shop != null) {
            ringOwner = null;
            ring.stop();
            gameContext.getChildren().remove(shop);
            shop = null;
            shopCard = null;
        }
    }

    private String translate(final String key) {
        return gameContext.getTranslator().translate(key);
    }

    /**
     * @return what a tower does: its name, its role, its damage, range, rate of fire and price
     */
    String describe(final Tower tower) {
        final String kind;
        if (tower instanceof DoubleTower) {
            kind = "Double";
        } else if (tower instanceof MissileTower) {
            kind = "Missile";
        } else if (tower instanceof CanonTower) {
            kind = "Canon";
        } else {
            kind = "Basic";
        }
        final java.util.Locale locale = "fra".equals(gameContext.getConfiguration().getLanguage())
            ? java.util.Locale.FRENCH : java.util.Locale.ENGLISH;
        final double period = (tower.getFireTickLimit() + 1) / TICKS_PER_SECOND;
        return translate("TowerDefense" + kind + "Name") + "\n" + translate("TowerDefense" + kind + "Role") + "\n"
            + translate("TowerDefenseDamage") + " " + (int) tower.damage + "  ·  "
            + translate("TowerDefenseRange") + " " + tower.range + " " + translate("TowerDefenseTiles") + "\n"
            + translate("TowerDefenseEvery") + " " + String.format(locale, "%.1f", period) + " s  ·  "
            + translate("TowerDefensePrice") + " " + (int) tower.cost + " " + translate("TowerDefenseCoins");
    }

    /**
     * The card beside the shop (at its right, or at its left near the right edge), inside the screen.
     */
    private void showShopCard(final String text, final double cx, final double cy, final double width,
                              final double height) {
        if (shop == null) {
            return;
        }
        if (shopCard != null) {
            shop.getChildren().remove(shopCard);
        }
        final double margin = tileHeight.get() * 0.3;
        final double fontSize = Math.max(13, tileHeight.get() * 0.32);
        final Group card = GameStyle.card(text, fontSize, Math.max(220, width * 0.28));
        final double cardWidth = card.getBoundsInLocal().getWidth();
        final double cardHeight = card.getBoundsInLocal().getHeight();
        final double rx = SHOP_RADIUS * tileWidth.get();
        double x = cx + rx + margin;
        if (x + cardWidth > width - margin) {
            x = cx - rx - margin - cardWidth;
        }
        x = Math.max(margin, Math.min(width - margin - cardWidth, x));
        final double y = Math.max(margin, Math.min(height - margin - cardHeight, cy - cardHeight / 2));
        card.setLayoutX(x);
        card.setLayoutY(y);
        card.setMouseTransparent(true);
        shopCard = card;
        shop.getChildren().add(card);
    }

    /**
     * Adds to the shop the icon of a type of tower, with its price; looked at, the card says what it does; a fixation
     * on it builds the tower.
     */
    private void createTowerIcon(final int towerType, final int col, final int row, final double cx, final double cy,
                                 final Group group) {
        final Tower model = newTower(col, row, towerType);
        final ImageView tower = new ImageView(switch (towerType) {
            case MISSILE_TOWER -> missileTowerImage;
            case DOUBLE_TOWER -> doubleTowerImage;
            case CANON_TOWER -> canonTowerImage;
            default -> basicTowerImage;
        });
        final double size = (SHOP_RADIUS - 0.5) * tileHeight.get();
        tower.setFitWidth(size);
        tower.setFitHeight(size);
        final double rx = SHOP_RADIUS * tileWidth.get();
        final double ry = SHOP_RADIUS * tileHeight.get();
        final StackPane stackPane = new StackPane();
        switch (towerType) {
            case MISSILE_TOWER -> {
                stackPane.setTranslateX(cx - rx);
                stackPane.setTranslateY(cy - size / 2);
            }
            case DOUBLE_TOWER -> {
                stackPane.setTranslateX(cx - size / 2);
                stackPane.setTranslateY(cy + ry - size);
            }
            case CANON_TOWER -> {
                stackPane.setTranslateX(cx + rx - size);
                stackPane.setTranslateY(cy - size / 2);
            }
            default -> {
                stackPane.setTranslateX(cx - size / 2);
                stackPane.setTranslateY(cy - ry);
            }
        }

        // The price, read from the tower (it used to be written apart)
        final Group costLabel = GameStyle.pill(String.valueOf((int) model.cost), Math.max(12, size / 5));
        costLabel.setMouseTransparent(true);
        StackPane.setAlignment(costLabel, Pos.BOTTOM_CENTER);
        stackPane.getChildren().addAll(tower, costLabel);
        tower.setPickOnBounds(true);
        group.getChildren().add(stackPane);

        final double width = tileWidth.get() * map.getNbCols();
        final double height = tileHeight.get() * map.getNbRows();
        final EventHandler<Event> describeIt = event -> showShopCard(describe(model), cx, cy, width, height);
        tower.addEventFilter(MouseEvent.MOUSE_ENTERED, describeIt);
        tower.addEventFilter(GazeEvent.GAZE_ENTERED, describeIt);
        fixate(tower, () -> size * 0.9, () -> {
            closeShop(group);
            createTower(col, row, towerType);
        });
    }

    /**
     * Adds to the shop the icon selling the tower of the tile; a fixation on it sells the tower.
     */
    private void createSellIcon(final int col, final int row, final double cx, final double cy, final Group group) {
        final Tower sold = getTower(col, row);
        final ImageView sellIcon = new ImageView(sellImages.getOrDefault(sold == null ? BasicTower.class : sold.getClass(),
            sellImages.get(BasicTower.class)));
        sellIcon.setPickOnBounds(true);
        final double size = (SHOP_RADIUS - 0.5) * tileHeight.get();
        sellIcon.setFitWidth(size);
        sellIcon.setFitHeight(size);
        sellIcon.setX(cx - size / 2);
        sellIcon.setY(cy - size / 2);
        group.getChildren().add(sellIcon);
        fixate(sellIcon, () -> size * 0.9, () -> {
            closeShop(group);
            sellTower(col, row);
        });
    }

    /**
     * Displays a message in a vertical queue, which disappears after a short time (wrapped inside the screen: it used
     * to go out of it).
     */
    public void showMessage(final Color fontColor, final String message) {
        final Text messageText = new Text(message);
        messageText.setTextAlignment(TextAlignment.CENTER);
        messageText.setFill(fontColor);
        messageText.setStyle(GameStyle.titleFontStyle(dimensions.getHeight() / 14)
            + " -fx-stroke: rgba(40,36,60,0.9); -fx-stroke-width: 2;");
        messageText.setWrappingWidth(Math.max(200, dimensions.getWidth() - 2 * MESSAGE_PADDING - 20));
        messageText.setOpacity(0);

        messages.getChildren().add(messageText);

        final Timeline showMessage = new Timeline(
            new KeyFrame(Duration.seconds(0.3), new KeyValue(messageText.opacityProperty(), 1)),
            new KeyFrame(Duration.seconds(3), new KeyValue(messageText.opacityProperty(), 1)),
            new KeyFrame(Duration.seconds(3.3), new KeyValue(messageText.opacityProperty(), 0)));

        showMessage.setOnFinished(e -> messages.getChildren().remove(messageText));

        showMessage.playFromStart();
    }
}

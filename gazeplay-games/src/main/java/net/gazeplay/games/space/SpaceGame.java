package net.gazeplay.games.space;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GameStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * "Space Game" (reviewed on 3/11/2026, maintainer's rules): the rocket goes along the bottom of the screen where the
 * gaze or the mouse is (as the setting of the inputs allows), and shoots lasers up by itself; aliens in flying saucers
 * go to and fro at the top and drop red balls. An alien hit is destroyed: a flash, a sound, one more alien.
 * <p>
 * Three lives: touched by a ball, the rocket loses one and blinks {@value #INVINCIBLE} s, during which nothing touches
 * it; the three lost, "Lost!" then the statistics. After 1 or 3 minutes, or 20 or 50 aliens destroyed, as chosen, the
 * other aliens go and the boss comes (15, 30 or 50 points of life by the level): destroyed, "Bravo" then the
 * statistics. Without end, no boss.
 * <p>
 * One loop moves everything in real time, at the animation speed of the settings; nothing moves and the time does not
 * run while the menu of the game is open.
 */
public class SpaceGame implements GameLifeCycle {

    static final int LIVES = 3;
    static final double SHOT_PERIOD = 0.45;
    static final double INVINCIBLE = 2;
    static final double SPAWN_DELAY = 0.8;
    static final double APPEARING = 0.6;
    static final double BOSS_ARRIVAL = 1.5;
    static final double BOSS_SHOT_PERIOD = 1.3;

    /**
     * Speeds, in heights of the screen per second, at the normal animation speed.
     */
    static final double SHIP_SPEED = 0.9;
    static final double LASER_SPEED = 1.4;
    static final double ALIEN_SPEED = 0.12;
    static final double BALL_SPEED = 0.4;
    static final double BOSS_SPEED = 0.15;

    static final String[] COLORS = {"Blue", "Green", "Yellow", "Orange", "Red"};
    static final String FOLDER = "data/space/images/";

    enum Phase { PLAY, BOSS, OVER }

    private final IGameContext gameContext;
    private final Stats stats;
    @Getter
    private final SpaceLevel level;
    @Getter
    private final SpaceEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private int lives;
    @Getter
    private int destroyed;
    @Getter
    private double timeLeft;

    private double width;
    private double height;
    private double top;

    private Group layer;
    private Group actors;
    private Group hud;
    private ImageView background;

    @Getter
    private double shipX;
    @Getter
    private double shipY;
    private double shipSize;
    private ImageView shipView;
    @Getter
    private double invincible;
    private double aimX;
    private boolean aiming;
    private double shotWait;
    private double spawnWait;

    private final Map<String, Image> alienImages = new HashMap<>();
    private Image flashImage;

    @Getter
    private final List<Alien> aliens = new ArrayList<>();
    @Getter
    private final List<Shot> shots = new ArrayList<>();
    @Getter
    private Boss boss;

    private final EventHandler<Event> aimHandler = this::aim;
    private PauseTransition lostPause;

    private final AnimationTimer loop = new AnimationTimer() {
        private long last = -1;

        @Override
        public void handle(final long now) {
            final double seconds = last < 0 ? 0 : Math.min(0.05, (now - last) / 1e9);
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
    private int shownSeconds = -1;

    public SpaceGame(final IGameContext gameContext, final Stats stats, final SpaceLevel level, final SpaceEnd end) {
        this(gameContext, stats, level, end, -1);
    }

    /**
     * @param gameSeed the seed of the game to replay, or a negative value for a new game
     */
    public SpaceGame(final IGameContext gameContext, final Stats stats, final SpaceLevel level, final SpaceEnd end,
                     final double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.level = level;
        this.end = end;
        if (gameSeed < 0) {
            random = new ReplayablePseudoRandom();
            stats.setGameSeed(random.getSeed());
        } else {
            random = new ReplayablePseudoRandom(gameSeed);
        }
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        width = dimension.getWidth();
        height = dimension.getHeight();
        top = height * 0.1;

        background = new ImageView(new Image(FOLDER + "night.png"));
        background.setFitWidth(width);
        background.setFitHeight(height);
        for (final String color : COLORS) {
            alienImages.put(color, new Image(FOLDER + color + ".png"));
        }
        flashImage = new Image("data/shooter/robot/images/Flash.png");

        // The rocket, at the bottom in the middle
        shipSize = height * 0.13;
        shipX = width / 2;
        shipY = height - shipSize * 0.6;
        aimX = shipX;
        shipView = new ImageView(new Image(FOLDER + "ship.png"));
        shipView.setFitWidth(shipSize * 1.25);
        shipView.setFitHeight(shipSize);
        shipView.setMouseTransparent(true);

        actors = new Group();
        actors.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        layer = new Group(background, actors, shipView, hud);
        gameContext.getChildren().add(layer);

        // The rocket goes where the gaze or the mouse is. Nothing to choose: with the mouse only, no gaze comes; with
        // the gaze only, the game area stops the mouse (GameContextFactory); with both, the last that moved leads
        gameContext.getGazeDeviceManager().addEventFilter(background);
        background.addEventFilter(GazeEvent.ANY, aimHandler);
        layer.addEventFilter(MouseEvent.MOUSE_MOVED, aimHandler);
        layer.addEventFilter(MouseEvent.MOUSE_DRAGGED, aimHandler);

        lives = LIVES;
        destroyed = 0;
        timeLeft = end.getSeconds();
        shotWait = SHOT_PERIOD;
        spawnWait = 0.3;
        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();
        gameContext.firstStart();
        phase = Phase.PLAY;
        placeShip();
        showHud();
        if (!manual) {
            loop.start();
        }
    }

    private void aim(final Event event) {
        if (event instanceof GazeEvent gaze && (gaze.getEventType() == GazeEvent.GAZE_MOVED
            || gaze.getEventType() == GazeEvent.GAZE_ENTERED)) {
            aimAt(gaze.getX());
        } else if (event instanceof MouseEvent mouse) {
            aimAt(mouse.getX());
        }
    }

    /**
     * The place looked at (or of the mouse), where the rocket goes along the bottom.
     */
    void aimAt(final double x) {
        aimX = x;
        aiming = true;
    }

    private double shipRadius() {
        return shipSize * 0.38;
    }

    private double alienSize() {
        return height * 0.1;
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        final double ratio = Math.max(0.1, gameContext.getConfiguration().getAnimationSpeedRatio());
        final double dt = seconds * ratio;

        moveShip(dt);
        if (phase == Phase.PLAY) {
            spawnAliens(dt);
        }
        for (final Alien alien : new ArrayList<>(aliens)) {
            alien.move(dt);
        }
        if (boss != null) {
            boss.move(dt);
        }
        shoot(dt);
        for (final Shot shot : new ArrayList<>(shots)) {
            shot.move(dt);
            if (phase == Phase.OVER) {
                return;
            }
        }
        invincible = Math.max(0, invincible - seconds);
        placeShip();

        if (phase == Phase.PLAY && end.getSeconds() > 0) {
            timeLeft = Math.max(0, timeLeft - seconds);
            if (timeLeft <= 0) {
                callTheBoss();
            }
        }
        if (end.getSeconds() > 0 && (int) Math.ceil(timeLeft) != shownSeconds) {
            showHud();
        }
    }

    private void moveShip(final double dt) {
        if (!aiming) {
            return;
        }
        final double speed = SHIP_SPEED * height * dt;
        final double dx = aimX - shipX;
        shipX += Math.abs(dx) <= speed ? dx : Math.signum(dx) * speed;
        final double margin = shipSize * 0.62;
        shipX = Math.max(margin, Math.min(width - margin, shipX));
    }

    private void placeShip() {
        shipView.setLayoutX(shipX - shipSize * 1.25 / 2);
        shipView.setLayoutY(shipY - shipSize / 2);
        // Blinking while nothing touches it after a life lost
        shipView.setOpacity(invincible > 0 && ((int) (invincible * 8)) % 2 == 0 ? 0.35 : 1);
    }

    private void spawnAliens(final double dt) {
        if (aliens.size() >= level.getAliens()) {
            return;
        }
        spawnWait -= dt;
        if (spawnWait > 0) {
            return;
        }
        spawnWait = SPAWN_DELAY;
        final double size = alienSize();
        final double x = size * 0.7 + (width - size * 1.4) * random.nextDouble();
        final double y = top + size / 2 + height * 0.35 * random.nextDouble();
        addAlien(x, y);
    }

    /**
     * An alien comes at this place (it fades in, and cannot be hit nor shoot meanwhile).
     */
    Alien addAlien(final double x, final double y) {
        final Alien alien = new Alien(x, y);
        aliens.add(alien);
        actors.getChildren().add(alien.view);
        stats.incrementNumberOfGoalsToReach();
        return alien;
    }

    private void shoot(final double dt) {
        shotWait -= dt;
        if (shotWait > 0) {
            return;
        }
        shotWait = SHOT_PERIOD;
        addShot(new Shot(shipX, shipY - shipSize * 0.45, 0, -1, true, LASER_SPEED));
    }

    private void addShot(final Shot shot) {
        shots.add(shot);
        actors.getChildren().add(shot.node);
    }

    /**
     * The rocket touched by a ball: one life less, unless it blinks.
     */
    void hurt() {
        if (phase == Phase.OVER || invincible > 0) {
            return;
        }
        lives--;
        showHud();
        if (lives <= 0) {
            lose();
        } else {
            invincible = INVINCIBLE;
            placeShip();
        }
    }

    /**
     * An alien destroyed: a flash, a sound, one more alien destroyed; the number to destroy reached, the boss comes.
     */
    void destroy(final Alien alien) {
        if (phase != Phase.PLAY || !aliens.remove(alien)) {
            return;
        }
        actors.getChildren().remove(alien.view);
        flash(alien.x, alien.y, alienSize());
        gameContext.getSoundManager().add("data/shooter/robot/sounds/shot" + (1 + random.nextInt(3)) + ".wav");
        destroyed++;
        stats.incrementNumberOfGoalsReached();
        gameContext.updateScore(stats, this);
        showHud();
        if (end.getAliens() > 0 && destroyed >= end.getAliens()) {
            callTheBoss();
        }
    }

    /**
     * The time over or the aliens destroyed: the other aliens fade away, the boss comes down from the top.
     */
    void callTheBoss() {
        if (phase != Phase.PLAY || !end.hasBoss()) {
            return;
        }
        phase = Phase.BOSS;
        for (final Alien alien : new ArrayList<>(aliens)) {
            aliens.remove(alien);
            final FadeTransition fade = new FadeTransition(Duration.millis(600), alien.view);
            fade.setToValue(0);
            fade.setOnFinished(event -> actors.getChildren().remove(alien.view));
            fade.play();
        }
        for (final Shot shot : new ArrayList<>(shots)) {
            if (!shot.fromShip) {
                shot.remove();
            }
        }
        boss = new Boss();
        actors.getChildren().add(boss.node);
        stats.incrementNumberOfGoalsToReach();
        showHud();
    }

    /**
     * The boss hit by a laser: one point of life less; none left, a big flash, "Bravo" then the statistics.
     */
    void hit(final Boss target) {
        if (phase != Phase.BOSS || target != boss || boss.arriving > 0) {
            return;
        }
        boss.life--;
        boss.showLife();
        final FadeTransition blink = new FadeTransition(Duration.millis(120), boss.view);
        blink.setFromValue(0.5);
        blink.setToValue(1);
        blink.play();
        if (boss.life <= 0) {
            actors.getChildren().remove(boss.node);
            flash(boss.x, boss.y, boss.height);
            gameContext.getSoundManager().add("data/shooter/robot/sounds/shot1.wav");
            stats.incrementNumberOfGoalsReached();
            win();
        }
    }

    private void flash(final double x, final double y, final double size) {
        final ImageView view = new ImageView(flashImage);
        view.setFitWidth(size * 1.25);
        view.setFitHeight(size);
        view.setLayoutX(x - size * 1.25 / 2);
        view.setLayoutY(y - size / 2);
        view.setMouseTransparent(true);
        actors.getChildren().add(view);
        final ScaleTransition pop = new ScaleTransition(Duration.millis(250), view);
        pop.setToX(1.4);
        pop.setToY(1.4);
        final FadeTransition fade = new FadeTransition(Duration.millis(400), view);
        fade.setToValue(0);
        fade.setOnFinished(event -> actors.getChildren().remove(view));
        pop.play();
        fade.play();
    }

    /**
     * The lives at the top left, the aliens destroyed at the top right, the time left in the middle (or the boss).
     */
    private void showHud() {
        hud.getChildren().clear();
        final double fontSize = height * 0.035;
        final double margin = height * 0.025;
        final double heart = height * 0.055;
        for (int i = 0; i < LIVES; i++) {
            final SVGPath icon = GameStyle.icon(i < lives ? "favorite_fill1" : "favorite", heart);
            icon.setFill(i < lives ? Color.rgb(235, 70, 95) : Color.rgb(200, 200, 215, 0.8));
            icon.setStroke(Color.WHITE);
            icon.setStrokeWidth(18);
            icon.setLayoutX(margin + i * heart * 1.1);
            icon.setLayoutY(margin);
            hud.getChildren().add(icon);
        }
        String text = gameContext.getTranslator().translate("SpaceScore") + " " + destroyed;
        if (end.getAliens() > 0) {
            text += " / " + end.getAliens();
        }
        final Group pill = GameStyle.pill(text, fontSize);
        pill.setLayoutX(width - pill.getBoundsInLocal().getWidth() - margin);
        pill.setLayoutY(margin);
        hud.getChildren().add(pill);
        String middle = null;
        shownSeconds = (int) Math.ceil(timeLeft);
        if (phase == Phase.BOSS || boss != null) {
            middle = gameContext.getTranslator().translate("SpaceBoss");
        } else if (end.getSeconds() > 0) {
            middle = String.format("%d:%02d", shownSeconds / 60, shownSeconds % 60);
        }
        if (middle != null) {
            final Group clock = GameStyle.pill(middle, fontSize);
            clock.setLayoutX(width / 2 - clock.getBoundsInLocal().getWidth() / 2);
            clock.setLayoutY(margin);
            hud.getChildren().add(clock);
        }
    }

    private void stopPlaying() {
        phase = Phase.OVER;
        loop.stop();
    }

    private void win() {
        if (phase == Phase.OVER) {
            return;
        }
        stopPlaying();
        stats.takeScreenShot();
        gameContext.updateScore(stats, this);
        gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
    }

    /**
     * The three lives lost: the rocket flashes, "Lost!", then the statistics.
     */
    private void lose() {
        if (phase == Phase.OVER) {
            return;
        }
        stopPlaying();
        shipView.setVisible(false);
        flash(shipX, shipY, shipSize);
        stats.takeScreenShot();
        gameContext.updateScore(stats, this);
        final Group card = GameStyle.card(gameContext.getTranslator().translate("SpaceLost"), height * 0.06,
            width * 0.6);
        card.setLayoutX(width / 2 - card.getBoundsInLocal().getWidth() / 2);
        card.setLayoutY(height / 2 - card.getBoundsInLocal().getHeight() / 2);
        hud.getChildren().add(card);
        lostPause = new PauseTransition(Duration.seconds(2));
        lostPause.setOnFinished(event -> gameContext.showRoundStats(stats, this));
        lostPause.play();
    }

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        loop.stop();
    }

    double getHeight() {
        return height;
    }

    @Override
    public void dispose() {
        stopPlaying();
        if (lostPause != null) {
            lostPause.stop();
        }
        if (background != null) {
            gameContext.getGazeDeviceManager().removeEventFilter(background);
        }
        aliens.clear();
        shots.clear();
        boss = null;
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * An alien in its saucer: it goes to and fro at the top, a little up and down, and drops a red ball from time to
     * time.
     */
    final class Alien {
        @Getter
        private double x;
        @Getter
        private double y;
        private final double baseY;
        private double dirX;
        private double time;
        private double shotWait;
        private double appearing = APPEARING;
        private final ImageView view;

        Alien(final double x, final double y) {
            this.x = x;
            this.y = y;
            baseY = y;
            dirX = random.nextBoolean() ? 1 : -1;
            time = 10 * random.nextDouble();
            shotWait = level.getShotPeriod() * (0.5 + random.nextDouble());
            view = new ImageView(alienImages.get(COLORS[random.nextInt(COLORS.length)]));
            view.setFitWidth(alienSize() * 1.25);
            view.setFitHeight(alienSize());
            view.setMouseTransparent(true);
            view.setOpacity(0);
            place();
        }

        double radius() {
            return alienSize() * 0.5;
        }

        boolean isAppearing() {
            return appearing > 0;
        }

        private void place() {
            view.setLayoutX(x - alienSize() * 1.25 / 2);
            view.setLayoutY(y - alienSize() / 2);
        }

        void move(final double dt) {
            if (appearing > 0) {
                appearing -= dt;
                view.setOpacity(Math.min(1, 1 - appearing / APPEARING));
                return;
            }
            time += dt;
            final double margin = alienSize() * 0.7;
            x += dirX * ALIEN_SPEED * height * dt;
            if (x < margin || x > width - margin) {
                dirX = -dirX;
                x = Math.max(margin, Math.min(width - margin, x));
            }
            y = baseY + Math.sin(time * 1.3) * height * 0.03;
            place();
            shotWait -= dt;
            if (shotWait <= 0) {
                shotWait = level.getShotPeriod() * (0.5 + random.nextDouble());
                addShot(new Shot(x, y + alienSize() * 0.4, 0, 1, false, BALL_SPEED));
            }
        }
    }

    /**
     * The boss: it comes down from the top, goes to and fro, and drops three balls at a time; its life above it.
     */
    final class Boss {
        @Getter
        private double x;
        @Getter
        private double y;
        private double dirX = 1;
        private final double width = SpaceGame.this.height * 0.4;
        private final double height = SpaceGame.this.height * 0.25;
        private double arriving = BOSS_ARRIVAL;
        private double shotWait = BOSS_SHOT_PERIOD;
        @Getter
        private int life = level.getBossLife();
        private final ImageView view;
        private final Rectangle bar;
        private final Rectangle barBack;
        private final Group node;

        Boss() {
            x = SpaceGame.this.width / 2;
            y = -height;
            view = new ImageView(new Image(FOLDER + "boss.png"));
            view.setFitWidth(width);
            view.setFitHeight(height);
            barBack = new Rectangle(width * 0.7, SpaceGame.this.height * 0.018);
            barBack.setArcWidth(barBack.getHeight());
            barBack.setArcHeight(barBack.getHeight());
            barBack.setFill(Color.rgb(255, 255, 255, 0.35));
            barBack.setStroke(Color.WHITE);
            bar = new Rectangle(width * 0.7, barBack.getHeight());
            bar.setArcWidth(bar.getHeight());
            bar.setArcHeight(bar.getHeight());
            bar.setFill(Color.rgb(240, 80, 100));
            node = new Group(view, barBack, bar);
            node.setMouseTransparent(true);
            showLife();
            place();
        }

        double radius() {
            return height * 0.45;
        }

        void showLife() {
            bar.setWidth(barBack.getWidth() * Math.max(0, life) / level.getBossLife());
        }

        private void place() {
            view.setLayoutX(x - width / 2);
            view.setLayoutY(y - height / 2);
            barBack.setX(x - barBack.getWidth() / 2);
            barBack.setY(y - height / 2 - barBack.getHeight() * 1.8);
            bar.setX(barBack.getX());
            bar.setY(barBack.getY());
        }

        void move(final double dt) {
            final double goal = top + height / 2 + SpaceGame.this.height * 0.03;
            if (arriving > 0) {
                arriving -= dt;
                y = goal - (goal + height) * Math.max(0, arriving) / BOSS_ARRIVAL;
                place();
                return;
            }
            x += dirX * BOSS_SPEED * SpaceGame.this.height * dt;
            if (x < width / 2 || x > SpaceGame.this.width - width / 2) {
                dirX = -dirX;
                x = Math.max(width / 2, Math.min(SpaceGame.this.width - width / 2, x));
            }
            y = goal;
            place();
            shotWait -= dt;
            if (shotWait <= 0) {
                shotWait = BOSS_SHOT_PERIOD;
                for (final double side : new double[]{-0.35, 0, 0.35}) {
                    final double length = Math.hypot(side, 1);
                    addShot(new Shot(x, y + height * 0.3, side / length, 1 / length, false, BALL_SPEED));
                }
            }
        }
    }

    /**
     * A shot: a laser of the rocket going up, or a red ball of an alien or of the boss.
     */
    final class Shot {
        @Getter
        private double x;
        @Getter
        private double y;
        private final double dirX;
        private final double dirY;
        @Getter
        private final boolean fromShip;
        private final double speed;
        private final Node node;
        private final double radius;

        Shot(final double x, final double y, final double dirX, final double dirY, final boolean fromShip,
             final double speed) {
            this.x = x;
            this.y = y;
            this.dirX = dirX;
            this.dirY = dirY;
            this.fromShip = fromShip;
            this.speed = speed;
            if (fromShip) {
                radius = height * 0.012;
                final Rectangle laser = new Rectangle(-radius, -radius * 2.2, radius * 2, radius * 4.4);
                laser.setArcWidth(radius * 2);
                laser.setArcHeight(radius * 2);
                laser.setFill(Color.rgb(150, 230, 255));
                laser.setStroke(Color.WHITE);
                laser.setStrokeWidth(Math.max(1.5, radius * 0.3));
                node = laser;
            } else {
                radius = height * 0.014;
                final Circle ball = new Circle(radius);
                ball.setFill(Color.rgb(240, 80, 80));
                ball.setStroke(Color.rgb(150, 30, 40));
                ball.setStrokeWidth(Math.max(2, radius * 0.3));
                node = ball;
            }
            node.setMouseTransparent(true);
            place();
        }

        private void place() {
            node.setLayoutX(x);
            node.setLayoutY(y);
        }

        void move(final double dt) {
            x += dirX * speed * height * dt;
            y += dirY * speed * height * dt;
            place();
            if (x < -radius || x > width + radius || y < -radius * 3 || y > height + radius) {
                remove();
                return;
            }
            if (fromShip) {
                for (final Alien alien : aliens) {
                    if (!alien.isAppearing() && Math.hypot(alien.x - x, alien.y - y) < alien.radius() + radius) {
                        remove();
                        destroy(alien);
                        return;
                    }
                }
                if (boss != null && boss.arriving <= 0 && Math.hypot(boss.x - x, boss.y - y) < boss.radius() + radius) {
                    remove();
                    hit(boss);
                }
            } else if (Math.hypot(shipX - x, shipY - y) < shipRadius() + radius) {
                remove();
                hurt();
            }
        }

        void remove() {
            shots.remove(this);
            actors.getChildren().remove(node);
        }
    }
}

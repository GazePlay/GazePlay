package net.gazeplay.games.surviveAgainstRobots;

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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * "Survive Against Robots" (reviewed on 3/11/2026, maintainer's rules; it was two games, with the keyboard and with
 * the gaze): the Biboule (the one of the shooting game) flies in a scene drawn at random (the sky, a garden, the
 * schoolyard) among robots (those of the shooting game) going here and there; it goes where the gaze or the mouse is
 * (as the setting of the inputs allows: the mouse only, the gaze only, or both, the last that moved), and shoots
 * sparkles by itself, at the nearest robot or forwards, the way it goes (maintainer's choice: no keyboard, no choice
 * of the input in the variants). A robot hit is destroyed: a flash, a sound, one more robot destroyed. The red robots
 * shoot at the Biboule.
 * <p>
 * Three lives: touched by a robot or a shot, the Biboule loses one and blinks {@value #INVINCIBLE} s, during which
 * nothing touches it; the three lost, "Lost!" then the statistics. The game ends also never, after 1 or 3 minutes, or
 * after 20 or 50 robots destroyed (then "Bravo" and the statistics). A robot destroyed sometimes leaves a bonus, taken
 * by flying on it: faster shots, a shield or the robots slowed for {@value #BONUS_EFFECT} s, or one life back.
 * <p>
 * One loop moves everything in real time, at the animation speed of the settings; nothing moves and the time does not
 * run while the menu of the game is open.
 */
public class SurviveAgainstRobots implements GameLifeCycle {

    static final int LIVES = 3;

    /**
     * One robot at first, one more each time this number of robots is destroyed (up to the level's number).
     */
    static final int MORE_ROBOTS_EACH = 5;

    static final double SHOT_PERIOD = 0.5;
    static final double FAST_SHOT_PERIOD = 0.25;
    static final double ROBOT_SHOT_PERIOD = 1.6;
    static final double INVINCIBLE = 2;
    static final double BONUS_CHANCE = 1.0 / 6;
    static final double BONUS_LIFETIME = 8;
    static final double BONUS_EFFECT = 8;
    static final double SPAWN_DELAY = 1.2;
    static final double APPEARING = 0.6;
    static final double SLOW_FACTOR = 0.5;

    /**
     * Speeds, in heights of the screen per second, at the normal animation speed.
     */
    static final double PLAYER_SPEED = 0.55;
    static final double ROBOT_SPEED = 0.18;
    static final double PLAYER_SHOT_SPEED = 1.1;
    static final double ROBOT_SHOT_SPEED = 0.45;

    static final String[] SCENES = {"sky", "garden", "schoolyard"};
    static final String[] COLORS = {"Blue", "Green", "Yellow", "Orange", "Red"};
    static final String SHOOTER_COLOR = "Red";

    enum Phase { PLAY, OVER }

    /**
     * The bonuses, and their pictures (data/surviveAgainstRobots/images).
     */
    @Getter
    enum BonusKind {
        FIRE("fire"), SHIELD("shield"), SLOW("slow"), HEART("heart");

        private final String picture;

        BonusKind(final String picture) {
            this.picture = picture;
        }
    }

    private final IGameContext gameContext;
    private final Stats stats;
    @Getter
    private final SurviveLevel level;
    @Getter
    private final SurviveShot shot;
    @Getter
    private final SurviveEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private int lives;
    @Getter
    private int destroyed;
    @Getter
    private double timeLeft;
    /**
     * The scene drawn for this game (a name of {@link #SCENES}).
     */
    @Getter
    private String scene;

    private double width;
    private double height;
    private double top;
    private double time;

    private Group layer;
    private Group actors;
    private Group hud;
    private ImageView background;

    @Getter
    private double playerX;
    @Getter
    private double playerY;
    private double playerSize;
    private ImageView playerView;
    private Circle shieldBubble;
    @Getter
    private double invincible;
    private double aimX;
    private double aimY;
    private boolean aiming;
    private double walkX = 1;
    private double walkY;
    private double shotWait;
    private double spawnWait;

    private Image robotFlash;
    private Image playerFlash;
    private final Map<String, Image> robotImages = new HashMap<>();
    private final Map<BonusKind, Image> bonusImages = new EnumMap<>(BonusKind.class);

    @Getter
    private final List<Robot> robots = new ArrayList<>();
    @Getter
    private final List<Shot> shots = new ArrayList<>();
    @Getter
    private final List<Bonus> bonuses = new ArrayList<>();
    /**
     * The bonuses in effect, and their seconds left.
     */
    @Getter
    private final Map<BonusKind, Double> effects = new EnumMap<>(BonusKind.class);

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
    private final Map<BonusKind, Integer> shownEffects = new EnumMap<>(BonusKind.class);

    public SurviveAgainstRobots(final IGameContext gameContext, final Stats stats, final SurviveLevel level,
                                final SurviveShot shot, final SurviveEnd end) {
        this(gameContext, stats, level, shot, end, -1);
    }

    /**
     * @param gameSeed the seed of the game to replay, or a negative value for a new game
     */
    public SurviveAgainstRobots(final IGameContext gameContext, final Stats stats, final SurviveLevel level,
                                final SurviveShot shot, final SurviveEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.level = level;
        this.shot = shot;
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

        scene = SCENES[random.nextInt(SCENES.length)];
        background = new ImageView(new Image("data/surviveAgainstRobots/images/" + scene + ".png"));
        background.setFitWidth(width);
        background.setFitHeight(height);
        for (final String color : COLORS) {
            robotImages.put(color, new Image("data/shooter/robot/images/" + color + ".png"));
        }
        for (final BonusKind kind : BonusKind.values()) {
            bonusImages.put(kind, new Image("data/surviveAgainstRobots/images/" + kind.getPicture() + ".png"));
        }
        robotFlash = new Image("data/shooter/robot/images/Flash.png");
        playerFlash = new Image("data/shooter/biboule/images/Flash.png");

        // The Biboule, of a color drawn at random, in the middle of the screen
        playerSize = height * 0.12;
        playerX = width / 2;
        playerY = (top + height) / 2;
        aimX = playerX;
        aimY = playerY;
        playerView = new ImageView(new Image("data/shooter/biboule/images/"
            + COLORS[random.nextInt(COLORS.length)] + ".png"));
        playerView.setFitWidth(playerSize * 1.25);
        playerView.setFitHeight(playerSize);
        playerView.setMouseTransparent(true);
        shieldBubble = new Circle(playerSize * 0.75);
        shieldBubble.setStyle("-fx-stroke: -gp-accent;");
        shieldBubble.setFill(Color.rgb(140, 190, 255, 0.25));
        shieldBubble.setStrokeWidth(Math.max(3, playerSize * 0.05));
        shieldBubble.setVisible(false);
        shieldBubble.setMouseTransparent(true);

        actors = new Group();
        actors.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        layer = new Group(background, actors, shieldBubble, playerView, hud);
        gameContext.getChildren().add(layer);

        // The Biboule goes where the gaze or the mouse is. Nothing to choose: with the mouse only, no gaze comes; with
        // the gaze only, the game area stops the mouse (GameContextFactory); with both, the last that moved leads
        gameContext.getGazeDeviceManager().addEventFilter(background);
        background.addEventFilter(GazeEvent.ANY, aimHandler);
        layer.addEventFilter(MouseEvent.MOUSE_MOVED, aimHandler);
        layer.addEventFilter(MouseEvent.MOUSE_DRAGGED, aimHandler);

        lives = LIVES;
        destroyed = 0;
        timeLeft = end.getSeconds();
        shotWait = SHOT_PERIOD;
        spawnWait = 0.5;
        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();
        gameContext.firstStart();
        phase = Phase.PLAY;
        placePlayer();
        showHud();
        if (!manual) {
            loop.start();
        }
    }

    private void aim(final Event event) {
        if (event instanceof GazeEvent gaze && (gaze.getEventType() == GazeEvent.GAZE_MOVED
            || gaze.getEventType() == GazeEvent.GAZE_ENTERED)) {
            aimAt(gaze.getX(), gaze.getY());
        } else if (event instanceof MouseEvent mouse) {
            aimAt(mouse.getX(), mouse.getY());
        }
    }

    /**
     * The point looked at (or of the mouse), where the Biboule goes.
     */
    void aimAt(final double x, final double y) {
        aimX = x;
        aimY = y;
        aiming = true;
    }

    private double playerRadius() {
        return playerSize * 0.42;
    }

    private double robotSize() {
        return height * 0.11;
    }

    private double slow() {
        return effects.containsKey(BonusKind.SLOW) ? SLOW_FACTOR : 1;
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase != Phase.PLAY || seconds <= 0) {
            return;
        }
        final double ratio = Math.max(0.1, gameContext.getConfiguration().getAnimationSpeedRatio());
        final double dt = seconds * ratio;
        time += dt;

        movePlayer(dt);
        spawnRobots(dt);
        for (final Robot robot : new ArrayList<>(robots)) {
            robot.move(dt);
        }
        // The robots touch the Biboule before its shots, sent after their moves
        touchRobots();
        if (phase != Phase.PLAY) {
            return;
        }
        shoot(dt);
        for (final Shot shot : new ArrayList<>(shots)) {
            shot.move(dt);
        }
        for (final Bonus bonus : new ArrayList<>(bonuses)) {
            bonus.age(seconds);
        }
        if (phase != Phase.PLAY) {
            return;
        }

        invincible = Math.max(0, invincible - seconds);
        for (final BonusKind kind : new ArrayList<>(effects.keySet())) {
            final double left = effects.get(kind) - seconds;
            if (left <= 0) {
                effects.remove(kind);
            } else {
                effects.put(kind, left);
            }
        }
        placePlayer();

        if (end.getSeconds() > 0) {
            timeLeft = Math.max(0, timeLeft - seconds);
            if (timeLeft <= 0) {
                win();
                return;
            }
        }
        if (hudChanged()) {
            showHud();
        }
    }

    private void movePlayer(final double dt) {
        if (!aiming) {
            return;
        }
        double dx = aimX - playerX;
        double dy = aimY - playerY;
        final double distance = Math.hypot(dx, dy);
        final double speed = PLAYER_SPEED * height * dt;
        if (distance < height * 0.01) {
            return;
        }
        // Its way: it shoots forwards that way, also once it is where the gaze or the mouse is
        walkX = dx / distance;
        walkY = dy / distance;
        if (distance > speed) {
            dx = dx / distance * speed;
            dy = dy / distance * speed;
        }
        final double margin = playerSize * 0.62;
        playerX = clamp(playerX + dx, margin, width - margin);
        playerY = clamp(playerY + dy, top + playerSize / 2, height - playerSize / 2);
    }

    private static double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void placePlayer() {
        playerView.setLayoutX(playerX - playerSize * 1.25 / 2);
        playerView.setLayoutY(playerY - playerSize / 2);
        // Blinking while nothing touches it after a life lost
        playerView.setOpacity(invincible > 0 && ((int) (invincible * 8)) % 2 == 0 ? 0.35 : 1);
        shieldBubble.setCenterX(playerX);
        shieldBubble.setCenterY(playerY);
        shieldBubble.setVisible(effects.containsKey(BonusKind.SHIELD));
    }

    /**
     * @return the number of robots wanted now: one at first, one more each {@value #MORE_ROBOTS_EACH} destroyed
     */
    int wantedRobots() {
        return Math.min(level.getRobots(), 1 + destroyed / MORE_ROBOTS_EACH);
    }

    private void spawnRobots(final double dt) {
        if (robots.size() >= wantedRobots()) {
            return;
        }
        spawnWait -= dt;
        if (spawnWait > 0) {
            return;
        }
        spawnWait = SPAWN_DELAY;
        // A place far enough from the Biboule (the farthest of a few tries, never a loop without end)
        final double size = robotSize();
        double bestX = width / 2;
        double bestY = top + size;
        double best = -1;
        for (int i = 0; i < 20; i++) {
            final double x = size * 0.6 + (width - size * 1.2) * random.nextDouble();
            final double y = top + size / 2 + (height - top - size) * random.nextDouble();
            final double distance = Math.hypot(x - playerX, y - playerY);
            if (distance > best) {
                best = distance;
                bestX = x;
                bestY = y;
            }
            if (distance > height * 0.45) {
                break;
            }
        }
        final boolean shooter = random.nextDouble() < level.getShooters();
        addRobot(bestX, bestY, shooter);
    }

    /**
     * A robot comes at this place (it fades in, harmless meanwhile).
     */
    Robot addRobot(final double x, final double y, final boolean shooter) {
        final Robot robot = new Robot(x, y, shooter);
        robots.add(robot);
        actors.getChildren().add(robot.view);
        stats.incrementNumberOfGoalsToReach();
        return robot;
    }

    private void shoot(final double dt) {
        shotWait -= dt;
        if (shotWait > 0) {
            return;
        }
        double dx;
        double dy;
        if (shot == SurviveShot.AUTOMATIC) {
            final Robot nearest = nearestRobot();
            if (nearest == null) {
                return;
            }
            dx = nearest.x - playerX;
            dy = nearest.y - playerY;
        } else {
            // Forwards: the way it goes (or went last)
            dx = walkX;
            dy = walkY;
        }
        final double length = Math.hypot(dx, dy);
        if (length == 0) {
            return;
        }
        shotWait = effects.containsKey(BonusKind.FIRE) ? FAST_SHOT_PERIOD : SHOT_PERIOD;
        final Shot sparkle = new Shot(playerX, playerY, dx / length, dy / length, true);
        shots.add(sparkle);
        actors.getChildren().add(sparkle.node);
    }

    /**
     * @return the nearest robot that is there (not coming), null if none
     */
    Robot nearestRobot() {
        Robot nearest = null;
        double best = Double.MAX_VALUE;
        for (final Robot robot : robots) {
            if (robot.appearing > 0) {
                continue;
            }
            final double distance = Math.hypot(robot.x - playerX, robot.y - playerY);
            if (distance < best) {
                best = distance;
                nearest = robot;
            }
        }
        return nearest;
    }

    private void touchRobots() {
        for (final Robot robot : robots) {
            if (robot.appearing <= 0
                && Math.hypot(robot.x - playerX, robot.y - playerY) < playerRadius() + robot.radius()) {
                hurt();
                return;
            }
        }
        for (final Bonus bonus : new ArrayList<>(bonuses)) {
            if (Math.hypot(bonus.x - playerX, bonus.y - playerY) < playerRadius() + bonus.radius()) {
                take(bonus);
            }
        }
    }

    /**
     * The Biboule touched by a robot or a shot: one life less, unless it blinks or has the shield.
     */
    void hurt() {
        if (phase != Phase.PLAY || invincible > 0 || effects.containsKey(BonusKind.SHIELD)) {
            return;
        }
        lives--;
        showHud();
        if (lives <= 0) {
            lose();
        } else {
            invincible = INVINCIBLE;
            placePlayer();
        }
    }

    /**
     * A robot destroyed: a flash, a sound, one more robot destroyed, sometimes a bonus at its place; the number to
     * destroy reached, "Bravo" then the statistics.
     */
    void destroy(final Robot robot) {
        if (phase != Phase.PLAY || !robots.remove(robot)) {
            return;
        }
        actors.getChildren().remove(robot.view);
        flash(robotFlash, robot.x, robot.y, robotSize());
        gameContext.getSoundManager().add("data/shooter/biboule/sounds/shot" + (1 + random.nextInt(3)) + ".wav");
        destroyed++;
        stats.incrementNumberOfGoalsReached();
        gameContext.updateScore(stats, this);
        if (random.nextDouble() < BONUS_CHANCE) {
            final List<BonusKind> kinds = new ArrayList<>(List.of(BonusKind.FIRE, BonusKind.SHIELD, BonusKind.SLOW));
            if (lives < LIVES) {
                kinds.add(BonusKind.HEART);
            }
            addBonus(kinds.get(random.nextInt(kinds.size())), robot.x, robot.y);
        }
        showHud();
        if (end.getRobots() > 0 && destroyed >= end.getRobots()) {
            win();
        }
    }

    Bonus addBonus(final BonusKind kind, final double x, final double y) {
        final Bonus bonus = new Bonus(kind, x, y);
        bonuses.add(bonus);
        actors.getChildren().add(bonus.view);
        return bonus;
    }

    /**
     * A bonus taken: one life back, or its effect for {@value #BONUS_EFFECT} s.
     */
    void take(final Bonus bonus) {
        if (!bonuses.remove(bonus)) {
            return;
        }
        actors.getChildren().remove(bonus.view);
        if (bonus.kind == BonusKind.HEART) {
            lives = Math.min(LIVES, lives + 1);
        } else {
            effects.put(bonus.kind, BONUS_EFFECT);
        }
        gameContext.getSoundManager().add("data/shooter/biboule/sounds/shot1.wav");
        placePlayer();
        showHud();
    }

    private void flash(final Image image, final double x, final double y, final double size) {
        final ImageView view = new ImageView(image);
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

    private boolean hudChanged() {
        if (end.getSeconds() > 0 && (int) Math.ceil(timeLeft) != shownSeconds) {
            return true;
        }
        if (!shownEffects.keySet().equals(effects.keySet())) {
            return true;
        }
        for (final Map.Entry<BonusKind, Double> effect : effects.entrySet()) {
            if ((int) Math.ceil(effect.getValue()) != shownEffects.get(effect.getKey())) {
                return true;
            }
        }
        return false;
    }

    /**
     * The lives at the top left, with the bonuses in effect under them; the robots destroyed at the top right; the
     * time left in the middle.
     */
    private void showHud() {
        hud.getChildren().clear();
        final double fontSize = height * 0.035;
        final double margin = height * 0.025;
        final double heart = height * 0.055;
        for (int i = 0; i < LIVES; i++) {
            final SVGPath icon = GameStyle.icon(i < lives ? "favorite_fill1" : "favorite", heart);
            icon.setFill(i < lives ? Color.rgb(235, 70, 95) : Color.rgb(120, 120, 130, 0.8));
            icon.setStroke(Color.WHITE);
            icon.setStrokeWidth(18);
            icon.setLayoutX(margin + i * heart * 1.1);
            icon.setLayoutY(margin);
            hud.getChildren().add(icon);
        }
        shownEffects.clear();
        double x = margin;
        for (final Map.Entry<BonusKind, Double> effect : effects.entrySet()) {
            final int left = (int) Math.ceil(effect.getValue());
            shownEffects.put(effect.getKey(), left);
            final ImageView icon = new ImageView(bonusImages.get(effect.getKey()));
            icon.setFitWidth(heart);
            icon.setFitHeight(heart);
            icon.setLayoutX(x);
            icon.setLayoutY(margin + heart * 1.2);
            final Group seconds = GameStyle.pill(String.valueOf(left), fontSize * 0.7);
            seconds.setLayoutX(x + heart * 0.55);
            seconds.setLayoutY(margin + heart * 1.75);
            hud.getChildren().addAll(icon, seconds);
            x += heart * 1.6;
        }

        String text = gameContext.getTranslator().translate("SurviveScore") + " " + destroyed;
        if (end.getRobots() > 0) {
            text += " / " + end.getRobots();
        }
        final Group pill = GameStyle.pill(text, fontSize);
        pill.setLayoutX(width - pill.getBoundsInLocal().getWidth() - margin);
        pill.setLayoutY(margin);
        hud.getChildren().add(pill);
        if (end.getSeconds() > 0) {
            shownSeconds = (int) Math.ceil(timeLeft);
            final Group clock = GameStyle.pill(String.format("%d:%02d", shownSeconds / 60, shownSeconds % 60),
                fontSize);
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
        if (phase != Phase.PLAY) {
            return;
        }
        stopPlaying();
        stats.takeScreenShot();
        gameContext.updateScore(stats, this);
        gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
    }

    /**
     * The three lives lost: the Biboule flashes, "Lost!", then the statistics.
     */
    private void lose() {
        if (phase != Phase.PLAY) {
            return;
        }
        stopPlaying();
        playerView.setVisible(false);
        shieldBubble.setVisible(false);
        flash(playerFlash, playerX, playerY, playerSize);
        stats.takeScreenShot();
        gameContext.updateScore(stats, this);
        final Group card = GameStyle.card(gameContext.getTranslator().translate("SurviveLost"), height * 0.06,
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

    /**
     * For the tests: the Biboule at this place.
     */
    void placePlayerAt(final double x, final double y) {
        playerX = x;
        playerY = y;
        placePlayer();
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
        robots.clear();
        shots.clear();
        bonuses.clear();
        effects.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A robot: it goes here and there, changing its way from time to time and bouncing on the edges; a red one shoots
     * at the Biboule.
     */
    final class Robot {
        @Getter
        private double x;
        @Getter
        private double y;
        private double dirX;
        private double dirY;
        private double turnWait;
        private double shotWait;
        private double appearing = APPEARING;
        @Getter
        private final boolean shooter;
        private final ImageView view;

        Robot(final double x, final double y, final boolean shooter) {
            this.x = x;
            this.y = y;
            this.shooter = shooter;
            final String color = shooter ? SHOOTER_COLOR : COLORS[random.nextInt(COLORS.length - 1)];
            view = new ImageView(robotImages.get(color));
            final double size = robotSize();
            view.setFitWidth(size * 1.25);
            view.setFitHeight(size);
            view.setMouseTransparent(true);
            view.setOpacity(0);
            shotWait = 0.5 + ROBOT_SHOT_PERIOD * random.nextDouble();
            turn();
            place();
        }

        double radius() {
            return robotSize() * 0.42;
        }

        boolean isAppearing() {
            return appearing > 0;
        }

        private void turn() {
            final double angle = 2 * Math.PI * random.nextDouble();
            dirX = Math.cos(angle);
            dirY = Math.sin(angle);
            turnWait = 1.5 + 1.5 * random.nextDouble();
        }

        private void place() {
            final double size = robotSize();
            view.setLayoutX(x - size * 1.25 / 2);
            view.setLayoutY(y - size / 2);
        }

        void move(final double dt) {
            if (appearing > 0) {
                appearing -= dt;
                view.setOpacity(Math.min(1, 1 - appearing / APPEARING));
                return;
            }
            turnWait -= dt;
            if (turnWait <= 0) {
                turn();
            }
            final double speed = ROBOT_SPEED * height * slow() * dt;
            final double size = robotSize();
            x += dirX * speed;
            y += dirY * speed;
            if (x < size * 0.62 || x > width - size * 0.62) {
                dirX = -dirX;
                x = clamp(x, size * 0.62, width - size * 0.62);
            }
            if (y < top + size / 2 || y > height - size / 2) {
                dirY = -dirY;
                y = clamp(y, top + size / 2, height - size / 2);
            }
            place();
            if (shooter) {
                shotWait -= dt;
                if (shotWait <= 0) {
                    shotWait = ROBOT_SHOT_PERIOD;
                    final double dx = playerX - x;
                    final double dy = playerY - y;
                    final double length = Math.hypot(dx, dy);
                    if (length > 0) {
                        final Shot shot = new Shot(x, y, dx / length, dy / length, false);
                        shots.add(shot);
                        actors.getChildren().add(shot.node);
                    }
                }
            }
        }

        ImageView view() {
            return view;
        }
    }

    /**
     * A shot: a sparkle of the Biboule, or a red ball of a robot.
     */
    final class Shot {
        @Getter
        private double x;
        @Getter
        private double y;
        private final double dirX;
        private final double dirY;
        @Getter
        private final boolean fromPlayer;
        private final Node node;
        private final double radius;

        Shot(final double x, final double y, final double dirX, final double dirY, final boolean fromPlayer) {
            this.x = x;
            this.y = y;
            this.dirX = dirX;
            this.dirY = dirY;
            this.fromPlayer = fromPlayer;
            radius = height * (fromPlayer ? 0.012 : 0.014);
            final Circle ball = new Circle(radius);
            if (fromPlayer) {
                ball.setFill(Color.rgb(255, 245, 160));
                ball.setStroke(Color.rgb(240, 190, 90));
            } else {
                ball.setFill(Color.rgb(240, 80, 80));
                ball.setStroke(Color.rgb(150, 30, 40));
            }
            ball.setStrokeWidth(Math.max(2, radius * 0.3));
            node = ball;
            node.setMouseTransparent(true);
            place();
        }

        private void place() {
            node.setLayoutX(x);
            node.setLayoutY(y);
        }

        void move(final double dt) {
            final double speed = (fromPlayer ? PLAYER_SHOT_SPEED : ROBOT_SHOT_SPEED * slow()) * height * dt;
            x += dirX * speed;
            y += dirY * speed;
            place();
            if (x < -radius || x > width + radius || y < -radius || y > height + radius) {
                remove();
                return;
            }
            if (fromPlayer) {
                for (final Robot robot : robots) {
                    if (!robot.isAppearing() && Math.hypot(robot.x - x, robot.y - y) < robot.radius() + radius) {
                        remove();
                        destroy(robot);
                        return;
                    }
                }
            } else if (Math.hypot(playerX - x, playerY - y) < playerRadius() + radius) {
                remove();
                hurt();
            }
        }

        private void remove() {
            shots.remove(this);
            actors.getChildren().remove(node);
        }
    }

    /**
     * A bonus left by a robot: taken by flying on it; it blinks its last 3 seconds, then goes.
     */
    final class Bonus {
        @Getter
        private final BonusKind kind;
        @Getter
        private final double x;
        @Getter
        private final double y;
        private double age;
        private final ImageView view;

        Bonus(final BonusKind kind, final double x, final double y) {
            this.kind = kind;
            this.x = x;
            this.y = y;
            final double size = height * 0.075;
            view = new ImageView(bonusImages.get(kind));
            view.setFitWidth(size);
            view.setFitHeight(size);
            view.setLayoutX(x - size / 2);
            view.setLayoutY(y - size / 2);
            view.setMouseTransparent(true);
        }

        double radius() {
            return height * 0.075 / 2;
        }

        void age(final double seconds) {
            age += seconds;
            if (age >= BONUS_LIFETIME) {
                bonuses.remove(this);
                actors.getChildren().remove(view);
            } else if (age >= BONUS_LIFETIME - 3) {
                view.setOpacity(((int) (age * 4)) % 2 == 0 ? 0.3 : 1);
            }
        }
    }
}

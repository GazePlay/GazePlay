package net.gazeplay.games.biboulejump;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GameStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Biboule Jump (reviewed on 4/11/2026, maintainer's rules): the Biboule bounces from cloud to cloud and goes where the
 * gaze or the mouse is, sideways. A point for each hundredth of the height of the screen climbed. Three lives: after a
 * fall, she starts again on a new cloud and the points stay. The game has no end, or ends after 1 or 3 minutes; then the
 * statistics. There is no high score any more.
 * <p>
 * Everything is measured in parts of the screen: a jump climbs the same part of it on every screen. One loop moves
 * everything in real time, at the animation speed of the settings; nothing moves while the menu of the game is open.
 * (Before the review: pixels per millisecond, so that a jump climbed 53 % of a screen in 1080p and 27 % in 4K, with clouds
 * up to 45 % apart; a score rounded down at each image; the game going on behind the menu of the game; moving clouds never
 * stopped; one fall ending the game; a square Biboule.)
 */
public class BibouleJump implements GameLifeCycle {

    static final int LIVES = 3;
    /**
     * The seconds before the first jump, and before the jump after a fall.
     */
    static final double FIRST_WAIT = 3;
    static final double WAIT_AFTER_FALL = 1.5;
    /**
     * In heights of the screen per second (squared): a jump climbs 53 % of the screen, a springboard twice as fast.
     */
    static final double GRAVITY = 4.63;
    static final double JUMP_SPEED = 2.22;
    static final double SPRINGBOARD_SPEED = 4.44;
    static final double FALL_SPEED = 0.74;
    /**
     * In widths of the screen per second: the Biboule going sideways, and a moving cloud.
     */
    static final double SIDE_SPEED = 0.37;
    static final double CLOUD_SPEED = 0.18;
    /**
     * How far a moving cloud goes from its place, in widths of the screen.
     */
    static final double CLOUD_RANGE = 0.18;
    /**
     * The height of the screen above which the Biboule makes the clouds go down, in parts of the screen.
     */
    static final double SCROLL_LINE = 0.45;
    static final double SPRINGBOARD_CHANCE = 0.2;
    /**
     * The height of the top of the cloud where she starts, in parts of the screen.
     */
    static final double START_LINE = 0.8;

    /**
     * The picture of the Biboule: its height in parts of the screen, its proportions, where its feet and its body are.
     */
    private static final double BIBOULE_HEIGHT = 0.16;
    private static final double BIBOULE_RATIO = 1.25;
    private static final double FEET_IN_PICTURE = 0.865;
    private static final double BODY_IN_PICTURE = 0.27;
    private static final double FEET_WIDTH = 0.2;

    private static final String[] BIBOULE_COLORS = {"Blue", "Green", "Yellow", "Orange", "Red"};
    private static final String DATA = "data/biboulejump/";
    private static final Map<String, Image> GAME_IMAGES = new HashMap<>();

    enum Phase { PLAY, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final BibouleJumpStats stats;
    @Getter
    private final BibouleJumpLevel level;
    @Getter
    private final BibouleJumpEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private int lives;
    @Getter
    private int score;
    @Getter
    private double timeLeft;
    @Getter
    private double waiting;
    @Getter
    private double bibouleX;
    @Getter
    private double feetY;
    @Getter
    private double velocity;
    @Getter
    private double aimX;
    private double climbed;
    private double squash;
    private double tilt;
    private boolean blinking;

    @Getter
    private double width;
    @Getter
    private double height;
    private double pictureWidth;
    private double pictureHeight;
    private Group layer;
    private Group cloudsLayer;
    private Group hud;
    private Rectangle area;
    private ImageView biboule;
    private PauseTransition lostPause;

    @Getter
    private final List<Cloud> clouds = new ArrayList<>();
    private Cloud highest;

    private final EventHandler<Event> aimHandler = this::aim;

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

    /**
     * @param gameSeed the seed of the game to replay, or a negative value for a new game
     */
    BibouleJump(final IGameContext gameContext, final BibouleJumpStats stats, final BibouleJumpLevel level,
                final BibouleJumpEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
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
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        width = dimension.getWidth();
        height = dimension.getHeight();

        final Image sky = gameImage(DATA + "sky.png");
        final ImageView background = new ImageView(sky);
        // Covers the screen, keeping its proportions
        final double scale = Math.max(width / Math.max(1, sky.getWidth()), height / Math.max(1, sky.getHeight()));
        background.setFitWidth(sky.getWidth() * scale);
        background.setFitHeight(sky.getHeight() * scale);
        background.setLayoutX((width - background.getFitWidth()) / 2);
        background.setLayoutY((height - background.getFitHeight()) / 2);
        background.setMouseTransparent(true);

        // The whole screen receives the gaze and the mouse, as the setting of the inputs allows
        area = new Rectangle(0, 0, width, height);
        area.setFill(Color.TRANSPARENT);

        pictureHeight = height * BIBOULE_HEIGHT;
        pictureWidth = pictureHeight * BIBOULE_RATIO;
        biboule = new ImageView(gameImage("data/shooter/biboule/images/"
            + BIBOULE_COLORS[random.nextInt(BIBOULE_COLORS.length)] + ".png"));
        biboule.setFitWidth(pictureWidth);
        biboule.setFitHeight(pictureHeight);
        biboule.setMouseTransparent(true);

        cloudsLayer = new Group();
        cloudsLayer.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        layer = new Group(background, area, cloudsLayer, biboule, hud);
        // The next clouds wait above the screen, and the screenshots of the statistics must not show them
        layer.setClip(new Rectangle(0, 0, width, height));
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addEventFilter(area);
        area.addEventFilter(GazeEvent.ANY, aimHandler);
        layer.addEventFilter(MouseEvent.MOUSE_MOVED, aimHandler);
        layer.addEventFilter(MouseEvent.MOUSE_DRAGGED, aimHandler);

        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();

        lives = LIVES;
        score = 0;
        climbed = 0;
        timeLeft = end.getSeconds();
        phase = Phase.PLAY;
        startOnANewCloud(FIRST_WAIT);
        showHud();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * @return an image of the game, loaded once
     */
    private static Image gameImage(final String path) {
        return GAME_IMAGES.computeIfAbsent(path, Image::new);
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
     * The place looked at (or of the mouse), where the Biboule goes.
     */
    void aimAt(final double x) {
        aimX = x;
    }

    /**
     * Puts the Biboule on a cloud in the middle of the screen, with new clouds above it, so that she can always go on;
     * she jumps after a wait.
     */
    private void startOnANewCloud(final double wait) {
        clouds.forEach(cloud -> cloudsLayer.getChildren().remove(cloud.node));
        clouds.clear();
        final double cloudWidth = width * level.getCloudWidth();
        highest = addCloud(width / 2, height * START_LINE, cloudWidth, false, false);
        feetY = highest.surface;
        generateClouds();
        bibouleX = width / 2;
        aimX = bibouleX;
        velocity = 0;
        squash = 0;
        tilt = 0;
        waiting = wait;
        placeBiboule();
    }

    /**
     * New clouds above the highest one, up to one screen above the screen. Each one can be reached from the one under it:
     * a jump climbs 53 % of the screen, and goes sideways while it lasts.
     */
    private void generateClouds() {
        final double cloudWidth = width * level.getCloudWidth();
        while (highest.surface > -height) {
            final double gap = level.getMinGap() + (level.getMaxGap() - level.getMinGap()) * random.nextDouble();
            final double shift = level.getMaxShift() * (2 * random.nextDouble() - 1);
            double x = highest.x + shift * width;
            // Stays inside the screen: goes the other way rather than against the edge
            if (x < cloudWidth / 2 || x > width - cloudWidth / 2) {
                x = highest.x - shift * width;
            }
            x = Math.max(cloudWidth / 2, Math.min(width - cloudWidth / 2, x));
            final boolean moving = random.nextDouble() < level.getMovingChance();
            final boolean springboard = !moving && random.nextDouble() < SPRINGBOARD_CHANCE;
            highest = addCloud(x, highest.surface - gap * height, cloudWidth, moving, springboard);
        }
    }

    private Cloud addCloud(final double x, final double surface, final double cloudWidth, final boolean moving,
                           final boolean springboard) {
        final Cloud cloud = new Cloud(x, surface, cloudWidth, moving, springboard);
        clouds.add(cloud);
        cloudsLayer.getChildren().add(cloud.node);
        return cloud;
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase != Phase.PLAY || seconds <= 0) {
            return;
        }
        final double scaled = seconds * Math.max(0.1, configuration.getAnimationSpeedRatio());
        clouds.forEach(cloud -> cloud.move(scaled));
        if (waiting > 0) {
            waitToJump(seconds);
            return;
        }
        moveSideways(scaled);
        moveUpAndDown(scaled);
        scrollIfHigh();
        placeBiboule();
        if (feetY - pictureHeight * FEET_IN_PICTURE > height) {
            fall();
            return;
        }
        if (end.getSeconds() > 0) {
            final int before = (int) Math.ceil(timeLeft);
            timeLeft = Math.max(0, timeLeft - seconds);
            if (timeLeft <= 0) {
                win();
            } else if ((int) Math.ceil(timeLeft) != before) {
                showHud();
            }
        }
    }

    private void waitToJump(final double seconds) {
        waiting -= seconds;
        if (blinking) {
            biboule.setOpacity(0.55 + 0.45 * Math.cos(waiting * 2 * Math.PI * 2.5));
        }
        if (waiting <= 0) {
            waiting = 0;
            blinking = false;
            biboule.setOpacity(1);
            jump(JUMP_SPEED, "bounce.wav");
        }
        placeBiboule();
    }

    private void moveSideways(final double seconds) {
        final double margin = pictureWidth * BODY_IN_PICTURE;
        final double wanted = Math.max(margin, Math.min(width - margin, aimX));
        final double most = SIDE_SPEED * width * seconds;
        final double move = Math.max(-most, Math.min(most, wanted - bibouleX));
        bibouleX += move;
        final double aimedTilt = most > 0 ? 10 * move / most : 0;
        tilt += (aimedTilt - tilt) * Math.min(1, seconds * 10);
    }

    /**
     * Gravity, then the landing: only when falling, when the feet go through the top of a cloud or of a springboard during
     * this step (a slow image cannot go through it unseen).
     */
    private void moveUpAndDown(final double seconds) {
        velocity = Math.min(FALL_SPEED, velocity + GRAVITY * seconds);
        final double before = feetY;
        feetY += velocity * height * seconds;
        squash = Math.max(0, squash - seconds / 0.15);
        if (velocity <= 0) {
            return;
        }
        final double feet = pictureWidth * FEET_WIDTH;
        for (final Cloud cloud : clouds) {
            if (cloud.springboard != null && crosses(before, cloud.springboardSurface())
                && Math.abs(bibouleX - cloud.springboardX()) <= cloud.springboardWidth() / 2 + feet) {
                feetY = cloud.springboardSurface();
                jump(SPRINGBOARD_SPEED, "boing.wav");
                return;
            }
            if (crosses(before, cloud.surface) && Math.abs(bibouleX - cloud.x) <= cloud.width * 0.45 + feet) {
                feetY = cloud.surface;
                jump(JUMP_SPEED, "bounce.wav");
                return;
            }
        }
    }

    private boolean crosses(final double before, final double surface) {
        return before <= surface && feetY >= surface;
    }

    private void jump(final double speed, final String sound) {
        velocity = -speed;
        squash = 1;
        gameContext.getSoundManager().add(DATA + "sounds/" + sound);
    }

    /**
     * Above the line, the clouds go down instead: this is what is climbed, and what gives the points.
     */
    private void scrollIfHigh() {
        final double line = height * SCROLL_LINE;
        if (feetY >= line) {
            return;
        }
        final double difference = line - feetY;
        feetY = line;
        final Iterator<Cloud> iterator = clouds.iterator();
        while (iterator.hasNext()) {
            final Cloud cloud = iterator.next();
            cloud.goDown(difference);
            if (cloud.surface > height * 1.2) {
                cloudsLayer.getChildren().remove(cloud.node);
                iterator.remove();
            }
        }
        generateClouds();
        climbed += difference / height;
        final int points = (int) (climbed * 100);
        if (points != score) {
            stats.incrementNumberOfGoalsReached(points - score);
            score = points;
            showHud();
        }
    }

    private void placeBiboule() {
        biboule.setLayoutX(bibouleX - pictureWidth / 2);
        biboule.setLayoutY(feetY - pictureHeight * FEET_IN_PICTURE);
        biboule.setRotate(tilt);
        biboule.setScaleX(1 + 0.18 * squash);
        biboule.setScaleY(1 - 0.18 * squash);
    }

    /**
     * Fallen under the screen: one life less; she starts again on a new cloud, or "Lost!" after the third.
     */
    void fall() {
        if (phase != Phase.PLAY) {
            return;
        }
        lives--;
        if (lives <= 0) {
            showHud();
            lose();
            return;
        }
        blinking = true;
        startOnANewCloud(WAIT_AFTER_FALL);
        showHud();
    }

    private void showHud() {
        hud.getChildren().clear();
        final double fontSize = height * 0.035;
        final double margin = height * 0.025;
        final double heart = height * 0.055;
        // The lives: the hearts shared by the reviewed games (GameStyle.lives)
        final Group hearts = GameStyle.lives(lives, LIVES, heart, width * 0.3, false);
        hearts.setLayoutX(margin);
        hearts.setLayoutY(margin * 0.6);
        hud.getChildren().add(hearts);
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("BibouleJumpScore") + " " + score,
            fontSize);
        pill.setLayoutX(width - pill.getBoundsInLocal().getWidth() - margin);
        pill.setLayoutY(margin * 0.6);
        hud.getChildren().add(pill);
        if (end.getSeconds() > 0) {
            final int seconds = (int) Math.ceil(timeLeft);
            final Group clock = GameStyle.pill(String.format("%d:%02d", seconds / 60, seconds % 60), fontSize);
            clock.setLayoutX((width - clock.getBoundsInLocal().getWidth()) / 2);
            clock.setLayoutY(margin * 0.6);
            hud.getChildren().add(clock);
        }
    }

    private void win() {
        if (phase != Phase.PLAY) {
            return;
        }
        phase = Phase.OVER;
        loop.stop();
        stats.takeScreenShot();
        gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
    }

    private void lose() {
        phase = Phase.OVER;
        loop.stop();
        biboule.setVisible(false);
        stats.takeScreenShot();
        final Group card = GameStyle.card(gameContext.getTranslator().translate("BibouleJumpLost") + "\n"
            + gameContext.getTranslator().translate("BibouleJumpScore") + " " + score, height * 0.06, width * 0.6);
        card.setLayoutX(width / 2 - card.getBoundsInLocal().getWidth() / 2);
        card.setLayoutY(height / 2 - card.getBoundsInLocal().getHeight() / 2);
        hud.getChildren().add(card);
        lostPause = new PauseTransition(Duration.seconds(2.5));
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
     * @return half of the width on which the feet of the Biboule land
     */
    double getFeetWidth() {
        return pictureWidth * FEET_WIDTH;
    }

    /**
     * @return the picture of the Biboule
     */
    ImageView getBiboule() {
        return biboule;
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (lostPause != null) {
            lostPause.stop();
        }
        if (area != null) {
            gameContext.getGazeDeviceManager().removeEventFilter(area);
        }
        clouds.clear();
        highest = null;
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A cloud, moving or not, maybe with a springboard on it. The Biboule lands on its surface, a little below the top of
     * its picture.
     */
    final class Cloud {
        private static final double RATIO = 3;
        private static final double SURFACE_IN_PICTURE = 0.28;
        private static final double SPRINGBOARD_WIDTH = 0.42;

        @Getter
        private double x;
        @Getter
        private double surface;
        @Getter
        private final double width;
        @Getter
        private final boolean moving;
        private final double home;
        private double direction;
        private final Group node;
        private final ImageView springboard;
        private final double springboardOffset;

        Cloud(final double x, final double surface, final double width, final boolean moving,
              final boolean withSpringboard) {
            this.x = x;
            this.surface = surface;
            this.width = width;
            this.moving = moving;
            this.home = x;
            this.direction = random.nextInt(2) == 0 ? -1 : 1;
            final double cloudHeight = width / RATIO;
            final ImageView picture = new ImageView(gameImage(DATA + "clouds/cloud" + (1 + random.nextInt(3)) + ".png"));
            picture.setFitWidth(width);
            picture.setFitHeight(cloudHeight);
            picture.setX(-width / 2);
            picture.setY(-cloudHeight * SURFACE_IN_PICTURE);
            node = new Group(picture);
            if (withSpringboard) {
                final double board = width * SPRINGBOARD_WIDTH;
                springboardOffset = (width - board) * 0.35 * (2 * random.nextDouble() - 1);
                springboard = new ImageView(gameImage(DATA + "bouncepad.png"));
                springboard.setFitWidth(board);
                springboard.setFitHeight(board / 2);
                springboard.setX(springboardOffset - board / 2);
                springboard.setY(-board / 2);
                node.getChildren().add(springboard);
            } else {
                springboardOffset = 0;
                springboard = null;
            }
            place();
        }

        private void place() {
            node.setLayoutX(x);
            node.setLayoutY(surface);
        }

        /**
         * A moving cloud goes left and right around its place, inside the screen.
         */
        void move(final double seconds) {
            if (!moving) {
                return;
            }
            final double screen = BibouleJump.this.width;
            final double left = Math.max(width / 2, home - CLOUD_RANGE * screen);
            final double right = Math.min(screen - width / 2, home + CLOUD_RANGE * screen);
            x += direction * CLOUD_SPEED * screen * seconds;
            if (x <= left) {
                x = left;
                direction = 1;
            } else if (x >= right) {
                x = right;
                direction = -1;
            }
            place();
        }

        void goDown(final double difference) {
            surface += difference;
            place();
        }

        boolean hasSpringboard() {
            return springboard != null;
        }

        double springboardX() {
            return x + springboardOffset;
        }

        double springboardWidth() {
            return width * SPRINGBOARD_WIDTH;
        }

        /**
         * @return the height where the Biboule touches the springboard, above the surface of the cloud
         */
        double springboardSurface() {
            return surface - width * SPRINGBOARD_WIDTH * 0.38;
        }
    }
}

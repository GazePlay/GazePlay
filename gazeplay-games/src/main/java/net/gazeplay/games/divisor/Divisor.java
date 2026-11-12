package net.gazeplay.games.divisor;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * "Diviseur" (name code Divisor; reviewed on 12/11/2026, maintainer's rules). A big target bounces across a meadow;
 * touched by the gaze or the mouse (at once, or after a fixation of the length of the settings, depending on the
 * variant), it bursts into two smaller targets going apart, and so on: 2, 3 or 4 divisions (7, 15 or 31 targets). The
 * game ends when they have all burst: "Bravo", the time taken, then the statistics. No life, no penalty. The targets
 * show the rabbits drawn for the game, the animals of the default images, or the images of the user.
 * <p>
 * One loop moves everything in real time, the speeds as parts of the height of the screen, at the animation speed of
 * the settings; nothing moves while the menu of the game is open, nothing bursts before the delay of the settings, a
 * new target cannot burst during its first {@value #ARMING} s. (Before the review: two games of the menu, "Divisor" and
 * "Rabbits"; speeds in pixels, twice as fast on a small screen; the two new targets born on each other; images
 * stretched; animations of the targets gone running until the end, and started again by a change of the animation
 * speed; nothing stopped by the menu of the game; "Score" not translated; tiny bursts; the images of the user shown in
 * silence instead of the animals.)
 */
public class Divisor implements GameLifeCycle {

    /**
     * The radius of the first target, as a part of the height of the screen (a third of it across, as before).
     */
    static final double FIRST_RADIUS = 1.0 / 6;
    /**
     * Each division makes the targets this much smaller...
     */
    static final double SHRINK = 0.7;
    /**
     * ... down to this radius, as a part of the height (a tenth of it across): smaller, they are hard to look at.
     */
    static final double SMALLEST_RADIUS = 0.05;
    /**
     * The seconds during which a new target cannot burst: the gaze is still where its parent was.
     */
    static final double ARMING = 0.6;
    /**
     * A look away shorter than this (seconds) does not stop a fixation: the target moves and the eye tracker trembles.
     */
    static final double FIXATION_GRACE = 0.3;

    /**
     * The side of a drawing of rabbit, on the radius of its target.
     */
    static final double RABBIT_SCALE = 2.4;
    static final String[] RABBITS = {"1", "2", "3", "4", "5", "6", "7"};
    private static final String RABBIT_FOLDER = "data/divisor/rabbit/images/";
    private static final String MEADOW = "data/divisor/images/Background.png";
    private static final String[] POPS = {"data/bubble/sounds/pop1.wav", "data/bubble/sounds/pop2.wav",
        "data/bubble/sounds/pop3.wav"};
    private static final Color[] CONFETTI = {Color.rgb(255, 202, 58), Color.rgb(255, 120, 150),
        Color.rgb(110, 200, 255), Color.rgb(150, 230, 120), Color.WHITE};

    enum Phase { WAIT, PLAY, OVER }

    private static final Map<String, Image> GAME_IMAGES = new HashMap<>();

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final DivisorTheme theme;
    @Getter
    private final DivisorTouch touch;
    @Getter
    private final DivisorLevel level;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private final List<Target> targets = new ArrayList<>();
    @Getter
    private int burst;
    /**
     * The seconds played, without the delay at the start nor the time the menu of the game was open.
     */
    @Getter
    private double playSeconds;
    private double waiting;
    private double width;
    private double height;

    private Group layer;
    private Group targetsLayer;
    private Group effects;
    private Group hud;
    private FixationRing ring;

    private ImageLibrary animals;
    private List<File> userImages = List.of();
    private int lastImage = -1;

    /**
     * The target being looked at, during a fixation.
     */
    @Getter
    private Target looked;
    @Getter
    private double fixationSeconds;
    private double awaySeconds;

    private final List<Transition> running = new ArrayList<>();

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
    Divisor(final IGameContext gameContext, final Stats stats, final DivisorTheme theme, final DivisorTouch touch,
            final DivisorLevel level, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.theme = theme;
        this.touch = touch;
        this.level = level;
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

        final Image meadow = gameImage(MEADOW);
        final ImageView background = new ImageView(meadow);
        // Covers the screen, keeping its proportions
        final double scale = Math.max(width / Math.max(1, meadow.getWidth()), height / Math.max(1, meadow.getHeight()));
        background.setFitWidth(meadow.getWidth() * scale);
        background.setFitHeight(meadow.getHeight() * scale);
        background.setLayoutX((width - background.getFitWidth()) / 2);
        background.setLayoutY((height - background.getFitHeight()) / 2);
        background.setMouseTransparent(true);

        targetsLayer = new Group();
        effects = new Group();
        effects.setMouseTransparent(true);
        ring = new FixationRing();
        ring.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        layer = new Group(background, targetsLayer, effects, ring, hud);
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();

        if (theme == DivisorTheme.MY_IMAGES) {
            userImages = UserImagesVariant.images(DivisorTheme.USER_IMAGES_FOLDER);
            if (userImages.isEmpty()) {
                // The game started without going through the menu, which asks for images
                final Group message = GameStyle.card(gameContext.getTranslator().translate("NoUserImages"),
                    height * 0.035, width * 0.5);
                message.setLayoutX((width - message.getBoundsInLocal().getWidth()) / 2);
                message.setLayoutY((height - message.getBoundsInLocal().getHeight()) / 2);
                hud.getChildren().add(message);
                return;
            }
        } else if (theme == DivisorTheme.ANIMALS) {
            animals = ImageUtils.createDefaultImageLibrary(null, random);
        }

        stats.incrementNumberOfGoalsToReach(level.getTargets());
        burst = 0;
        playSeconds = 0;
        final double radius = radiusOf(0);
        final double angle = firstAngle();
        addTarget(0, randomBetween(radius, width - radius), randomBetween(radius, height - radius), angle);
        // The first target can burst at once, after the delay of the settings
        targets.get(0).age = ARMING;
        waiting = configuration.getGameStartDelay() / 1000.0;
        phase = Phase.WAIT;
        showHud();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * @return an image of the game, loaded once and not larger than needed: a game started again uses the same ones
     */
    private static Image gameImage(final String path) {
        return GAME_IMAGES.computeIfAbsent(path, key -> new Image(key, 900, 900, true, true));
    }

    /**
     * @return the radius of the targets after this number of divisions
     */
    double radiusOf(final int division) {
        return height * Math.max(SMALLEST_RADIUS, FIRST_RADIUS * Math.pow(SHRINK, division));
    }

    /**
     * @return a direction, in radians, never close to a side of the screen: a target going straight across would only
     * go back and forth
     */
    private double firstAngle() {
        final double degrees = 90 * random.nextInt(4) + 25 + 40 * random.nextDouble();
        return Math.toRadians(degrees);
    }

    private double randomBetween(final double min, final double max) {
        return max <= min ? (min + max) / 2 : min + (max - min) * random.nextDouble();
    }

    /**
     * Adds a target of a division at a point (kept inside the screen), going in a direction.
     */
    private Target addTarget(final int division, final double x, final double y, final double angle) {
        final Target target = new Target(division, nextImage(), angle);
        target.x = Math.max(target.radius, Math.min(width - target.radius, x));
        target.y = Math.max(target.radius, Math.min(height - target.radius, y));
        target.place();
        targets.add(target);
        targetsLayer.getChildren().add(target.node);
        gameContext.getGazeDeviceManager().addEventFilter(target.node);
        return target;
    }

    private Image nextImage() {
        switch (theme) {
            case RABBITS -> {
                return gameImage(RABBIT_FOLDER + RABBITS[nextIndex(RABBITS.length)] + ".png");
            }
            case ANIMALS -> {
                return animals.pickRandomImage();
            }
            default -> {
                Image loaded = null;
                for (int i = 0; i < userImages.size() && (loaded == null || loaded.isError()); i++) {
                    loaded = new Image(userImages.get(nextIndex(userImages.size())).toURI().toString(), 640, 640,
                        true, true);
                }
                return loaded;
            }
        }
    }

    /**
     * @return a random place in a list, not the last one if there is another
     */
    private int nextIndex(final int size) {
        int index = random.nextInt(size);
        if (index == lastImage && size > 1) {
            index = (index + 1) % size;
        }
        lastImage = index;
        return index;
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        if (phase == Phase.WAIT) {
            waiting -= seconds;
            if (waiting <= 0) {
                phase = Phase.PLAY;
            }
            return;
        }
        playSeconds += seconds;
        final double speed = Math.max(0.1, configuration.getAnimationSpeedRatio()) * height
            / level.getCrossingSeconds();
        for (final Target target : targets) {
            target.age += seconds;
            target.move(seconds * speed);
        }
        if (ring.isVisible() && looked != null) {
            ring.moveTo(looked.x, looked.y);
        }
        if (touch == DivisorTouch.AT_ONCE) {
            for (final Target target : List.copyOf(targets)) {
                if (target.isTouched() && phase == Phase.PLAY) {
                    burst(target);
                }
            }
        } else {
            fixation(seconds);
        }
    }

    /**
     * The fixation: its length is that of the settings, read at each step, so that a change in the menu of the game
     * counts at once. A short look away only stops the ring for a moment; another target looked at starts again.
     */
    private void fixation(final double seconds) {
        if (looked != null && !looked.isTouched()) {
            awaySeconds += seconds;
            if (awaySeconds <= FIXATION_GRACE) {
                return;
            }
            stopFixation();
        }
        if (looked == null) {
            looked = targets.stream().filter(Target::isTouched).findFirst().orElse(null);
            if (looked == null) {
                return;
            }
            ring.setSize(looked.radius * 2.3);
            ring.show(looked.x, looked.y);
        }
        awaySeconds = 0;
        fixationSeconds += seconds;
        final double length = configuration.getFixationLength() / 1000.0;
        ring.setProgress(length <= 0 ? 1 : fixationSeconds / length);
        if (fixationSeconds >= length) {
            burst(looked);
        }
    }

    private void stopFixation() {
        looked = null;
        fixationSeconds = 0;
        awaySeconds = 0;
        ring.stop();
    }

    /**
     * A target burst: a point, a pop, confetti; two smaller ones going apart until the last division; the end when
     * there is none left.
     */
    void burst(final Target target) {
        if (phase != Phase.PLAY || !targets.contains(target)) {
            return;
        }
        if (target == looked) {
            stopFixation();
        }
        targets.remove(target);
        targetsLayer.getChildren().remove(target.node);
        gameContext.getGazeDeviceManager().removeEventFilter(target.node);
        playBurst(target);
        gameContext.getSoundManager().add(POPS[random.nextInt(POPS.length)]);
        burst++;
        stats.incrementNumberOfGoalsReached();
        if (target.division < level.getDivisions()) {
            // Two halves going apart, across the way of their parent
            final double angle = target.angle + Math.PI / 2 + Math.toRadians(-30 + 60 * random.nextDouble());
            final Target first = addTarget(target.division + 1, target.x, target.y, angle);
            final Target second = addTarget(target.division + 1, target.x, target.y, angle + Math.PI);
            // Apart at once, so that they are not drawn on each other
            final double apart = first.radius * 0.6;
            first.shift(Math.cos(angle) * apart, Math.sin(angle) * apart);
            second.shift(-Math.cos(angle) * apart, -Math.sin(angle) * apart);
        }
        showHud();
        if (targets.isEmpty()) {
            win();
        }
    }

    /**
     * The image grows and fades, confetti fly around.
     */
    private void playBurst(final Target target) {
        final Group ghost = target.drawing();
        ghost.setLayoutX(target.x);
        ghost.setLayoutY(target.y);
        effects.getChildren().add(ghost);
        final ScaleTransition grow = new ScaleTransition(Duration.millis(220), ghost);
        grow.setToX(1.35);
        grow.setToY(1.35);
        final FadeTransition fade = new FadeTransition(Duration.millis(220), ghost);
        fade.setToValue(0);
        final ParallelTransition pop = new ParallelTransition(grow, fade);
        pop.setOnFinished(event -> effects.getChildren().remove(ghost));
        play(pop);

        final int count = 14;
        final double size = Math.max(4, target.radius * 0.11);
        for (int i = 0; i < count; i++) {
            final double angle = 2 * Math.PI * i / count + random.nextDouble() * 0.4;
            final Circle confetti = new Circle(size, CONFETTI[i % CONFETTI.length]);
            confetti.setStroke(Color.rgb(40, 60, 30, 0.5));
            confetti.setLayoutX(target.x + Math.cos(angle) * target.radius * 0.5);
            confetti.setLayoutY(target.y + Math.sin(angle) * target.radius * 0.5);
            effects.getChildren().add(confetti);
            final double distance = target.radius * (1.1 + 0.6 * random.nextDouble());
            final TranslateTransition fly = new TranslateTransition(Duration.millis(520), confetti);
            fly.setToX(Math.cos(angle) * distance);
            fly.setToY(Math.sin(angle) * distance);
            fly.setInterpolator(Interpolator.EASE_OUT);
            final FadeTransition vanish = new FadeTransition(Duration.millis(300), confetti);
            vanish.setDelay(Duration.millis(220));
            vanish.setToValue(0);
            final ParallelTransition animation = new ParallelTransition(fly, vanish);
            animation.setOnFinished(event -> effects.getChildren().remove(confetti));
            play(animation);
        }
    }

    private void play(final Transition transition) {
        running.add(transition);
        final var finished = transition.getOnFinished();
        transition.setOnFinished(event -> {
            running.remove(transition);
            if (finished != null) {
                finished.handle(event);
            }
        });
        transition.play();
    }

    private void showHud() {
        hud.getChildren().clear();
        final double margin = height * 0.025;
        final Group score = GameStyle.pill(gameContext.getTranslator().translate("DivisorBurst") + " " + burst
            + " / " + level.getTargets(), height * 0.035);
        score.setLayoutX(width - score.getBoundsInLocal().getWidth() - margin);
        score.setLayoutY(margin * 0.6);
        hud.getChildren().add(score);
    }

    /**
     * All burst: the time taken in the middle of the screen, "Bravo", then the statistics.
     */
    private void win() {
        phase = Phase.OVER;
        loop.stop();
        final Group time = GameStyle.pill(gameContext.getTranslator().translate("DivisorTime") + " "
            + Math.max(1, Math.round(playSeconds)) + " s", height * 0.06);
        time.setLayoutX((width - time.getBoundsInLocal().getWidth()) / 2);
        time.setLayoutY(height * 0.62);
        hud.getChildren().add(time);
        stats.takeScreenShot();
        gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
    }

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        loop.stop();
    }

    double getWidth() {
        return width;
    }

    double getHeight() {
        return height;
    }

    FixationRing getRing() {
        return ring;
    }

    Group getHud() {
        return hud;
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        for (final Transition transition : List.copyOf(running)) {
            transition.stop();
        }
        running.clear();
        if (ring != null) {
            ring.stop();
        }
        for (final Target target : targets) {
            gameContext.getGazeDeviceManager().removeEventFilter(target.node);
        }
        targets.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A target: its image (a rabbit as drawn, or an image cut to a circle with a white border) and a transparent circle
     * catching the gaze and the mouse; it bounces on the sides of the screen.
     */
    final class Target {
        @Getter
        private final int division;
        @Getter
        private final double radius;
        private final Image image;
        @Getter
        private final Group node;
        @Getter
        private double x;
        @Getter
        private double y;
        private double angle;
        private double dx;
        private double dy;
        /**
         * The seconds since it was born.
         */
        private double age;
        private boolean onGaze;
        private boolean onMouse;

        Target(final int division, final Image image, final double angle) {
            this.division = division;
            this.radius = radiusOf(division);
            this.image = image;
            this.angle = angle;
            dx = Math.cos(angle);
            dy = Math.sin(angle);
            final Circle zone = new Circle(radius, Color.TRANSPARENT);
            final Group drawing = drawing();
            drawing.setMouseTransparent(true);
            node = new Group(zone, drawing);
            final EventHandler<Event> handler = this::onPointer;
            node.addEventFilter(GazeEvent.ANY, handler);
            node.addEventFilter(MouseEvent.ANY, handler);
        }

        /**
         * @return the image of the target, centered on (0, 0), keeping its proportions
         */
        Group drawing() {
            final ImageView view = new ImageView(image);
            view.setSmooth(true);
            if (theme == DivisorTheme.RABBITS) {
                // The drawings have transparent margins (about a tenth on each side): a little larger, the rabbit
                // fills its circle
                final double side = radius * RABBIT_SCALE;
                view.setPreserveRatio(true);
                view.setFitWidth(side);
                view.setFitHeight(side);
                view.setX(-side / 2);
                view.setY(-side / 2);
                return new Group(view);
            }
            view.setFitWidth(radius * 2);
            view.setFitHeight(radius * 2);
            view.setX(-radius);
            view.setY(-radius);
            if (image != null) {
                final double side = Math.min(image.getWidth(), image.getHeight());
                view.setViewport(new Rectangle2D((image.getWidth() - side) / 2, (image.getHeight() - side) / 2,
                    side, side));
            }
            view.setClip(new Circle(0, 0, radius));
            final Circle frame = new Circle(radius);
            frame.setFill(null);
            frame.setStroke(Color.WHITE);
            frame.setStrokeWidth(Math.max(3, radius * 0.06));
            return new Group(view, frame);
        }

        private void onPointer(final Event event) {
            final var type = event.getEventType();
            if (type == GazeEvent.GAZE_ENTERED || type == GazeEvent.GAZE_MOVED) {
                onGaze = true;
            } else if (type == GazeEvent.GAZE_EXITED) {
                onGaze = false;
            } else if (type == MouseEvent.MOUSE_ENTERED || type == MouseEvent.MOUSE_MOVED) {
                onMouse = true;
            } else if (type == MouseEvent.MOUSE_EXITED) {
                onMouse = false;
            }
        }

        /**
         * @return whether the gaze or the mouse is on it, and it is old enough to burst
         */
        boolean isTouched() {
            return (onGaze || onMouse) && age >= ARMING;
        }

        /**
         * Goes a distance, bouncing on the sides of the screen.
         */
        void move(final double distance) {
            x += dx * distance;
            y += dy * distance;
            if (x < radius) {
                x = radius;
                dx = Math.abs(dx);
            } else if (x > width - radius) {
                x = width - radius;
                dx = -Math.abs(dx);
            }
            if (y < radius) {
                y = radius;
                dy = Math.abs(dy);
            } else if (y > height - radius) {
                y = height - radius;
                dy = -Math.abs(dy);
            }
            angle = Math.atan2(dy, dx);
            place();
        }

        void shift(final double offsetX, final double offsetY) {
            x = Math.max(radius, Math.min(width - radius, x + offsetX));
            y = Math.max(radius, Math.min(height - radius, y + offsetY));
            place();
        }

        void place() {
            node.setLayoutX(x);
            node.setLayoutY(y);
        }

        /**
         * For the tests: the gaze enters it.
         */
        void setGaze(final boolean on) {
            onGaze = on;
        }

        double getAge() {
            return age;
        }

        double getDx() {
            return dx;
        }

        double getDy() {
            return dy;
        }
    }
}

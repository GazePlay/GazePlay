package net.gazeplay.games.ninja;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
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
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.LevelsReport;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * "Ninja" (reviewed on 4/11/2026, maintainer's rules): one fruit (or one image of the user) goes across an orchard, in
 * the way and at the speed of the variant, to follow with the eyes. It is cut in two as soon as it is touched, or after
 * a fixation of the length of the settings; another one comes elsewhere. There is no life and no penalty: a target
 * missed goes on. The game has no end, or ends after 20 or 50 targets cut, with "Bravo" then the statistics.
 * <p>
 * One loop moves everything in real time, at the animation speed of the settings; nothing moves while the menu of the
 * game is open. (Before the review: durations of 1 to 3 s whatever the length of the way, a target half out of the
 * screen or out of it, and a "dynamic" speed that could only slow down with the fixation of 2 s of the settings.)
 */
public class Ninja implements GameLifeCycle {

    static final String[] FRUITS = {"apple", "pear", "orange", "lemon", "strawberry", "peach", "plum", "kiwi"};
    /**
     * The seconds between a cut and the next target.
     */
    static final double GAP = 0.6;
    /**
     * A look away shorter than this (seconds) does not stop a fixation: the fruit moves and the eye tracker trembles.
     */
    static final double FIXATION_GRACE = 0.3;
    /**
     * The seconds to go through the height of the screen, from the slowest to the fastest, of the adaptive speed.
     */
    static final double[] ADAPTIVE_CROSSINGS = {8, 6, 5, 4, 3, 2.5, 2, 1.5};
    /**
     * Every {@value} targets, the adaptive speed is checked.
     */
    static final int ADAPTIVE_TARGETS = 3;
    /**
     * All the targets cut within this time (seconds, without the fixation): faster.
     */
    static final double ADAPTIVE_QUICK = 1;
    /**
     * All the targets cut after this time (seconds, without the fixation): slower.
     */
    static final double ADAPTIVE_SLOW = 3;

    enum Phase { PLAY, OVER }

    private static final Map<String, Image> GAME_IMAGES = new HashMap<>();

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final NinjaTrajectory trajectory;
    @Getter
    private final NinjaSpeed speed;
    @Getter
    private final NinjaCut cut;
    @Getter
    private final NinjaEnd end;
    @Getter
    private final NinjaTarget target;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private int cuts;
    private double width;
    private double height;

    private Group layer;
    private Group effects;
    private Group hud;
    private FixationRing ring;

    /**
     * The target: its image, and a circle a little larger catching the gaze and the mouse; it stays registered, moved and
     * filled again for each new target.
     */
    @Getter
    private Group fruit;
    private Circle zone;
    private Circle frame;
    private ImageView view;
    private Image image;
    private int lastImage = -1;
    private List<File> userImages = List.of();
    private final Image[] fruitImages = new Image[FRUITS.length];

    @Getter
    private double fruitX;
    @Getter
    private double fruitY;
    @Getter
    private double radius;
    private double fromX;
    private double fromY;
    private double toX;
    private double toY;
    /**
     * The seconds before the next target, 0 when it is shown.
     */
    @Getter
    private double gap;
    /**
     * The seconds the target has been shown.
     */
    private double shownSeconds;
    @Getter
    private double crossingSeconds;
    private int adaptiveLevel;
    private final List<Double> lastTimes = new ArrayList<>();

    private boolean onGaze;
    private boolean onMouse;
    @Getter
    private double fixationSeconds;
    private double awaySeconds;

    private final List<Transition> running = new ArrayList<>();
    private final EventHandler<Event> pointerHandler = this::onPointer;

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
    Ninja(final IGameContext gameContext, final Stats stats, final NinjaTrajectory trajectory, final NinjaSpeed speed,
          final NinjaCut cut, final NinjaEnd end, final NinjaTarget target, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        // The limits of score and of time of the settings
        gameContext.startScoreLimiter();
        gameContext.startTimeLimiter();
        this.stats = stats;
        this.trajectory = trajectory;
        this.speed = speed;
        this.cut = cut;
        this.end = end;
        this.target = target;
        if (gameSeed < 0) {
            random = new ReplayablePseudoRandom();
            stats.setGameSeed(random.getSeed());
        } else {
            random = new ReplayablePseudoRandom(gameSeed);
        }
        crossingSeconds = speed.getCrossingSeconds();
        for (int i = 0; i < ADAPTIVE_CROSSINGS.length; i++) {
            if (ADAPTIVE_CROSSINGS[i] == crossingSeconds) {
                adaptiveLevel = i;
            }
        }
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        width = dimension.getWidth();
        height = dimension.getHeight();

        final Image orchard = gameImage("orchard");
        final ImageView background = new ImageView(orchard);
        // Covers the screen, keeping its proportions
        final double scale = Math.max(width / Math.max(1, orchard.getWidth()), height / Math.max(1, orchard.getHeight()));
        background.setFitWidth(orchard.getWidth() * scale);
        background.setFitHeight(orchard.getHeight() * scale);
        background.setLayoutX((width - background.getFitWidth()) / 2);
        background.setLayoutY((height - background.getFitHeight()) / 2);
        background.setMouseTransparent(true);

        zone = new Circle(0, Color.TRANSPARENT);
        view = new ImageView();
        view.setMouseTransparent(true);
        frame = new Circle();
        frame.setFill(null);
        frame.setStroke(Color.WHITE);
        frame.setMouseTransparent(true);
        fruit = new Group(zone, view, frame);
        fruit.setVisible(false);
        effects = new Group();
        effects.setMouseTransparent(true);
        ring = new FixationRing();
        hud = new Group();
        hud.setMouseTransparent(true);
        layer = new Group(background, fruit, effects, ring, hud);
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();
        gameContext.firstStart();

        if (target == NinjaTarget.MY_IMAGES) {
            userImages = UserImagesVariant.images(NinjaTarget.USER_IMAGES_FOLDER);
            if (userImages.isEmpty()) {
                // The game started without going through the menu, which asks for images
                final Group message = GameStyle.card(gameContext.getTranslator().translate("NoUserImages"),
                    height * 0.035, width * 0.5);
                message.setLayoutX((width - message.getBoundsInLocal().getWidth()) / 2);
                message.setLayoutY((height - message.getBoundsInLocal().getHeight()) / 2);
                hud.getChildren().add(message);
                return;
            }
        } else {
            for (int i = 0; i < FRUITS.length; i++) {
                fruitImages[i] = gameImage(FRUITS[i]);
            }
        }

        gameContext.getGazeDeviceManager().addEventFilter(fruit);
        fruit.addEventFilter(GazeEvent.ANY, pointerHandler);
        fruit.addEventFilter(MouseEvent.ANY, pointerHandler);

        phase = Phase.PLAY;
        showHud();
        prepareTarget();
        showTarget();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * @return an image of the game, loaded once: a game started again uses the same ones (about 14 MB in memory for
     * each load of the orchard and of the fruits, which took the memory of the tests)
     */
    private static Image gameImage(final String name) {
        return GAME_IMAGES.computeIfAbsent(name, key -> new Image("data/ninja/images/" + key + ".png"));
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

    private boolean pointerOnTarget() {
        return onGaze || onMouse;
    }

    /**
     * Chooses the next target and its way, and puts it in its place, still hidden: a look already there starts as soon
     * as it shows.
     */
    private void prepareTarget() {
        radius = Math.max(height * 0.06, Math.min(height * 0.22, configuration.getElementSize()));
        final double margin = radius * 1.12;
        image = nextImage();
        fillTarget();
        fruitX = randomBetween(margin, width - margin);
        fruitY = randomBetween(margin, height - margin);
        switch (trajectory) {
            case RANDOM -> setWayTo(randomBetween(margin, width - margin), randomBetween(margin, height - margin));
            case VERTICAL -> setLine(fruitX, margin, fruitX, height - margin);
            case HORIZONTAL -> setLine(margin, fruitY, width - margin, fruitY);
            case DIAGONAL_DOWN -> setLine(margin, margin, width - margin, height - margin);
            case DIAGONAL_UP -> setLine(width - margin, margin, margin, height - margin);
        }
        placeTarget();
    }

    private double randomBetween(final double min, final double max) {
        return max <= min ? (min + max) / 2 : min + (max - min) * random.nextDouble();
    }

    private void setWayTo(final double x, final double y) {
        toX = x;
        toY = y;
    }

    /**
     * Puts the target at a random place of a line, going to one of its ends.
     */
    private void setLine(final double x0, final double y0, final double x1, final double y1) {
        final double along = random.nextDouble();
        fruitX = x0 + (x1 - x0) * along;
        fruitY = y0 + (y1 - y0) * along;
        final boolean forward = random.nextDouble() < 0.5;
        fromX = forward ? x0 : x1;
        fromY = forward ? y0 : y1;
        toX = forward ? x1 : x0;
        toY = forward ? y1 : y0;
    }

    private Image nextImage() {
        if (target == NinjaTarget.FRUITS) {
            return fruitImages[nextIndex(FRUITS.length)];
        }
        Image loaded = null;
        for (int i = 0; i < userImages.size() && (loaded == null || loaded.isError()); i++) {
            loaded = new Image(userImages.get(nextIndex(userImages.size())).toURI().toString(), 640, 640, true, true);
        }
        return loaded;
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
     * Sizes the target; the image of a user is cut to a circle with a white border, a fruit is drawn with its own.
     */
    private void fillTarget() {
        zone.setRadius(radius * 1.05);
        view.setImage(image);
        view.setFitWidth(radius * 2);
        view.setFitHeight(radius * 2);
        view.setX(-radius);
        view.setY(-radius);
        view.setSmooth(true);
        final boolean round = target == NinjaTarget.MY_IMAGES;
        frame.setVisible(round);
        if (round) {
            frame.setRadius(radius);
            frame.setStrokeWidth(Math.max(3, radius * 0.06));
            view.setViewport(centerSquare(image));
            view.setClip(new Circle(0, 0, radius));
        }
    }

    private static Rectangle2D centerSquare(final Image image) {
        final double side = Math.min(image.getWidth(), image.getHeight());
        return new Rectangle2D((image.getWidth() - side) / 2, (image.getHeight() - side) / 2, side, side);
    }

    private void placeTarget() {
        fruit.setLayoutX(fruitX);
        fruit.setLayoutY(fruitY);
        if (ring.isVisible()) {
            ring.moveTo(fruitX, fruitY);
        }
    }

    /**
     * The target appears, a little pop; a new goal to reach.
     */
    private void showTarget() {
        gap = 0;
        shownSeconds = 0;
        fixationSeconds = 0;
        awaySeconds = 0;
        fruit.setVisible(true);
        fruit.setScaleX(0.6);
        fruit.setScaleY(0.6);
        final ScaleTransition pop = new ScaleTransition(Duration.millis(160), fruit);
        pop.setToX(1);
        pop.setToY(1);
        play(pop);
        stats.incrementNumberOfGoalsToReach();
        if (speed == NinjaSpeed.ADAPTIVE) {
            final LevelsReport levels = stats.getLevelsReport();
            if (levels != null) {
                levels.addRoundLevel((long) (crossingSeconds * 1000));
            }
        }
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase != Phase.PLAY || seconds <= 0) {
            return;
        }
        if (gap > 0) {
            gap -= seconds;
            if (gap <= 0) {
                showTarget();
            }
            return;
        }
        shownSeconds += seconds;
        final double ratio = Math.max(0.1, configuration.getAnimationSpeedRatio());
        move(seconds * ratio * height / crossingSeconds);
        placeTarget();
        if (cut == NinjaCut.AT_ONCE) {
            if (pointerOnTarget()) {
                cutTarget();
            }
        } else {
            fixation(seconds);
        }
    }

    /**
     * Goes a distance along the way, then on to the next place or back along the line.
     */
    private void move(final double distance) {
        double left = distance;
        for (int i = 0; i < 8 && left > 0; i++) {
            final double dx = toX - fruitX;
            final double dy = toY - fruitY;
            final double remaining = Math.hypot(dx, dy);
            if (left < remaining) {
                fruitX += dx / remaining * left;
                fruitY += dy / remaining * left;
                return;
            }
            fruitX = toX;
            fruitY = toY;
            left -= remaining;
            if (trajectory == NinjaTrajectory.RANDOM) {
                final double margin = radius * 1.12;
                setWayTo(randomBetween(margin, width - margin), randomBetween(margin, height - margin));
            } else {
                toX = fromX;
                toY = fromY;
                fromX = fruitX;
                fromY = fruitY;
            }
        }
    }

    /**
     * The fixation: its length is that of the settings, read at each step, so that a change in the menu of the game
     * counts at once. A short look away only stops the ring for a moment.
     */
    private void fixation(final double seconds) {
        final double length = configuration.getFixationLength() / 1000.0;
        if (pointerOnTarget()) {
            awaySeconds = 0;
            if (!ring.isVisible()) {
                ring.setSize(radius * 2.5);
                ring.show(fruitX, fruitY);
            }
            fixationSeconds += seconds;
            ring.setProgress(length <= 0 ? 1 : fixationSeconds / length);
            if (fixationSeconds >= length) {
                cutTarget();
            }
        } else if (fixationSeconds > 0) {
            awaySeconds += seconds;
            if (awaySeconds > FIXATION_GRACE) {
                fixationSeconds = 0;
                ring.stop();
            }
        }
    }

    /**
     * The target cut: a point, a sound, the blade and the two halves; then the next one, or the end.
     */
    void cutTarget() {
        if (phase != Phase.PLAY || gap > 0) {
            return;
        }
        cuts++;
        ring.stop();
        fruit.setVisible(false);
        playCutEffect();
        gameContext.getSoundManager().add("data/ninja/sounds/slice" + (1 + random.nextInt(3)) + ".wav");
        stats.incrementNumberOfGoalsReached();
        gameContext.updateScore(stats, this);
        if (phase != Phase.PLAY) {
            // The limit of score of the settings has ended the game
            return;
        }
        showHud();
        adapt();
        if (end.getTargets() > 0 && cuts >= end.getTargets()) {
            win();
            return;
        }
        gap = GAP;
        prepareTarget();
    }

    /**
     * The adaptive speed: every three targets, faster if all of them were cut quickly, slower if all of them took a long
     * time. The time of the fixation does not count, so that it works whatever the length of the settings.
     */
    private void adapt() {
        if (speed != NinjaSpeed.ADAPTIVE) {
            return;
        }
        final double fixation = cut == NinjaCut.FIXATION ? configuration.getFixationLength() / 1000.0 : 0;
        lastTimes.add(Math.max(0, shownSeconds - fixation));
        if (lastTimes.size() < ADAPTIVE_TARGETS) {
            return;
        }
        if (lastTimes.stream().allMatch(time -> time < ADAPTIVE_QUICK)) {
            adaptiveLevel = Math.min(ADAPTIVE_CROSSINGS.length - 1, adaptiveLevel + 1);
        } else if (lastTimes.stream().allMatch(time -> time > ADAPTIVE_SLOW)) {
            adaptiveLevel = Math.max(0, adaptiveLevel - 1);
        }
        crossingSeconds = ADAPTIVE_CROSSINGS[adaptiveLevel];
        lastTimes.clear();
    }

    /**
     * The stroke of the blade through the target, and its two halves going apart and falling.
     */
    private void playCutEffect() {
        final double angle = -55 + 110 * random.nextDouble();
        final double x = fruitX;
        final double y = fruitY;
        final double r = radius;

        final double thickness = Math.max(3, r * 0.06);
        final Polygon blade = new Polygon(-r * 1.7, 0, 0, -thickness, r * 1.7, 0, 0, thickness);
        blade.setFill(Color.WHITE);
        blade.setStroke(Color.rgb(90, 110, 150, 0.6));
        blade.setStrokeWidth(1);
        blade.setLayoutX(x);
        blade.setLayoutY(y);
        blade.setRotate(angle);
        blade.setScaleX(0.2);
        effects.getChildren().add(blade);
        final ScaleTransition stroke = new ScaleTransition(Duration.millis(90), blade);
        stroke.setToX(1);
        final FadeTransition bladeFade = new FadeTransition(Duration.millis(180), blade);
        bladeFade.setDelay(Duration.millis(90));
        bladeFade.setToValue(0);
        final ParallelTransition bladeAnimation = new ParallelTransition(stroke, bladeFade);
        bladeAnimation.setOnFinished(event -> effects.getChildren().remove(blade));
        play(bladeAnimation);

        final double across = Math.toRadians(angle + 90);
        half(angle, x, y, r, -1, Math.cos(across), Math.sin(across));
        half(angle, x, y, r, 1, Math.cos(across), Math.sin(across));
    }

    private void half(final double angle, final double x, final double y, final double r, final int side,
                      final double acrossX, final double acrossY) {
        final ImageView part = new ImageView(image);
        part.setFitWidth(r * 2);
        part.setFitHeight(r * 2);
        part.setX(-r);
        part.setY(-r);
        part.setSmooth(true);
        // The image stays as it was; only the line of the cut follows the blade
        final Rectangle halfBox = new Rectangle(-r * 2, side < 0 ? -r * 2 : 0, r * 4, r * 2);
        halfBox.getTransforms().add(new Rotate(angle, 0, 0));
        if (target == NinjaTarget.MY_IMAGES) {
            part.setViewport(centerSquare(image));
            part.setClip(Shape.intersect(new Circle(0, 0, r), halfBox));
        } else {
            part.setClip(halfBox);
        }
        final Group holder = new Group(part);
        holder.setLayoutX(x);
        holder.setLayoutY(y);
        effects.getChildren().add(holder);

        final TranslateTransition apart = new TranslateTransition(Duration.millis(620), holder);
        apart.setToX(side * acrossX * r * 0.85);
        apart.setToY(side * acrossY * r * 0.85 + height * 0.1);
        final javafx.animation.RotateTransition spin = new javafx.animation.RotateTransition(Duration.millis(620), holder);
        spin.setByAngle(side * 22);
        final FadeTransition fade = new FadeTransition(Duration.millis(380), holder);
        fade.setDelay(Duration.millis(240));
        fade.setToValue(0);
        final ParallelTransition animation = new ParallelTransition(apart, spin, fade);
        animation.setOnFinished(event -> effects.getChildren().remove(holder));
        play(animation);
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
        final String score = gameContext.getTranslator().translate("NinjaScore") + " " + cuts
            + (end.getTargets() > 0 ? " / " + end.getTargets() : "");
        final Group pill = GameStyle.pill(score, height * 0.035);
        final double margin = height * 0.025;
        pill.setLayoutX(width - pill.getBoundsInLocal().getWidth() - margin);
        pill.setLayoutY(margin * 0.6);
        hud.getChildren().add(pill);
    }

    private void win() {
        phase = Phase.OVER;
        loop.stop();
        stats.takeScreenShot();
        gameContext.updateScore(stats, this);
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

    boolean isTargetShown() {
        return fruit.isVisible();
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        for (final Transition transition : List.copyOf(running)) {
            transition.stop();
        }
        running.clear();
        if (fruit != null) {
            gameContext.getGazeDeviceManager().removeEventFilter(fruit);
        }
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }
}

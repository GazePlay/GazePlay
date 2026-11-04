package net.gazeplay.games.moles;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
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
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The moles game (reviewed on 4/11/2026, maintainer's rules): moles (or Biboules, or images of the user) come out of the
 * holes of a meadow, one to three at a time according to the level. A target is hit as soon as it is touched, or after a
 * fixation of the length of the settings. It stays out for the time of the fixation and a time of the level, so that it
 * can always be hit. There is no life and no penalty; the game has no end, or ends after 1 or 3 minutes, or after 20 or
 * 50 targets, with "Bravo" then the statistics.
 * <p>
 * One loop moves everything in real time, at the animation speed of the settings; nothing moves while the menu of the
 * gear is open. (Before the review: a timer sending out a target every 0.5 s, up to 10 at a time, even with the menu
 * open and after the end of the game; targets out 2 to 4.5 s for a fixation of 2 s by default.)
 */
public class Moles implements GameLifeCycle {

    static final String[] BIBOULE_COLORS = {"Blue", "Green", "Yellow", "Orange", "Red"};
    /**
     * The seconds a target takes to come out, at the normal animation speed.
     */
    static final double RISE = 0.35;
    /**
     * The seconds a target takes to go back: when it is missed, when it is hit.
     */
    static final double FALL = 0.4;
    static final double FALL_HIT = 0.2;
    /**
     * The seconds before the next target comes out, at most (at least the third of it).
     */
    static final double SPAWN_WAIT = 1.2;
    /**
     * A look away shorter than this (seconds) does not stop a fixation: the eye tracker trembles.
     */
    static final double FIXATION_GRACE = 0.3;

    /**
     * The holes of the levels with 9 holes, in staggered rows from far to near: width and height ratios, and size.
     */
    private static final double[][] NINE_HOLES = {
        {0.12, 0.42, 0.82}, {0.44, 0.42, 0.82}, {0.76, 0.42, 0.82},
        {0.28, 0.62, 0.91}, {0.60, 0.62, 0.91}, {0.90, 0.62, 0.91},
        {0.12, 0.85, 1}, {0.44, 0.85, 1}, {0.76, 0.85, 1}};
    private static final double[][] SIX_HOLES = {
        {0.20, 0.48, 1.04}, {0.50, 0.48, 1.04}, {0.80, 0.48, 1.04},
        {0.20, 0.84, 1.15}, {0.50, 0.84, 1.15}, {0.80, 0.84, 1.15}};

    enum Phase { PLAY, OVER }

    enum State { HIDDEN, RISING, UP, FALLING }

    private static final Map<String, Image> GAME_IMAGES = new HashMap<>();

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final MolesTarget target;
    @Getter
    private final MolesLevel level;
    @Getter
    private final MolesHit hit;
    @Getter
    private final MolesEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private int hits;
    @Getter
    private double timeLeft;
    private double spawnWait;
    private Hole lastHole;

    private double width;
    private double height;
    private Group layer;
    private Group effects;
    private Group hud;
    private List<File> userImages = List.of();

    @Getter
    private final List<Hole> holes = new ArrayList<>();
    private final List<TargetAOI> targetAOIList = new ArrayList<>();
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
    Moles(final IGameContext gameContext, final Stats stats, final MolesTarget target, final MolesLevel level,
          final MolesHit hit, final MolesEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        // The limits of score and of time of the settings
        gameContext.startScoreLimiter();
        gameContext.startTimeLimiter();
        this.stats = stats;
        this.target = target;
        this.level = level;
        this.hit = hit;
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

        final Image field = gameImage("field");
        final ImageView background = new ImageView(field);
        // Covers the screen, keeping its proportions
        final double scale = Math.max(width / Math.max(1, field.getWidth()), height / Math.max(1, field.getHeight()));
        background.setFitWidth(field.getWidth() * scale);
        background.setFitHeight(field.getHeight() * scale);
        background.setLayoutX((width - background.getFitWidth()) / 2);
        background.setLayoutY((height - background.getFitHeight()) / 2);
        background.setMouseTransparent(true);

        final Group holesLayer = new Group();
        effects = new Group();
        effects.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        layer = new Group(background, holesLayer, effects, hud);
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();
        gameContext.firstStart();

        if (target == MolesTarget.MY_IMAGES) {
            userImages = UserImagesVariant.images(MolesTarget.USER_IMAGES_FOLDER);
            if (userImages.isEmpty()) {
                // The game started without going through the menu, which asks for images
                final Group message = GameStyle.card(gameContext.getTranslator().translate("NoUserImages"),
                    height * 0.035, width * 0.5);
                message.setLayoutX((width - message.getBoundsInLocal().getWidth()) / 2);
                message.setLayoutY((height - message.getBoundsInLocal().getHeight()) / 2);
                hud.getChildren().add(message);
                return;
            }
        }

        final double size = Math.min(width * 0.17, height * 0.26);
        // From far to near: a nearer hole is drawn over a farther target
        for (final double[] place : level.getHoles() == 6 ? SIX_HOLES : NINE_HOLES) {
            final Hole hole = new Hole(place[0] * width, place[1] * height, size * place[2]);
            holes.add(hole);
            holesLayer.getChildren().add(hole.node);
        }

        hits = 0;
        timeLeft = end.getSeconds();
        spawnWait = 0.5;
        phase = Phase.PLAY;
        showHud();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * @return an image of the game, loaded once
     */
    private static Image gameImage(final String name) {
        return GAME_IMAGES.computeIfAbsent(name, key -> new Image(
            key.startsWith("data/") ? key : "data/whackmole/images/" + key + ".png"));
    }

    /**
     * @return the image of the next target; null if none of the images of the user can be read
     */
    private Image nextImage() {
        return switch (target) {
            case MOLES -> gameImage("mole");
            case BIBOULES -> gameImage("data/shooter/biboule/images/"
                + BIBOULE_COLORS[random.nextInt(BIBOULE_COLORS.length)] + ".png");
            case MY_IMAGES -> {
                Image loaded = null;
                for (int i = 0; i < userImages.size() && (loaded == null || loaded.isError()); i++) {
                    loaded = new Image(userImages.get(random.nextInt(userImages.size())).toURI().toString(), 640, 640,
                        true, true);
                }
                yield loaded;
            }
        };
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase != Phase.PLAY || seconds <= 0) {
            return;
        }
        final double ratio = Math.max(0.1, configuration.getAnimationSpeedRatio());
        for (final Hole hole : holes) {
            hole.step(seconds, ratio);
            if (phase != Phase.PLAY) {
                return;
            }
        }
        if (end.getSeconds() > 0) {
            final int before = (int) Math.ceil(timeLeft);
            timeLeft = Math.max(0, timeLeft - seconds);
            if (timeLeft <= 0) {
                win();
                return;
            }
            if ((int) Math.ceil(timeLeft) != before) {
                showHud();
            }
        }
        spawnWait -= seconds * ratio;
        if (spawnWait <= 0 && targetsOut() < level.getTargets()) {
            sendOut();
            spawnWait = SPAWN_WAIT * (1 + 2 * random.nextDouble()) / 3;
        }
    }

    /**
     * @return the targets coming out, out or going back
     */
    int targetsOut() {
        int count = 0;
        for (final Hole hole : holes) {
            if (hole.state != State.HIDDEN) {
                count++;
            }
        }
        return count;
    }

    /**
     * A target comes out of a hole drawn at random among the empty ones, if possible not the last one used.
     */
    private void sendOut() {
        final List<Hole> empty = new ArrayList<>();
        for (final Hole hole : holes) {
            if (hole.state == State.HIDDEN && hole != lastHole) {
                empty.add(hole);
            }
        }
        if (empty.isEmpty()) {
            return;
        }
        final Hole hole = empty.get(random.nextInt(empty.size()));
        lastHole = hole;
        hole.comeOut();
    }

    /**
     * A target hit: a point, a sound and a star; the game may end.
     */
    private void targetHit(final Hole hole) {
        hits++;
        gameContext.getSoundManager().add("data/whackmole/sounds/hit" + (1 + random.nextInt(2)) + ".wav");
        playStar(hole);
        stats.incrementNumberOfGoalsReached();
        gameContext.updateScore(stats, this);
        if (phase != Phase.PLAY) {
            // The limit of score of the settings has ended the game
            return;
        }
        showHud();
        if (end.getTargets() > 0 && hits >= end.getTargets()) {
            win();
        }
    }

    private void playStar(final Hole hole) {
        final double outer = hole.size * 0.42;
        final double inner = outer * 0.5;
        final Double[] points = new Double[20];
        for (int i = 0; i < 10; i++) {
            final double angle = Math.PI * i / 5 - Math.PI / 2;
            final double radius = i % 2 == 0 ? outer : inner;
            points[2 * i] = radius * Math.cos(angle);
            points[2 * i + 1] = radius * Math.sin(angle);
        }
        final Polygon star = new Polygon();
        star.getPoints().addAll(points);
        star.setFill(Color.rgb(255, 216, 90));
        star.setStroke(Color.WHITE);
        star.setStrokeWidth(Math.max(3, hole.size * 0.025));
        star.setLayoutX(hole.x);
        star.setLayoutY(hole.y - hole.size * 0.3);
        star.setScaleX(0.3);
        star.setScaleY(0.3);
        effects.getChildren().add(star);
        final ScaleTransition pop = new ScaleTransition(Duration.millis(320), star);
        pop.setToX(1.15);
        pop.setToY(1.15);
        final FadeTransition fade = new FadeTransition(Duration.millis(200), star);
        fade.setDelay(Duration.millis(150));
        fade.setToValue(0);
        final ParallelTransition animation = new ParallelTransition(pop, fade);
        running.add(animation);
        animation.setOnFinished(event -> {
            running.remove(animation);
            effects.getChildren().remove(star);
        });
        animation.play();
    }

    private void showHud() {
        hud.getChildren().clear();
        final double fontSize = height * 0.035;
        final double margin = height * 0.025;
        final String score = gameContext.getTranslator().translate("MolesScore") + " " + hits
            + (end.getTargets() > 0 ? " / " + end.getTargets() : "");
        final Group pill = GameStyle.pill(score, fontSize);
        pill.setLayoutX(width - pill.getBoundsInLocal().getWidth() - margin);
        pill.setLayoutY(margin * 0.6);
        hud.getChildren().add(pill);
        if (end.getSeconds() > 0) {
            final int seconds = (int) Math.ceil(timeLeft);
            final Group clock = GameStyle.pill(String.format("%d:%02d", seconds / 60, seconds % 60), fontSize);
            clock.setLayoutX(margin);
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
        holes.forEach(Hole::stopRing);
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

    double getHeight() {
        return height;
    }

    double getWidth() {
        return width;
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        for (final Transition transition : List.copyOf(running)) {
            transition.stop();
        }
        running.clear();
        final long now = System.currentTimeMillis();
        for (final Hole hole : holes) {
            hole.stopRing();
            hole.closeArea(now);
            gameContext.getGazeDeviceManager().removeEventFilter(hole.zone);
        }
        stats.setTargetAOIList(new ArrayList<>(targetAOIList));
        holes.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A hole and its target. The zone catching the gaze and the mouse does not move: it covers the place of the target
     * out of its hole, so that the eyes need not follow the target coming out, and a look already there counts as soon
     * as the target shows. The zones of two holes never meet.
     */
    final class Hole {
        @Getter
        private final double x;
        @Getter
        private final double y;
        @Getter
        private final double size;
        private final double hiddenOffset;
        private final Group node;
        private final Group body;
        private final ImageView view;
        private final Circle frame;
        @Getter
        private final Rectangle zone;
        @Getter
        private final FixationRing ring;

        @Getter
        private State state = State.HIDDEN;
        private double motion;
        private double fallTime;
        private double outSeconds;
        private boolean onGaze;
        private boolean onMouse;
        @Getter
        private double fixationSeconds;
        private double awaySeconds;
        private TargetAOI area;

        Hole(final double x, final double y, final double size) {
            this.x = x;
            this.y = y;
            this.size = size;
            final double holeWidth = size * 1.25;
            final double holeHeight = holeWidth * 0.42;
            hiddenOffset = size + holeHeight;

            final ImageView holeView = new ImageView(gameImage("hole"));
            holeView.setFitWidth(holeWidth);
            holeView.setFitHeight(holeHeight);
            holeView.setX(-holeWidth / 2);
            holeView.setY(-holeHeight / 2);
            holeView.setMouseTransparent(true);

            view = new ImageView();
            view.setSmooth(true);
            frame = new Circle();
            frame.setFill(null);
            frame.setStroke(Color.WHITE);
            frame.setVisible(false);
            body = new Group(view, frame);
            body.setMouseTransparent(true);
            body.setVisible(false);

            // The target shows above the front edge of the hole only: it comes out from behind it
            final Rectangle above = new Rectangle(-size * 2, -size * 3, size * 4, size * 3);
            final Ellipse inside = new Ellipse(0, 0, holeWidth * 0.40, holeHeight * 0.37);
            final Group pit = new Group(body);
            pit.setClip(Shape.union(above, inside));
            pit.setMouseTransparent(true);

            // The width and the top of what is drawn, down to the front edge of the hole; it used to cover the whole width
            // of the hole and more above the target, up to the hole of the row above (maintainer's remark of 4/11/2026)
            final double zoneWidth = size * switch (target) {
                case MOLES -> 0.72;
                case BIBOULES -> 0.9;
                case MY_IMAGES -> 1;
            };
            final double zoneTop = size * switch (target) {
                case MOLES -> -0.78;
                case BIBOULES -> -0.75;
                case MY_IMAGES -> -0.95;
            };
            zone = new Rectangle(-zoneWidth / 2, zoneTop, zoneWidth, -zoneTop + holeHeight * 0.37);
            zone.setFill(Color.TRANSPARENT);
            ring = new FixationRing();
            ring.setSize(size * 1.15);
            node = new Group(holeView, pit, zone, ring);
            node.setLayoutX(x);
            node.setLayoutY(y);

            gameContext.getGazeDeviceManager().addEventFilter(zone);
            final EventHandler<Event> handler = this::onPointer;
            zone.addEventFilter(GazeEvent.ANY, handler);
            zone.addEventFilter(MouseEvent.ANY, handler);
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
         * Puts an image in the target, its bottom just below the edge of the hole, without changing its proportions; an
         * image of the user is cut to a circle with a white border.
         */
        private void fill(final Image image) {
            view.setImage(image);
            final boolean round = target == MolesTarget.MY_IMAGES;
            // A mole goes below the front edge of the hole, which cuts its body along its curve; a Biboule or an image of
            // the user, all face, stays above it
            final double bottom = size * (target == MolesTarget.MOLES ? 0.22 : 0.05);
            if (round) {
                final double side = Math.min(image.getWidth(), image.getHeight());
                view.setViewport(new Rectangle2D((image.getWidth() - side) / 2, (image.getHeight() - side) / 2, side,
                    side));
                view.setFitWidth(size);
                view.setFitHeight(size);
                view.setX(-size / 2);
                view.setY(bottom - size);
                view.setClip(new Circle(0, bottom - size / 2, size / 2));
                frame.setCenterX(0);
                frame.setCenterY(bottom - size / 2);
                frame.setRadius(size / 2);
                frame.setStrokeWidth(Math.max(3, size * 0.03));
                frame.setVisible(true);
            } else {
                final double scale = size / Math.max(1, Math.max(image.getWidth(), image.getHeight()));
                final double shownWidth = image.getWidth() * scale;
                final double shownHeight = image.getHeight() * scale;
                view.setFitWidth(shownWidth);
                view.setFitHeight(shownHeight);
                view.setX(-shownWidth / 2);
                view.setY(bottom - shownHeight);
            }
        }

        void comeOut() {
            final Image image = nextImage();
            if (image == null) {
                return;
            }
            fill(image);
            state = State.RISING;
            motion = 0;
            outSeconds = 0;
            fixationSeconds = 0;
            awaySeconds = 0;
            body.setTranslateY(hiddenOffset);
            body.setVisible(true);
            area = new TargetAOI(x, y, (int) (size / 2), System.currentTimeMillis());
            targetAOIList.add(area);
            stats.incrementNumberOfGoalsToReach();
        }

        void step(final double seconds, final double ratio) {
            switch (state) {
                case RISING -> {
                    motion += seconds * ratio / RISE;
                    if (motion >= 1) {
                        motion = 1;
                        state = State.UP;
                    }
                    body.setTranslateY(hiddenOffset * (1 - easeOut(motion)));
                    targetIsOut(seconds, ratio);
                }
                case UP -> targetIsOut(seconds, ratio);
                case FALLING -> {
                    motion += seconds * ratio / fallTime;
                    if (motion >= 1) {
                        state = State.HIDDEN;
                        body.setVisible(false);
                        frame.setVisible(false);
                    } else {
                        body.setTranslateY(hiddenOffset * motion * motion);
                    }
                }
                default -> {
                    // In its hole
                }
            }
        }

        private double easeOut(final double t) {
            return 1 - (1 - t) * (1 - t);
        }

        /**
         * The target can be hit as soon as it shows; it goes back after the time of the fixation, read at each step, so
         * that a change in the menu of the gear counts at once, and the time of the level.
         */
        private void targetIsOut(final double seconds, final double ratio) {
            outSeconds += seconds;
            final double length = configuration.getFixationLength() / 1000.0;
            final boolean withFixation = hit == MolesHit.FIXATION;
            if (onGaze || onMouse) {
                awaySeconds = 0;
                if (!withFixation) {
                    fallAfterHit();
                    return;
                }
                if (!ring.isVisible()) {
                    ring.show(0, -size * 0.3);
                }
                fixationSeconds += seconds;
                ring.setProgress(length <= 0 ? 1 : fixationSeconds / length);
                if (fixationSeconds >= length) {
                    fallAfterHit();
                    return;
                }
            } else if (fixationSeconds > 0) {
                awaySeconds += seconds;
                if (awaySeconds > FIXATION_GRACE) {
                    fixationSeconds = 0;
                    ring.stop();
                }
            }
            // A fixation under way is not cut short: the target waits for its end
            if (outSeconds >= (RISE + level.getStay()) / ratio + (withFixation ? length : 0)
                && fixationSeconds == 0) {
                fall(FALL);
            }
        }

        private void fallAfterHit() {
            fall(FALL_HIT);
            targetHit(this);
        }

        private void fall(final double time) {
            stopRing();
            closeArea(System.currentTimeMillis());
            state = State.FALLING;
            fallTime = time;
            motion = 0;
        }

        void closeArea(final long now) {
            if (area != null) {
                area.setTimeEnded(now);
                area = null;
            }
        }

        void stopRing() {
            ring.stop();
        }

        boolean isTargetShown() {
            return body.isVisible();
        }
    }
}

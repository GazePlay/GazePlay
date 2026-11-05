package net.gazeplay.games.shooter;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GameStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * The shooting game (reviewed on 2/11/2026, maintainer's rules; it was "ShootTheBiboules" and "ShootTheRobots"): a
 * cage (Biboules, in a classroom) or a flying saucer (robots, above a street) goes to and fro at the top; targets come
 * out of it one by one ({@value #TARGETS} at a time), change color and grow while flying to a place of the screen.
 * A target is shot as soon as the gaze (or the mouse) enters it, or after a fixation, as chosen in the variants: the
 * wand or the gun at the bottom turns to it, a flash, a sound, one more point. The game ends never, after 50 or 100
 * targets, or after 1 or 3 minutes (then "Bravo" and the statistics).
 * <p>
 * One loop moves everything in real time, at the animation speed of the settings; nothing moves and the time does not
 * run while the menu of the game is open.
 */
@Slf4j
public class ShooterGame implements GameLifeCycle {

    static final int TARGETS = 5;

    /**
     * Time a target takes to fly to its place, at the normal speed, in seconds (drawn between both).
     */
    static final double SLOWEST = 7;
    static final double FASTEST = 4;

    /**
     * How much a target grows on its way (it used to grow 10 times, hiding the screen).
     */
    static final double GROWTH = 2.6;

    static final String[] COLORS = {"Blue", "Green", "Yellow", "Orange", "Red"};

    enum Phase { PLAY, OVER }

    private final IGameContext gameContext;
    private final Stats stats;
    @Getter
    private final ShooterTheme theme;
    @Getter
    private final ShooterShot shot;
    @Getter
    private final ShooterEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;

    @Getter
    private int score;

    /**
     * Time left, in seconds, for a game ended by a time.
     */
    @Getter
    private double timeLeft;

    private double width;
    private double height;
    private double time;

    private Group layer;
    private Group targetsLayer;
    private Group hud;
    private ImageView box;
    private Group gun;
    private ImageView gunShot;
    private ImageView background;

    private final Image[] frames = new Image[COLORS.length];
    private Image flash;

    @Getter
    private final List<Target> targets = new ArrayList<>();

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

    public ShooterGame(final IGameContext gameContext, final Stats stats, final ShooterTheme theme,
                       final ShooterShot shot, final ShooterEnd end) {
        this(gameContext, stats, theme, shot, end, -1);
    }

    /**
     * @param gameSeed the seed of the game to replay, or a negative value for a new game
     */
    public ShooterGame(final IGameContext gameContext, final Stats stats, final ShooterTheme theme,
                       final ShooterShot shot, final ShooterEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.theme = theme;
        this.shot = shot;
        this.end = end;
        if (gameSeed < 0) {
            random = new ReplayablePseudoRandom();
            stats.setGameSeed(random.getSeed());
        } else {
            random = new ReplayablePseudoRandom(gameSeed);
        }
    }

    private String path(final String name) {
        return "data/shooter/" + theme.getFolder() + "/" + name;
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        width = dimension.getWidth();
        height = dimension.getHeight();
        for (int i = 0; i < COLORS.length; i++) {
            frames[i] = new Image(path("images/" + COLORS[i] + ".png"));
        }
        flash = new Image(path("images/Flash.png"));

        background = new ImageView(new Image(path("images/Background.png")));
        background.setFitWidth(width);
        background.setFitHeight(height);

        box = new ImageView(new Image(path("images/Cage.png")));
        box.setPreserveRatio(true);
        box.setFitHeight(height * (theme == ShooterTheme.BIBOULES ? 0.2 : 0.13));
        box.setMouseTransparent(true);

        // The wand or the gun: its picture is centered on the bottom of the screen, so that it turns around it
        final double gunHeight = height * 0.9;
        final ImageView gunView = new ImageView(new Image(path("images/hand.png")));
        gunView.setPreserveRatio(true);
        gunView.setFitHeight(gunHeight);
        gunShot = new ImageView(new Image(path("images/handShot.png")));
        gunShot.setPreserveRatio(true);
        gunShot.setFitHeight(gunHeight);
        gunShot.setOpacity(0);
        gun = new Group(gunView, gunShot);
        gun.setMouseTransparent(true);
        gun.setLayoutX(width / 2 - gunView.getBoundsInLocal().getWidth() / 2);
        gun.setLayoutY(height - gunHeight / 2);

        targetsLayer = new Group();
        hud = new Group();
        hud.setMouseTransparent(true);
        layer = new Group(background, box, targetsLayer, gun, hud);
        gameContext.getChildren().add(layer);

        // The wand or the gun follows the gaze and the mouse
        gameContext.getGazeDeviceManager().addEventFilter(background);
        background.addEventFilter(GazeEvent.ANY, aimHandler);
        layer.addEventFilter(MouseEvent.MOUSE_MOVED, aimHandler);

        score = 0;
        timeLeft = end.getSeconds();
        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();
        gameContext.firstStart();
        phase = Phase.PLAY;
        placeBox();
        for (int i = 0; i < TARGETS; i++) {
            final Target target = new Target();
            targets.add(target);
            targetsLayer.getChildren().add(target.node);
            target.start(i * 0.8);
        }
        showScore();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * Turns the wand or the gun towards the gaze or the mouse.
     */
    private void aim(final Event event) {
        double x;
        double y;
        if (event instanceof GazeEvent gaze && (gaze.getEventType() == GazeEvent.GAZE_MOVED
            || gaze.getEventType() == GazeEvent.GAZE_ENTERED)) {
            x = gaze.getX();
            y = gaze.getY();
        } else if (event instanceof MouseEvent mouse) {
            x = mouse.getX();
            y = mouse.getY();
        } else {
            return;
        }
        aimAt(x, y);
    }

    private void aimAt(final double x, final double y) {
        final double angle = Math.toDegrees(Math.atan2(x - width / 2, height - y));
        gun.setRotate(Math.max(-75, Math.min(75, angle)));
    }

    /**
     * The cage (or the saucer) goes to and fro at the top, a little up and down.
     */
    private void placeBox() {
        final double boxWidth = box.getBoundsInLocal().getWidth();
        final double centerX = width / 2 + Math.sin(time * 0.35) * (width / 2 - boxWidth);
        final double top = height * (theme == ShooterTheme.BIBOULES ? 0.12 : 0.03);
        box.setLayoutX(centerX - boxWidth / 2);
        box.setLayoutY(top + Math.sin(time * 0.9) * height * 0.03);
    }

    private void showScore() {
        hud.getChildren().clear();
        String text = gameContext.getTranslator().translate("ShooterScore") + " " + score;
        if (end.getTargets() > 0) {
            text += " / " + end.getTargets();
        }
        final Group pill = GameStyle.pill(text, height * 0.035);
        pill.setLayoutX(width - pill.getBoundsInLocal().getWidth() - height * 0.03);
        pill.setLayoutY(height * 0.025);
        hud.getChildren().add(pill);
        if (end.getSeconds() > 0) {
            final int seconds = (int) Math.ceil(timeLeft);
            final Group clock = GameStyle.pill(String.format("%d:%02d", seconds / 60, seconds % 60), height * 0.035);
            clock.setLayoutX(height * 0.03);
            clock.setLayoutY(height * 0.025);
            hud.getChildren().add(clock);
        }
    }

    /**
     * One step of the game, of a duration in seconds: the box and the targets move, the time runs.
     */
    void step(final double seconds) {
        if (phase != Phase.PLAY || seconds <= 0) {
            return;
        }
        final double ratio = Math.max(0.1, gameContext.getConfiguration().getAnimationSpeedRatio());
        time += seconds * ratio;
        placeBox();
        for (final Target target : targets) {
            target.move(seconds * ratio);
        }
        if (end.getSeconds() > 0) {
            final int before = (int) Math.ceil(timeLeft);
            timeLeft = Math.max(0, timeLeft - seconds);
            if ((int) Math.ceil(timeLeft) != before) {
                showScore();
            }
            if (timeLeft <= 0) {
                finish();
            }
        }
    }

    /**
     * A target shot: one more point, the wand or the gun flashes, a sound; the end reached, "Bravo" then the
     * statistics.
     */
    void shoot(final Target target) {
        if (phase != Phase.PLAY || target.shot) {
            return;
        }
        target.shot = true;
        target.stopFixation();
        score++;
        stats.incrementNumberOfGoalsReached();
        gameContext.updateScore(stats, this);
        gameContext.getSoundManager().add(path("sounds/shot" + (1 + random.nextInt(3)) + ".wav"));
        aimAt(target.x, target.y);
        gunShot.setOpacity(1);
        final FadeTransition fire = new FadeTransition(Duration.millis(450), gunShot);
        fire.setToValue(0);
        fire.play();
        target.burst();
        showScore();
        if (end.getTargets() > 0 && score >= end.getTargets()) {
            finish();
        }
    }

    private void finish() {
        if (phase != Phase.PLAY) {
            return;
        }
        phase = Phase.OVER;
        loop.stop();
        targets.forEach(Target::stopFixation);
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

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        for (final Target target : targets) {
            target.dispose();
        }
        targets.clear();
        if (background != null) {
            gameContext.getGazeDeviceManager().removeEventFilter(background);
        }
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A target: it comes out of the box, changes color and grows while flying to a place of the screen; not shot when
     * it gets there, it fades and comes out again.
     */
    final class Target {
        @Getter
        private double x;
        @Getter
        private double y;
        private double fromX;
        private double fromY;
        private double toX;
        private double toY;
        private double duration;
        private double elapsed;
        private double wait;
        private boolean shot;
        private final ImageView view = new ImageView();
        private final Circle zone = new Circle();
        private final Arc ring;
        private final Group node;
        private final DoubleProperty progress = new SimpleDoubleProperty(0);
        private Timeline fixation;
        private final EventHandler<Event> handler = this::handle;
        private final double baseSize = height * 0.075;

        Target() {
            view.setPreserveRatio(true);
            view.setMouseTransparent(true);
            zone.setFill(Color.TRANSPARENT);
            ring = new Arc(0, 0, 10, 10, 90, 0);
            ring.setType(ArcType.OPEN);
            ring.setFill(null);
            ring.setStrokeLineCap(StrokeLineCap.ROUND);
            ring.setStyle("-fx-stroke: -gp-accent;");
            ring.setMouseTransparent(true);
            progress.addListener((observable, oldValue, newValue) -> ring.setLength(-360 * newValue.doubleValue()));
            node = new Group(zone, view, ring);
            gameContext.getGazeDeviceManager().addEventFilter(node);
            node.addEventFilter(GazeEvent.ANY, handler);
            node.addEventFilter(MouseEvent.ANY, handler);
        }

        private void handle(final Event event) {
            if (event.getEventType() == GazeEvent.GAZE_ENTERED || event.getEventType() == MouseEvent.MOUSE_ENTERED) {
                if (shot || wait > 0) {
                    return;
                }
                if (ShooterGame.this.shot == ShooterShot.AT_ONCE) {
                    shoot(this);
                } else {
                    startFixation();
                }
            } else if (event.getEventType() == GazeEvent.GAZE_EXITED
                || event.getEventType() == MouseEvent.MOUSE_EXITED) {
                stopFixation();
            }
        }

        private void startFixation() {
            stopFixation();
            fixation = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(progress, 0)),
                new KeyFrame(Duration.millis(gameContext.getConfiguration().getFixationLength()),
                    new KeyValue(progress, 1)));
            fixation.setOnFinished(event -> shoot(this));
            fixation.playFromStart();
        }

        void stopFixation() {
            if (fixation != null) {
                fixation.stop();
                fixation = null;
            }
            progress.set(0);
        }

        /**
         * Comes out of the box again, after a delay in seconds.
         */
        void start(final double delay) {
            stopFixation();
            shot = false;
            wait = delay;
            elapsed = 0;
            fromX = box.getLayoutX() + box.getBoundsInLocal().getWidth() / 2;
            fromY = box.getLayoutY() + box.getBoundsInLocal().getHeight() / 2;
            // To a place of the screen, not under the wand nor the score
            final double margin = baseSize * GROWTH / 2;
            toX = margin + (width - 2 * margin) * random.nextDouble();
            toY = height * 0.18 + (height * 0.55) * random.nextDouble();
            duration = FASTEST + (SLOWEST - FASTEST) * random.nextDouble();
            x = fromX;
            y = fromY;
            view.setImage(frames[0]);
            node.setOpacity(1);
            node.setScaleX(1);
            node.setScaleY(1);
            node.setVisible(delay <= 0);
            size(0);
        }

        /**
         * The size of the target along its way (0 to 1).
         */
        private void size(final double along) {
            final double size = baseSize * (0.5 + (GROWTH - 0.5) * along);
            view.setFitWidth(size * 1.25);
            view.setFitHeight(size);
            view.setLayoutX(-size * 1.25 / 2);
            view.setLayoutY(-size / 2);
            zone.setRadius(size * 0.6);
            ring.setRadiusX(size * 0.7);
            ring.setRadiusY(size * 0.7);
            ring.setStrokeWidth(Math.max(4, size * 0.08));
            node.setLayoutX(x);
            node.setLayoutY(y);
        }

        void move(final double seconds) {
            if (shot) {
                return;
            }
            if (wait > 0) {
                wait -= seconds;
                if (wait <= 0) {
                    start(0);
                }
                return;
            }
            if (fixation != null) {
                // Looked at, with the fixation: it waits, so that the fixation can end
                return;
            }
            elapsed += seconds;
            final double along = Math.min(1, elapsed / duration);
            final double eased = 1 - (1 - along) * (1 - along);
            x = fromX + (toX - fromX) * eased;
            y = fromY + (toY - fromY) * eased;
            view.setImage(frames[Math.min(frames.length - 1, (int) (along * frames.length))]);
            size(along);
            if (along >= 1) {
                // Not shot: it fades, then comes out again
                shot = true;
                stopFixation();
                final FadeTransition fade = new FadeTransition(Duration.millis(400), node);
                fade.setToValue(0);
                fade.setOnFinished(event -> {
                    if (phase == Phase.PLAY) {
                        stats.incrementNumberOfGoalsToReach();
                        start(0.3);
                    }
                });
                fade.play();
            }
        }

        /**
         * Shot: a flash, it pops and fades, then comes out again.
         */
        void burst() {
            stats.incrementNumberOfGoalsToReach();
            view.setImage(flash);
            final ScaleTransition pop = new ScaleTransition(Duration.millis(250), node);
            pop.setToX(1.4);
            pop.setToY(1.4);
            final FadeTransition fade = new FadeTransition(Duration.millis(400), node);
            fade.setToValue(0);
            fade.setOnFinished(event -> {
                if (phase == Phase.PLAY) {
                    start(0.4);
                }
            });
            pop.play();
            fade.play();
        }

        /**
         * @return the node of the target (for the tests)
         */
        Group node() {
            return node;
        }

        void dispose() {
            stopFixation();
            gameContext.getGazeDeviceManager().removeEventFilter(node);
        }
    }
}

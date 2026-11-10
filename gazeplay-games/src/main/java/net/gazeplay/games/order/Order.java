package net.gazeplay.games.order;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Text;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * "Ordre" (Order; reviewed on 10/11/2026, maintainer's rules): targets, all alike, appear one after the other at places
 * drawn at random, then stay; the player fixates them in the order they appeared. A right target shows its number and
 * a check; a wrong one a cross, then every target shows its number (the right answer) for 2 seconds, one life is lost,
 * and a new round comes. Three lives (hearts); 3, 5 or 7 targets, or progressive (3, one more after each round won);
 * the game ends after 3, 5 or 10 rounds won ("Bravo"), or never, or when the lives are lost ("Lost!").
 * <p>
 * Everything is drawn by the game. One loop, in real seconds: the targets appear at the speed of the settings, the
 * fixations last the length of the settings (read at each step), nothing can be chosen before the delay of the
 * settings nor while the targets appear, and nothing moves while the menu of the game is open. (Before the review:
 * targets that could overlap, no end, no lives, the right answer never shown, the former progress indicator, the time
 * limiter, 3 pictures of unknown source.)
 */
public class Order implements GameLifeCycle {

    static final int LIVES = 3;
    /**
     * In seconds (the appearing at the speed of the settings): before the first target, between two targets, the
     * right answer shown after a mistake, a round won, the end shown.
     */
    static final double BEFORE = 0.8;
    static final double APPEAR = 1.0;
    static final double REVEAL = 2.0;
    static final double WON = 1.2;
    static final double SHOW = 2.2;

    private static final String SOUNDS = "data/order/sounds/";
    private static final Color BACKGROUND = Color.rgb(232, 236, 244);
    private static final Color TARGET = Color.rgb(242, 100, 58);
    private static final Color RIGHT = Color.web(GameStyle.SUCCESS_COLOR);
    private static final Color WRONG = Color.rgb(214, 64, 64);

    enum Phase { WAIT, APPEAR, PLAY, REVEAL, WON, SHOW, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final OrderLevel level;
    @Getter
    private final OrderEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private int count;
    @Getter
    private final List<Target> targets = new ArrayList<>();
    /**
     * The number (from 0) of the next target to fixate.
     */
    @Getter
    private int next;
    @Getter
    private int lives;
    @Getter
    private int won;
    private boolean bravo;
    private double elapsed;
    @Getter
    private double waiting;

    private double width;
    private double height;
    @Getter
    private double radius;
    private double top;

    private Group layer;
    private Group targetsLayer;
    private FixationRing ring;
    private Group hud;
    private Group message;
    private final List<Node> gazeNodes = new ArrayList<>();

    private Target looked;
    @Getter
    private double progress;

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
    Order(final IGameContext gameContext, final Stats stats, final OrderLevel level, final OrderEnd end,
          final double gameSeed) {
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
        radius = Math.min(width, height) * 0.065;
        top = height * 0.12;

        final Rectangle background = new Rectangle(0, 0, width, height);
        background.setFill(BACKGROUND);
        background.setMouseTransparent(true);
        targetsLayer = new Group();
        ring = new FixationRing();
        hud = new Group();
        hud.setMouseTransparent(true);
        message = new Group();
        message.setMouseTransparent(true);
        layer = new Group(background, targetsLayer, ring, hud, message);
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addStats(stats);
        lives = LIVES;
        won = 0;
        count = level.getTargets();
        elapsed = 0;
        waiting = configuration.getGameStartDelay() / 1000.0;
        phase = Phase.WAIT;
        showHud();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * A new round: the places of its targets drawn at random, far enough from each other; then they appear.
     */
    private void newRound() {
        targets.forEach(target -> forget(target.node));
        targets.clear();
        targetsLayer.getChildren().clear();
        message.getChildren().clear();
        looked = null;
        progress = 0;
        ring.stop();
        final List<double[]> places = places(count);
        for (int i = 0; i < count; i++) {
            final Target target = new Target(i, places.get(i)[0], places.get(i)[1]);
            targets.add(target);
        }
        next = 0;
        elapsed = 0;
        phase = Phase.APPEAR;
        showHud();
    }

    /**
     * @return places for targets, at least 2.6 radii apart (no overlap, room for the ring), inside the screen under the
     * band of the hearts and the score
     */
    List<double[]> places(final int number) {
        final double margin = radius * 1.3;
        final List<double[]> places = new ArrayList<>();
        int attempts = 0;
        while (places.size() < number) {
            final double x = margin + random.nextDouble() * (width - 2 * margin);
            final double y = top + margin + random.nextDouble() * (height - top - 2 * margin);
            boolean far = true;
            for (final double[] place : places) {
                if (Math.hypot(place[0] - x, place[1] - y) < radius * 2.6) {
                    far = false;
                    break;
                }
            }
            attempts++;
            if (far) {
                places.add(new double[]{x, y});
            } else if (attempts > 5000) {
                // Never on a screen: start again
                places.clear();
                attempts = 0;
            }
        }
        return places;
    }

    /**
     * Makes a node receive the gaze and the mouse.
     */
    private void listen(final Node node, final Runnable enter, final Runnable exit) {
        final EventHandler<Event> handler = event -> {
            if (event.getEventType() == MouseEvent.MOUSE_ENTERED || event.getEventType() == GazeEvent.GAZE_ENTERED) {
                enter.run();
            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                || event.getEventType() == GazeEvent.GAZE_EXITED) {
                exit.run();
            }
        };
        node.addEventFilter(MouseEvent.ANY, handler);
        node.addEventFilter(GazeEvent.ANY, handler);
        gameContext.getGazeDeviceManager().addEventFilter(node);
        gazeNodes.add(node);
    }

    private void forget(final Node node) {
        gameContext.getGazeDeviceManager().removeEventFilter(node);
        gazeNodes.remove(node);
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        final double scaled = seconds * Math.max(0.1, configuration.getAnimationSpeedRatio());
        targets.forEach(target -> target.grow(seconds));
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= waiting) {
                    newRound();
                }
            }
            case APPEAR -> {
                elapsed += scaled;
                final int shown = (int) Math.floor((elapsed - BEFORE) / APPEAR) + 1;
                for (int i = 0; i < Math.min(shown, count); i++) {
                    targets.get(i).show();
                }
                if (elapsed >= BEFORE + (count - 1) * APPEAR + APPEAR * 0.5) {
                    phase = Phase.PLAY;
                    stats.notifyNewRoundReady();
                    stats.incrementNumberOfGoalsToReach();
                    showHud();
                }
            }
            case PLAY -> fixate(seconds);
            case REVEAL -> {
                elapsed += seconds;
                if (elapsed >= REVEAL) {
                    if (lives <= 0) {
                        bravo = false;
                        endGame("OrderLost");
                    } else {
                        newRound();
                    }
                }
            }
            case WON -> {
                elapsed += seconds;
                if (elapsed >= WON) {
                    if (end.getRounds() > 0 && won >= end.getRounds()) {
                        bravo = true;
                        finish();
                    } else {
                        if (level == OrderLevel.PROGRESSIVE) {
                            count = Math.min(OrderLevel.MOST, count + 1);
                        }
                        newRound();
                    }
                }
            }
            case SHOW -> {
                elapsed += seconds;
                if (elapsed >= SHOW) {
                    finish();
                }
            }
            default -> {
            }
        }
    }

    private void fixate(final double seconds) {
        if (looked == null || looked.found) {
            return;
        }
        if (!ring.isVisible()) {
            // Looked at since it appeared: the ring shows now that it can be chosen
            ring.setSize(radius * 2.5);
            ring.show(looked.x, looked.y);
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            final Target target = looked;
            progress = 0;
            ring.stop();
            choose(target);
        }
    }

    void lookAt(final Target target) {
        if (looked == target) {
            return;
        }
        looked = target;
        progress = 0;
        ring.stop();
        if (phase == Phase.PLAY && !target.found) {
            ring.setSize(radius * 2.5);
            ring.show(target.x, target.y);
        }
    }

    void lookAway(final Target target) {
        if (looked == target) {
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    /**
     * A target chosen: the next one in the order shows its number and a check; another one shows a cross, then every
     * target its number, and one life is lost.
     */
    void choose(final Target target) {
        if (phase != Phase.PLAY || target.found) {
            return;
        }
        looked = null;
        if (target.index == next) {
            target.markRight();
            next++;
            gameContext.getSoundManager().add(SOUNDS + "right.wav");
            if (next == count) {
                won++;
                stats.incrementNumberOfGoalsReached();
                phase = Phase.WON;
                elapsed = 0;
                showHud();
            }
            return;
        }
        target.markWrong();
        gameContext.getSoundManager().add(SOUNDS + "wrong.wav");
        for (final Target other : targets) {
            if (!other.found) {
                other.showNumber();
            }
        }
        lives--;
        phase = Phase.REVEAL;
        elapsed = 0;
        showHud();
    }

    private void endGame(final String key) {
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate(key), height * 0.05);
        pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY((height - pill.getBoundsInLocal().getHeight()) / 2);
        message.getChildren().setAll(pill);
        phase = Phase.SHOW;
        elapsed = 0;
        showHud();
    }

    private void finish() {
        phase = Phase.OVER;
        loop.stop();
        stats.takeScreenShot();
        if (bravo) {
            gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
        } else {
            gameContext.showRoundStats(stats, this);
        }
    }

    boolean isBravo() {
        return bravo;
    }

    private void showHud() {
        hud.getChildren().clear();
        final double fontSize = height * 0.035;
        final double margin = height * 0.025;
        final Group hearts = GameStyle.lives(lives, LIVES, height * 0.055, width * 0.3, false);
        hearts.setLayoutX(margin);
        hearts.setLayoutY(margin * 0.6);
        hud.getChildren().add(hearts);
        final Group rounds = GameStyle.pill(gameContext.getTranslator().translate("OrderRounds") + " " + won
            + (end.getRounds() > 0 ? " / " + end.getRounds() : ""), fontSize);
        rounds.setLayoutX((width - rounds.getBoundsInLocal().getWidth()) / 2);
        rounds.setLayoutY(margin * 0.6);
        hud.getChildren().add(rounds);
        final Group targetCount = GameStyle.pill(gameContext.getTranslator().translate("OrderTargets") + " " + count,
            fontSize);
        targetCount.setLayoutX(width - targetCount.getBoundsInLocal().getWidth() - margin);
        targetCount.setLayoutY(margin * 0.6);
        hud.getChildren().add(targetCount);
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
        if (ring != null) {
            ring.stop();
        }
        new ArrayList<>(gazeNodes).forEach(this::forget);
        targets.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A target: rings of the main color, all alike; it grows when it appears, then shows its number with a check
     * (right), a cross (wrong), or its number alone (the right answer after a mistake).
     */
    final class Target {
        @Getter
        private final int index;
        @Getter
        private final double x;
        @Getter
        private final double y;
        private final Group node = new Group();
        private final Group face = new Group();
        @Getter
        private boolean shown;
        @Getter
        private boolean found;
        private double grown;

        Target(final int index, final double x, final double y) {
            this.index = index;
            this.x = x;
            this.y = y;
            final Circle outer = new Circle(radius, TARGET);
            outer.setStroke(TARGET.deriveColor(0, 1, 0.7, 1));
            outer.setStrokeWidth(Math.max(2, radius * 0.06));
            final Circle middle = new Circle(radius * 0.66, Color.WHITE);
            final Circle inner = new Circle(radius * 0.33, TARGET);
            face.getChildren().addAll(outer, middle, inner);
            node.getChildren().add(face);
            node.setLayoutX(x);
            node.setLayoutY(y);
            node.setVisible(false);
            node.setScaleX(0);
            node.setScaleY(0);
            targetsLayer.getChildren().add(node);
        }

        void show() {
            if (shown) {
                return;
            }
            shown = true;
            node.setVisible(true);
            listen(node, () -> lookAt(this), () -> lookAway(this));
            gameContext.getSoundManager().add(SOUNDS + "appear.wav");
        }

        void grow(final double seconds) {
            if (!shown || grown >= 1) {
                return;
            }
            grown = Math.min(1, grown + seconds / 0.25);
            // A small bounce at the end
            final double scale = grown < 0.8 ? grown / 0.8 * 1.1 : 1.1 - (grown - 0.8) / 0.2 * 0.1;
            node.setScaleX(scale);
            node.setScaleY(scale);
        }

        void markRight() {
            found = true;
            face.getChildren().setAll(disc(RIGHT), number(Color.WHITE), check());
        }

        void markWrong() {
            found = true;
            // Its number stays seen, with a cross in a corner: the right answer is then complete
            face.getChildren().setAll(disc(WRONG), number(Color.WHITE), cross());
        }

        void showNumber() {
            face.getChildren().setAll(disc(Color.rgb(90, 96, 112)), number(Color.WHITE));
        }

        /**
         * @return whether the target shows its number
         */
        boolean showsNumber() {
            return face.getChildren().stream().anyMatch(child -> child instanceof Text);
        }

        private Circle disc(final Color color) {
            final Circle disc = new Circle(radius, color);
            disc.setStroke(Color.WHITE);
            disc.setStrokeWidth(Math.max(2, radius * 0.08));
            return disc;
        }

        private Text number(final Color color) {
            final Text text = new Text(Integer.toString(index + 1));
            text.setFont(GameStyle.titleFont(radius * 1.05));
            text.setFill(color);
            text.setTextOrigin(VPos.CENTER);
            text.setX(-text.getLayoutBounds().getWidth() / 2);
            text.setY(-radius * 0.05);
            return text;
        }

        private Group check() {
            final Circle badge = new Circle(radius * 0.72, -radius * 0.72, radius * 0.34, Color.WHITE);
            final Polyline mark = new Polyline(radius * 0.56, -radius * 0.72, radius * 0.68, -radius * 0.6,
                radius * 0.9, -radius * 0.86);
            mark.setStroke(RIGHT);
            mark.setStrokeWidth(radius * 0.1);
            mark.setStrokeLineCap(StrokeLineCap.ROUND);
            mark.setStrokeLineJoin(StrokeLineJoin.ROUND);
            return new Group(badge, mark);
        }

        private Group cross() {
            final Circle badge = new Circle(radius * 0.72, -radius * 0.72, radius * 0.34, Color.WHITE);
            final double a = radius * 0.13;
            final double cx = radius * 0.72;
            final double cy = -radius * 0.72;
            final Polyline cross = new Polyline(cx - a, cy - a, cx + a, cy + a, cx, cy, cx + a, cy - a, cx - a, cy + a);
            cross.setStroke(WRONG);
            cross.setStrokeWidth(radius * 0.1);
            cross.setStrokeLineCap(StrokeLineCap.ROUND);
            cross.setStrokeLineJoin(StrokeLineJoin.ROUND);
            return new Group(badge, cross);
        }
    }
}

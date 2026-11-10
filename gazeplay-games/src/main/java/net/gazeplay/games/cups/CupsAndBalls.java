package net.gazeplay.games.cups;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * "Gobelets Pass Pass" (Cups and Balls, name code CupsBalls; reviewed on 10/11/2026, maintainer's rules): cups in a row
 * on a table; one is lifted to show the ball under it, then they are exchanged two by two, one passing in front of
 * the other in an arc. The player fixates the cup hiding the ball: the right one lifts and shows it; a wrong one lifts
 * empty, then the right one shows the ball, and a life is lost. 3, 4 or 5 cups, more exchanges and faster by the
 * level; three lives (hearts); the game ends after 3, 5 or 10 balls found ("Bravo"), or never, or when the lives are
 * lost ("Lost!"). The places of the ball are given to the statistics as areas of interest (where the gaze should
 * follow), as before.
 * <p>
 * Everything is drawn by the game. One loop, in real seconds: the cups move at the speed of the settings, the
 * fixations last the length of the settings (read at each step), nothing can be chosen before the delay of the
 * settings nor while the cups move, and nothing moves while the menu of the game is open. (Before the review: a wrong
 * cup could be followed by another one until the ball was found, so the game could not be lost; no end; small cups
 * sliding on a hidden grid; the former progress indicator, almost invisible; the limiters; a picture of unknown
 * source.)
 */
public class CupsAndBalls implements GameLifeCycle {

    static final int LIVES = 3;
    /**
     * In seconds (at the speed of the settings): the cup lifted to show the ball (up, still, down), the silence before
     * the exchanges, the end of a round shown, the end shown.
     */
    static final double LIFT = 0.45;
    static final double HOLD = 0.9;
    static final double BEFORE = 0.4;
    static final double REVEAL = 1.8;
    static final double SHOW = 2.2;

    private static final String SOUNDS = "data/cups/sounds/";
    private static final Color BACKGROUND = Color.rgb(232, 236, 244);
    private static final Color CUP = Color.rgb(214, 66, 66);
    private static final Color CUP_DARK = Color.rgb(150, 40, 40);

    enum Phase { WAIT, SHOW_BALL, EXCHANGE, PLAY, REVEAL, SHOW, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final CupsLevel level;
    @Getter
    private final CupsEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private final List<Cup> cups = new ArrayList<>();
    @Getter
    private Cup ballCup;
    @Getter
    private int exchangesDone;
    private Cup moving1;
    private Cup moving2;
    @Getter
    private int lives;
    @Getter
    private int found;
    private boolean bravo;
    private Cup chosen;
    private double elapsed;
    @Getter
    private double waiting;

    private double width;
    private double height;
    @Getter
    private double cupWidth;
    private double cupHeight;
    private double tableY;
    private double[] slotX;

    private Group layer;
    private Group cupsLayer;
    private Circle ball;
    private FixationRing ring;
    private Group hud;
    private Group message;
    private final List<Node> gazeNodes = new ArrayList<>();
    @Getter
    private final ArrayList<TargetAOI> targetAOIList = new ArrayList<>();

    private Cup looked;
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
    CupsAndBalls(final IGameContext gameContext, final Stats stats, final CupsLevel level, final CupsEnd end,
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
        final int count = level.getCups();
        cupWidth = Math.min(width * 0.86 / (count * 1.35), height * 0.26);
        cupHeight = cupWidth * 1.1;
        tableY = height * 0.7;
        slotX = new double[count];
        final double gap = (width - count * cupWidth) / (count + 1);
        for (int slot = 0; slot < count; slot++) {
            slotX[slot] = gap + slot * (cupWidth + gap) + cupWidth / 2;
        }

        final Rectangle background = new Rectangle(0, 0, width, height);
        background.setFill(BACKGROUND);
        final Rectangle table = new Rectangle(0, tableY, width, height - tableY);
        table.setFill(Color.rgb(196, 150, 104));
        final Rectangle edge = new Rectangle(0, tableY, width, height * 0.02);
        edge.setFill(Color.rgb(166, 120, 78));
        final Group ground = new Group(background, table, edge);
        ground.setMouseTransparent(true);
        ball = new Circle(cupWidth * 0.18, Color.rgb(250, 204, 50));
        ball.setStroke(Color.rgb(200, 150, 30));
        ball.setStrokeWidth(Math.max(2, cupWidth * 0.025));
        ball.setMouseTransparent(true);
        ball.setVisible(false);
        cupsLayer = new Group();
        ring = new FixationRing();
        hud = new Group();
        hud.setMouseTransparent(true);
        message = new Group();
        message.setMouseTransparent(true);
        layer = new Group(ground, ball, cupsLayer, ring, hud, message);
        gameContext.getChildren().add(layer);
        for (int slot = 0; slot < count; slot++) {
            final Cup cup = new Cup(slot);
            cups.add(cup);
            cupsLayer.getChildren().add(cup.node);
            listen(cup.node, () -> lookAt(cup), () -> lookAway(cup));
        }

        gameContext.getGazeDeviceManager().addStats(stats);
        lives = LIVES;
        found = 0;
        elapsed = 0;
        waiting = configuration.getGameStartDelay() / 1000.0;
        phase = Phase.WAIT;
        showHud();
        if (!manual) {
            loop.start();
        }
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

    /**
     * A new round: the ball under a cup drawn at random, shown by lifting it.
     */
    private void newRound() {
        message.getChildren().clear();
        cups.forEach(cup -> cup.lift = 0);
        ballCup = cups.get(random.nextInt(cups.size()));
        exchangesDone = 0;
        chosen = null;
        looked = null;
        progress = 0;
        ring.stop();
        placeAll();
        startAoi();
        stats.notifyNewRoundReady();
        phase = Phase.SHOW_BALL;
        elapsed = 0;
        showHud();
    }

    /**
     * The place of the ball (under its cup) starts an area of interest; the last one ends.
     */
    private void startAoi() {
        endAoi();
        final long now = System.currentTimeMillis();
        targetAOIList.add(new TargetAOI(ballCup.x, tableY - cupHeight / 2, (int) ((cupWidth + cupHeight) / 3), now));
    }

    private void endAoi() {
        if (!targetAOIList.isEmpty() && targetAOIList.get(targetAOIList.size() - 1).getTimeEnded() == 0) {
            targetAOIList.get(targetAOIList.size() - 1).setTimeEnded(System.currentTimeMillis());
        }
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        final double scaled = seconds * Math.max(0.1, configuration.getAnimationSpeedRatio());
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= waiting) {
                    newRound();
                }
            }
            case SHOW_BALL -> {
                elapsed += scaled;
                ballCup.lift = liftAt(elapsed);
                placeAll();
                if (elapsed >= 2 * LIFT + HOLD + BEFORE) {
                    ballCup.lift = 0;
                    startExchange();
                }
            }
            case EXCHANGE -> {
                elapsed += scaled;
                final double t = Math.min(1, elapsed / level.getExchangeSeconds());
                moving1.progress = t;
                moving2.progress = t;
                placeAll();
                if (t >= 1) {
                    moving1.endMove();
                    moving2.endMove();
                    exchangesDone++;
                    if (ballCup == moving1 || ballCup == moving2) {
                        startAoi();
                    }
                    if (exchangesDone >= level.getExchanges()) {
                        phase = Phase.PLAY;
                        stats.incrementNumberOfGoalsToReach();
                        showHud();
                    } else {
                        startExchange();
                    }
                }
            }
            case PLAY -> fixate(seconds);
            case REVEAL -> {
                elapsed += scaled;
                // The chosen cup lifts; after a wrong one, the right one too
                chosen.lift = Math.min(1, elapsed / LIFT);
                if (chosen != ballCup) {
                    ballCup.lift = Math.max(0, Math.min(1, (elapsed - LIFT) / LIFT));
                }
                placeAll();
                if (elapsed >= REVEAL) {
                    afterRound();
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

    /**
     * @return how high a cup is lifted (0 to 1) at a time of its showing: up, still, down
     */
    private static double liftAt(final double time) {
        if (time < LIFT) {
            return time / LIFT;
        }
        if (time < LIFT + HOLD) {
            return 1;
        }
        return Math.max(0, 1 - (time - LIFT - HOLD) / LIFT);
    }

    /**
     * Two cups drawn at random exchange their places: one passes in front (lower), the other behind (higher).
     */
    private void startExchange() {
        final int a = random.nextInt(cups.size());
        int b = random.nextInt(cups.size() - 1);
        if (b >= a) {
            b++;
        }
        moving1 = cupAt(a);
        moving2 = cupAt(b);
        moving1.startMove(b, 1);
        moving2.startMove(a, -1);
        moving2.node.toBack();
        phase = Phase.EXCHANGE;
        elapsed = 0;
        gameContext.getSoundManager().add(SOUNDS + "slide.wav");
    }

    private Cup cupAt(final int slot) {
        for (final Cup cup : cups) {
            if (cup.slot == slot) {
                return cup;
            }
        }
        throw new IllegalStateException("No cup at " + slot);
    }

    private void placeAll() {
        cups.forEach(Cup::place);
        // The ball under its cup, seen when it is lifted
        ball.setCenterX(ballCup.x);
        ball.setCenterY(tableY - ball.getRadius() - cupHeight * 0.02);
        ball.setVisible(ballCup.lift > 0.05);
    }

    private void fixate(final double seconds) {
        if (looked == null) {
            return;
        }
        if (!ring.isVisible()) {
            showRing(looked);
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            final Cup cup = looked;
            progress = 0;
            ring.stop();
            choose(cup);
        }
    }

    void lookAt(final Cup cup) {
        if (looked == cup) {
            return;
        }
        looked = cup;
        progress = 0;
        ring.stop();
        if (phase == Phase.PLAY) {
            showRing(cup);
        }
    }

    void lookAway(final Cup cup) {
        if (looked == cup) {
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    private void showRing(final Cup cup) {
        if (phase != Phase.PLAY) {
            return;
        }
        ring.setSize(cupWidth * 0.8);
        ring.show(cup.x, tableY - cupHeight * 0.5);
    }

    /**
     * A cup chosen: the right one shows the ball (one more found); a wrong one lifts empty, then the right one shows
     * the ball, and a life is lost.
     */
    void choose(final Cup cup) {
        if (phase != Phase.PLAY) {
            return;
        }
        chosen = cup;
        looked = null;
        ring.stop();
        endAoi();
        if (cup == ballCup) {
            found++;
            stats.incrementNumberOfGoalsReached();
            gameContext.getSoundManager().add(SOUNDS + "found.wav");
        } else {
            lives--;
            gameContext.getSoundManager().add(SOUNDS + "missed.wav");
        }
        phase = Phase.REVEAL;
        elapsed = 0;
        showHud();
    }

    private void afterRound() {
        if (lives <= 0) {
            bravo = false;
            final Group pill = GameStyle.pill(gameContext.getTranslator().translate("CupsLost"), height * 0.05);
            pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
            pill.setLayoutY(height * 0.3 - pill.getBoundsInLocal().getHeight() / 2);
            message.getChildren().setAll(pill);
            phase = Phase.SHOW;
            elapsed = 0;
            return;
        }
        if (end.getFound() > 0 && found >= end.getFound()) {
            bravo = true;
            finish();
            return;
        }
        newRound();
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
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("CupsScore") + " " + found
            + (end.getFound() > 0 ? " / " + end.getFound() : ""), fontSize);
        pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY(margin * 0.6);
        hud.getChildren().add(pill);
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
        endAoi();
        stats.setTargetAOIList(targetAOIList);
        if (ring != null) {
            ring.stop();
        }
        for (final Node node : new ArrayList<>(gazeNodes)) {
            gameContext.getGazeDeviceManager().removeEventFilter(node);
        }
        gazeNodes.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A cup upside down: red with white stripes; it slides to another place along an arc, or lifts.
     */
    final class Cup {
        @Getter
        private int slot;
        private int from;
        private int to;
        private int side;
        private double progress = 1;
        private double lift;
        @Getter
        private double x;
        private final Group node;

        Cup(final int slot) {
            this.slot = slot;
            this.from = slot;
            this.to = slot;
            final double w = cupWidth;
            final double h = cupHeight;
            final Polygon body = new Polygon(-w * 0.32, -h, w * 0.32, -h, w * 0.5, 0, -w * 0.5, 0);
            body.setFill(CUP);
            body.setStroke(CUP_DARK);
            body.setStrokeWidth(Math.max(2, w * 0.03));
            final Polygon stripe1 = new Polygon(-w * 0.37, -h * 0.72, w * 0.37, -h * 0.72, w * 0.39, -h * 0.62,
                -w * 0.39, -h * 0.62);
            final Polygon stripe2 = new Polygon(-w * 0.43, -h * 0.38, w * 0.43, -h * 0.38, w * 0.45, -h * 0.28,
                -w * 0.45, -h * 0.28);
            stripe1.setFill(Color.rgb(255, 245, 235));
            stripe2.setFill(Color.rgb(255, 245, 235));
            final Ellipse top = new Ellipse(0, -h, w * 0.32, w * 0.07);
            top.setFill(CUP_DARK);
            final Ellipse rim = new Ellipse(0, 0, w * 0.5, w * 0.08);
            rim.setFill(CUP);
            rim.setStroke(CUP_DARK);
            rim.setStrokeWidth(Math.max(2, w * 0.03));
            node = new Group(rim, body, stripe1, stripe2, top);
            place();
        }

        void startMove(final int target, final int arcSide) {
            from = slot;
            to = target;
            side = arcSide;
            progress = 0;
        }

        void endMove() {
            slot = to;
            from = to;
            progress = 1;
            place();
        }

        void place() {
            // Smooth start and end, along an arc (in front lower, behind higher)
            final double t = progress * progress * (3 - 2 * progress);
            x = slotX[from] + (slotX[to] - slotX[from]) * t;
            final double arc = Math.sin(Math.PI * t) * cupHeight * 0.35 * side;
            node.setLayoutX(x);
            node.setLayoutY(tableY + arc - lift * cupHeight * 0.75);
            node.setScaleX(1 + Math.sin(Math.PI * t) * 0.06 * side);
            node.setScaleY(1 + Math.sin(Math.PI * t) * 0.06 * side);
        }

        boolean isLifted() {
            return lift > 0.5;
        }
    }
}

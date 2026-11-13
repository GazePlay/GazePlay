package net.gazeplay.games.bottle;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * "Bouteille" (name code Bottle; reviewed on 13/11/2026, maintainer's rules): bottles on the two shelves of a stall of
 * a fair, everything drawn by the game. A fixation on a bottle throws a ball from the bottom of the screen, under it,
 * which breaks it into pieces of glass; several balls can fly at once.
 * <p>
 * Break them all: a series ends when all the bottles are broken. The right color: a card asks for a color ("Casse les
 * bouteilles rouges"); a bottle of another color shakes without breaking and counts a mistake (no lives); the series
 * ends when the bottles of that color are broken, the next one asks for another color. The bottles are red, blue, green
 * or yellow, each with its symbol on its label (round, star, heart, triangle). A series finished: a star and
 * "Bravo !", then new bottles; the game ends after 1, 3 or 5 series ("Bravo", then the statistics), or has no end.
 * <p>
 * One loop, in real seconds: the fixation lasts the length of the settings (read at each step), nothing can be chosen
 * before the delay of the settings nor while the menu of the game is open. (Before the review: pictures and a sound of
 * unknown source, bottles stretched with the screen, one ball at a time, a bottle chosen while it flew ignored, the game
 * over after a single series, nothing removed at the end.)
 */
@Slf4j
public class BottleGame implements GameLifeCycle {

    /**
     * The seconds a ball flies, a wrong bottle shakes, pieces of glass fall, and the reward lasts.
     */
    static final double THROW = 0.4;
    static final double SHAKE = 0.5;
    static final double SHARDS = 0.9;
    static final double REWARD = 2;

    private static final String SOUNDS = "data/bottle/sounds/";

    enum Phase { WAIT, PLAY, REWARD, OVER }

    enum State { STANDING, TARGETED, BROKEN }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final BottleMode mode;
    @Getter
    private final BottleCount count;
    @Getter
    private final BottleSize size;
    @Getter
    private final BottleEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private BottleColor target;
    @Getter
    private int series;
    @Getter
    private int toBreak;
    @Getter
    private int broken;
    private double elapsed;
    private double phaseTime;
    private boolean rewarded;

    private double width;
    private double height;
    private double left;
    private double right;
    private final double[] shelves = new double[2];
    private double bottleWidth;
    private double bottleHeight;

    private Group layer;
    private Group bottlesLayer;
    private Group effects;
    private Group card;
    private Group hud;
    private Group reward;
    private FixationRing ring;
    @Getter
    private final List<Bottle> bottles = new ArrayList<>();
    private final List<Ball> balls = new ArrayList<>();
    private final List<Shard> shards = new ArrayList<>();
    @Getter
    private Bottle looked;
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
    BottleGame(final IGameContext gameContext, final Stats stats, final BottleMode mode, final BottleCount count,
               final BottleSize size, final BottleEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.mode = mode;
        this.count = count;
        this.size = size;
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
        left = mode == BottleMode.COLOR ? width * 0.34 : width * 0.08;
        right = width * (mode == BottleMode.COLOR ? 0.95 : 0.92);
        shelves[0] = height * 0.45;
        shelves[1] = height * 0.77;
        final double gap = shelves[1] - shelves[0];
        final double slot = (right - left) / (count.getBottles() / 2.0);
        bottleHeight = gap * size.getHeight();
        bottleWidth = bottleHeight * 0.38;
        if (bottleWidth > slot * 0.8) {
            // Narrower, the same shape
            bottleWidth = slot * 0.8;
            bottleHeight = bottleWidth / 0.38;
        }

        final Group stall = stall();
        bottlesLayer = new Group();
        effects = new Group();
        effects.setMouseTransparent(true);
        card = new Group();
        card.setMouseTransparent(true);
        ring = new FixationRing();
        ring.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        reward = new Group();
        reward.setMouseTransparent(true);
        layer = new Group(stall, bottlesLayer, effects, card, ring, hud, reward);
        gameContext.getChildren().add(layer);
        gameContext.getGazeDeviceManager().addStats(stats);
        series = 0;
        newSeries();
        phase = Phase.WAIT;
        phaseTime = 0;
        elapsed = 0;
        if (!manual) {
            loop.start();
        }
    }

    // --- The drawings ---

    /**
     * The stall: a wall, a striped awning at the top, two wooden shelves, a striped counter at the bottom.
     */
    private Group stall() {
        final Group group = new Group();
        final Rectangle wall = new Rectangle(0, 0, width, height);
        wall.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.rgb(236, 222, 200)),
            new Stop(1, Color.rgb(222, 204, 178))));
        group.getChildren().add(wall);
        final double stripe = width / 16;
        final double awning = height * 0.09;
        final Color red = Color.rgb(205, 70, 65);
        final Color cream = Color.rgb(252, 238, 205);
        for (int i = 0; i < 16; i++) {
            final Color color = i % 2 == 0 ? red : cream;
            final Rectangle band = new Rectangle(i * stripe, 0, stripe + 1, awning);
            band.setFill(color);
            final Circle scallop = new Circle(i * stripe + stripe / 2, awning, stripe / 2, color);
            group.getChildren().addAll(band, scallop);
        }
        final double plank = height * 0.025;
        for (final double y : shelves) {
            final Rectangle shadow = new Rectangle(left - width * 0.02, y + plank, right - left + width * 0.04,
                plank * 0.9);
            shadow.setFill(Color.rgb(90, 60, 40, 0.18));
            final Rectangle board = new Rectangle(left - width * 0.03, y, right - left + width * 0.06, plank);
            board.setArcWidth(plank * 0.6);
            board.setArcHeight(plank * 0.6);
            board.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(170, 115, 70)), new Stop(1, Color.rgb(120, 78, 45))));
            group.getChildren().addAll(shadow, board);
        }
        final double counterTop = height * 0.86;
        final Rectangle edge = new Rectangle(0, counterTop, width, height * 0.02);
        edge.setFill(Color.rgb(110, 72, 45));
        group.getChildren().add(edge);
        for (int i = 0; i < 16; i++) {
            final Rectangle band = new Rectangle(i * stripe, counterTop + height * 0.02, stripe + 1,
                height - counterTop - height * 0.02);
            band.setFill(i % 2 == 0 ? red : cream);
            group.getChildren().add(band);
        }
        group.setMouseTransparent(true);
        return group;
    }

    /**
     * A bottle standing on (0, 0): its glass, a gleam, a label with the symbol of its color.
     */
    private Group bottlePicture(final BottleColor color, final double w, final double h, final List<Shape> glassOut) {
        final String path = String.format(Locale.ROOT,
            "M %1$f %2$f Q %1$f 0 %3$f 0 L %4$f 0 Q %5$f 0 %5$f %2$f L %5$f %6$f C %5$f %7$f %8$f %9$f %8$f %10$f "
                + "L %8$f %11$f L %12$f %13$f L %12$f %14$f L %15$f %14$f L %15$f %13$f L %16$f %11$f L %16$f %10$f "
                + "C %16$f %9$f %1$f %7$f %1$f %6$f Z",
            -w / 2, -h * 0.05, -w * 0.4, w * 0.4, w / 2, -h * 0.55, -h * 0.7, w * 0.18, -h * 0.68, -h * 0.82,
            -h * 0.94, w * 0.22, -h * 0.96, -h, -w * 0.22, -w * 0.18);
        final SVGPath glass = new SVGPath();
        glass.setContent(path);
        final Color c = color.getColor();
        glass.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, new Stop(0, c.darker()),
            new Stop(0.35, c), new Stop(0.6, c.brighter()), new Stop(1, c.darker().darker())));
        glass.setStroke(c.darker().darker());
        glass.setStrokeWidth(Math.max(1.5, w * 0.03));
        final Rectangle gleam = new Rectangle(-w * 0.34, -h * 0.52, w * 0.1, h * 0.42);
        gleam.setArcWidth(w * 0.1);
        gleam.setArcHeight(w * 0.1);
        gleam.setFill(Color.rgb(255, 255, 255, 0.4));
        final Rectangle label = new Rectangle(-w * 0.42, -h * 0.42, w * 0.84, h * 0.24);
        label.setArcWidth(w * 0.12);
        label.setArcHeight(w * 0.12);
        label.setFill(Color.rgb(252, 248, 238));
        label.setStroke(c.darker());
        label.setStrokeWidth(Math.max(1, w * 0.02));
        final Shape symbol = symbol(color, Math.min(w * 0.3, h * 0.1), -h * 0.3);
        symbol.setFill(c.darker());
        for (final var node : List.of(gleam, label, symbol)) {
            node.setMouseTransparent(true);
        }
        if (glassOut != null) {
            glassOut.add(glass);
        }
        return new Group(glass, gleam, label, symbol);
    }

    /**
     * The symbol of a color, centered on (0, y): a round, a star, a heart, a triangle.
     */
    private static Shape symbol(final BottleColor color, final double r, final double y) {
        return switch (color) {
            case RED -> new Circle(0, y, r);
            case BLUE -> {
                final Polygon star = new Polygon();
                for (int i = 0; i < 10; i++) {
                    final double a = -Math.PI / 2 + i * Math.PI / 5;
                    final double d = i % 2 == 0 ? r * 1.15 : r * 0.48;
                    star.getPoints().addAll(d * Math.cos(a), y + d * Math.sin(a));
                }
                yield star;
            }
            case GREEN -> {
                final Circle leftLobe = new Circle(-r * 0.48, y - r * 0.3, r * 0.55);
                final Circle rightLobe = new Circle(r * 0.48, y - r * 0.3, r * 0.55);
                final Polygon point = new Polygon(-r * 1.0, y - r * 0.12, r * 1.0, y - r * 0.12, 0, y + r);
                yield Shape.union(Shape.union(leftLobe, rightLobe), point);
            }
            case YELLOW -> new Polygon(0, y - r * 1.05, r * 1.05, y + r * 0.8, -r * 1.05, y + r * 0.8);
        };
    }

    private void showHud() {
        final double fontSize = height * 0.03;
        final Group bottlesPill = GameStyle.pill(gameContext.getTranslator().translate("BottleCount") + " " + broken
            + " / " + toBreak, fontSize);
        bottlesPill.setLayoutX(height * 0.03);
        bottlesPill.setLayoutY(height * 0.02);
        final Group seriesPill = GameStyle.pill(gameContext.getTranslator().translate("BottleSeries") + " " + series
            + (end.getSeries() > 0 ? " / " + end.getSeries() : ""), fontSize);
        seriesPill.setLayoutX(width - seriesPill.getBoundsInLocal().getWidth() - height * 0.03);
        seriesPill.setLayoutY(height * 0.02);
        hud.getChildren().setAll(bottlesPill, seriesPill);
    }

    /**
     * The card of the color to break, at the left: the sentence and a big bottle of that color.
     */
    private void showCard() {
        card.getChildren().clear();
        if (mode != BottleMode.COLOR) {
            return;
        }
        final double cardLeft = width * 0.04;
        final double cardTop = height * 0.17;
        final double cardWidth = width * 0.26;
        final double cardHeight = height * 0.64;
        final Rectangle frame = GameStyle.targetCard(cardLeft, cardTop, cardWidth, cardHeight);
        final Group sentence = GameStyle.pill(gameContext.getTranslator().translate(target.getKey()), height * 0.026);
        sentence.setLayoutX(cardLeft + (cardWidth - sentence.getBoundsInLocal().getWidth()) / 2);
        sentence.setLayoutY(cardTop + height * 0.025);
        final double h = cardHeight * 0.6;
        final Group big = bottlePicture(target, Math.min(h * 0.38, cardWidth * 0.5), h, null);
        big.setLayoutX(cardLeft + cardWidth / 2);
        big.setLayoutY(cardTop + cardHeight - height * 0.05);
        card.getChildren().addAll(frame, sentence, big);
    }

    // --- The game ---

    /**
     * New bottles on the shelves; in the right color, a color to break (never the one just asked), a quarter of the
     * bottles of that color and the others of the other colors.
     */
    private void newSeries() {
        clearBottles();
        final int n = count.getBottles();
        final BottleColor[] all = BottleColor.values();
        final List<BottleColor> colors = new ArrayList<>();
        if (mode == BottleMode.COLOR) {
            BottleColor next;
            do {
                next = all[random.nextInt(all.length)];
            } while (next == target);
            target = next;
            final int targets = Math.max(1, n / 4);
            for (int i = 0; i < targets; i++) {
                colors.add(target);
            }
            final List<BottleColor> others = new ArrayList<>();
            for (final BottleColor color : all) {
                if (color != target) {
                    others.add(color);
                }
            }
            for (int i = 0; colors.size() < n; i++) {
                colors.add(i < others.size() ? others.get(i) : others.get(random.nextInt(others.size())));
            }
            for (int i = colors.size() - 1; i > 0; i--) {
                final int j = random.nextInt(i + 1);
                final BottleColor swap = colors.get(i);
                colors.set(i, colors.get(j));
                colors.set(j, swap);
            }
            toBreak = targets;
        } else {
            for (int i = 0; i < n; i++) {
                colors.add(all[random.nextInt(all.length)]);
            }
            toBreak = n;
        }
        broken = 0;
        final int perShelf = n / 2;
        final double slot = (right - left) / perShelf;
        for (int i = 0; i < n; i++) {
            final double x = left + slot * (i % perShelf + 0.5);
            final double y = shelves[i / perShelf];
            final Bottle bottle = new Bottle(colors.get(i), x, y);
            bottles.add(bottle);
        }
        reward.getChildren().clear();
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach(toBreak);
        showCard();
        showHud();
    }

    private void clearBottles() {
        for (final Bottle bottle : bottles) {
            gameContext.getGazeDeviceManager().removeEventFilter(bottle.glass);
        }
        bottles.clear();
        bottlesLayer.getChildren().clear();
        for (final Ball ball : balls) {
            effects.getChildren().remove(ball.node);
        }
        balls.clear();
        ring.stop();
        looked = null;
        progress = 0;
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        phaseTime += seconds;
        moveBalls(seconds);
        moveShards(seconds);
        for (final Bottle bottle : bottles) {
            bottle.shake(seconds);
        }
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= configuration.getGameStartDelay() / 1000.0) {
                    setPhase(Phase.PLAY);
                }
            }
            case PLAY -> fixate(seconds);
            case REWARD -> {
                if (!rewarded) {
                    rewarded = true;
                    showReward();
                }
                if (phaseTime >= REWARD) {
                    if (end.getSeries() > 0 && series >= end.getSeries()) {
                        finish();
                    } else {
                        setPhase(Phase.PLAY);
                        newSeries();
                    }
                }
            }
            default -> {
            }
        }
    }

    private void setPhase(final Phase next) {
        phase = next;
        phaseTime = 0;
    }

    private void fixate(final double seconds) {
        if (looked == null || Stats.isConfigMenuOpen()) {
            return;
        }
        if (looked.state != State.STANDING) {
            lookAway(looked);
            return;
        }
        if (!ring.isVisible()) {
            ring.setSize(Math.max(height * 0.07, bottleWidth * 1.2));
            ring.show(looked.x, looked.y - bottleHeight * 0.3);
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            final Bottle bottle = looked;
            ring.stop();
            looked = null;
            progress = 0;
            choose(bottle);
        }
    }

    void lookAt(final Bottle bottle) {
        if (looked == bottle) {
            return;
        }
        looked = bottle;
        progress = 0;
        ring.stop();
    }

    void lookAway(final Bottle bottle) {
        if (looked == bottle) {
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    /**
     * A bottle chosen: a ball thrown at it, or in the right color a bottle of another color that shakes.
     */
    void choose(final Bottle bottle) {
        if (phase != Phase.PLAY || bottle.state != State.STANDING) {
            return;
        }
        if (mode == BottleMode.COLOR && bottle.color != target) {
            stats.incrementNumberOfMistakes();
            gameContext.getSoundManager().add("data/dice/sounds/wrong.wav");
            bottle.shaking = SHAKE;
            return;
        }
        bottle.state = State.TARGETED;
        final double r = Math.max(height * 0.018, Math.min(bottleWidth * 0.32, height * 0.03));
        final Circle ball = new Circle(r);
        ball.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.rgb(255, 250, 240)),
            new Stop(1, Color.rgb(205, 195, 180))));
        ball.setStroke(Color.rgb(205, 70, 65));
        ball.setStrokeWidth(Math.max(2, r * 0.25));
        effects.getChildren().add(ball);
        balls.add(new Ball(ball, bottle));
        gameContext.getSoundManager().add(SOUNDS + "throw.wav");
    }

    private void moveBalls(final double seconds) {
        final Iterator<Ball> it = balls.iterator();
        while (it.hasNext()) {
            final Ball ball = it.next();
            ball.age += seconds;
            final double u = Math.min(1, ball.age / THROW);
            final Bottle bottle = ball.bottle;
            final double targetY = bottle.y - bottleHeight * 0.35;
            final double startY = height + ball.node.getRadius();
            // Up from under the bottle, slowing a little
            final double y = startY + (targetY - startY) * (1 - (1 - u) * (1 - u));
            ball.node.setCenterX(bottle.x);
            ball.node.setCenterY(y);
            ball.node.setRotate(u * 540);
            if (u >= 1) {
                effects.getChildren().remove(ball.node);
                it.remove();
                breakBottle(bottle);
            }
        }
    }

    /**
     * A bottle hit: it breaks into pieces of glass that fall; the series ends when the last bottle to break is broken.
     */
    private void breakBottle(final Bottle bottle) {
        bottle.state = State.BROKEN;
        bottlesLayer.getChildren().remove(bottle.node);
        gameContext.getGazeDeviceManager().removeEventFilter(bottle.glass);
        if (looked == bottle) {
            lookAway(bottle);
        }
        for (int i = 0; i < 10; i++) {
            final double s = bottleWidth * (0.12 + random.nextDouble() * 0.16);
            final Polygon piece = new Polygon(0, -s, s * 0.8, s * 0.6, -s * 0.7, s * 0.4);
            piece.setFill(bottle.color.getColor().deriveColor(0, 1, 1, 0.85));
            piece.setStroke(Color.rgb(255, 255, 255, 0.6));
            piece.setLayoutX(bottle.x + (random.nextDouble() - 0.5) * bottleWidth);
            piece.setLayoutY(bottle.y - bottleHeight * (0.2 + random.nextDouble() * 0.5));
            effects.getChildren().add(piece);
            shards.add(new Shard(piece, (random.nextDouble() - 0.5) * width * 0.25, -height * (0.1 + random.nextDouble()
                * 0.25), (random.nextDouble() - 0.5) * 720));
        }
        gameContext.getSoundManager().add(SOUNDS + "glass.wav");
        stats.incrementNumberOfGoalsReached();
        broken++;
        showHud();
        if (broken >= toBreak && phase == Phase.PLAY) {
            series++;
            gameContext.getSoundManager().add("data/goosegame/sounds/win.wav");
            rewarded = false;
            showHud();
            setPhase(Phase.REWARD);
        }
    }

    private void moveShards(final double seconds) {
        final Iterator<Shard> it = shards.iterator();
        while (it.hasNext()) {
            final Shard shard = it.next();
            shard.age += seconds;
            shard.vy += height * 1.6 * seconds;
            shard.node.setLayoutX(shard.node.getLayoutX() + shard.vx * seconds);
            shard.node.setLayoutY(shard.node.getLayoutY() + shard.vy * seconds);
            shard.node.setRotate(shard.node.getRotate() + shard.spin * seconds);
            shard.node.setOpacity(Math.max(0, 1 - shard.age / SHARDS));
            if (shard.age >= SHARDS) {
                effects.getChildren().remove(shard.node);
                it.remove();
            }
        }
    }

    /**
     * A star beside "Bravo !", in the middle of the shelves.
     */
    private void showReward() {
        final double r = height * 0.045;
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("BottleWell"), height * 0.045);
        final double pillWidth = pill.getBoundsInLocal().getWidth();
        final double pillHeight = pill.getBoundsInLocal().getHeight();
        final double gap = r * 0.4;
        final double middle = (left + right) / 2;
        final double startX = middle - (2 * r + gap + pillWidth) / 2;
        final double cy = (shelves[0] + shelves[1]) / 2 - bottleHeight * 0.3;
        final Polygon star = new Polygon();
        for (int i = 0; i < 10; i++) {
            final double a = -Math.PI / 2 + i * Math.PI / 5;
            final double d = i % 2 == 0 ? r : r * 0.45;
            star.getPoints().addAll(startX + r + d * Math.cos(a), cy + r * 0.08 + d * Math.sin(a));
        }
        star.setFill(Color.rgb(255, 200, 40));
        star.setStroke(Color.rgb(200, 120, 0));
        star.setStrokeWidth(Math.max(2, r * 0.07));
        pill.setLayoutX(startX + 2 * r + gap);
        pill.setLayoutY(cy - pillHeight / 2);
        reward.getChildren().setAll(star, pill);
    }

    private void finish() {
        setPhase(Phase.OVER);
        loop.stop();
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

    /**
     * @return the flying balls, for the tests
     */
    int getBallsInFlight() {
        return balls.size();
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (bottlesLayer != null) {
            clearBottles();
        }
        balls.clear();
        shards.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    private static final class Ball {
        private final Circle node;
        private final Bottle bottle;
        private double age;

        Ball(final Circle node, final Bottle bottle) {
            this.node = node;
            this.bottle = bottle;
        }
    }

    private static final class Shard {
        private final Polygon node;
        private final double vx;
        private double vy;
        private final double spin;
        private double age;

        Shard(final Polygon node, final double vx, final double vy, final double spin) {
            this.node = node;
            this.vx = vx;
            this.vy = vy;
            this.spin = spin;
        }
    }

    /**
     * A bottle on a shelf: its color, where it stands, its glass (looked at), whether it is standing, aimed at or broken.
     */
    final class Bottle {
        @Getter
        private final BottleColor color;
        private final double x;
        private final double y;
        private final Group node;
        private final Shape glass;
        @Getter
        private State state = State.STANDING;
        private double shaking;

        Bottle(final BottleColor color, final double x, final double y) {
            this.color = color;
            this.x = x;
            this.y = y;
            final List<Shape> glassOut = new ArrayList<>();
            node = bottlePicture(color, bottleWidth, bottleHeight, glassOut);
            glass = glassOut.get(0);
            node.setLayoutX(x);
            node.setLayoutY(y);
            final EventHandler<Event> handler = event -> {
                final var type = event.getEventType();
                if (type == MouseEvent.MOUSE_ENTERED || type == GazeEvent.GAZE_ENTERED) {
                    lookAt(this);
                } else if (type == MouseEvent.MOUSE_EXITED || type == GazeEvent.GAZE_EXITED) {
                    lookAway(this);
                }
            };
            glass.addEventFilter(MouseEvent.ANY, handler);
            glass.addEventFilter(GazeEvent.ANY, handler);
            gameContext.getGazeDeviceManager().addEventFilter(glass);
            bottlesLayer.getChildren().add(node);
        }

        void shake(final double seconds) {
            if (shaking > 0) {
                shaking = Math.max(0, shaking - seconds);
                node.setRotate(shaking == 0 ? 0 : Math.sin(shaking * 40) * 8 * shaking / SHAKE);
            }
        }

        boolean isShaking() {
            return shaking > 0;
        }
    }
}

package net.gazeplay.games.dottodot;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
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
 * "Point par point" (name code DotToDot; reviewed on 12/11/2026, maintainer's rules). The dots of a figure show
 * numbers (1, 2, 3...) or letters (A, B, C...); a fixation on the next one draws the line from the previous one (the
 * first one just starts). A wrong dot shakes and counts a mistake; the dots already joined are left alone. After the
 * last dot, the line back to the first one draws itself, the figure fills with its color and its name shows ("It's a
 * fish!"); then another figure. The game ends after 3, 5 or 10 drawings ("Bravo"), or never.
 * <p>
 * Easy: 4 to 7 dots, the next dot pulses; normal: 8 to 12; hard: 13 to 20; progressive: easy first, the next level after
 * 3 drawings in a row without a mistake. The figures (24, named, made for the project) keep their proportions on any
 * screen. One loop, in real seconds: the fixation lasts the length of the settings (read at each step), nothing can be
 * chosen before the delay of the settings, nothing moves while the menu of the game is open. (Before the review: the
 * variants "numbers" and "order" were the same game, a dot already joined counted a mistake and a wrong one showed
 * nothing, the game could start at the dot 2, figures stretched on a screen not in 16:9, dots and numbers of a fixed
 * size over the lines, no reward, two goals to reach for each figure, a replay with another seed.)
 */
public class DotToDot implements GameLifeCycle {

    /**
     * In seconds: a line drawing itself, a wrong dot shaking, the finished drawing shown.
     */
    static final double LINE = 0.25;
    static final double SHAKE = 0.45;
    static final double SHOW = 2.6;

    private static final Color DOT = Color.rgb(252, 250, 244);
    private static final Color INK = Color.rgb(48, 44, 64);
    private static final Color WRONG = Color.rgb(214, 64, 64);
    private static final String[] POPS = {"data/bubble/sounds/pop1.wav", "data/bubble/sounds/pop2.wav",
        "data/bubble/sounds/pop3.wav"};

    enum Phase { WAIT, PLAY, SHOW, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final DotLevel level;
    @Getter
    private final DotLabels labels;
    @Getter
    private final DotEnd end;
    private final ReplayablePseudoRandom random;
    private final List<DotFigure> figures = DotFigure.all();

    @Getter
    private Phase phase = Phase.OVER;
    /**
     * The level of the figures now: the level chosen, or where the progressive level is.
     */
    @Getter
    private DotLevel figureLevel;
    @Getter
    private DotFigure figure;
    @Getter
    private final List<Dot> dots = new ArrayList<>();
    /**
     * The place of the next dot to join, from 0.
     */
    @Getter
    private int next;
    @Getter
    private int drawings;
    @Getter
    private int roundMistakes;
    @Getter
    private int streak;
    private DotFigure lastFigure;
    private double elapsed;
    private double pulse;

    private double width;
    private double height;
    private double dotRadius;

    private Group layer;
    private Group fillLayer;
    private Group linesLayer;
    private Group dotsLayer;
    private Group hud;
    private Group message;
    private FixationRing ring;
    private final List<Node> gazeNodes = new ArrayList<>();
    private final List<Line> growing = new ArrayList<>();
    private final List<double[]> growingEnds = new ArrayList<>();
    private Polygon fill;

    @Getter
    private Dot looked;
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
    DotToDot(final IGameContext gameContext, final Stats stats, final DotLevel level, final DotLabels labels,
             final DotEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.level = level;
        this.labels = labels;
        this.end = end;
        if (gameSeed < 0) {
            random = new ReplayablePseudoRandom();
            stats.setGameSeed(random.getSeed());
        } else {
            random = new ReplayablePseudoRandom(gameSeed);
        }
        figureLevel = level == DotLevel.PROGRESSIVE ? DotLevel.EASY : level;
    }

    @Override
    public void launch() {
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        width = dimension.getWidth();
        height = dimension.getHeight();

        final Rectangle background = new Rectangle(0, 0, width, height);
        background.setStyle("-fx-fill: -gp-bg;");
        background.setMouseTransparent(true);
        fillLayer = new Group();
        fillLayer.setMouseTransparent(true);
        linesLayer = new Group();
        linesLayer.setMouseTransparent(true);
        dotsLayer = new Group();
        ring = new FixationRing();
        ring.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);
        message = new Group();
        message.setMouseTransparent(true);
        layer = new Group(background, fillLayer, linesLayer, dotsLayer, ring, hud, message);
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addStats(stats);
        drawings = 0;
        streak = 0;
        newFigure();
        phase = Phase.WAIT;
        elapsed = 0;
        if (!manual) {
            loop.start();
        }
    }

    /**
     * A new figure of the level, never the same twice in a row; its dots in a box keeping its proportions.
     */
    private void newFigure() {
        clearFigure();
        final List<DotFigure> candidates = figures.stream()
            .filter(candidate -> candidate.level().equals(figureLevel.name()) && candidate != lastFigure).toList();
        figure = candidates.get(random.nextInt(candidates.size()));
        lastFigure = figure;

        // Between the band of the score at the top and the name of the drawing at the bottom, with room for half a dot
        // all around
        final double top = height * 0.12;
        final double bottom = height * 0.12;
        final double margin = height * 0.05;
        final double boxHeight = Math.min(height - top - bottom - 2 * margin,
            (width - 2 * margin) * 0.92 / figure.aspect());
        final double boxWidth = boxHeight * figure.aspect();
        final double boxX = (width - boxWidth) / 2;
        final double boxY = top + margin + (height - top - bottom - 2 * margin - boxHeight) / 2;
        final List<Point2D> points = new ArrayList<>();
        for (final double[] point : figure.points()) {
            points.add(new Point2D(boxX + point[0] * boxWidth, boxY + point[1] * boxHeight));
        }
        // Dots as large as they can be without touching each other
        double closest = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                closest = Math.min(closest, points.get(i).distance(points.get(j)));
            }
        }
        dotRadius = Math.max(height * 0.018, Math.min(height * 0.042, closest * 0.4));
        for (int i = 0; i < points.size(); i++) {
            dots.add(new Dot(i, points.get(i)));
        }
        next = 0;
        roundMistakes = 0;
        showHud();
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
    }

    private void clearFigure() {
        dots.forEach(dot -> forget(dot.zone));
        dots.clear();
        dotsLayer.getChildren().clear();
        linesLayer.getChildren().clear();
        fillLayer.getChildren().clear();
        message.getChildren().clear();
        growing.clear();
        growingEnds.clear();
        fill = null;
        looked = null;
        progress = 0;
        ring.stop();
    }

    private void showHud() {
        hud.getChildren().clear();
        final double margin = height * 0.025;
        final Group score = GameStyle.pill(gameContext.getTranslator().translate("DotDrawings") + " " + drawings
            + (end.getDrawings() > 0 ? " / " + end.getDrawings() : ""), height * 0.032);
        score.setLayoutX((width - score.getBoundsInLocal().getWidth()) / 2);
        score.setLayoutY(margin);
        hud.getChildren().add(score);
    }

    /**
     * Makes a dot receive the gaze and the mouse.
     */
    private void listen(final Dot dot) {
        final EventHandler<Event> handler = event -> {
            if (event.getEventType() == MouseEvent.MOUSE_ENTERED || event.getEventType() == GazeEvent.GAZE_ENTERED) {
                lookAt(dot);
            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                || event.getEventType() == GazeEvent.GAZE_EXITED) {
                lookAway(dot);
            }
        };
        dot.zone.addEventFilter(MouseEvent.ANY, handler);
        dot.zone.addEventFilter(GazeEvent.ANY, handler);
        gameContext.getGazeDeviceManager().addEventFilter(dot.zone);
        gazeNodes.add(dot.zone);
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
        dots.forEach(dot -> dot.shake(seconds));
        growLines(seconds);
        pulseNext(seconds);
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= configuration.getGameStartDelay() / 1000.0) {
                    phase = Phase.PLAY;
                }
            }
            case PLAY -> fixate(seconds);
            case SHOW -> {
                elapsed += seconds;
                if (fill != null) {
                    fill.setOpacity(Math.min(1, elapsed / 0.6));
                }
                if (elapsed >= SHOW) {
                    if (end.getDrawings() > 0 && drawings >= end.getDrawings()) {
                        finish();
                    } else {
                        newFigure();
                        phase = Phase.PLAY;
                    }
                }
            }
            default -> {
            }
        }
    }

    /**
     * Easy figures: the next dot pulses gently, to help.
     */
    private void pulseNext(final double seconds) {
        pulse += seconds;
        for (final Dot dot : dots) {
            final boolean help = figureLevel == DotLevel.EASY && phase == Phase.PLAY && dot.index == next;
            final double scale = help ? 1 + 0.12 * Math.abs(Math.sin(pulse * Math.PI * 1.2)) : 1;
            dot.disc.setScaleX(scale);
            dot.disc.setScaleY(scale);
        }
    }

    private void growLines(final double seconds) {
        for (int i = 0; i < growing.size(); i++) {
            final Line line = growing.get(i);
            final double[] ends = growingEnds.get(i);
            ends[4] = Math.min(1, ends[4] + seconds / LINE);
            line.setEndX(ends[0] + (ends[2] - ends[0]) * ends[4]);
            line.setEndY(ends[1] + (ends[3] - ends[1]) * ends[4]);
        }
    }

    private boolean isEnabled(final Dot dot) {
        return phase == Phase.PLAY && !dot.joined;
    }

    private void fixate(final double seconds) {
        if (looked == null || !isEnabled(looked)) {
            return;
        }
        if (!ring.isVisible()) {
            showRing(looked);
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            final Dot dot = looked;
            progress = 0;
            ring.stop();
            // Looked at again to choose once more
            looked = null;
            choose(dot);
        }
    }

    private void showRing(final Dot dot) {
        ring.setSize(dotRadius * 3.2);
        ring.show(dot.point.getX(), dot.point.getY());
    }

    void lookAt(final Dot dot) {
        if (looked == dot) {
            return;
        }
        looked = dot;
        progress = 0;
        ring.stop();
        if (isEnabled(dot)) {
            showRing(dot);
        }
    }

    void lookAway(final Dot dot) {
        if (looked == dot) {
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    /**
     * A dot chosen: the next one is joined (a line from the previous one); another one shakes and counts a mistake.
     */
    void choose(final Dot dot) {
        if (phase != Phase.PLAY || dot.joined) {
            return;
        }
        if (dot.index != next) {
            dot.wrong();
            roundMistakes++;
            stats.incrementNumberOfMistakes();
            gameContext.getSoundManager().add("data/dice/sounds/wrong.wav");
            return;
        }
        dot.join();
        gameContext.getSoundManager().add(POPS[random.nextInt(POPS.length)]);
        if (next > 0) {
            addLine(dots.get(next - 1).point, dot.point);
        }
        next++;
        if (next == dots.size()) {
            complete();
        }
    }

    private void addLine(final Point2D from, final Point2D to) {
        final Line line = new Line(from.getX(), from.getY(), from.getX(), from.getY());
        line.setStyle("-fx-stroke: -gp-accent;");
        line.setStrokeWidth(Math.max(4, dotRadius * 0.28));
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        linesLayer.getChildren().add(line);
        growing.add(line);
        growingEnds.add(new double[]{from.getX(), from.getY(), to.getX(), to.getY(), 0});
    }

    /**
     * The last dot: the line back to the first one, the figure filled with its color, its name; the progressive level
     * goes up after 3 drawings in a row without a mistake.
     */
    private void complete() {
        addLine(dots.get(dots.size() - 1).point, dots.get(0).point);
        final double[] coordinates = new double[dots.size() * 2];
        for (int i = 0; i < dots.size(); i++) {
            coordinates[2 * i] = dots.get(i).point.getX();
            coordinates[2 * i + 1] = dots.get(i).point.getY();
        }
        fill = new Polygon(coordinates);
        fill.setFill(Color.web(figure.color()));
        fill.setOpacity(0);
        fillLayer.getChildren().setAll(fill);
        drawings++;
        stats.incrementNumberOfGoalsReached();
        gameContext.getSoundManager().add("data/goosegame/sounds/win.wav");
        final String name = gameContext.getTranslator().translate(figure.name());
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("DotItIs").replace("{0}", name),
            height * 0.05);
        pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY(height - pill.getBoundsInLocal().getHeight() - height * 0.03);
        message.getChildren().setAll(pill);
        if (level == DotLevel.PROGRESSIVE) {
            streak = roundMistakes == 0 ? streak + 1 : 0;
            if (streak >= DotLevel.PROGRESS_AFTER && figureLevel != DotLevel.HARD) {
                figureLevel = figureLevel == DotLevel.EASY ? DotLevel.NORMAL : DotLevel.HARD;
                streak = 0;
            }
        }
        showHud();
        phase = Phase.SHOW;
        elapsed = 0;
    }

    private void finish() {
        phase = Phase.OVER;
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

    double getDotRadius() {
        return dotRadius;
    }

    FixationRing getRing() {
        return ring;
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (ring != null) {
            ring.stop();
        }
        new ArrayList<>(gazeNodes).forEach(this::forget);
        dots.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A dot: a light disc ringed with the main color and its number or letter; filled with the main color once joined;
     * a larger invisible circle catches the gaze and the mouse.
     */
    final class Dot {
        @Getter
        private final int index;
        @Getter
        private final Point2D point;
        @Getter
        private final Circle zone;
        private final Group disc;
        private final Circle back;
        private final Text text;
        @Getter
        private boolean joined;
        private double shaking;

        Dot(final int index, final Point2D point) {
            this.index = index;
            this.point = point;
            zone = new Circle(point.getX(), point.getY(), dotRadius * 1.45, Color.TRANSPARENT);
            back = new Circle(0, 0, dotRadius);
            back.setFill(DOT);
            back.setStyle("-fx-stroke: -gp-accent;");
            back.setStrokeWidth(Math.max(2, dotRadius * 0.14));
            text = new Text(labels.label(index));
            text.setFont(GameStyle.titleFont(dotRadius * 1.05));
            text.setTextOrigin(VPos.CENTER);
            text.setFill(INK);
            text.setX(-text.getLayoutBounds().getWidth() / 2 - text.getLayoutBounds().getMinX());
            disc = new Group(back, text);
            disc.setLayoutX(point.getX());
            disc.setLayoutY(point.getY());
            disc.setMouseTransparent(true);
            dotsLayer.getChildren().addAll(zone, disc);
            listen(this);
        }

        void join() {
            joined = true;
            back.setStyle("-fx-fill: -gp-accent; -fx-stroke: -gp-accent;");
            text.setStyle("-fx-fill: -gp-on-accent;");
        }

        void wrong() {
            back.setStyle("-fx-stroke: -gp-accent;");
            back.setFill(WRONG);
            text.setFill(Color.WHITE);
            shaking = SHAKE;
        }

        void shake(final double seconds) {
            if (shaking <= 0) {
                return;
            }
            shaking = Math.max(0, shaking - seconds);
            final double t = SHAKE - shaking;
            disc.setTranslateX(Math.sin(t * 50) * dotRadius * 0.25 * (shaking / SHAKE));
            if (shaking == 0 && !joined) {
                back.setFill(DOT);
                text.setFill(INK);
            }
        }

        boolean isHelped() {
            return disc.getScaleX() > 1;
        }
    }
}

package net.gazeplay.games.draw;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * "Gribouille" (name code Scribble, folder draw; reviewed on 12/11/2026, maintainer's rules): drawing with the eyes.
 * The gaze (or the mouse, without clicking) draws on a sheet, or on a blackboard; the pen lifts when the gaze leaves
 * the sheet, for example to look at the tool bar beside it, or when the eye tracker loses it for a moment. The line
 * follows every gaze point, smoothed so that the trembling of the eye tracker does not show. A fixation on a button of
 * the tool bar chooses a color (or, in the rainbow mode, the colors change by themselves along the line), the eraser,
 * one of three widths, a new sheet, or keeps the drawing as a picture in the folder "dessins" of GazePlay. No end: the
 * game is left by its menu.
 * <p>
 * One loop, in real seconds: the fixation on the buttons lasts the length of the settings (read at each step), nothing
 * can be drawn nor chosen before the delay of the settings nor while the menu of the game is open. (Before the review:
 * one gaze point out of five, not smoothed, in broken lines; a black frame of two thirds of the window, sized on the
 * whole window; the color changed at each entry in the frame, counted as a goal; nothing else to do: no colors, no
 * eraser, no new sheet, no saving.)
 */
@Slf4j
public class Scribble implements GameLifeCycle {

    /**
     * How much a new gaze point moves the pen (the rest of the way is kept: a smoothing of the trembling), and a mouse
     * point.
     */
    static final double GAZE_SMOOTHING = 0.35;
    static final double MOUSE_SMOOTHING = 0.8;
    /**
     * A source silent longer than this (seconds, e.g. the eye tracker losing the eyes for a blink) lifts the pen.
     */
    static final double LIFT_AFTER = 0.35;
    /**
     * The widths of the line, as parts of the height of the screen.
     */
    static final double[] WIDTHS = {0.006, 0.013, 0.026};
    /**
     * The seconds a message shows.
     */
    static final double MESSAGE = 2.5;

    private static final Color[] COLORS = {null, Color.rgb(229, 57, 53), Color.rgb(251, 140, 0), Color.rgb(253, 216, 53),
        Color.rgb(67, 160, 71), Color.rgb(30, 136, 229), Color.rgb(142, 36, 170), Color.rgb(240, 98, 146)};
    private static final DateTimeFormatter FILE_NAME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    enum Phase { WAIT, PLAY, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final ScribbleMode mode;
    @Getter
    private final ScribbleBackground background;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private Color color;
    @Getter
    private boolean eraser;
    @Getter
    private int widthIndex = 1;
    /**
     * Whether something has been drawn on this sheet, and whether the sheet has already counted as a goal.
     */
    @Getter
    private boolean drawn;
    private boolean counted;
    @Getter
    private int sheets;
    @Getter
    private File lastSaved;
    private double hue;
    private double clock;
    private double elapsed;
    private double messageLeft;

    private double width;
    private double height;
    @Getter
    private Canvas canvas;
    private GraphicsContext pen;
    private final Pen gazePen = new Pen();
    private final Pen mousePen = new Pen();

    private Group layer;
    private Group toolsLayer;
    private Group message;
    private FixationRing ring;
    @Getter
    private final List<Tool> tools = new ArrayList<>();
    private final List<Node> gazeNodes = new ArrayList<>();

    @Getter
    private Tool looked;
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
    Scribble(final IGameContext gameContext, final Stats stats, final ScribbleMode mode,
             final ScribbleBackground background, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.mode = mode;
        this.background = background;
        // Nothing is drawn at random: the seed is only kept, as for the other games
        if (gameSeed < 0) {
            stats.setGameSeed(new ReplayablePseudoRandom().getSeed());
        }
        color = background.getInk();
    }

    @Override
    public void launch() {
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        width = dimension.getWidth();
        height = dimension.getHeight();

        final Rectangle room = new Rectangle(0, 0, width, height);
        room.setStyle("-fx-fill: -gp-bg;");
        room.setMouseTransparent(true);

        // The tool bar at the right, two buttons a row; the sheet in the rest of the screen
        final double margin = height * 0.03;
        final double cell = (height - 2 * margin) / 7;
        final double toolsWidth = cell * 2;
        // Whole pixels: the picture kept has the size of the sheet
        canvas = new Canvas(Math.floor(width - toolsWidth - 3 * margin), Math.floor(height - 2 * margin));
        canvas.setLayoutX(margin);
        canvas.setLayoutY(margin);
        final Rectangle clip = new Rectangle(canvas.getWidth(), canvas.getHeight());
        clip.setArcWidth(height * 0.04);
        clip.setArcHeight(height * 0.04);
        canvas.setClip(clip);
        pen = canvas.getGraphicsContext2D();
        pen.setLineCap(StrokeLineCap.ROUND);
        pen.setLineJoin(StrokeLineJoin.ROUND);
        clearSheet();
        final Rectangle frame = new Rectangle(margin, margin, canvas.getWidth(), canvas.getHeight());
        frame.setArcWidth(height * 0.04);
        frame.setArcHeight(height * 0.04);
        frame.setFill(null);
        frame.setStyle("-fx-stroke: -gp-border; -fx-stroke-width: 3;");
        frame.setMouseTransparent(true);

        toolsLayer = new Group();
        ring = new FixationRing();
        ring.setMouseTransparent(true);
        message = new Group();
        message.setMouseTransparent(true);
        layer = new Group(room, canvas, frame, toolsLayer, ring, message);
        gameContext.getChildren().add(layer);
        buildTools(width - toolsWidth - margin, margin, cell);

        final EventHandler<Event> drawing = this::onCanvasEvent;
        canvas.addEventFilter(GazeEvent.ANY, drawing);
        canvas.addEventFilter(MouseEvent.ANY, drawing);
        gameContext.getGazeDeviceManager().addEventFilter(canvas);
        gazeNodes.add(canvas);

        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        sheets = 1;
        elapsed = 0;
        phase = Phase.WAIT;
        if (!manual) {
            loop.start();
        }
    }

    /**
     * The buttons of the tool bar, in a grid of two columns: the colors (palette mode), the eraser and the three
     * widths, a new sheet and keeping the drawing.
     */
    private void buildTools(final double x0, final double top, final double cell) {
        final double radius = cell * 0.4;
        // Centered in the height (the rainbow mode has no colors): the rows of the colors, of the eraser and the
        // widths, and of the last two buttons
        final int rows = (mode == ScribbleMode.PALETTE ? COLORS.length / 2 : 0) + 2 + 1;
        final double y0 = top + (7 - rows) * cell / 2;
        int slot = 0;
        if (mode == ScribbleMode.PALETTE) {
            for (final Color base : COLORS) {
                final Color tool = base == null ? background.getInk() : base;
                addTool(slot++, x0, y0, cell, radius, Tool.Kind.COLOR, tool, 0);
            }
        }
        addTool(slot++, x0, y0, cell, radius, Tool.Kind.ERASER, null, 0);
        for (int i = 0; i < WIDTHS.length; i++) {
            addTool(slot++, x0, y0, cell, radius, Tool.Kind.WIDTH, null, i);
        }
        // The last two buttons on a row of their own
        if (slot % 2 != 0) {
            slot++;
        }
        addTool(slot++, x0, y0, cell, radius, Tool.Kind.NEW_SHEET, null, 0);
        addTool(slot, x0, y0, cell, radius, Tool.Kind.SAVE, null, 0);
        paintTools();
    }

    private void addTool(final int slot, final double x0, final double y0, final double cell, final double radius,
                         final Tool.Kind kind, final Color toolColor, final int widthIndex) {
        final double cx = x0 + (slot % 2 + 0.5) * cell;
        final int row = slot / 2;
        final double cy = y0 + (row + 0.5) * cell;
        final Tool tool = new Tool(kind, toolColor, widthIndex, cx, cy, radius);
        tools.add(tool);
        toolsLayer.getChildren().add(tool.node);
        final EventHandler<Event> handler = event -> {
            if (event.getEventType() == MouseEvent.MOUSE_ENTERED || event.getEventType() == GazeEvent.GAZE_ENTERED) {
                lookAt(tool);
            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                || event.getEventType() == GazeEvent.GAZE_EXITED) {
                lookAway(tool);
            }
        };
        tool.disc.addEventFilter(MouseEvent.ANY, handler);
        tool.disc.addEventFilter(GazeEvent.ANY, handler);
        gameContext.getGazeDeviceManager().addEventFilter(tool.disc);
        gazeNodes.add(tool.disc);
    }

    private void paintTools() {
        for (final Tool tool : tools) {
            final boolean chosen = switch (tool.kind) {
                case COLOR -> !eraser && tool.color.equals(color);
                case ERASER -> eraser;
                case WIDTH -> tool.widthIndex == widthIndex;
                default -> false;
            };
            tool.choose(chosen);
        }
    }

    /**
     * The gaze and the mouse on the sheet: each move draws, a way out lifts the pen.
     */
    private void onCanvasEvent(final Event event) {
        final var type = event.getEventType();
        if (type == GazeEvent.GAZE_MOVED || type == GazeEvent.GAZE_ENTERED) {
            final GazeEvent gaze = (GazeEvent) event;
            draw(gazePen, gaze.getX(), gaze.getY(), GAZE_SMOOTHING);
        } else if (type == MouseEvent.MOUSE_MOVED || type == MouseEvent.MOUSE_ENTERED) {
            final MouseEvent mouse = (MouseEvent) event;
            draw(mousePen, mouse.getX(), mouse.getY(), MOUSE_SMOOTHING);
        } else if (type == GazeEvent.GAZE_EXITED) {
            gazePen.down = false;
        } else if (type == MouseEvent.MOUSE_EXITED) {
            mousePen.down = false;
        }
    }

    /**
     * A point of a source on the sheet: the pen goes part of the way to it and draws the line; it starts a new line
     * when it was lifted or when the source was silent for a moment.
     */
    void draw(final Pen source, final double x, final double y, final double smoothing) {
        if (phase != Phase.PLAY || Stats.isConfigMenuOpen()) {
            return;
        }
        if (!source.down || clock - source.last > LIFT_AFTER) {
            source.x = x;
            source.y = y;
            source.down = true;
            source.last = clock;
            dot(x, y);
            return;
        }
        source.last = clock;
        final double nx = source.x + (x - source.x) * smoothing;
        final double ny = source.y + (y - source.y) * smoothing;
        final double distance = Math.hypot(nx - source.x, ny - source.y);
        if (distance < 0.5) {
            return;
        }
        pen.setStroke(ink(distance));
        pen.setLineWidth(lineWidth());
        pen.strokeLine(source.x, source.y, nx, ny);
        source.x = nx;
        source.y = ny;
        drawn = true;
    }

    private void dot(final double x, final double y) {
        final double size = lineWidth();
        pen.setFill(ink(0));
        pen.fillOval(x - size / 2, y - size / 2, size, size);
        drawn = true;
    }

    private double lineWidth() {
        return height * WIDTHS[widthIndex] * (eraser ? 2 : 1);
    }

    /**
     * @return the ink now: the paper for the eraser, the color chosen, or in the rainbow mode a hue going round along
     * the line
     */
    private Color ink(final double distance) {
        if (eraser) {
            return background.getPaper();
        }
        if (mode == ScribbleMode.RAINBOW) {
            hue = (hue + distance * 0.35) % 360;
            return Color.hsb(hue, 0.72, background == ScribbleBackground.BOARD ? 1 : 0.92);
        }
        return color;
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        clock += seconds;
        if (messageLeft > 0) {
            messageLeft -= seconds;
            if (messageLeft <= 0) {
                message.getChildren().clear();
            }
        }
        if (phase == Phase.WAIT) {
            elapsed += seconds;
            if (elapsed >= configuration.getGameStartDelay() / 1000.0) {
                phase = Phase.PLAY;
            }
            return;
        }
        fixate(seconds);
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
            final Tool tool = looked;
            progress = 0;
            ring.stop();
            // Looked at again to choose once more
            looked = null;
            use(tool);
        }
    }

    private void showRing(final Tool tool) {
        ring.setSize(tool.radius * 2.5);
        ring.show(tool.x, tool.y);
    }

    void lookAt(final Tool tool) {
        if (looked == tool) {
            return;
        }
        looked = tool;
        progress = 0;
        ring.stop();
        if (phase == Phase.PLAY) {
            showRing(tool);
        }
    }

    void lookAway(final Tool tool) {
        if (looked == tool) {
            looked = null;
            progress = 0;
            ring.stop();
        }
    }

    /**
     * A button of the tool bar chosen.
     */
    void use(final Tool tool) {
        if (phase != Phase.PLAY) {
            return;
        }
        switch (tool.kind) {
            case COLOR -> {
                color = tool.color;
                eraser = false;
            }
            case ERASER -> eraser = true;
            case WIDTH -> widthIndex = tool.widthIndex;
            case NEW_SHEET -> newSheet();
            case SAVE -> save();
            default -> {
            }
        }
        paintTools();
    }

    /**
     * A new sheet: the drawing made counts as a goal (once), the sheet is cleared.
     */
    void newSheet() {
        countSheet();
        clearSheet();
        drawn = false;
        counted = false;
        sheets++;
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
    }

    private void countSheet() {
        if (drawn && !counted) {
            counted = true;
            stats.incrementNumberOfGoalsReached();
        }
    }

    private void clearSheet() {
        pen.setFill(background.getPaper());
        pen.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gazePen.down = false;
        mousePen.down = false;
    }

    /**
     * Keeps the drawing as a picture (PNG) in the folder "dessins" of GazePlay, under the date and the time.
     */
    void save() {
        final File folder = GazePlayDirectories.getDrawingsDirectory();
        final String base = "gribouille_" + LocalDateTime.now().format(FILE_NAME);
        File file = new File(folder, base + ".png");
        for (int i = 2; file.exists(); i++) {
            file = new File(folder, base + "-" + i + ".png");
        }
        try {
            final SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(background.getPaper());
            final WritableImage image = canvas.snapshot(parameters, null);
            ImageIO.write(toBufferedImage(image), "png", file);
            lastSaved = file;
            countSheet();
            gameContext.getSoundManager().add("data/goosegame/sounds/win.wav");
            say("ScribbleSaved");
        } catch (final IOException | RuntimeException e) {
            log.warn("The drawing could not be saved in {}", file, e);
            say("ScribbleNotSaved");
        }
    }

    /**
     * @return the picture as an image of AWT, for ImageIO (without the module javafx.swing)
     */
    private static BufferedImage toBufferedImage(final WritableImage image) {
        final int w = (int) image.getWidth();
        final int h = (int) image.getHeight();
        final BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final PixelReader reader = image.getPixelReader();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                out.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        return out;
    }

    private void say(final String key) {
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate(key), height * 0.035);
        pill.setLayoutX(canvas.getLayoutX() + (canvas.getWidth() - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY(canvas.getLayoutY() + height * 0.03);
        message.getChildren().setAll(pill);
        messageLeft = MESSAGE;
    }

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        loop.stop();
    }

    Pen getGazePen() {
        return gazePen;
    }

    Pen getMousePen() {
        return mousePen;
    }

    Group getMessage() {
        return message;
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (ring != null) {
            ring.stop();
        }
        for (final Node node : List.copyOf(gazeNodes)) {
            gameContext.getGazeDeviceManager().removeEventFilter(node);
        }
        gazeNodes.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * Where a source (the gaze, the mouse) has its pen, whether it is down, and when it last moved.
     */
    static final class Pen {
        double x;
        double y;
        boolean down;
        double last;

        boolean isDown() {
            return down;
        }
    }

    /**
     * A round button of the tool bar.
     */
    final class Tool {
        enum Kind { COLOR, ERASER, WIDTH, NEW_SHEET, SAVE }

        @Getter
        private final Kind kind;
        @Getter
        private final Color color;
        @Getter
        private final int widthIndex;
        private final double x;
        private final double y;
        private final double radius;
        private final Group node;
        private final Circle disc;
        private final Circle mark;

        Tool(final Kind kind, final Color color, final int widthIndex, final double x, final double y,
             final double radius) {
            this.kind = kind;
            this.color = color;
            this.widthIndex = widthIndex;
            this.x = x;
            this.y = y;
            this.radius = radius;
            disc = new Circle(x, y, radius);
            disc.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
            mark = new Circle(x, y, radius * 1.12);
            mark.setFill(null);
            mark.setStyle("-fx-stroke: -gp-accent;");
            mark.setStrokeWidth(Math.max(3, radius * 0.12));
            mark.setMouseTransparent(true);
            node = new Group(disc, mark);
            final Node picture = picture();
            if (picture != null) {
                picture.setMouseTransparent(true);
                node.getChildren().add(1, picture);
            }
        }

        /**
         * @return what the button shows: its color, an eraser, a dot of its width, or an icon
         */
        private Node picture() {
            return switch (kind) {
                case COLOR -> {
                    final Circle swatch = new Circle(x, y, radius * 0.72, color);
                    swatch.setStroke(Color.rgb(0, 0, 0, 0.2));
                    yield swatch;
                }
                case ERASER -> {
                    final Rectangle rubber = new Rectangle(x - radius * 0.5, y - radius * 0.28, radius, radius * 0.56);
                    rubber.setArcWidth(radius * 0.2);
                    rubber.setArcHeight(radius * 0.2);
                    rubber.setFill(Color.rgb(244, 143, 177));
                    rubber.setStroke(Color.rgb(120, 60, 90));
                    rubber.setStrokeWidth(Math.max(1, radius * 0.05));
                    rubber.setRotate(-30);
                    yield rubber;
                }
                case WIDTH -> {
                    final Circle size = new Circle(x, y, Math.max(2, height * WIDTHS[widthIndex] * 0.9));
                    size.setStyle("-fx-fill: -gp-text;");
                    yield size;
                }
                case NEW_SHEET -> icon("add");
                case SAVE -> icon("save");
            };
        }

        private Node icon(final String name) {
            final double size = radius * 1.0;
            final SVGPath icon = GameStyle.icon(name, size);
            icon.setStyle("-fx-fill: -gp-accent;");
            final Group group = new Group(icon);
            group.setLayoutX(x - size / 2);
            group.setLayoutY(y - size / 2);
            return group;
        }

        void choose(final boolean chosen) {
            mark.setVisible(chosen);
        }

        boolean isChosen() {
            return mark.isVisible();
        }
    }
}

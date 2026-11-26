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
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Rotate;
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
import java.util.stream.IntStream;

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
     * The widths of the eraser for each width of the line, as parts of the height of the screen: wide enough for the
     * eraser drawn under the gaze to be seen (point 46; before: twice the line, 0.012 to 0.052).
     */
    static final double[] ERASER_WIDTHS = {0.025, 0.045, 0.07};
    /**
     * The seconds a message shows.
     */
    static final double MESSAGE = 2.5;
    /**
     * The seconds the question "Erase the drawing?" waits for an answer before it goes away (tester's feedback of
     * 23/11/2026, point 45: the new sheet erased everything at once).
     */
    static final double ASK = 5;
    /**
     * The height of the tool bar, in rows of buttons: 7 rows, half a row of space, the row of the new sheet alone.
     */
    static final double TOOL_ROWS = 8.5;
    /**
     * Line by fixation (point 48): how far the pen may move (part of the height of the screen) and still be a fixation.
     */
    static final double FIXATION_RADIUS = 0.035;

    private static final Color[] COLORS = {null, Color.rgb(229, 57, 53), Color.rgb(251, 140, 0), Color.rgb(253, 216, 53),
        Color.rgb(67, 160, 71), Color.rgb(30, 136, 229), Color.rgb(142, 36, 170), Color.rgb(240, 98, 146)};
    /**
     * The picture of the rainbow pencil: the hues of its line.
     */
    private static final LinearGradient RAINBOW = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
        IntStream.rangeClosed(0, 6).mapToObj(i -> new Stop(i / 6.0, Color.hsb(i * 50, 0.72, 0.95))).toList());
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
    private final ScribbleTrace trace;

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
    /**
     * The seconds left to answer the question "Erase the drawing?", 0 when it is not asked.
     */
    @Getter
    private double askLeft;
    private Tool newSheetTool;
    private Tool confirmTool;
    private Tool cancelTool;

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
    /**
     * The eraser drawn on the sheet while it is chosen, its tip on the pen (tester's feedback of 23/11/2026, point 46:
     * the eraser button alone did not say much; maintainer's choice of 26/11/2026, like the coin of the scratch cards).
     */
    @Getter
    private Group rubber;
    private double rubberSize;
    private boolean rubberPlaced;
    private FixationRing ring;
    /**
     * Line by fixation: the ring of a fixation on the sheet, the source fixing, and the hollow circle following it while
     * the pencil is up.
     */
    private FixationRing sheetRing;
    private Pen fixing;
    @Getter
    private Circle liftedPencil;
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
    Scribble(final IGameContext gameContext, final Stats stats, final ScribbleMode mode, final ScribbleTrace trace,
             final ScribbleBackground background, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.mode = mode;
        this.trace = trace;
        this.background = background;
        mousePen.gaze = false;
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
        final double cell = (height - 2 * margin) / TOOL_ROWS;
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
        rubber = new Group();
        rubber.setMouseTransparent(true);
        rubber.setVisible(false);
        // Seen on the sheet only, not over the tool bar
        final Group rubberHolder = new Group(rubber);
        rubberHolder.setMouseTransparent(true);
        rubberHolder.setClip(new Rectangle(margin, margin, canvas.getWidth(), canvas.getHeight()));
        liftedPencil = new Circle(Math.max(height * 0.012, 4));
        liftedPencil.setFill(null);
        liftedPencil.setStrokeWidth(Math.max(2, height * 0.004));
        liftedPencil.setMouseTransparent(true);
        liftedPencil.setVisible(false);
        rubberHolder.getChildren().add(liftedPencil);
        sheetRing = new FixationRing();
        sheetRing.setMouseTransparent(true);
        sheetRing.setSize(height * FIXATION_RADIUS * 2.4);
        layer = new Group(room, canvas, rubberHolder, frame, toolsLayer, sheetRing, ring, message);
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
     * The buttons of the tool bar, in a grid of two columns: the colors (palette mode) or the rainbow pencil (rainbow
     * mode), the eraser and the three widths, keeping the drawing; then, apart under half a row of space, the new
     * sheet alone in the middle of its row (point 45), whose ✔ and ✖ take its row while the question is asked.
     */
    private void buildTools(final double x0, final double top, final double cell) {
        final double radius = cell * 0.4;
        record Spec(Tool.Kind kind, Color color, int widthIndex) {
        }
        final List<Spec> grid = new ArrayList<>();
        if (mode == ScribbleMode.PALETTE) {
            for (final Color base : COLORS) {
                grid.add(new Spec(Tool.Kind.COLOR, base == null ? background.getInk() : base, 0));
            }
        } else {
            // Without colors, nothing left the eraser (tester's feedback of 23/11/2026, point 44)
            grid.add(new Spec(Tool.Kind.RAINBOW, null, 0));
        }
        grid.add(new Spec(Tool.Kind.ERASER, null, 0));
        for (int i = 0; i < WIDTHS.length; i++) {
            grid.add(new Spec(Tool.Kind.WIDTH, null, i));
        }
        grid.add(new Spec(Tool.Kind.SAVE, null, 0));
        final int rows = (grid.size() + 1) / 2;
        // Centered in the height (the rainbow mode has fewer rows)
        final double y0 = top + (TOOL_ROWS - (rows + 1.5)) * cell / 2;
        for (int slot = 0; slot < grid.size(); slot++) {
            final Spec spec = grid.get(slot);
            // Alone on its row, the last button is in its middle
            final boolean alone = slot == grid.size() - 1 && slot % 2 == 0;
            final double cx = x0 + (alone ? 1 : slot % 2 + 0.5) * cell;
            // The row of the slot (two buttons a row)
            final int row = Math.floorDiv(slot, 2);
            final double cy = y0 + (row + 0.5) * cell;
            addTool(cx, cy, radius, spec.kind(), spec.color(), spec.widthIndex(), true);
        }
        final double lastY = y0 + (rows + 1) * cell;
        newSheetTool = addTool(x0 + cell, lastY, radius, Tool.Kind.NEW_SHEET, null, 0, true);
        confirmTool = addTool(x0 + cell / 2, lastY, radius, Tool.Kind.CONFIRM, null, 0, false);
        cancelTool = addTool(x0 + cell * 1.5, lastY, radius, Tool.Kind.CANCEL, null, 0, false);
        paintTools();
    }

    private Tool addTool(final double cx, final double cy, final double radius, final Tool.Kind kind,
                         final Color toolColor, final int widthIndex, final boolean shown) {
        final Tool tool = new Tool(kind, toolColor, widthIndex, cx, cy, radius);
        tools.add(tool);
        if (shown) {
            toolsLayer.getChildren().add(tool.node);
        }
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
        return tool;
    }

    private void paintTools() {
        // The dots of the widths show what is drawn: the color chosen, or the rainbow (tester's feedback of
        // 23/11/2026, point 47); with the eraser, the color it will go back to
        final Paint dotPaint = mode == ScribbleMode.RAINBOW ? RAINBOW : color;
        for (final Tool tool : tools) {
            if (tool.dot != null) {
                tool.dot.setFill(dotPaint);
            }
            final boolean chosen = switch (tool.kind) {
                case COLOR -> !eraser && tool.color.equals(color);
                case RAINBOW -> !eraser;
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
            leaveSheet(gazePen);
        } else if (type == MouseEvent.MOUSE_EXITED) {
            leaveSheet(mousePen);
        }
    }

    /**
     * A source out of the sheet: its pencil lifts (and, by fixation, its fixation stops).
     */
    void leaveSheet(final Pen source) {
        source.down = false;
        source.onSheet = false;
        source.tracked = false;
        if (fixing == source) {
            stopSheetFixation();
            liftedPencil.setVisible(false);
        }
    }

    /**
     * A point of a source on the sheet: the pen goes part of the way to it and draws the line; it starts a new line
     * when it was lifted or when the source was silent for a moment. By fixation, see {@link #drawByFixation}.
     */
    void draw(final Pen source, final double x, final double y, final double smoothing) {
        if (phase != Phase.PLAY || Stats.isConfigMenuOpen()) {
            return;
        }
        if (trace == ScribbleTrace.FIXATION) {
            drawByFixation(source, x, y, smoothing);
            return;
        }
        if (!source.down || clock - source.last > LIFT_AFTER) {
            source.x = x;
            source.y = y;
            source.down = true;
            source.last = clock;
            dot(x, y);
            moveRubber(x, y);
            return;
        }
        source.last = clock;
        strokeTo(source, x, y, smoothing);
    }

    /**
     * The pen goes part of the way to a point and draws the line.
     */
    private void strokeTo(final Pen source, final double x, final double y, final double smoothing) {
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
        moveRubber(nx, ny);
    }

    /**
     * A point of a source on the sheet, the line by fixation (tester's feedback of 23/11/2026, point 48; maintainer's
     * choices of 26/11/2026): the pencil up, a hollow circle follows the source and nothing is drawn; a fixation (the
     * pen staying within {@link #FIXATION_RADIUS} for the length of the settings, see {@link #fixateOnSheet}) puts it
     * down where it is, and the line follows; another fixation lifts it. The eye tracker silent for a moment lifts it
     * too, not a still mouse: a still mouse is a fixation.
     */
    private void drawByFixation(final Pen source, final double x, final double y, final double smoothing) {
        if (source.gaze && clock - source.last > LIFT_AFTER) {
            source.down = false;
            source.tracked = false;
        }
        source.last = clock;
        source.onSheet = true;
        if (source.down) {
            strokeTo(source, x, y, smoothing);
        } else if (!source.tracked) {
            source.x = x;
            source.y = y;
            source.tracked = true;
        } else {
            source.x += (x - source.x) * smoothing;
            source.y += (y - source.y) * smoothing;
        }
        moveRubber(source.x, source.y);
        // A new fixation when the pen is out of the circle of the last one
        final double radius = height * FIXATION_RADIUS;
        if (!source.anchored || Math.hypot(source.x - source.ax, source.y - source.ay) > radius) {
            source.ax = source.x;
            source.ay = source.y;
            source.anchorStart = clock;
            source.anchored = true;
            source.needsMove = false;
            if (fixing == source) {
                stopSheetFixation();
            }
        }
        if (fixing != source && fixing != null) {
            stopSheetFixation();
            liftedPencil.setVisible(false);
        }
        fixing = source;
        showLiftedPencil();
    }

    /**
     * The hollow circle of the pencil up, of the ink now, where the source fixing is; hidden while it draws, and with
     * the eraser (the eraser drawn follows instead).
     */
    private void showLiftedPencil() {
        final boolean shown = fixing != null && fixing.onSheet && !fixing.down && !eraser;
        liftedPencil.setVisible(shown);
        if (shown) {
            liftedPencil.setStroke(mode == ScribbleMode.RAINBOW ? RAINBOW : color);
            liftedPencil.setCenterX(canvas.getLayoutX() + fixing.x);
            liftedPencil.setCenterY(canvas.getLayoutY() + fixing.y);
        }
    }

    /**
     * One step of a fixation on the sheet: its ring fills with the time the pen stays; full, the pencil goes down (a
     * dot where it is) or up. Another fixation needs the pen to move first. Stopped when the eye tracker loses the
     * eyes.
     */
    private void fixateOnSheet() {
        final Pen source = fixing;
        if (source == null || !source.onSheet || !source.anchored || source.needsMove) {
            return;
        }
        if (source.gaze && clock - source.last > LIFT_AFTER) {
            source.down = false;
            source.tracked = false;
            source.anchored = false;
            stopSheetFixation();
            liftedPencil.setVisible(false);
            return;
        }
        final double progress = (clock - source.anchorStart) * 1000 / Math.max(1, configuration.getFixationLength());
        if (!sheetRing.isVisible()) {
            sheetRing.show(canvas.getLayoutX() + source.ax, canvas.getLayoutY() + source.ay);
        }
        sheetRing.setProgress(Math.min(1, progress));
        if (progress >= 1) {
            stopSheetFixation();
            source.needsMove = true;
            if (source.down) {
                source.down = false;
            } else {
                // Down under the hollow circle, where the pen is now
                source.down = true;
                dot(source.x, source.y);
            }
            showLiftedPencil();
        }
    }

    private void stopSheetFixation() {
        sheetRing.stop();
    }

    /**
     * Puts the tip of the eraser drawn at a point of the sheet, when the eraser is chosen.
     */
    private void moveRubber(final double x, final double y) {
        if (!eraser) {
            return;
        }
        rubber.setLayoutX(canvas.getLayoutX() + x);
        rubber.setLayoutY(canvas.getLayoutY() + y);
        rubberPlaced = true;
    }

    /**
     * Shows the eraser drawn while the eraser is chosen, of its width now: at the middle of the sheet the first time,
     * then where it was left (looking at the tool bar does not move it).
     */
    private void updateRubber() {
        rubber.setVisible(eraser);
        if (!eraser) {
            return;
        }
        final double size = lineWidth();
        if (size != rubberSize) {
            rubberSize = size;
            rubber.getChildren().setAll(background == ScribbleBackground.BOARD
                ? boardBrush(size * 1.3) : rubberPicture(size));
        }
        if (!rubberPlaced) {
            moveRubber(canvas.getWidth() / 2, canvas.getHeight() / 2);
        }
    }

    /**
     * @return an eraser seen from above, its rounded pink tip of this width around (0, 0), its blue sleeve going up to
     * the right, and a soft shadow
     */
    private static Node rubberPicture(final double size) {
        final double length = size * 2.4;
        final Rectangle shadow = new Rectangle(-size / 2 + size * 0.1, -size / 2 + size * 0.14, length, size);
        shadow.setArcWidth(size * 0.5);
        shadow.setArcHeight(size * 0.5);
        shadow.setFill(Color.rgb(0, 0, 0, 0.22));
        final Rectangle tip = new Rectangle(-size / 2, -size / 2, size * 1.6, size);
        tip.setArcWidth(size * 0.5);
        tip.setArcHeight(size * 0.5);
        tip.setFill(Color.rgb(244, 143, 177));
        tip.setStroke(Color.rgb(120, 60, 90));
        tip.setStrokeWidth(Math.max(1, size * 0.06));
        final Rectangle sleeve = new Rectangle(size * 0.95, -size * 0.54, length - size * 1.45, size * 1.08);
        sleeve.setArcWidth(size * 0.2);
        sleeve.setArcHeight(size * 0.2);
        sleeve.setFill(Color.rgb(66, 133, 244));
        sleeve.setStroke(Color.rgb(28, 68, 140));
        sleeve.setStrokeWidth(Math.max(1, size * 0.06));
        final Group picture = new Group(shadow, tip, sleeve);
        picture.getTransforms().add(new Rotate(-40, 0, 0));
        return picture;
    }

    /**
     * @return a board brush to wipe the chalk off the blackboard (maintainer, 26/11/2026: clearer than an eraser
     * there), of this width, the middle of its grey felt on (0, 0), its wooden back above, a little tilted, with a
     * soft shadow
     */
    static Group boardBrush(final double width) {
        final double felt = width * 0.22;
        final double back = width * 0.34;
        final Rectangle shadow = new Rectangle(-width / 2 + width * 0.05, -felt / 2 - back + width * 0.08, width,
            felt + back);
        shadow.setArcWidth(width * 0.12);
        shadow.setArcHeight(width * 0.12);
        shadow.setFill(Color.rgb(0, 0, 0, 0.25));
        final Rectangle pad = new Rectangle(-width / 2, -felt / 2, width, felt);
        pad.setArcWidth(width * 0.06);
        pad.setArcHeight(width * 0.06);
        pad.setFill(Color.rgb(84, 86, 96));
        pad.setStroke(Color.rgb(40, 40, 46));
        pad.setStrokeWidth(Math.max(1, width * 0.025));
        final Rectangle wood = new Rectangle(-width * 0.46, -felt / 2 - back, width * 0.92, back + width * 0.02);
        wood.setArcWidth(width * 0.2);
        wood.setArcHeight(width * 0.2);
        wood.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(214, 160, 98)), new Stop(1, Color.rgb(160, 104, 52))));
        wood.setStroke(Color.rgb(104, 64, 30));
        wood.setStrokeWidth(Math.max(1, width * 0.025));
        final Rectangle grain = new Rectangle(-width * 0.3, -felt / 2 - back * 0.62, width * 0.6, back * 0.12);
        grain.setArcWidth(back * 0.12);
        grain.setArcHeight(back * 0.12);
        grain.setFill(Color.rgb(120, 76, 36, 0.45));
        final Group picture = new Group(shadow, wood, grain, pad);
        picture.getTransforms().add(new Rotate(-12, 0, 0));
        return picture;
    }

    private void dot(final double x, final double y) {
        final double size = lineWidth();
        pen.setFill(ink(0));
        pen.fillOval(x - size / 2, y - size / 2, size, size);
        drawn = true;
    }

    private double lineWidth() {
        return height * (eraser ? ERASER_WIDTHS : WIDTHS)[widthIndex];
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
        // The question stays as long as ✔ and ✖ (they take it away)
        if (messageLeft > 0 && askLeft <= 0) {
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
        // The time to answer stops while ✔ or ✖ is being chosen: they never go away under the ring
        if (askLeft > 0 && looked != confirmTool && looked != cancelTool) {
            askLeft -= seconds;
            if (askLeft <= 0) {
                endAsking();
            }
        }
        fixate(seconds);
        if (trace == ScribbleTrace.FIXATION) {
            fixateOnSheet();
        }
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
        // Any other button forgets the question
        if (askLeft > 0 && tool.kind != Tool.Kind.CONFIRM) {
            endAsking();
        }
        switch (tool.kind) {
            case COLOR -> {
                color = tool.color;
                eraser = false;
            }
            case RAINBOW -> eraser = false;
            case ERASER -> eraser = true;
            case WIDTH -> widthIndex = tool.widthIndex;
            case NEW_SHEET -> ask();
            case CONFIRM -> {
                endAsking();
                newSheet();
            }
            case SAVE -> save();
            default -> {
            }
        }
        paintTools();
        updateRubber();
        showLiftedPencil();
    }

    /**
     * The new sheet chosen: nothing is erased yet, its row shows ✔ (erase) and ✖ (keep), to choose by another fixation,
     * with the question at the top of the sheet; without an answer, all goes back after {@link #ASK} seconds. The ✔
     * and ✖ are on each side of the place of the new sheet, not under the gaze still on it.
     */
    private void ask() {
        toolsLayer.getChildren().remove(newSheetTool.node);
        toolsLayer.getChildren().addAll(confirmTool.node, cancelTool.node);
        askLeft = ASK;
        say("ScribbleEraseQuestion");
        messageLeft = ASK;
    }

    private void endAsking() {
        askLeft = 0;
        toolsLayer.getChildren().removeAll(confirmTool.node, cancelTool.node);
        if (!toolsLayer.getChildren().contains(newSheetTool.node)) {
            toolsLayer.getChildren().add(newSheetTool.node);
        }
        if (looked == confirmTool || looked == cancelTool) {
            lookAway(looked);
        }
        message.getChildren().clear();
        messageLeft = 0;
    }

    /**
     * @return whether the question "Erase the drawing?" is asked: ✔ and ✖ shown in place of the new sheet
     */
    boolean isAsking() {
        return askLeft > 0;
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
        if (sheetRing != null) {
            sheetRing.stop();
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
        /**
         * Line by fixation: whether it is the gaze (not the mouse), on the sheet, followed while the pencil is up, and
         * its fixation: its center, its start, and whether the pen must move before another one.
         */
        boolean gaze = true;
        boolean onSheet;
        boolean tracked;
        boolean anchored;
        boolean needsMove;
        double ax;
        double ay;
        double anchorStart;

        boolean isDown() {
            return down;
        }
    }

    /**
     * A round button of the tool bar.
     */
    final class Tool {
        enum Kind { COLOR, RAINBOW, ERASER, WIDTH, NEW_SHEET, SAVE, CONFIRM, CANCEL }

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
        /**
         * The dot of a width button, of the color chosen (point 47); null for the other buttons.
         */
        private Circle dot;

        Tool(final Kind kind, final Color color, final int widthIndex, final double x, final double y,
             final double radius) {
            this.kind = kind;
            this.color = color;
            this.widthIndex = widthIndex;
            this.x = x;
            this.y = y;
            this.radius = radius;
            disc = new Circle(x, y, radius);
            // Light or dark as chosen in the settings, whatever the theme (24/11/2026)
            disc.setStyle(GameStyle.BUTTON_STYLE);
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
         * @return what the button shows: its color, the rainbow, an eraser, a dot of its width, or an icon
         */
        private Node picture() {
            return switch (kind) {
                case COLOR -> {
                    final Circle swatch = new Circle(x, y, radius * 0.72, color);
                    swatch.setStroke(Color.rgb(0, 0, 0, 0.2));
                    yield swatch;
                }
                case RAINBOW -> {
                    final Circle swatch = new Circle(x, y, radius * 0.72, RAINBOW);
                    swatch.setStroke(Color.rgb(0, 0, 0, 0.2));
                    yield swatch;
                }
                case ERASER -> {
                    if (background == ScribbleBackground.BOARD) {
                        // The brush, a little lower: its back is above its felt
                        final Group brush = boardBrush(radius * 1.25);
                        brush.setLayoutX(x);
                        brush.setLayoutY(y + radius * 0.12);
                        yield brush;
                    }
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
                    // Of the color chosen (see paintTools), with a ring of the text color of the button, so that a
                    // white dot is seen on a light button and a black one on a dark button
                    dot = new Circle(x, y, Math.max(2, height * WIDTHS[widthIndex] * 0.9));
                    dot.setStyle("-fx-stroke: -gp-button-text;");
                    dot.setStrokeWidth(Math.max(1, radius * 0.04));
                    yield dot;
                }
                // A bin: clearer than a plus (maintainer, 26/11/2026)
                case NEW_SHEET -> icon("delete");
                case SAVE -> icon("save");
                case CONFIRM -> mark(Color.web(GameStyle.SUCCESS_COLOR), -0.36, 0.02, -0.1, 0.28, 0.38, -0.26);
                case CANCEL -> {
                    final Group cross = new Group(
                        mark(Color.rgb(229, 57, 53), -0.28, -0.28, 0.28, 0.28),
                        mark(Color.rgb(229, 57, 53), -0.28, 0.28, 0.28, -0.28));
                    yield cross;
                }
            };
        }

        /**
         * @return a thick line through these points (parts of the radius from the center): the ✔ or half the ✖
         */
        private Node mark(final Color stroke, final double... points) {
            final Polyline line = new Polyline();
            for (int i = 0; i < points.length; i += 2) {
                line.getPoints().addAll(x + points[i] * radius, y + points[i + 1] * radius);
            }
            line.setStroke(stroke);
            line.setStrokeWidth(radius * 0.16);
            line.setStrokeLineCap(StrokeLineCap.ROUND);
            line.setStrokeLineJoin(StrokeLineJoin.ROUND);
            return line;
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

        double getX() {
            return x;
        }

        double getY() {
            return y;
        }

        Circle getDisc() {
            return disc;
        }

        Group getNode() {
            return node;
        }

        Circle getDot() {
            return dot;
        }
    }

    /**
     * @return the settings of the menu (Esc) acting on this game (17/11/2026): the others are greyed
     */
    @Override
    public java.util.Set<net.gazeplay.MenuSetting> menuSettings() {
        return net.gazeplay.MenuSetting.fixationWithSharedRing(net.gazeplay.MenuSetting.GAME_BUTTONS);
    }
}

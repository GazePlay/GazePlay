package net.gazeplay.games.draw;

import javafx.geometry.Dimension2D;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Gribouille" (reviewed on 12/11/2026, maintainer's rules): the start delay, the line drawn and smoothed, the pen
 * lifted, the tool bar (colors, eraser, widths, new sheet, keeping the drawing), the goals, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class ScribbleTest {

    /** The games launched by a test, closed after it (18/11/2026). */
    @org.junit.jupiter.api.extension.RegisterExtension
    final net.gazeplay.games.LaunchedGames launchedGames = new net.gazeplay.games.LaunchedGames();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    private final Configuration configuration = mock(Configuration.class, withSettings().stubOnly());

    @Mock
    Translator translator;

    @Mock
    Stats stats;

    private final Pane gameArea = new Pane();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1600, 900));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getFixationLength()).thenReturn(500);
        when(configuration.getGameStartDelay()).thenReturn(0);
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private Scribble launched(final ScribbleMode mode, final ScribbleBackground background) throws Exception {
        return launched(mode, ScribbleTrace.FREE, background);
    }

    private Scribble launched(final ScribbleMode mode, final ScribbleTrace trace, final ScribbleBackground background)
        throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final Scribble game = new Scribble(gameContext, stats, mode, trace, background, 3);
            game.setManual();
            launchedGames.add(game).launch();
            game.step(0.02);
            return game;
        });
    }

    private static Scribble.Tool tool(final Scribble game, final Scribble.Tool.Kind kind) {
        return game.getTools().stream().filter(tool -> tool.getKind() == kind).findFirst().orElseThrow();
    }

    private static Color pixel(final Scribble game, final double x, final double y) {
        final WritableImage image = game.getCanvas().snapshot(null, null);
        return image.getPixelReader().getColor((int) x, (int) y);
    }

    private static void line(final Scribble game, final Scribble.Pen pen, final double smoothing) {
        for (int x = 100; x <= 400; x += 10) {
            game.draw(pen, x, 200, smoothing);
            game.step(0.01);
        }
    }

    @Test
    void shouldDrawNothingDuringTheStartDelay() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final Scribble game = launched(ScribbleMode.PALETTE, ScribbleBackground.SHEET);
        fx(() -> {
            assertEquals(Scribble.Phase.WAIT, game.getPhase());
            game.draw(game.getGazePen(), 100, 100, Scribble.GAZE_SMOOTHING);
            assertFalse(game.isDrawn());
            for (int i = 0; i < 60; i++) {
                game.step(0.02);
            }
            assertEquals(Scribble.Phase.PLAY, game.getPhase());
            game.draw(game.getGazePen(), 100, 100, Scribble.GAZE_SMOOTHING);
            assertTrue(game.isDrawn());
            return null;
        });
    }

    @Test
    void shouldDrawASmoothedLineWithTheInk() throws Exception {
        final Scribble game = launched(ScribbleMode.PALETTE, ScribbleBackground.SHEET);
        fx(() -> {
            final Scribble.Pen pen = game.getGazePen();
            game.draw(pen, 100, 200, Scribble.GAZE_SMOOTHING);
            assertTrue(pen.isDown());
            game.draw(pen, 200, 200, Scribble.GAZE_SMOOTHING);
            // Only part of the way: the trembling is smoothed
            assertEquals(100 + 100 * Scribble.GAZE_SMOOTHING, pen.x, 1e-9);
            line(game, pen, Scribble.GAZE_SMOOTHING);
            assertEquals(ScribbleBackground.SHEET.getInk(), pixel(game, 300, 200));
            assertEquals(ScribbleBackground.SHEET.getPaper(), pixel(game, 300, 400));
            return null;
        });
    }

    @Test
    void shouldLiftThePenWhenTheSourceIsSilent() throws Exception {
        final Scribble game = launched(ScribbleMode.PALETTE, ScribbleBackground.SHEET);
        fx(() -> {
            final Scribble.Pen pen = game.getGazePen();
            game.draw(pen, 100, 200, Scribble.GAZE_SMOOTHING);
            game.step(Scribble.LIFT_AFTER + 0.05);
            game.draw(pen, 500, 500, Scribble.GAZE_SMOOTHING);
            // A new line starts where the gaze is: nothing joins the two points
            assertEquals(500, pen.x, 1e-9);
            assertEquals(ScribbleBackground.SHEET.getPaper(), pixel(game, 300, 350));
            return null;
        });
    }

    @Test
    void shouldKeepTheGazeAndTheMouseApart() throws Exception {
        final Scribble game = launched(ScribbleMode.PALETTE, ScribbleBackground.SHEET);
        fx(() -> {
            game.draw(game.getGazePen(), 100, 100, Scribble.GAZE_SMOOTHING);
            game.draw(game.getMousePen(), 600, 600, Scribble.MOUSE_SMOOTHING);
            game.draw(game.getGazePen(), 120, 100, Scribble.GAZE_SMOOTHING);
            assertEquals(100 + 20 * Scribble.GAZE_SMOOTHING, game.getGazePen().x, 1e-9);
            assertEquals(600, game.getMousePen().x, 1e-9);
            return null;
        });
    }

    @Test
    void shouldChooseAToolAfterTheFixation() throws Exception {
        final Scribble game = launched(ScribbleMode.PALETTE, ScribbleBackground.SHEET);
        fx(() -> {
            final Scribble.Tool red = game.getTools().stream()
                .filter(tool -> tool.getKind() == Scribble.Tool.Kind.COLOR)
                .skip(1).findFirst().orElseThrow();
            game.lookAt(red);
            game.step(0.3);
            assertNotEquals(red.getColor(), game.getColor());
            game.step(0.25);
            assertEquals(red.getColor(), game.getColor());
            assertTrue(red.isChosen());
            assertNull(game.getLooked());

            final Scribble.Tool wide = game.getTools().stream()
                .filter(tool -> tool.getKind() == Scribble.Tool.Kind.WIDTH && tool.getWidthIndex() == 2)
                .findFirst().orElseThrow();
            game.lookAt(wide);
            game.step(0.2);
            game.lookAway(wide);
            game.step(0.5);
            assertEquals(1, game.getWidthIndex());
            game.use(wide);
            assertEquals(2, game.getWidthIndex());
            return null;
        });
    }

    @Test
    void shouldEraseWithThePaper() throws Exception {
        final Scribble game = launched(ScribbleMode.PALETTE, ScribbleBackground.BOARD);
        fx(() -> {
            line(game, game.getGazePen(), Scribble.GAZE_SMOOTHING);
            assertEquals(ScribbleBackground.BOARD.getInk(), pixel(game, 300, 200));
            game.use(tool(game, Scribble.Tool.Kind.ERASER));
            assertTrue(game.isEraser());
            game.step(1);
            line(game, game.getGazePen(), Scribble.GAZE_SMOOTHING);
            assertEquals(ScribbleBackground.BOARD.getPaper(), pixel(game, 300, 200));
            return null;
        });
    }

    /**
     * Tester's feedback of 23/11/2026, point 46 (maintainer's choice of 26/11/2026): an eraser drawn follows the pen
     * while the eraser is chosen, and erases as wide as its tip.
     */
    @Test
    void shouldShowTheEraserUnderThePen() throws Exception {
        for (final ScribbleMode mode : ScribbleMode.values()) {
            final Scribble game = launched(mode, ScribbleBackground.SHEET);
            fx(() -> {
                assertFalse(game.getRubber().isVisible());
                line(game, game.getGazePen(), Scribble.GAZE_SMOOTHING);
                game.use(tool(game, Scribble.Tool.Kind.ERASER));
                assertTrue(game.getRubber().isVisible());
                // At first in the middle of the sheet
                final double left = game.getCanvas().getLayoutX();
                final double top = game.getCanvas().getLayoutY();
                assertEquals(left + game.getCanvas().getWidth() / 2, game.getRubber().getLayoutX(), 1e-9);
                game.step(1);
                final Scribble.Pen pen = game.getGazePen();
                line(game, pen, Scribble.GAZE_SMOOTHING);
                assertEquals(left + pen.x, game.getRubber().getLayoutX(), 1e-9);
                assertEquals(top + pen.y, game.getRubber().getLayoutY(), 1e-9);
                // The first line erased, also off its middle
                assertEquals(ScribbleBackground.SHEET.getPaper(), pixel(game, 300, 200));
                assertEquals(ScribbleBackground.SHEET.getPaper(), pixel(game, 300, 204));
                // Looking at the tool bar does not move it; a width changes its size
                final double x = game.getRubber().getLayoutX();
                final double before = game.getRubber().getBoundsInLocal().getHeight();
                game.use(game.getTools().stream()
                    .filter(tool -> tool.getKind() == Scribble.Tool.Kind.WIDTH && tool.getWidthIndex() == 2)
                    .findFirst().orElseThrow());
                assertEquals(x, game.getRubber().getLayoutX(), 1e-9);
                assertTrue(game.getRubber().getBoundsInLocal().getHeight() > before);
                // Back to drawing: hidden
                final Scribble.Tool pencil = game.getTools().stream()
                    .filter(tool -> tool.getKind() == Scribble.Tool.Kind.COLOR
                        || tool.getKind() == Scribble.Tool.Kind.RAINBOW)
                    .findFirst().orElseThrow();
                game.use(pencil);
                assertFalse(game.getRubber().isVisible());
                return null;
            });
        }
        // On the blackboard, a board brush (maintainer, 26/11/2026): its wooden back, its grain, its felt, a shadow
        final Scribble board = launched(ScribbleMode.PALETTE, ScribbleBackground.BOARD);
        fx(() -> {
            board.use(tool(board, Scribble.Tool.Kind.ERASER));
            assertTrue(board.getRubber().isVisible());
            final javafx.scene.Group picture = (javafx.scene.Group) board.getRubber().getChildren().get(0);
            assertEquals(4, picture.getChildren().size());
            line(board, board.getGazePen(), Scribble.GAZE_SMOOTHING);
            assertEquals(ScribbleBackground.BOARD.getPaper(), pixel(board, 300, 200));
            return null;
        });
    }

    /**
     * Tester's feedback of 23/11/2026, point 47: the dots of the widths take the color chosen (the rainbow in the
     * rainbow mode), and keep it with the eraser.
     */
    @Test
    void shouldPaintTheDotsOfTheWidthsWithTheColorChosen() throws Exception {
        final Scribble game = launched(ScribbleMode.PALETTE, ScribbleBackground.BOARD);
        fx(() -> {
            final java.util.List<Scribble.Tool> widths = game.getTools().stream()
                .filter(tool -> tool.getKind() == Scribble.Tool.Kind.WIDTH).toList();
            assertEquals(Scribble.WIDTHS.length, widths.size());
            for (final Scribble.Tool width : widths) {
                assertEquals(ScribbleBackground.BOARD.getInk(), width.getDot().getFill());
            }
            final Scribble.Tool red = game.getTools().stream()
                .filter(tool -> tool.getKind() == Scribble.Tool.Kind.COLOR).skip(1).findFirst().orElseThrow();
            game.use(red);
            game.use(tool(game, Scribble.Tool.Kind.ERASER));
            for (final Scribble.Tool width : widths) {
                assertEquals(red.getColor(), width.getDot().getFill());
            }
            assertNull(tool(game, Scribble.Tool.Kind.SAVE).getDot());
            return null;
        });
        final Scribble rainbow = launched(ScribbleMode.RAINBOW, ScribbleBackground.SHEET);
        fx(() -> {
            assertInstanceOf(javafx.scene.paint.LinearGradient.class,
                tool(rainbow, Scribble.Tool.Kind.WIDTH).getDot().getFill());
            return null;
        });
    }

    @Test
    void shouldChangeTheColorAlongTheLineInTheRainbowMode() throws Exception {
        final Scribble game = launched(ScribbleMode.RAINBOW, ScribbleBackground.SHEET);
        fx(() -> {
            assertTrue(game.getTools().stream().noneMatch(tool -> tool.getKind() == Scribble.Tool.Kind.COLOR));
            final Scribble.Pen pen = game.getGazePen();
            for (int x = 100; x <= 900; x += 10) {
                game.draw(pen, x, 200, 1);
            }
            assertNotEquals(pixel(game, 150, 200), pixel(game, 600, 200));
            return null;
        });
    }

    /**
     * Tester's feedback of 23/11/2026, point 44: in the rainbow mode, nothing left the eraser.
     */
    @Test
    void shouldGoBackFromTheEraserToTheRainbowPencil() throws Exception {
        final Scribble game = launched(ScribbleMode.RAINBOW, ScribbleBackground.SHEET);
        fx(() -> {
            final Scribble.Tool rainbow = tool(game, Scribble.Tool.Kind.RAINBOW);
            final Scribble.Tool eraser = tool(game, Scribble.Tool.Kind.ERASER);
            assertTrue(rainbow.isChosen());
            game.use(eraser);
            assertTrue(game.isEraser());
            assertTrue(eraser.isChosen());
            assertFalse(rainbow.isChosen());
            game.lookAt(rainbow);
            game.step(0.55);
            assertFalse(game.isEraser());
            assertTrue(rainbow.isChosen());
            assertFalse(eraser.isChosen());
            line(game, game.getGazePen(), Scribble.GAZE_SMOOTHING);
            assertNotEquals(ScribbleBackground.SHEET.getPaper(), pixel(game, 300, 200));
            return null;
        });
        // In the palette mode, a color leaves the eraser: no rainbow pencil
        final Scribble palette = launched(ScribbleMode.PALETTE, ScribbleBackground.SHEET);
        assertTrue(palette.getTools().stream().noneMatch(tool -> tool.getKind() == Scribble.Tool.Kind.RAINBOW));
    }

    @Test
    void shouldCountEachSheetDrawnOnce() throws Exception {
        final Scribble game = launched(ScribbleMode.PALETTE, ScribbleBackground.SHEET);
        fx(() -> {
            verify(stats, times(1)).incrementNumberOfGoalsToReach();
            game.newSheet();
            // An empty sheet does not count
            verify(stats, never()).incrementNumberOfGoalsReached();
            line(game, game.getGazePen(), Scribble.GAZE_SMOOTHING);
            game.newSheet();
            verify(stats, times(1)).incrementNumberOfGoalsReached();
            verify(stats, times(3)).incrementNumberOfGoalsToReach();
            assertEquals(3, game.getSheets());
            assertFalse(game.isDrawn());
            assertEquals(ScribbleBackground.SHEET.getPaper(), pixel(game, 300, 200));
            return null;
        });
    }

    /**
     * Tester's feedback of 23/11/2026, point 45: the new sheet erased the drawing at once. It is now alone at the
     * bottom, apart, and asks first: ✔ erases, ✖ or no answer keeps the drawing.
     */
    @Test
    void shouldAskBeforeErasingTheDrawing() throws Exception {
        for (final ScribbleMode mode : ScribbleMode.values()) {
            final Scribble game = launched(mode, ScribbleBackground.SHEET);
            fx(() -> {
                final Scribble.Tool newSheet = tool(game, Scribble.Tool.Kind.NEW_SHEET);
                final Scribble.Tool confirm = tool(game, Scribble.Tool.Kind.CONFIRM);
                final Scribble.Tool cancel = tool(game, Scribble.Tool.Kind.CANCEL);
                final double radius = game.getTools().get(0).getDisc().getRadius();
                // Below all the other buttons, with space between, and in the middle of its row
                for (final Scribble.Tool other : game.getTools()) {
                    if (other != newSheet && other != confirm && other != cancel) {
                        assertTrue(newSheet.getY() - other.getY() > 2.5 * radius, other.getKind().toString());
                    }
                }
                assertTrue(confirm.getX() < newSheet.getX() && newSheet.getX() < cancel.getX());
                assertFalse(isShown(confirm));
                assertTrue(isShown(newSheet));

                line(game, game.getGazePen(), Scribble.GAZE_SMOOTHING);
                game.lookAt(newSheet);
                game.step(0.55);
                assertTrue(game.isAsking());
                assertTrue(game.isDrawn());
                assertNotEquals(ScribbleBackground.SHEET.getPaper(), pixel(game, 300, 200));
                assertTrue(isShown(confirm) && isShown(cancel));
                assertFalse(isShown(newSheet));
                assertFalse(game.getMessage().getChildren().isEmpty());

                // ✖: the drawing stays
                game.use(cancel);
                assertFalse(game.isAsking());
                assertTrue(isShown(newSheet));
                assertFalse(isShown(confirm));
                assertTrue(game.getMessage().getChildren().isEmpty());
                assertNotEquals(ScribbleBackground.SHEET.getPaper(), pixel(game, 300, 200));

                // No answer: the question goes away, still after the message would have
                game.use(newSheet);
                game.step(Scribble.MESSAGE + 0.1);
                assertTrue(game.isAsking());
                game.step(Scribble.ASK);
                assertFalse(game.isAsking());
                assertTrue(isShown(newSheet));
                assertEquals(1, game.getSheets());

                // The time stops while ✔ or ✖ is being chosen (maintainer, 26/11/2026), with a slow fixation
                when(configuration.getFixationLength()).thenReturn(8000);
                game.use(newSheet);
                game.lookAt(cancel);
                game.step(Scribble.ASK + 1);
                assertTrue(game.isAsking());
                assertTrue(isShown(cancel));
                assertFalse(game.getMessage().getChildren().isEmpty());
                game.lookAway(cancel);
                game.step(Scribble.ASK);
                assertFalse(game.isAsking());
                when(configuration.getFixationLength()).thenReturn(500);

                // Another button: the question goes away too
                game.use(newSheet);
                game.use(tool(game, Scribble.Tool.Kind.ERASER));
                assertFalse(game.isAsking());

                // ✔ by a fixation: the sheet is erased
                game.use(newSheet);
                game.lookAt(confirm);
                game.step(0.55);
                assertFalse(game.isAsking());
                assertEquals(2, game.getSheets());
                assertFalse(game.isDrawn());
                assertEquals(ScribbleBackground.SHEET.getPaper(), pixel(game, 300, 200));
                assertTrue(isShown(newSheet));
                return null;
            });
        }
    }

    private static boolean isShown(final Scribble.Tool tool) {
        return tool.getNode().getParent() != null;
    }

    @Test
    void shouldKeepTheDrawingAsAPictureInTheDrawingsFolder() throws Exception {
        final Scribble game = launched(ScribbleMode.PALETTE, ScribbleBackground.BOARD);
        final File saved = fx(() -> {
            line(game, game.getGazePen(), Scribble.GAZE_SMOOTHING);
            game.use(tool(game, Scribble.Tool.Kind.SAVE));
            return game.getLastSaved();
        });
        assertNotNull(saved);
        try {
            assertEquals(GazePlayDirectories.getDrawingsDirectory().getCanonicalFile(),
                saved.getParentFile().getCanonicalFile());
            assertTrue(saved.getName().matches("gribouille_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}(-\\d+)?\\.png"),
                saved.getName());
            final BufferedImage image = ImageIO.read(saved);
            assertEquals((int) game.getCanvas().getWidth(), image.getWidth());
            assertEquals(ScribbleBackground.BOARD.getPaper(), argb(image.getRGB(10, image.getHeight() - 10)));
            assertEquals(ScribbleBackground.BOARD.getInk(), argb(image.getRGB(300, 200)));
            verify(stats, times(1)).incrementNumberOfGoalsReached();
            fx(() -> {
                assertFalse(game.getMessage().getChildren().isEmpty());
                game.step(Scribble.MESSAGE + 0.1);
                assertTrue(game.getMessage().getChildren().isEmpty());
                // Kept then a new sheet: counted once
                game.newSheet();
                return null;
            });
            verify(stats, times(1)).incrementNumberOfGoalsReached();
        } finally {
            assertTrue(saved.delete());
        }
    }

    private static Color argb(final int value) {
        return Color.rgb((value >> 16) & 0xFF, (value >> 8) & 0xFF, value & 0xFF);
    }

    @Test
    void shouldStopAndLeaveTheGameArea() throws Exception {
        final Scribble game = launched(ScribbleMode.PALETTE, ScribbleBackground.SHEET);
        fx(() -> {
            assertFalse(gameArea.getChildren().isEmpty());
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertEquals(Scribble.Phase.OVER, game.getPhase());
            game.draw(game.getGazePen(), 100, 100, 1);
            assertFalse(game.isDrawn());
            return null;
        });
    }

    /**
     * Tester's feedback of 23/11/2026, point 48 (maintainer's choices of 26/11/2026): by fixation, nothing is drawn
     * while the pencil is up (a hollow circle follows the gaze); a fixation puts it down, another one lifts it, and
     * another fixation needs the gaze to move first; a still mouse is a fixation; leaving the sheet lifts the pencil.
     */
    @Test
    void shouldStartAndEndTheLineByFixation() throws Exception {
        final Scribble game = launched(ScribbleMode.PALETTE, ScribbleTrace.FIXATION, ScribbleBackground.SHEET);
        fx(() -> {
            final Scribble.Pen gaze = game.getGazePen();
            line(game, gaze, Scribble.GAZE_SMOOTHING);
            assertFalse(gaze.isDown());
            assertFalse(game.isDrawn());
            assertEquals(ScribbleBackground.SHEET.getPaper(), pixel(game, 300, 200));
            assertTrue(game.getLiftedPencil().isVisible());

            hold(game, gaze, 400, 200, 0.3);
            assertFalse(gaze.isDown());
            hold(game, gaze, 400, 200, 0.4);
            assertTrue(gaze.isDown());
            assertFalse(game.getLiftedPencil().isVisible());
            assertEquals(ScribbleBackground.SHEET.getInk(), pixel(game, 400, 200));

            for (int x = 400; x <= 700; x += 10) {
                game.draw(gaze, x, 200, Scribble.GAZE_SMOOTHING);
                game.step(0.01);
            }
            assertEquals(ScribbleBackground.SHEET.getInk(), pixel(game, 550, 200));

            hold(game, gaze, 700, 200, 1.0);
            assertFalse(gaze.isDown());
            assertTrue(game.getLiftedPencil().isVisible());
            // Still there: the pencil stays up until the gaze has moved
            hold(game, gaze, 700, 200, 1.0);
            assertFalse(gaze.isDown());
            for (int y = 200; y <= 600; y += 10) {
                game.draw(gaze, 700, y, Scribble.GAZE_SMOOTHING);
                game.step(0.01);
            }
            assertEquals(ScribbleBackground.SHEET.getPaper(), pixel(game, 700, 400));

            // The eye tracker silent: no fixation
            game.step(1);
            assertFalse(gaze.isDown());

            // A still mouse: a fixation without any new point
            game.leaveSheet(gaze);
            final Scribble.Pen mouse = game.getMousePen();
            game.draw(mouse, 200, 600, Scribble.MOUSE_SMOOTHING);
            for (int i = 0; i < 6; i++) {
                game.step(0.1);
            }
            assertTrue(mouse.isDown());
            game.leaveSheet(mouse);
            assertFalse(mouse.isDown());
            assertFalse(game.getLiftedPencil().isVisible());
            return null;
        });
    }

    /**
     * The gaze kept on a point for this time, a point each tenth of a second (the eye tracker does not stay silent).
     */
    private static void hold(final Scribble game, final Scribble.Pen pen, final double x, final double y,
                             final double seconds) {
        for (double t = 0; t < seconds - 1e-9; t += 0.1) {
            game.draw(pen, x, y, Scribble.GAZE_SMOOTHING);
            game.step(0.1);
        }
    }

    @Test
    void shouldReadTheVariants() {
        final Set<IGameVariant> variants = new ScribbleGameVariantGenerator().getVariants();
        assertEquals(8, variants.size());
        final Scribble board = ScribbleGameLauncher.create(gameContext,
            new GroupedGameVariant("ScribbleRainbow", "ScribbleFixationLine", "ScribbleBoard", null), stats, 1);
        assertEquals(ScribbleMode.RAINBOW, board.getMode());
        assertEquals(ScribbleTrace.FIXATION, board.getTrace());
        assertEquals(ScribbleBackground.BOARD, board.getBackground());
        final Scribble other = ScribbleGameLauncher.create(gameContext, null, stats, 1);
        assertEquals(ScribbleMode.PALETTE, other.getMode());
        assertEquals(ScribbleTrace.FREE, other.getTrace());
        assertEquals(ScribbleBackground.SHEET, other.getBackground());
    }
}

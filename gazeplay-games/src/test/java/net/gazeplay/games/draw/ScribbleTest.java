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
        return fx(() -> {
            gameArea.getChildren().clear();
            final Scribble game = new Scribble(gameContext, stats, mode, background, 3);
            game.setManual();
            game.launch();
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

    @Test
    void shouldReadTheVariants() {
        final Set<IGameVariant> variants = new ScribbleGameVariantGenerator().getVariants();
        assertEquals(4, variants.size());
        final Scribble board = ScribbleGameLauncher.create(gameContext,
            new GroupedGameVariant("ScribbleRainbow", "ScribbleDrawing", "ScribbleBoard", null), stats, 1);
        assertEquals(ScribbleMode.RAINBOW, board.getMode());
        assertEquals(ScribbleBackground.BOARD, board.getBackground());
        final Scribble other = ScribbleGameLauncher.create(gameContext, null, stats, 1);
        assertEquals(ScribbleMode.PALETTE, other.getMode());
        assertEquals(ScribbleBackground.SHEET, other.getBackground());
    }
}

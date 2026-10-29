package net.gazeplay.games.colors;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.gamevariants.UserImagesVariant;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class ColorsGameTest {

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int RED = 0xFFE53935;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    @Mock
    Configuration configuration;

    @Mock
    Translator translator;

    @Mock
    ColorsGamesStats stats;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getRoot()).thenReturn(new Pane());
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1366, 768));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getFixationLength()).thenReturn(500);
        when(configuration.getUserName()).thenReturn("");
        when(configuration.getColorsDefaultImage()).thenReturn(Configuration.DEFAULT_VALUE_COLORS_DEFAULT_IMAGE);
    }

    @Test
    void shouldTurnTheDrawingIntoBlackAndWhite() {
        final int[] pixels = {0xFF101010, 0xFFF0F0F0, 0x00000000, 0xFF202020};
        ColorsGame.toBlackAndWhite(pixels);
        // A transparent pixel counts as white
        assertArrayEquals(new int[]{BLACK, WHITE, WHITE, BLACK}, pixels);
    }

    /**
     * A zone closed by black lines is filled up to them, and up to the borders of the drawing (the last column used to
     * stay white).
     */
    @Test
    void shouldFillAZoneUpToTheLinesAndTheBorders() {
        // 5 x 3: a black column in the middle
        final int[] pixels = {
            WHITE, WHITE, BLACK, WHITE, WHITE,
            WHITE, WHITE, BLACK, WHITE, WHITE,
            WHITE, WHITE, BLACK, WHITE, WHITE};
        ColorsGame.floodFill(pixels, 5, 3, 4, 1, RED);
        assertArrayEquals(new int[]{
            WHITE, WHITE, BLACK, RED, RED,
            WHITE, WHITE, BLACK, RED, RED,
            WHITE, WHITE, BLACK, RED, RED}, pixels);
    }

    /**
     * Each level has its coloring pages, and the game starts on the first one (it used to start on the drawing of the
     * settings, the 7th one).
     */
    @ParameterizedTest
    @EnumSource(value = ColoringVariant.class, names = {"EASY", "NORMAL", "HARD"})
    void shouldShowTheColoringPagesOfTheLevel(final ColoringVariant variant) throws Exception {
        final ColorsGame game = launchedGame(variant);
        assertEquals(8, game.getDrawings().size());
        assertTrue(game.getDrawings().stream().allMatch(path -> path.contains("/" + variant.getFolder() + "/draw-")));
        assertEquals(0, game.getIndex());
    }

    @Test
    void shouldPlayTheEasyPagesForAGameSavedWithoutVariant() {
        assertEquals(ColoringVariant.EASY, ColorsGameLauncher.variantOf(null));
    }

    /**
     * The custom palette plays the images given by the user in the settings (folder images/Coloring); without images, a
     * message instead of a drawing.
     */
    @Test
    void shouldPlayTheImagesOfTheUserInTheCustomPalette() throws Exception {
        assertEquals("Coloring", UserImagesVariant.folderOf(new EnumGameVariant<>(ColoringVariant.CUSTOM)));
        assertNull(UserImagesVariant.folderOf(new EnumGameVariant<>(ColoringVariant.EASY)));
        final File image = new File(UserImagesVariant.directory("Coloring"), "test-drawing.png");
        try {
            Files.deleteIfExists(image.toPath());
            final ColorsGame empty = launchedGame(ColoringVariant.CUSTOM);
            assertTrue(empty.getDrawings().isEmpty());
            assertNull(empty.getWritableImg());

            ImageIO.write(new BufferedImage(40, 30, BufferedImage.TYPE_INT_RGB), "png", image);
            final ColorsGame game = launchedGame(ColoringVariant.CUSTOM);
            assertEquals(List.of(image.toURI().toString()), game.getDrawings());
            assertEquals(40, (int) game.getWritableImg().getWidth());
        } finally {
            Files.deleteIfExists(image.toPath());
        }
    }

    /**
     * Four variants, named after their palette (the names of the values stay those written in the saved
     * games).
     */
    @Test
    void shouldNameTheVariantsAfterTheirPalette() {
        assertEquals(List.of("SmallPalette", "MediumPalette", "LargePalette", "CustomPalette"),
            new ColorsGameVariantGenerator().getVariants().stream().map(variant -> variant.getLabel(translator)).toList());
    }

    /**
     * The previous and next buttons change the drawing without launching the game again (it used to add nodes already
     * shown: IllegalArgumentException).
     */
    @Test
    void shouldChangeTheDrawingWithoutLaunchingTheGameAgain() throws Exception {
        final ColorsGame game = launchedGame();
        final int first = game.getIndex();
        final Pane root = gameContext.getRoot();
        final int nodes = root.getChildren().size();

        WaitForAsyncUtils.asyncFx(() -> game.changeDrawing(1)).get();
        assertEquals((first + 1) % 8, game.getIndex());
        WaitForAsyncUtils.asyncFx(() -> game.changeDrawing(-1)).get();
        WaitForAsyncUtils.asyncFx(() -> game.changeDrawing(-1)).get();
        assertEquals(Math.floorMod(first - 1, 8), game.getIndex());
        assertEquals(nodes, root.getChildren().size());
    }

    @Test
    void shouldUndoAndRestartTheColoring() throws Exception {
        final ColorsGame game = launchedGame();
        final int width = (int) game.getWritableImg().getWidth();
        final int height = (int) game.getWritableImg().getHeight();
        final int corner = game.getWritableImg().getPixelReader().getArgb(1, 1);
        assertEquals(WHITE, corner, "The background of the drawing is white");

        assertTrue(WaitForAsyncUtils.asyncFx(() -> game.fill(1, 1, RED)).get());
        assertEquals(RED, game.getWritableImg().getPixelReader().getArgb(width - 1, height - 1),
            "The background is filled up to the opposite corner");
        assertFalse(WaitForAsyncUtils.asyncFx(() -> game.fill(2, 2, RED)).get(), "Already of this color");

        WaitForAsyncUtils.asyncFx(game::undo).get();
        assertEquals(WHITE, game.getWritableImg().getPixelReader().getArgb(1, 1));

        WaitForAsyncUtils.asyncFx(() -> game.fill(1, 1, RED)).get();
        WaitForAsyncUtils.asyncFx(game::restartDrawing).get();
        assertEquals(WHITE, game.getWritableImg().getPixelReader().getArgb(1, 1));
        WaitForAsyncUtils.asyncFx(game::undo).get();
        assertEquals(RED, game.getWritableImg().getPixelReader().getArgb(1, 1), "A start again can be undone");
    }

    @Test
    void shouldKeepTheLastUndoLevels() throws Exception {
        final ColorsGame game = launchedGame();
        WaitForAsyncUtils.asyncFx(() -> {
            for (int i = 0; i < ColorsGame.UNDO_LEVELS + 5; i++) {
                game.fill(1, 1, i % 2 == 0 ? RED : 0xFF1E88E5);
            }
            for (int i = 0; i < ColorsGame.UNDO_LEVELS + 5; i++) {
                game.undo();
            }
        }).get();
        // The first fillings cannot be undone any more: the background is not white again
        assertNotEquals(WHITE, game.getWritableImg().getPixelReader().getArgb(1, 1));
    }

    private ColorsGame launchedGame() throws Exception {
        return launchedGame(ColoringVariant.EASY);
    }

    private ColorsGame launchedGame(final ColoringVariant variant) throws Exception {
        return WaitForAsyncUtils.asyncFx(() -> {
            final ColorsGame game = new ColorsGame(gameContext, stats, translator, variant);
            game.launch();
            return game;
        }).get();
    }
}

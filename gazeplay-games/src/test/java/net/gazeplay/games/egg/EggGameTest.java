package net.gazeplay.games.egg;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.AfterEach;
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Egg Game (reviewed on 2/11/2026, maintainer's rules): the egg cracks at each fixation and hatches after the number
 * of the variant, staying on it is enough; then a new egg, without end; animals or the images of the user; the
 * variants.
 */
@ExtendWith(ApplicationExtension.class)
class EggGameTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    @Mock
    Configuration configuration;

    @Mock
    Translator translator;

    @Mock
    Stats stats;

    private final Pane gameArea = new Pane();

    private final List<File> created = new ArrayList<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1366, 768));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getFixationLength()).thenReturn(150);
    }

    @AfterEach
    void tearDown() throws Exception {
        for (final File file : created) {
            Files.deleteIfExists(file.toPath());
        }
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private EggGame launched(final int steps, final EggContent content) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final EggGame game = new EggGame(gameContext, stats, steps, content, 3);
            game.launch();
            return game;
        });
    }

    /**
     * Each fixation cracks the egg; the last one hatches it; then a new egg, whole, and so on without end.
     */
    @Test
    void shouldHatchAfterTheFixationsOfTheVariantThenGiveANewEgg() throws Exception {
        final EggGame game = launched(3, EggContent.ANIMALS);
        fx(() -> {
            game.crack();
            assertEquals(1, game.getCracks());
            assertEquals(EggGame.Phase.READY, game.getPhase());
            game.crack();
            game.crack();
            assertEquals(EggGame.Phase.HATCHED, game.getPhase());
            assertEquals(1, game.getHatched());
            game.crack();
            game.skipPause();
            assertEquals(EggGame.Phase.READY, game.getPhase());
            assertEquals(0, game.getCracks());
            return null;
        });
        verify(stats, times(3)).incrementNumberOfGoalsReached();
        verify(stats, times(6)).incrementNumberOfGoalsToReach();
        verify(gameContext, never()).showRoundStats(any(), any());
    }

    /**
     * Staying on the egg cracks it again and again until it hatches; looking away before the end of a fixation does
     * not crack it.
     */
    @Test
    void shouldCrackAgainWhileStayingOnTheEgg() throws Exception {
        final EggGame away = launched(3, EggContent.ANIMALS);
        fx(() -> {
            away.egg().fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, 0, 0, 0));
            away.egg().fireEvent(new GazeEvent(GazeEvent.GAZE_EXITED, 0, 0, 0));
            return null;
        });
        WaitForAsyncUtils.sleep(400, TimeUnit.MILLISECONDS);
        assertEquals(0, (int) fx(away::getCracks), "looked away at once");

        final EggGame staying = launched(3, EggContent.ANIMALS);
        fx(() -> {
            staying.egg().fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, 0, 0, 0));
            return null;
        });
        WaitForAsyncUtils.sleep(3 * 150 + 500, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(EggGame.Phase.HATCHED, fx(staying::getPhase), "hatched without looking away");
    }

    /**
     * "My images": the images of the user come out of the eggs; without any, the animals.
     */
    @Test
    void shouldHatchTheImagesOfTheUser() throws Exception {
        final File folder = UserImagesVariant.directory(EggContent.USER_IMAGES_FOLDER);
        final File[] previous = folder.listFiles();
        org.junit.jupiter.api.Assumptions.assumeTrue(previous == null || previous.length == 0);
        final EggGame without = launched(2, EggContent.MY_IMAGES);
        assertTrue(fx(without::nextInside).startsWith(EggGame.FOLDER), "an animal");
        final File file = new File(folder, "photo.png");
        ImageIO.write(new BufferedImage(40, 30, BufferedImage.TYPE_INT_RGB), "png", file);
        created.add(file);
        final EggGame with = launched(2, EggContent.MY_IMAGES);
        assertTrue(fx(with::nextInside).contains(EggContent.USER_IMAGES_FOLDER), "an image of the user");
        fx(() -> {
            with.crack();
            with.crack();
            assertEquals(EggGame.Phase.HATCHED, with.getPhase());
            with.dispose();
            return null;
        });
        assertTrue(gameArea.getChildren().isEmpty());
    }

    /**
     * 8 variants: 2 to 5 steps, animals or my images; a variant of before keeps its number of steps.
     */
    @Test
    void shouldOfferTheStepsAndWhatComesOut() {
        final List<IGameVariant> variants = new ArrayList<>(new EggGameVariantGenerator().getVariants());
        assertEquals(8, variants.size());
        final GroupedGameVariant first = (GroupedGameVariant) variants.getFirst();
        assertEquals("EggAnimals", first.getSubGroup());
        assertEquals("EggSteps2", first.getValue());
        assertEquals(2, EggGameGameLauncher.stepsOf(first));
        assertEquals(4, variants.stream().filter(variant -> EggContent.USER_IMAGES_FOLDER
            .equals(UserImagesVariant.folderOf(variant))).count());
        final IGameVariant former = new IntStringGameVariant(4, "ImageShrink");
        assertEquals(4, EggGameGameLauncher.stepsOf(former));
        assertEquals(EggContent.ANIMALS, EggGameGameLauncher.contentOf(former));
    }
}

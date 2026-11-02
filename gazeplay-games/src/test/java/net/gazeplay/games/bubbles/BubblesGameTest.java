package net.gazeplay.games.bubbles;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Bubbles (reviewed on 2/11/2026, maintainer's rules): one card, the content and the way of bursting in the variants;
 * each bubble burst takes a pane off the hidden picture; the picture found, "Bravo" then a new picture; bubbles at once
 * or after a fixation; the images of the user in the bubbles and as pictures.
 */
@ExtendWith(ApplicationExtension.class)
class BubblesGameTest {

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
        when(configuration.getFixationLength()).thenReturn(100);
        when(configuration.getAnimationSpeedRatio()).thenReturn(1.0);
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

    private BubblesGame launched(final BubblesContent content, final BubblesGameVariant direction,
                                 final BubblesBurst burst) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final BubblesGame game = new BubblesGame(gameContext, stats, content, direction, burst, 7);
            game.setManual();
            game.launch();
            return game;
        });
    }

    /**
     * Each bubble burst takes one pane off; the 36 panes off: "Bravo", then a new picture under 36 panes again. While
     * "Bravo" is shown, a burst counts for nothing.
     */
    @Test
    void shouldFindThePictureThenHideANewOne() throws Exception {
        final BubblesGame game = launched(BubblesContent.COLORS, BubblesGameVariant.TOP, BubblesBurst.AT_ONCE);
        final String first = fx(game::pictureUrl);
        fx(() -> {
            assertEquals(BubblesGame.TILES, game.panesLeft());
            game.burst(game.getBubbles().getFirst());
            assertEquals(BubblesGame.TILES - 1, game.panesLeft());
            for (int i = 1; i < BubblesGame.TILES; i++) {
                game.burst(game.getBubbles().get(i % BubblesGame.BUBBLES));
            }
            assertEquals(0, game.panesLeft());
            assertEquals(BubblesGame.Phase.FOUND, game.getPhase());
            game.burst(game.getBubbles().getFirst());
            return null;
        });
        verify(stats, times(BubblesGame.TILES)).incrementNumberOfGoalsReached();
        verify(stats, times(BubblesGame.TILES)).incrementNumberOfGoalsToReach();
        @SuppressWarnings("unchecked") final ArgumentCaptor<EventHandler<ActionEvent>> end =
            ArgumentCaptor.forClass(EventHandler.class);
        verify(gameContext).playWinTransition(anyLong(), end.capture());
        fx(() -> {
            end.getValue().handle(null);
            assertEquals(BubblesGame.Phase.PLAY, game.getPhase());
            assertEquals(BubblesGame.TILES, game.panesLeft());
            assertEquals(1, game.getPictureIndex());
            assertNotEquals(first, game.pictureUrl(), "another picture");
            return null;
        });
        verify(gameContext).endWinTransition();
        verify(stats, times(2 * BubblesGame.TILES)).incrementNumberOfGoalsToReach();
    }

    /**
     * Bursting at once: the gaze entering a bubble bursts it. After a fixation: only once the fixation is over.
     */
    @Test
    void shouldBurstAtOnceOrAfterAFixation() throws Exception {
        final BubblesGame atOnce = launched(BubblesContent.COLORS, BubblesGameVariant.TOP, BubblesBurst.AT_ONCE);
        fx(() -> {
            atOnce.getBubbles().getFirst().node().fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, 0, 0, 0));
            assertEquals(BubblesGame.TILES - 1, atOnce.panesLeft());
            return null;
        });
        final BubblesGame fixation = launched(BubblesContent.COLORS, BubblesGameVariant.FIX,
            BubblesBurst.FIXATION);
        fx(() -> {
            fixation.getBubbles().getFirst().node().fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, 0, 0, 0));
            assertEquals(BubblesGame.TILES, fixation.panesLeft(), "not at once");
            return null;
        });
        WaitForAsyncUtils.sleep(400, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(BubblesGame.TILES - 1, (int) fx(fixation::panesLeft), "after the fixation");
    }

    /**
     * The bubbles go in the direction of the variant, and come back from the other side; still bubbles stay.
     */
    @Test
    void shouldMoveTheBubblesInTheDirectionOfTheVariant() throws Exception {
        final BubblesGame up = launched(BubblesContent.ANIMALS, BubblesGameVariant.TOP, BubblesBurst.AT_ONCE);
        fx(() -> {
            // A bubble in the middle of its way (one at its end would come back from the bottom)
            final BubblesGame.Bubble bubble = up.getBubbles().stream()
                .filter(b -> b.getY() > 300 && b.getY() < 700).findFirst().orElseThrow();
            final double y = bubble.getY();
            up.step(0.5);
            assertTrue(bubble.getY() < y, "up");
            for (int i = 0; i < 400; i++) {
                up.step(0.05);
            }
            assertTrue(bubble.getY() < 768 + 200 && bubble.getY() > -200, "comes back from the bottom");
            return null;
        });
        final BubblesGame right = launched(BubblesContent.COLORS, BubblesGameVariant.RIGHT, BubblesBurst.AT_ONCE);
        fx(() -> {
            final BubblesGame.Bubble bubble = right.getBubbles().stream()
                .filter(b -> b.getX() > 100 && b.getX() < 1000).findFirst().orElseThrow();
            final double x = bubble.getX();
            right.step(0.5);
            assertTrue(bubble.getX() > x, "to the right");
            return null;
        });
        final BubblesGame still = launched(BubblesContent.COLORS, BubblesGameVariant.FIX, BubblesBurst.AT_ONCE);
        fx(() -> {
            final BubblesGame.Bubble bubble = still.getBubbles().getFirst();
            final double x = bubble.getX();
            final double y = bubble.getY();
            still.step(0.2);
            assertEquals(x, bubble.getX(), 1e-9);
            assertEquals(y, bubble.getY(), 5);
            return null;
        });
    }

    /**
     * "My images": the hidden pictures are the images of the user; without any, the drawn scenes.
     */
    @Test
    void shouldHideTheImagesOfTheUser() throws Exception {
        final File folder = UserImagesVariant.directory(BubblesContent.USER_IMAGES_FOLDER);
        final File[] previous = folder.listFiles();
        org.junit.jupiter.api.Assumptions.assumeTrue(previous == null || previous.length == 0);
        final BubblesGame without = launched(BubblesContent.MY_IMAGES, BubblesGameVariant.TOP, BubblesBurst.AT_ONCE);
        assertTrue(fx(without::pictureUrl).startsWith(BubblesGame.FOLDER), "a drawn scene");
        for (final String name : new String[]{"a.png", "b.png"}) {
            final File file = new File(folder, name);
            ImageIO.write(new BufferedImage(40, 30, BufferedImage.TYPE_INT_RGB), "png", file);
            created.add(file);
        }
        final BubblesGame with = launched(BubblesContent.MY_IMAGES, BubblesGameVariant.TOP, BubblesBurst.AT_ONCE);
        assertTrue(fx(with::pictureUrl).contains(BubblesContent.USER_IMAGES_FOLDER), "an image of the user");
        fx(() -> {
            with.dispose();
            return null;
        });
        assertTrue(gameArea.getChildren().isEmpty());
    }

    /**
     * 30 variants: 5 directions, for each content and each way of bursting; "My images" plays with the images of the
     * user; a variant of before (a direction only) plays colors at once.
     */
    @Test
    void shouldOfferTheContentsAndTheWaysOfBursting() {
        final List<IGameVariant> variants = new ArrayList<>(new BubblesGameVariantGenerator().getVariants());
        assertEquals(30, variants.size());
        final GroupedGameVariant first = (GroupedGameVariant) variants.getFirst();
        assertEquals("BubblesBurstAtOnce", first.getGroup());
        assertEquals("BubblesColors", first.getSubGroup());
        assertEquals("ToTop", first.getValue());
        assertEquals(10, variants.stream().filter(variant -> BubblesContent.USER_IMAGES_FOLDER
            .equals(UserImagesVariant.folderOf(variant))).count());
        final IGameVariant former = new EnumGameVariant<>(BubblesGameVariant.FIX);
        assertEquals(BubblesContent.COLORS, BubblesGameLauncher.contentOf(former));
        assertEquals(BubblesGameVariant.FIX, BubblesGameLauncher.directionOf(former));
        assertEquals(BubblesBurst.AT_ONCE, BubblesGameLauncher.burstOf(former));
    }
}

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
import java.util.Map;
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

    /**
     * The panes of the level of before (hard), that of a game without level.
     */
    private static final int TILES = BubblesLevel.HARD.panes();

    /** The games launched by a test, closed after it (18/11/2026). */
    @org.junit.jupiter.api.extension.RegisterExtension
    final net.gazeplay.games.LaunchedGames launchedGames = new net.gazeplay.games.LaunchedGames();

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
            launchedGames.add(game).launch();
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
            assertEquals(TILES, game.panesLeft());
            game.burst(game.getBubbles().getFirst());
            assertEquals(TILES - 1, game.panesLeft());
            for (int i = 1; i < TILES; i++) {
                game.burst(game.getBubbles().get(i % BubblesGame.BUBBLES));
            }
            assertEquals(0, game.panesLeft());
            assertEquals(BubblesGame.Phase.FOUND, game.getPhase());
            game.burst(game.getBubbles().getFirst());
            return null;
        });
        verify(stats, times(TILES)).incrementNumberOfGoalsReached();
        verify(stats, times(TILES)).incrementNumberOfGoalsToReach();
        @SuppressWarnings("unchecked") final ArgumentCaptor<EventHandler<ActionEvent>> end =
            ArgumentCaptor.forClass(EventHandler.class);
        verify(gameContext).playWinTransition(anyLong(), end.capture());
        fx(() -> {
            end.getValue().handle(null);
            // The picture stays, without the bubbles, until the arrow is chosen (point 51)
            assertEquals(BubblesGame.Phase.FOUND, game.getPhase());
            assertNotNull(game.getNextButton());
            assertTrue(gameArea.getChildren().getFirst() instanceof javafx.scene.Group layer
                && layer.getChildren().contains(game.getNextButton()));
            game.step(5);
            assertEquals(BubblesGame.Phase.FOUND, game.getPhase(), "no time limit");
            assertFalse(game.getBubbles().getFirst().node().getParent().isVisible());
            game.getNextButton().fireEvent(new javafx.scene.input.MouseEvent(
                javafx.scene.input.MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null));
            assertNull(game.getNextButton());
            assertTrue(game.getBubbles().getFirst().node().getParent().isVisible());
            assertEquals(BubblesGame.Phase.PLAY, game.getPhase());
            assertEquals(TILES, game.panesLeft());
            assertEquals(1, game.getPictureIndex());
            assertNotEquals(first, game.pictureUrl(), "another picture");
            return null;
        });
        verify(gameContext).endWinTransition();
        verify(stats, times(2 * TILES)).incrementNumberOfGoalsToReach();
    }

    /**
     * Bursting at once: the gaze entering a bubble bursts it. After a fixation: only once the fixation is over.
     */
    @Test
    void shouldBurstAtOnceOrAfterAFixation() throws Exception {
        final BubblesGame atOnce = launched(BubblesContent.COLORS, BubblesGameVariant.TOP, BubblesBurst.AT_ONCE);
        fx(() -> {
            atOnce.getBubbles().getFirst().node().fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, 0, 0, 0));
            assertEquals(TILES - 1, atOnce.panesLeft());
            return null;
        });
        final BubblesGame fixation = launched(BubblesContent.COLORS, BubblesGameVariant.FIX,
            BubblesBurst.FIXATION);
        fx(() -> {
            fixation.getBubbles().getFirst().node().fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, 0, 0, 0));
            assertEquals(TILES, fixation.panesLeft(), "not at once");
            return null;
        });
        WaitForAsyncUtils.sleep(400, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(TILES - 1, (int) fx(fixation::panesLeft), "after the fixation");
    }

    /**
     * With the setting keeping the fixation of the moving targets (20/11/2026), a bubble left keeps the progress of its
     * fixation, which goes on when the gaze comes back; it is offered in the fixation variant only.
     */
    @Test
    void shouldKeepTheFixationOfABubbleWhenTheSettingSaysSo() throws Exception {
        when(configuration.isMovingFixationKept()).thenReturn(true);
        when(configuration.getFixationLength()).thenReturn(800);
        final BubblesGame game = launched(BubblesContent.COLORS, BubblesGameVariant.FIX, BubblesBurst.FIXATION);
        assertTrue(game.menuSettings().contains(net.gazeplay.MenuSetting.MOVING_FIXATION));
        final javafx.scene.Node bubble = fx(() -> game.getBubbles().getFirst().node());
        fx(() -> {
            bubble.fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, 0, 0, 0));
            return null;
        });
        WaitForAsyncUtils.sleep(400, TimeUnit.MILLISECONDS);
        fx(() -> {
            bubble.fireEvent(new GazeEvent(GazeEvent.GAZE_EXITED, 0, 0, 0));
            return null;
        });
        WaitForAsyncUtils.sleep(900, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(TILES, (int) fx(game::panesLeft), "kept, not burst while left");
        fx(() -> {
            bubble.fireEvent(new GazeEvent(GazeEvent.GAZE_ENTERED, 0, 0, 0));
            return null;
        });
        // About 400 ms are left; less than a whole fixation of 800 ms
        WaitForAsyncUtils.sleep(650, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(TILES - 1, (int) fx(game::panesLeft), "the fixation went on");
        assertFalse(launched(BubblesContent.COLORS, BubblesGameVariant.FIX, BubblesBurst.AT_ONCE).menuSettings()
            .contains(net.gazeplay.MenuSetting.MOVING_FIXATION));
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
        // Point 50: each image decoded once, in the background, at the size of the biggest bubble; the next hidden
        // picture decoded in the background too
        final Map<String, javafx.scene.image.Image> decoded = fx(with::bubblePictures);
        assertEquals(2, decoded.size());
        assertNotEquals(fx(with::pictureUrl), fx(with::nextPictureUrl));
        final long until = System.currentTimeMillis() + 10_000;
        while (decoded.values().stream().anyMatch(image -> image.getProgress() < 1)
            && System.currentTimeMillis() < until) {
            WaitForAsyncUtils.sleep(50, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
        fx(() -> {
            for (final BubblesGame.Bubble bubble : with.getBubbles()) {
                bubble.respawn(0);
                final List<javafx.scene.image.Image> shown = bubble.node().getChildren().stream()
                    .filter(javafx.scene.image.ImageView.class::isInstance)
                    .map(node -> ((javafx.scene.image.ImageView) node).getImage()).toList();
                assertEquals(1, shown.size());
                assertTrue(decoded.values().stream().anyMatch(image -> image == shown.getFirst()),
                    "the image decoded once");
            }
            with.dispose();
            return null;
        });
        assertTrue(gameArea.getChildren().isEmpty());
    }

    /**
     * 30 variants: 5 directions, for each content and each way of bursting; "My images" plays with the images of the
     * user; a variant of before (a direction only) plays colors at once.
     */
    /**
     * Tester's feedback of 23/11/2026, point 49: easy, medium and hard levels, 9, 16 and 36 panes, bigger bubbles at the
     * easy levels; easy first; a variant without level is hard, the game of before.
     */
    @Test
    void shouldHideThePictureUnderThePanesOfTheLevel() throws Exception {
        final double[] largest = new double[BubblesLevel.values().length];
        for (final BubblesLevel level : BubblesLevel.values()) {
            final BubblesGame game = fx(() -> {
                gameArea.getChildren().clear();
                final BubblesGame played = new BubblesGame(gameContext, stats, BubblesContent.COLORS,
                    BubblesGameVariant.FIX, BubblesBurst.AT_ONCE, 7).withLevel(level);
                played.setManual();
                launchedGames.add(played).launch();
                return played;
            });
            assertEquals(level.panes(), (int) fx(game::panesLeft));
            largest[level.ordinal()] = fx(() -> game.getBubbles().stream()
                .mapToDouble(bubble -> bubble.node().getBoundsInLocal().getWidth()).max().orElseThrow());
        }
        assertEquals(List.of(9, 16, 36), java.util.Arrays.stream(BubblesLevel.values()).map(BubblesLevel::panes)
            .toList());
        assertTrue(largest[0] > largest[2], "bigger bubbles at the easy level");
        assertEquals(BubblesLevel.EASY, BubblesLevel.of(new BubblesGameVariantGenerator().getVariants().iterator()
            .next()));
        assertEquals(BubblesLevel.HARD, BubblesLevel.of(new EnumGameVariant<>(BubblesGameVariant.FIX)));
    }

    @Test
    void shouldOfferTheContentsAndTheWaysOfBursting() {
        final List<IGameVariant> variants = new ArrayList<>(new BubblesGameVariantGenerator().getVariants());
        // 30, each with the 3 levels since 26/11/2026
        assertEquals(30 * 3, variants.size());
        final GroupedGameVariant first = (GroupedGameVariant) variants.getFirst();
        assertEquals("BubblesBurstAtOnce", first.getGroup());
        assertEquals("BubblesColors", first.getSubGroup());
        assertEquals("ToTop", first.getValue());
        assertEquals(10 * 3, variants.stream().filter(variant -> BubblesContent.USER_IMAGES_FOLDER
            .equals(UserImagesVariant.folderOf(variant))).count());
        final IGameVariant former = new EnumGameVariant<>(BubblesGameVariant.FIX);
        assertEquals(BubblesContent.COLORS, BubblesGameLauncher.contentOf(former));
        assertEquals(BubblesGameVariant.FIX, BubblesGameLauncher.directionOf(former));
        assertEquals(BubblesBurst.AT_ONCE, BubblesGameLauncher.burstOf(former));
    }
}

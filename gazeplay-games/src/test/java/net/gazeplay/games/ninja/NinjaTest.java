package net.gazeplay.games.ninja;

import javafx.event.Event;
import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Ninja" (reviewed on 4/11/2026, maintainer's rules): the ways and speeds of the target, inside the screen; cut at once
 * or after the fixation of the settings, read at once; the end; the adaptive speed, which can now go both ways whatever
 * the fixation; the variants.
 */
@ExtendWith(ApplicationExtension.class)
class NinjaTest {

    private static final double WIDTH = 1600;
    private static final double HEIGHT = 900;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    /**
     * Read at each step: it keeps no trace of its calls, which took the whole memory of the tests in a first version.
     */
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
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(WIDTH, HEIGHT));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getAnimationSpeedRatio()).thenReturn(1.0);
        when(configuration.getElementSize()).thenReturn(100);
        when(configuration.getFixationLength()).thenReturn(500);
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private Ninja launched(final NinjaTrajectory trajectory, final NinjaSpeed speed, final NinjaCut cut,
                           final NinjaEnd end) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final Ninja game = new Ninja(gameContext, stats, trajectory, speed, cut, end, NinjaTarget.FRUITS, 5);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private static void steps(final Ninja game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    private static void enter(final Ninja game) {
        Event.fireEvent(game.getFruit(), new GazeEvent(GazeEvent.GAZE_ENTERED, 1, 0, 0));
    }

    private static void exit(final Ninja game) {
        Event.fireEvent(game.getFruit(), new GazeEvent(GazeEvent.GAZE_EXITED, -1, 0, 0));
    }

    /**
     * The target used to go half out of the screen at the right and at the bottom; "Horizontal Dynamic" took its abscissa
     * as its height, and could leave the screen for good.
     */
    @Test
    void shouldKeepTheWholeTargetInsideTheScreenOnEveryWay() throws Exception {
        for (final NinjaTrajectory trajectory : NinjaTrajectory.values()) {
            for (int seed = 0; seed < 2; seed++) {
                final int gameSeed = seed + 1;
                final Ninja game = fx(() -> {
                    gameArea.getChildren().clear();
                    final Ninja g = new Ninja(gameContext, stats, trajectory, NinjaSpeed.FAST, NinjaCut.FIXATION,
                        NinjaEnd.ENDLESS, NinjaTarget.FRUITS, gameSeed);
                    g.setManual();
                    g.launch();
                    return g;
                });
                fx(() -> {
                    for (int i = 0; i < 300; i++) {
                        game.step(0.04);
                        final double r = game.getRadius();
                        assertTrue(game.getFruitX() >= r && game.getFruitX() <= WIDTH - r, trajectory + " x " + game.getFruitX());
                        assertTrue(game.getFruitY() >= r && game.getFruitY() <= HEIGHT - r, trajectory + " y " + game.getFruitY());
                    }
                    return null;
                });
            }
        }
    }

    @Test
    void shouldGoThroughTheHeightOfTheScreenInTheSecondsOfTheSpeed() throws Exception {
        assertEquals(5, NinjaSpeed.SLOW.getCrossingSeconds());
        assertEquals(3, NinjaSpeed.NORMAL.getCrossingSeconds());
        assertEquals(2, NinjaSpeed.FAST.getCrossingSeconds());
        final Ninja game = launched(NinjaTrajectory.RANDOM, NinjaSpeed.NORMAL, NinjaCut.FIXATION, NinjaEnd.ENDLESS);
        fx(() -> {
            final double x = game.getFruitX();
            final double y = game.getFruitY();
            game.step(0.05);
            assertEquals(HEIGHT / 3 * 0.05, Math.hypot(game.getFruitX() - x, game.getFruitY() - y), 0.5);
            return null;
        });
    }

    /**
     * Back and forth at the same speed whatever the way: a horizontal one is not faster for being longer.
     */
    @Test
    void shouldGoBackAndForthOnALine() throws Exception {
        final Ninja game = launched(NinjaTrajectory.HORIZONTAL, NinjaSpeed.FAST, NinjaCut.FIXATION, NinjaEnd.ENDLESS);
        fx(() -> {
            final double y = game.getFruitY();
            double smallest = WIDTH;
            double largest = 0;
            for (int i = 0; i < 700; i++) {
                game.step(0.02);
                assertEquals(y, game.getFruitY(), 1e-9);
                smallest = Math.min(smallest, game.getFruitX());
                largest = Math.max(largest, game.getFruitX());
            }
            // Up to both ends, within a step of 0.02 s
            final double step = HEIGHT / NinjaSpeed.FAST.getCrossingSeconds() * 0.02;
            assertEquals(game.getRadius() * 1.12, smallest, step);
            assertEquals(WIDTH - game.getRadius() * 1.12, largest, step);
            return null;
        });
    }

    @Test
    void shouldCutAtOnceAndBringAnotherTargetAfterTheGap() throws Exception {
        final Ninja game = launched(NinjaTrajectory.RANDOM, NinjaSpeed.NORMAL, NinjaCut.AT_ONCE, NinjaEnd.ENDLESS);
        fx(() -> {
            assertTrue(game.isTargetShown());
            enter(game);
            game.step(0.02);
            assertEquals(1, game.getCuts());
            assertFalse(game.isTargetShown());
            assertTrue(game.getGap() > 0);
            exit(game);
            steps(game, Ninja.GAP + 0.1);
            assertTrue(game.isTargetShown());
            return null;
        });
        verify(stats, times(2)).incrementNumberOfGoalsToReach();
        verify(stats, times(1)).incrementNumberOfGoalsReached();
        verify(gameContext.getSoundManager()).add(contains("data/ninja/sounds/slice"));
    }

    @Test
    void shouldCutAfterTheFixationOfTheSettingsWithTheRing() throws Exception {
        final Ninja game = launched(NinjaTrajectory.RANDOM, NinjaSpeed.NORMAL, NinjaCut.FIXATION, NinjaEnd.ENDLESS);
        fx(() -> {
            enter(game);
            steps(game, 0.2);
            assertEquals(0, game.getCuts());
            assertTrue(game.getRing().isVisible(), "the ring of the fixation");
            assertEquals(0.4, game.getRing().getProgress(), 0.1);
            steps(game, 0.4);
            assertEquals(1, game.getCuts());
            assertFalse(game.getRing().isVisible());
            return null;
        });
    }

    /**
     * Maintainer's rule of 4/11/2026: the length is that of the settings, changed by the gear during a game.
     */
    @Test
    void shouldReadTheLengthOfTheFixationDuringTheGame() throws Exception {
        final Ninja game = launched(NinjaTrajectory.RANDOM, NinjaSpeed.NORMAL, NinjaCut.FIXATION, NinjaEnd.ENDLESS);
        fx(() -> {
            when(configuration.getFixationLength()).thenReturn(4000);
            enter(game);
            steps(game, 1);
            assertEquals(0, game.getCuts());
            when(configuration.getFixationLength()).thenReturn(500);
            game.step(0.02);
            assertEquals(1, game.getCuts());
            return null;
        });
    }

    /**
     * A short look away (the fruit moves, the eye tracker trembles) only stops the ring; a longer one starts it again.
     */
    @Test
    void shouldForgiveAShortLookAwayOnly() throws Exception {
        final Ninja game = launched(NinjaTrajectory.RANDOM, NinjaSpeed.NORMAL, NinjaCut.FIXATION, NinjaEnd.ENDLESS);
        fx(() -> {
            enter(game);
            steps(game, 0.2);
            final double reached = game.getFixationSeconds();
            exit(game);
            steps(game, Ninja.FIXATION_GRACE / 2);
            assertEquals(reached, game.getFixationSeconds(), 1e-9);
            assertTrue(game.getRing().isVisible());
            steps(game, Ninja.FIXATION_GRACE);
            assertEquals(0, game.getFixationSeconds(), 1e-9);
            assertFalse(game.getRing().isVisible());
            return null;
        });
    }

    /**
     * A look already where the next target comes starts as soon as it shows (it used to be lost).
     */
    @Test
    void shouldCountALookAlreadyThereWhenTheTargetShows() throws Exception {
        final Ninja game = launched(NinjaTrajectory.RANDOM, NinjaSpeed.NORMAL, NinjaCut.AT_ONCE, NinjaEnd.ENDLESS);
        fx(() -> {
            game.cutTarget();
            enter(game);
            steps(game, Ninja.GAP - 0.1);
            assertEquals(1, game.getCuts(), "not during the gap");
            steps(game, 0.2);
            assertEquals(2, game.getCuts());
            return null;
        });
    }

    @Test
    void shouldEndOnceAfterTheTargetsOfTheVariant() throws Exception {
        final Ninja game = launched(NinjaTrajectory.RANDOM, NinjaSpeed.NORMAL, NinjaCut.AT_ONCE, NinjaEnd.CUT_20);
        fx(() -> {
            for (int i = 0; i < 25; i++) {
                game.cutTarget();
                steps(game, Ninja.GAP + 0.1);
            }
            return null;
        });
        assertEquals(20, game.getCuts());
        assertEquals(Ninja.Phase.OVER, game.getPhase());
        verify(gameContext, times(1)).playWinTransition(anyLong(), any());
        verify(stats, times(20)).incrementNumberOfGoalsReached();
    }

    @Test
    void shouldNeverEndWithoutAnEnd() throws Exception {
        final Ninja game = launched(NinjaTrajectory.RANDOM, NinjaSpeed.NORMAL, NinjaCut.AT_ONCE, NinjaEnd.ENDLESS);
        fx(() -> {
            for (int i = 0; i < 60; i++) {
                game.cutTarget();
                steps(game, Ninja.GAP + 0.1);
            }
            return null;
        });
        assertEquals(Ninja.Phase.PLAY, game.getPhase());
        verify(gameContext, never()).playWinTransition(anyLong(), any());
    }

    /**
     * Before the review, with the fixation of 2 s of the settings, every target looked slow: the dynamic speed could only
     * slow down. The time of the fixation no longer counts: quick players get a faster fruit.
     */
    @Test
    void shouldGetFasterWithQuickCutsWhateverTheFixation() throws Exception {
        when(configuration.getFixationLength()).thenReturn(2000);
        final Ninja game = launched(NinjaTrajectory.RANDOM, NinjaSpeed.ADAPTIVE, NinjaCut.FIXATION, NinjaEnd.ENDLESS);
        fx(() -> {
            assertEquals(3, game.getCrossingSeconds());
            for (int i = 0; i < Ninja.ADAPTIVE_TARGETS; i++) {
                enter(game);
                steps(game, 2.2);
                exit(game);
                assertEquals(i + 1, game.getCuts());
                steps(game, Ninja.GAP + 0.1);
            }
            assertEquals(2.5, game.getCrossingSeconds());
            return null;
        });
    }

    @Test
    void shouldGetSlowerWithSlowCutsAndNotChangeInBetween() throws Exception {
        final Ninja game = launched(NinjaTrajectory.RANDOM, NinjaSpeed.ADAPTIVE, NinjaCut.AT_ONCE, NinjaEnd.ENDLESS);
        fx(() -> {
            for (int i = 0; i < Ninja.ADAPTIVE_TARGETS; i++) {
                assertEquals(3, game.getCrossingSeconds(), "only every three targets");
                steps(game, Ninja.ADAPTIVE_SLOW + 0.5);
                game.cutTarget();
                steps(game, Ninja.GAP + 0.1);
            }
            assertEquals(4, game.getCrossingSeconds());
            return null;
        });
    }

    /**
     * A fixed speed stays as it is.
     */
    @Test
    void shouldKeepAFixedSpeed() throws Exception {
        final Ninja game = launched(NinjaTrajectory.RANDOM, NinjaSpeed.SLOW, NinjaCut.AT_ONCE, NinjaEnd.ENDLESS);
        fx(() -> {
            for (int i = 0; i < 9; i++) {
                game.cutTarget();
                steps(game, Ninja.GAP + 0.1);
            }
            assertEquals(5, game.getCrossingSeconds());
            return null;
        });
    }

    @Test
    void shouldGiveTheVariantsWithMyImagesAndReadOnesOfBefore() {
        final Set<IGameVariant> variants = new NinjaGameVariantGenerator().getVariants();
        assertEquals(NinjaTarget.values().length * NinjaSpeed.values().length * NinjaCut.values().length
            * NinjaEnd.values().length * NinjaTrajectory.values().length, variants.size());
        final GroupedGameVariant mine = new GroupedGameVariant(NinjaTarget.MY_IMAGES.getKey(), NinjaSpeed.FAST.getKey(),
            NinjaTrajectory.DIAGONAL_UP.getKey(), NinjaTarget.USER_IMAGES_FOLDER, NinjaCut.FIXATION.getKey(),
            NinjaEnd.CUT_50.getKey());
        assertTrue(variants.contains(mine));
        assertEquals("images", UserImagesVariant.folderOf(mine), "the images shared by all the games");
        assertEquals(NinjaTrajectory.DIAGONAL_UP, NinjaGameLauncher.trajectoryOf(mine));
        assertEquals(NinjaSpeed.FAST, NinjaGameLauncher.speedOf(mine));
        assertEquals(NinjaCut.FIXATION, NinjaGameLauncher.cutOf(mine));
        assertEquals(NinjaEnd.CUT_50, NinjaGameLauncher.endOf(mine));
        assertEquals(NinjaTarget.MY_IMAGES, NinjaGameLauncher.targetOf(mine));

        // As given to the JVM of the game
        assertEquals(mine, IGameVariant.toGameVariant(mine.toString()));

        final IGameVariant old = new IntGameVariant(1);
        assertEquals(NinjaTrajectory.RANDOM, NinjaGameLauncher.trajectoryOf(old));
        assertEquals(NinjaSpeed.NORMAL, NinjaGameLauncher.speedOf(old));
        assertEquals(NinjaCut.AT_ONCE, NinjaGameLauncher.cutOf(old));
        assertEquals(NinjaEnd.ENDLESS, NinjaGameLauncher.endOf(null));
        assertEquals(NinjaTarget.FRUITS, NinjaGameLauncher.targetOf(null));
    }

    /**
     * "My images": the images of the user are the targets, in a circle; without any, a message and no target.
     */
    @Test
    void shouldCutTheImagesOfTheUserOrSaySoWithoutAny() throws Exception {
        final java.io.File folder = UserImagesVariant.directory(NinjaTarget.USER_IMAGES_FOLDER);
        final java.io.File image = new java.io.File(folder, "test-ninja.png");
        final java.io.File[] others = folder.listFiles();
        org.junit.jupiter.api.Assumptions.assumeTrue(others == null || others.length == 0);
        final Ninja without = fx(() -> {
            gameArea.getChildren().clear();
            final Ninja g = new Ninja(gameContext, stats, NinjaTrajectory.RANDOM, NinjaSpeed.NORMAL, NinjaCut.AT_ONCE,
                NinjaEnd.ENDLESS, NinjaTarget.MY_IMAGES, 5);
            g.setManual();
            g.launch();
            return g;
        });
        assertEquals(Ninja.Phase.OVER, without.getPhase());
        verify(translator).translate("NoUserImages");
        try (java.io.InputStream in = getClass().getClassLoader()
            .getResourceAsStream("data/ninja/images/apple.png")) {
            java.nio.file.Files.copy(in, image.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        try {
            final Ninja game = fx(() -> {
                gameArea.getChildren().clear();
                final Ninja g = new Ninja(gameContext, stats, NinjaTrajectory.RANDOM, NinjaSpeed.NORMAL,
                    NinjaCut.AT_ONCE, NinjaEnd.ENDLESS, NinjaTarget.MY_IMAGES, 5);
                g.setManual();
                g.launch();
                enter(g);
                g.step(0.02);
                return g;
            });
            assertEquals(1, game.getCuts());
        } finally {
            assertTrue(image.delete());
        }
    }

    @Test
    void shouldRemoveEverythingWhenDisposed() throws Exception {
        final Ninja game = launched(NinjaTrajectory.RANDOM, NinjaSpeed.NORMAL, NinjaCut.AT_ONCE, NinjaEnd.ENDLESS);
        fx(() -> {
            game.cutTarget();
            game.dispose();
            game.step(1);
            return null;
        });
        assertTrue(gameArea.getChildren().isEmpty());
        assertEquals(Ninja.Phase.OVER, game.getPhase());
    }
}

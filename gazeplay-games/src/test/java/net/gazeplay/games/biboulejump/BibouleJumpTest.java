package net.gazeplay.games.biboulejump;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Biboule Jump (reviewed on 4/11/2026, maintainer's rules): the same jump in parts of the screen on every screen, clouds
 * that can always be reached, three lives with the points kept, exact points, the ends, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class BibouleJumpTest {

    private static final double STEP = 1.0 / 60;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    /**
     * Read at each step: it keeps no trace of its calls.
     */
    private final Configuration configuration = mock(Configuration.class, withSettings().stubOnly());

    @Mock
    Translator translator;

    @Mock
    BibouleJumpStats stats;

    private final Pane gameArea = new Pane();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getAnimationSpeedRatio()).thenReturn(1.0);
        screen(1920, 1080);
    }

    private void screen(final double width, final double height) {
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(width, height));
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private BibouleJump launched(final BibouleJumpLevel level, final BibouleJumpEnd end, final double seed)
        throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final BibouleJump game = new BibouleJump(gameContext, stats, level, end, seed);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private static void steps(final BibouleJump game, final double seconds) {
        for (double t = 0; t < seconds; t += STEP) {
            game.step(STEP);
        }
    }

    /**
     * Plays well: aims at the highest cloud it can still land on, reckoned from its place and its speed, so that the
     * tests below show that a way up always exists. A moving cloud counts only if it can be caught wherever it goes.
     */
    private static void playWell(final BibouleJump game) {
        BibouleJump.Cloud best = null;
        for (final BibouleJump.Cloud cloud : game.getClouds()) {
            final double time = secondsToLandOn(game, cloud);
            if (time < 0) {
                continue;
            }
            final double side = BibouleJump.SIDE_SPEED * game.getWidth() * time;
            final double runaway = cloud.isMoving() ? BibouleJump.CLOUD_SPEED * game.getWidth() * time : 0;
            final double reach = side - runaway + cloud.getWidth() * 0.45 + game.getFeetWidth();
            if (Math.abs(cloud.getX() - game.getBibouleX()) <= reach
                && (best == null || cloud.getSurface() < best.getSurface())) {
                best = cloud;
            }
        }
        if (best != null) {
            game.aimAt(best.getX());
        }
    }

    /**
     * @return the seconds before the feet come down on the surface of a cloud, negative if they never will
     */
    private static double secondsToLandOn(final BibouleJump game, final BibouleJump.Cloud cloud) {
        final double above = (game.getFeetY() - cloud.getSurface()) / game.getHeight();
        final double speed = game.getVelocity();
        final double up = speed < 0 ? speed * speed / (2 * BibouleJump.GRAVITY) : 0;
        if (above > up * 0.97) {
            return -1;
        }
        final double down = up - above;
        return (speed < 0 ? -speed / BibouleJump.GRAVITY : 0) + down / BibouleJump.FALL_SPEED;
    }

    @Test
    void shouldClimbTheSamePartOfTheScreenOnEveryScreen() throws Exception {
        final double[][] screens = {{1366, 768}, {1920, 1080}, {3840, 2160}, {1280, 1024}};
        for (final double[] size : screens) {
            screen(size[0], size[1]);
            final BibouleJump game = launched(BibouleJumpLevel.EASY, BibouleJumpEnd.ENDLESS, 1);
            fx(() -> {
                final double start = game.getFeetY();
                double highest = start;
                steps(game, BibouleJump.FIRST_WAIT);
                // Stops at the line where the clouds start going down: the points give the rest
                for (int i = 0; i < 40; i++) {
                    game.step(STEP);
                    highest = Math.min(highest, game.getFeetY());
                }
                final double climbed = (start - highest) / size[1] + game.getScore() / 100.0;
                assertEquals(0.51, climbed, 0.04, size[0] + " x " + size[1]);
                return null;
            });
        }
    }

    @Test
    void shouldPutTheCloudsWithinReachAndInsideTheScreen() throws Exception {
        for (final BibouleJumpLevel level : BibouleJumpLevel.values()) {
            final BibouleJump game = launched(level, BibouleJumpEnd.ENDLESS, 3);
            fx(() -> {
                final List<BibouleJump.Cloud> clouds = game.getClouds();
                for (int i = 1; i < clouds.size(); i++) {
                    final BibouleJump.Cloud under = clouds.get(i - 1);
                    final BibouleJump.Cloud cloud = clouds.get(i);
                    final double gap = (under.getSurface() - cloud.getSurface()) / game.getHeight();
                    assertTrue(gap >= level.getMinGap() - 1e-9 && gap <= level.getMaxGap() + 1e-9, level + " gap " + gap);
                    assertTrue(gap < 0.5, "under what a jump climbs");
                    assertTrue(Math.abs(under.getX() - cloud.getX()) / game.getWidth() <= level.getMaxShift() + 1e-9,
                        level + " sideways");
                    assertTrue(cloud.getX() - cloud.getWidth() / 2 >= -1e-9, "inside on the left");
                    assertTrue(cloud.getX() + cloud.getWidth() / 2 <= game.getWidth() + 1e-9, "inside on the right");
                }
                return null;
            });
        }
    }

    /**
     * The clouds can really be reached, in the most difficult level too, on small and very large screens.
     */
    @Test
    void shouldLetAGoodPlayerClimbOnEveryLevelAndEveryScreen() throws Exception {
        final double[][] screens = {{1366, 768}, {1920, 1080}, {3840, 2160}};
        for (final double[] size : screens) {
            screen(size[0], size[1]);
            for (final BibouleJumpLevel level : BibouleJumpLevel.values()) {
                final BibouleJump game = launched(level, BibouleJumpEnd.ENDLESS, 11);
                fx(() -> {
                    steps(game, BibouleJump.FIRST_WAIT);
                    for (int i = 0; i < 60 * 45 && game.getPhase() == BibouleJump.Phase.PLAY; i++) {
                        playWell(game);
                        game.step(STEP);
                    }
                    assertEquals(BibouleJump.LIVES, game.getLives(), level + " " + size[0] + ": no fall");
                    assertTrue(game.getScore() > 300, level + " " + size[0] + ": climbs, " + game.getScore());
                    return null;
                });
            }
        }
    }

    @Test
    void shouldCountEachPointWithAnyNumberOfImages() throws Exception {
        final int[] scores = new int[2];
        final double[] steps = {1.0 / 30, 1.0 / 120};
        for (int i = 0; i < 2; i++) {
            final BibouleJump game = launched(BibouleJumpLevel.EASY, BibouleJumpEnd.ENDLESS, 1);
            final double step = steps[i];
            scores[i] = fx(() -> {
                steps(game, BibouleJump.FIRST_WAIT);
                for (double t = 0; t < 0.45; t += step) {
                    game.step(step);
                }
                return game.getScore();
            });
        }
        assertTrue(scores[1] > 0);
        assertEquals(scores[0], scores[1], 4, "almost the same at 30 and at 120 images per second");
    }

    @Test
    void shouldGiveTheStatsThePointsOfTheScore() throws Exception {
        final BibouleJump game = launched(BibouleJumpLevel.EASY, BibouleJumpEnd.ENDLESS, 1);
        final List<Integer> given = new ArrayList<>();
        doAnswer(invocation -> {
            given.add(invocation.getArgument(0));
            return null;
        }).when(stats).incrementNumberOfGoalsReached(anyInt());
        fx(() -> {
            steps(game, BibouleJump.FIRST_WAIT + 0.45);
            return null;
        });
        assertTrue(game.getScore() > 0);
        assertEquals(game.getScore(), given.stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void shouldWaitBeforeTheFirstJump() throws Exception {
        final BibouleJump game = launched(BibouleJumpLevel.EASY, BibouleJumpEnd.TIME_1, 1);
        fx(() -> {
            final double feet = game.getFeetY();
            game.aimAt(0);
            steps(game, BibouleJump.FIRST_WAIT - 0.2);
            assertEquals(feet, game.getFeetY(), 1e-9, "on its cloud");
            assertEquals(game.getWidth() / 2, game.getBibouleX(), 1e-9, "does not go sideways");
            assertEquals(60, game.getTimeLeft(), 1e-9, "the time waits too");
            steps(game, 0.4);
            assertTrue(game.getFeetY() < feet, "it jumps");
            assertTrue(game.getTimeLeft() < 60);
            return null;
        });
    }

    @Test
    void shouldLoseALifeAndKeepThePointsAfterAFall() throws Exception {
        final BibouleJump game = launched(BibouleJumpLevel.NORMAL, BibouleJumpEnd.ENDLESS, 2);
        fx(() -> {
            steps(game, BibouleJump.FIRST_WAIT + 0.4);
            final int score = game.getScore();
            assertTrue(score > 0);
            game.fall();
            assertEquals(BibouleJump.LIVES - 1, game.getLives());
            assertEquals(score, game.getScore(), "the points stay");
            assertEquals(BibouleJump.WAIT_AFTER_FALL, game.getWaiting(), 1e-9);
            assertEquals(BibouleJump.START_LINE * game.getHeight(), game.getFeetY(), 1e-9, "on a new cloud");
            final BibouleJump.Cloud first = game.getClouds().get(0);
            assertEquals(game.getWidth() / 2, first.getX(), 1e-9);
            assertEquals(game.getFeetY(), first.getSurface(), 1e-9);
            return null;
        });
    }

    @Test
    void shouldFallWhenNoCloudIsLookedAt() throws Exception {
        final BibouleJump game = launched(BibouleJumpLevel.HARD, BibouleJumpEnd.ENDLESS, 2);
        fx(() -> {
            steps(game, BibouleJump.FIRST_WAIT);
            // Goes to a side where nothing is under it: the clouds are removed for the test
            game.getClouds().clear();
            steps(game, 5);
            assertTrue(game.getLives() < BibouleJump.LIVES);
            return null;
        });
    }

    @Test
    void shouldEndWithLostAfterTheThirdFall() throws Exception {
        final BibouleJump game = launched(BibouleJumpLevel.EASY, BibouleJumpEnd.ENDLESS, 1);
        fx(() -> {
            game.fall();
            game.fall();
            assertEquals(BibouleJump.Phase.PLAY, game.getPhase());
            game.fall();
            assertEquals(BibouleJump.Phase.OVER, game.getPhase());
            assertEquals(0, game.getLives());
            assertFalse(game.getBiboule().isVisible());
            game.step(STEP);
            game.fall();
            assertEquals(0, game.getLives(), "over: nothing more");
            game.dispose();
            return null;
        });
        verify(gameContext, never()).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldEndWithBravoWhenTheTimeIsOver() throws Exception {
        final BibouleJump game = launched(BibouleJumpLevel.EASY, BibouleJumpEnd.TIME_1, 1);
        fx(() -> {
            steps(game, BibouleJump.FIRST_WAIT);
            for (int i = 0; i < 60 * 62 && game.getPhase() == BibouleJump.Phase.PLAY; i++) {
                playWell(game);
                game.step(STEP);
            }
            assertEquals(BibouleJump.Phase.OVER, game.getPhase());
            return null;
        });
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldNeverEndAloneWithoutFall() throws Exception {
        final BibouleJump game = launched(BibouleJumpLevel.EASY, BibouleJumpEnd.ENDLESS, 1);
        fx(() -> {
            steps(game, BibouleJump.FIRST_WAIT);
            for (int i = 0; i < 60 * 70; i++) {
                playWell(game);
                game.step(STEP);
            }
            assertEquals(BibouleJump.Phase.PLAY, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldFollowTheAnimationSpeedOfTheSettings() throws Exception {
        final BibouleJump normal = launched(BibouleJumpLevel.EASY, BibouleJumpEnd.ENDLESS, 1);
        final double[] moved = new double[2];
        moved[0] = fx(() -> {
            steps(normal, BibouleJump.FIRST_WAIT);
            normal.aimAt(normal.getWidth());
            final double before = normal.getBibouleX();
            steps(normal, 0.2);
            return normal.getBibouleX() - before;
        });
        when(configuration.getAnimationSpeedRatio()).thenReturn(0.5);
        final BibouleJump slow = launched(BibouleJumpLevel.EASY, BibouleJumpEnd.ENDLESS, 1);
        moved[1] = fx(() -> {
            steps(slow, BibouleJump.FIRST_WAIT);
            slow.aimAt(slow.getWidth());
            final double before = slow.getBibouleX();
            steps(slow, 0.2);
            return slow.getBibouleX() - before;
        });
        assertEquals(moved[0] / 2, moved[1], 2);
    }

    @Test
    void shouldGoSidewaysAtTheSpeedOfTheScreenAndStayInIt() throws Exception {
        final BibouleJump game = launched(BibouleJumpLevel.EASY, BibouleJumpEnd.ENDLESS, 1);
        fx(() -> {
            steps(game, BibouleJump.FIRST_WAIT);
            game.aimAt(game.getWidth() * 2);
            final double before = game.getBibouleX();
            steps(game, 0.5);
            assertEquals(BibouleJump.SIDE_SPEED * 0.5, (game.getBibouleX() - before) / game.getWidth(), 0.01);
            steps(game, 1);
            assertTrue(game.getBibouleX() < game.getWidth(), "the Biboule stays in the screen");
            return null;
        });
    }

    @Test
    void shouldKeepMovingCloudsInsideTheScreenAndStillOnesStill() throws Exception {
        final BibouleJump game = launched(BibouleJumpLevel.HARD, BibouleJumpEnd.ENDLESS, 4);
        fx(() -> {
            final List<double[]> before = new ArrayList<>();
            game.getClouds().forEach(cloud -> before.add(new double[]{cloud.getX()}));
            assertTrue(game.getClouds().stream().anyMatch(BibouleJump.Cloud::isMoving));
            for (int i = 0; i < 400; i++) {
                game.getClouds().forEach(cloud -> cloud.move(STEP));
                for (final BibouleJump.Cloud cloud : game.getClouds()) {
                    assertTrue(cloud.getX() - cloud.getWidth() / 2 >= -1e-9);
                    assertTrue(cloud.getX() + cloud.getWidth() / 2 <= game.getWidth() + 1e-9);
                }
            }
            for (int i = 0; i < before.size(); i++) {
                final BibouleJump.Cloud cloud = game.getClouds().get(i);
                if (!cloud.isMoving()) {
                    assertEquals(before.get(i)[0], cloud.getX(), 1e-9);
                }
            }
            return null;
        });
    }

    @Test
    void shouldHaveNoMovingCloudAndSomeSpringboardsInTheEasyLevel() throws Exception {
        final BibouleJump game = launched(BibouleJumpLevel.EASY, BibouleJumpEnd.ENDLESS, 6);
        fx(() -> {
            assertTrue(game.getClouds().stream().noneMatch(BibouleJump.Cloud::isMoving));
            int springboards = 0;
            for (int i = 0; i < 60 * 40; i++) {
                playWell(game);
                game.step(STEP);
                springboards += (int) game.getClouds().stream().filter(BibouleJump.Cloud::hasSpringboard).count();
            }
            assertTrue(springboards > 0);
            return null;
        });
    }

    @Test
    void shouldJumpMuchHigherOnASpringboard() throws Exception {
        final BibouleJump game = launched(BibouleJumpLevel.EASY, BibouleJumpEnd.ENDLESS, 6);
        fx(() -> {
            steps(game, BibouleJump.FIRST_WAIT);
            double fastest = 0;
            for (int i = 0; i < 60 * 60 && fastest == 0; i++) {
                final BibouleJump.Cloud board = game.getClouds().stream().filter(BibouleJump.Cloud::hasSpringboard)
                    .filter(cloud -> cloud.getSurface() > 0).findFirst().orElse(null);
                if (board != null && game.getVelocity() > 0 && board.springboardSurface() > game.getFeetY()
                    && board.springboardSurface() - game.getFeetY() < game.getHeight() * 0.3) {
                    game.aimAt(board.springboardX());
                } else {
                    playWell(game);
                }
                game.step(STEP);
                if (game.getVelocity() < -BibouleJump.JUMP_SPEED) {
                    fastest = game.getVelocity();
                }
            }
            assertEquals(-BibouleJump.SPRINGBOARD_SPEED, fastest, 0.2);
            return null;
        });
        verify(gameContext.getSoundManager(), atLeastOnce()).add(contains("boing"));
    }

    @Test
    void shouldStopAfterDispose() throws Exception {
        final BibouleJump game = launched(BibouleJumpLevel.EASY, BibouleJumpEnd.ENDLESS, 1);
        fx(() -> {
            assertFalse(gameArea.getChildren().isEmpty());
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertEquals(BibouleJump.Phase.OVER, game.getPhase());
            game.step(STEP);
            return null;
        });
    }

    @Test
    void shouldReplayWithTheSameClouds() throws Exception {
        final BibouleJump first = launched(BibouleJumpLevel.NORMAL, BibouleJumpEnd.ENDLESS, 42);
        final double[] places = fx(() -> first.getClouds().stream().mapToDouble(BibouleJump.Cloud::getX).toArray());
        final BibouleJump second = launched(BibouleJumpLevel.NORMAL, BibouleJumpEnd.ENDLESS, 42);
        assertArrayEquals(places, fx(() -> second.getClouds().stream().mapToDouble(BibouleJump.Cloud::getX).toArray()));
    }

    @Test
    void shouldGiveAllTheVariants() {
        final Set<IGameVariant> variants = new BibouleJumpGameVariantGenerator().getVariants();
        assertEquals(9, variants.size());
        final IGameVariant hard = new GroupedGameVariant(BibouleJumpGameVariantGenerator.GROUP,
            BibouleJumpLevel.HARD.getKey(), BibouleJumpEnd.TIME_3.getKey(), null);
        assertTrue(variants.contains(hard));
        assertEquals(BibouleJumpLevel.HARD, BibouleJumpGameLauncher.levelOf(hard));
        assertEquals(BibouleJumpEnd.TIME_3, BibouleJumpGameLauncher.endOf(hard));
    }

    @Test
    void shouldPlayTheNormalLevelWithoutEndForAnOldVariant() {
        final IGameVariant old = new IntGameVariant(1);
        assertEquals(BibouleJumpLevel.NORMAL, BibouleJumpGameLauncher.levelOf(old));
        assertEquals(BibouleJumpEnd.ENDLESS, BibouleJumpGameLauncher.endOf(old));
    }

    @Test
    void shouldMakeTheEasyLevelEasierThanTheHardOne() {
        assertTrue(BibouleJumpLevel.EASY.getCloudWidth() > BibouleJumpLevel.NORMAL.getCloudWidth());
        assertTrue(BibouleJumpLevel.NORMAL.getCloudWidth() > BibouleJumpLevel.HARD.getCloudWidth());
        assertTrue(BibouleJumpLevel.EASY.getMaxGap() < BibouleJumpLevel.NORMAL.getMaxGap());
        assertTrue(BibouleJumpLevel.NORMAL.getMaxGap() < BibouleJumpLevel.HARD.getMaxGap());
        assertEquals(0, BibouleJumpLevel.EASY.getMovingChance());
    }
}

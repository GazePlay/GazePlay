package net.gazeplay.games.gazeRace;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * GazeRace (reviewed on 9/11/2026, maintainer's rules): the same game in parts of the screen on every screen, the delay
 * at the start, the points, the lives and the invincibility, the barriers, the ends, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class GazeRaceTest {

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
    GazeRaceStats stats;

    private final Pane gameArea = new Pane();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getAnimationSpeedRatio()).thenReturn(1.0);
        when(configuration.getGameStartDelay()).thenReturn(0);
        screen(1920, 1080);
    }

    private void screen(final double width, final double height) {
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(width, height));
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private GazeRace launched(final GazeRaceVariant road, final GazeRaceLevel level, final GazeRaceEnd end,
                              final double seed) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final GazeRace game = new GazeRace(gameContext, stats, road, level, end, seed);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private GazeRace launched(final GazeRaceLevel level, final GazeRaceEnd end) throws Exception {
        return launched(GazeRaceVariant.HORIZONTAL, level, end, 1);
    }

    private static void steps(final GazeRace game, final double seconds) {
        for (double t = 0; t < seconds; t += STEP) {
            game.step(STEP);
        }
    }

    /**
     * Steps without anything on the road: no car can be hit.
     */
    private static void emptySteps(final GazeRace game, final double seconds) {
        for (double t = 0; t < seconds && game.getPhase() == GazeRace.Phase.PLAY; t += STEP) {
            game.getMobiles().clear();
            game.step(STEP);
        }
    }

    @Test
    void shouldGoByAtTheSamePartOfTheRoadOnEveryScreen() throws Exception {
        final double[][] screens = {{1366, 768}, {1920, 1080}, {3840, 2160}, {1280, 1024}};
        for (final double[] size : screens) {
            screen(size[0], size[1]);
            final GazeRace game = launched(GazeRaceLevel.NORMAL, GazeRaceEnd.ENDLESS);
            fx(() -> {
                game.getMobiles().clear();
                final GazeRace.Mobile car = game.addCar();
                assertNotNull(car);
                final double before = car.getAlong();
                game.step(0.05);
                final double moved = Math.abs(car.getAlong() - before) / game.getLength();
                assertEquals(GazeRaceLevel.NORMAL.getStartSpeed() * 0.05 * Math.abs(car.screenSpeed()), moved, 0.0005,
                    size[0] + " x " + size[1]);
                assertEquals(GazeRace.CAR_LENGTH, car.getLength() / Math.min(size[0], size[1]), 1e-9);
                return null;
            });
        }
    }

    /**
     * Maintainer's remark of 9/11/2026: the cars coming the other way went back as fast as the road, so that they looked
     * parked on it. On the road, the cars of both sides now move, each its way.
     */
    @Test
    void shouldMoveTheCarsOfBothSidesOnTheRoad() throws Exception {
        final GazeRace game = launched(GazeRaceLevel.NORMAL, GazeRaceEnd.ENDLESS);
        fx(() -> {
            game.getMobiles().clear();
            GazeRace.Mobile overtaking = null;
            GazeRace.Mobile coming = null;
            while (overtaking == null || coming == null) {
                final GazeRace.Mobile car = game.addCar();
                if (car != null && car.getDirection() > 0 && overtaking == null) {
                    overtaking = car;
                } else if (car != null && car.getDirection() < 0 && coming == null) {
                    coming = car;
                }
            }
            final double overtakingBefore = overtaking.getAlong();
            final double comingBefore = coming.getAlong();
            game.step(0.05);
            final double road = -GazeRaceLevel.NORMAL.getStartSpeed() * game.getLength() * 0.05;
            final double overtakingOnRoad = overtaking.getAlong() - overtakingBefore - road;
            final double comingOnRoad = coming.getAlong() - comingBefore - road;
            // The road goes a little faster during the step
            assertEquals(-2 * road, overtakingOnRoad, 0.1, "goes forward on the road, twice the speed of the road");
            assertEquals(road, comingOnRoad, 0.1, "comes the other way on the road, at the speed of the road");
            return null;
        });
    }

    /**
     * Maintainer's remark of 9/11/2026: on the wide white band in the middle, where no car went, nothing could happen.
     * On the fine line now, the cars of both sides can hit the player, and almost as often as in the middle of a side.
     */
    @Test
    void shouldLeaveNoSafePlaceOnTheLineInTheMiddle() throws Exception {
        final GazeRace game = launched(GazeRaceLevel.NORMAL, GazeRaceEnd.ENDLESS);
        fx(() -> {
            final double width = game.getPlayer().getWidth();
            final double line = game.getAcross() / 2;
            final double side = (game.getAcross() * GazeRace.EDGE + line) / 2;
            int onTheLineOvertaking = 0;
            int onTheLineComing = 0;
            int inTheSide = 0;
            int overtaking = 0;
            for (int i = 0; i < 4000; i++) {
                game.getMobiles().clear();
                final GazeRace.Mobile car = game.addCar();
                final boolean touchesTheLine = Math.abs(car.getAcross() - line) < (width + car.getWidth()) / 2 * GazeRace.HIT_BOX;
                if (car.getDirection() > 0) {
                    overtaking++;
                    onTheLineOvertaking += touchesTheLine ? 1 : 0;
                    inTheSide += Math.abs(car.getAcross() - side) < (width + car.getWidth()) / 2 * GazeRace.HIT_BOX ? 1 : 0;
                } else {
                    onTheLineComing += touchesTheLine ? 1 : 0;
                }
            }
            // Measured on 9/11/2026: 202 + 199 on the line, 596 in the middle of the side (4000 cars); only touching the
            // line, the cars hit the player on it 3.6 times less often than in the middle of a side
            final String counts = "line: " + onTheLineOvertaking + " + " + onTheLineComing + ", side: " + inTheSide
                + " of " + overtaking;
            assertTrue(onTheLineOvertaking > 0 && onTheLineComing > 0, "both sides: " + counts);
            assertTrue(onTheLineOvertaking + onTheLineComing > inTheSide / 2, "almost as often: " + counts);
            return null;
        });
    }

    @Test
    void shouldWaitForTheDelayOfTheSettings() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final GazeRace game = launched(GazeRaceLevel.NORMAL, GazeRaceEnd.TIME_1);
        fx(() -> {
            final double along = game.getPlayer().getAlong();
            game.aimAt(game.getWidth(), 0);
            steps(game, 0.9);
            assertTrue(game.getMobiles().isEmpty(), "no car yet");
            assertEquals(along, game.getPlayer().getAlong(), 1e-9, "the player waits");
            assertEquals(60, game.getTimeLeft(), 1e-9, "the time waits too");
            steps(game, 0.2);
            assertFalse(game.getMobiles().isEmpty(), "the cars come");
            assertTrue(game.getPlayer().getAlong() > along);
            assertTrue(game.getTimeLeft() < 60);
            return null;
        });
    }

    @Test
    void shouldGiveAPointEveryThreeSecondsAndTellTheStats() throws Exception {
        final GazeRace game = launched(GazeRaceLevel.EASY, GazeRaceEnd.ENDLESS);
        fx(() -> {
            emptySteps(game, 9.1);
            return null;
        });
        assertEquals(3, game.getScore());
        verify(stats, times(3)).incrementNumberOfGoalsReached();
    }

    @Test
    void shouldCountThePointsWithAnyNumberOfImages() throws Exception {
        final int[] scores = new int[2];
        final double[] steps = {1.0 / 30, 1.0 / 144};
        for (int i = 0; i < 2; i++) {
            final GazeRace game = launched(GazeRaceLevel.EASY, GazeRaceEnd.ENDLESS);
            final double step = steps[i];
            scores[i] = fx(() -> {
                for (double t = 0; t < 12.1; t += step) {
                    game.getMobiles().clear();
                    game.step(step);
                }
                return game.getScore();
            });
        }
        assertEquals(4, scores[0]);
        assertEquals(scores[0], scores[1], "the same at 30 and at 144 images per second");
    }

    @Test
    void shouldLoseALifeAndBeInvincibleAfterHittingACar() throws Exception {
        final GazeRace game = launched(GazeRaceLevel.NORMAL, GazeRaceEnd.ENDLESS);
        fx(() -> {
            emptySteps(game, 10);
            assertTrue(game.getSpeed() > GazeRaceLevel.NORMAL.getStartSpeed());
            final GazeRace.Mobile car = game.addCar();
            car.moveTo(game.getPlayer().getAlong(), game.getPlayer().getAcross());
            game.step(STEP);
            assertEquals(GazeRace.LIVES - 1, game.getLives());
            assertFalse(game.getMobiles().contains(car), "the car hit is removed");
            assertEquals(GazeRaceLevel.NORMAL.getStartSpeed(), game.getSpeed(), 0.001, "the road starts again slowly");
            assertEquals(GazeRace.INVINCIBLE, game.getInvincible(), 0.05);
            // Invincible: another car goes through
            final GazeRace.Mobile other = game.addCar();
            other.moveTo(game.getPlayer().getAlong(), game.getPlayer().getAcross());
            game.step(STEP);
            assertEquals(GazeRace.LIVES - 1, game.getLives());
            emptySteps(game, GazeRace.INVINCIBLE);
            assertEquals(0, game.getInvincible(), 1e-9);
            final GazeRace.Mobile third = game.addCar();
            third.moveTo(game.getPlayer().getAlong(), game.getPlayer().getAcross());
            game.step(STEP);
            assertEquals(GazeRace.LIVES - 2, game.getLives());
            return null;
        });
    }

    @Test
    void shouldLoseALifeOnABarrierThatStays() throws Exception {
        final GazeRace game = launched(GazeRaceLevel.EASY, GazeRaceEnd.ENDLESS);
        fx(() -> {
            emptySteps(game, GazeRace.BARRIER_EVERY - 0.5);
            game.getMobiles().clear();
            steps(game, 0.6);
            final GazeRace.Mobile barrier = game.getMobiles().stream()
                .filter(mobile -> mobile.getKind() == GazeRace.Kind.BARRIER).findFirst().orElseThrow();
            assertEquals(game.getAcross() / 2, barrier.getAcross(), 1e-9, "on the band in the middle");
            assertTrue(barrier.getAlong() > game.getLength(), "comes from ahead");
            assertEquals(-1, barrier.getDirection());
            game.hit(barrier);
            assertEquals(GazeRace.LIVES - 1, game.getLives());
            assertTrue(game.getMobiles().contains(barrier), "a barrier stays");
            return null;
        });
    }

    @Test
    void shouldEndWithLostAfterTheThirdLife() throws Exception {
        final GazeRace game = launched(GazeRaceLevel.EASY, GazeRaceEnd.ENDLESS);
        fx(() -> {
            emptySteps(game, 1);
            for (int i = 0; i < 2; i++) {
                game.hit(game.addCar());
                assertEquals(GazeRace.Phase.PLAY, game.getPhase());
            }
            game.hit(game.addCar());
            assertEquals(GazeRace.Phase.OVER, game.getPhase());
            assertEquals(0, game.getLives());
            game.step(STEP);
            game.hit(game.addCar());
            assertEquals(0, game.getLives(), "over: nothing more");
            game.dispose();
            return null;
        });
        verify(gameContext, never()).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldEndWithBravoWhenTheTimeIsOver() throws Exception {
        final GazeRace game = launched(GazeRaceLevel.HARD, GazeRaceEnd.TIME_1);
        fx(() -> {
            emptySteps(game, 61);
            assertEquals(GazeRace.Phase.OVER, game.getPhase());
            assertEquals(GazeRace.LIVES, game.getLives());
            return null;
        });
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldNeverEndAloneWithoutHit() throws Exception {
        final GazeRace game = launched(GazeRaceLevel.EASY, GazeRaceEnd.ENDLESS);
        fx(() -> {
            emptySteps(game, 200);
            assertEquals(GazeRace.Phase.PLAY, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldReachTheTopSpeedAndNoMore() throws Exception {
        for (final GazeRaceLevel level : GazeRaceLevel.values()) {
            final GazeRace game = launched(level, GazeRaceEnd.ENDLESS);
            fx(() -> {
                emptySteps(game, GazeRace.SECONDS_TO_TOP / 2);
                assertEquals((level.getStartSpeed() + level.getTopSpeed()) / 2, game.getSpeed(), 0.002, level.name());
                emptySteps(game, GazeRace.SECONDS_TO_TOP);
                assertEquals(level.getTopSpeed(), game.getSpeed(), 1e-9, level.name());
                return null;
            });
        }
    }

    @Test
    void shouldFollowTheAnimationSpeedOfTheSettings() throws Exception {
        final double[] moved = new double[2];
        for (int i = 0; i < 2; i++) {
            when(configuration.getAnimationSpeedRatio()).thenReturn(i == 0 ? 1.0 : 0.5);
            final GazeRace game = launched(GazeRaceLevel.NORMAL, GazeRaceEnd.ENDLESS);
            moved[i] = fx(() -> {
                game.getMobiles().clear();
                final GazeRace.Mobile car = game.addCar();
                final double before = car.getAlong();
                game.step(0.05);
                return Math.abs(car.getAlong() - before);
            });
        }
        assertEquals(moved[0] / 2, moved[1], 0.5);
    }

    @Test
    void shouldDriveTheCarTowardsTheGazeAtItsSpeedAndStayOnTheRoad() throws Exception {
        final GazeRace game = launched(GazeRaceLevel.EASY, GazeRaceEnd.ENDLESS);
        fx(() -> {
            final GazeRace.Mobile player = game.getPlayer();
            final double before = player.getAlong();
            game.aimAt(game.getWidth() * 2, player.getAcross());
            emptySteps(game, 0.5);
            assertEquals(GazeRace.PLAYER_SPEED * 0.5, (player.getAlong() - before) / game.getHeight(), 0.01);
            game.aimAt(-500, game.getHeight() * 3);
            emptySteps(game, 10);
            assertEquals(player.getLength() / 2, player.getAlong(), 1e-9, "stops at the start of the road");
            assertEquals(game.getAcross() * (1 - GazeRace.EDGE) - player.getWidth() / 2, player.getAcross(), 1e-9,
                "stops at the edge of the road");
            return null;
        });
    }

    @Test
    void shouldBringTheCarsOfEachSideFromTheirSideOfTheScreen() throws Exception {
        for (final GazeRaceLevel level : GazeRaceLevel.values()) {
            final GazeRace game = launched(level, GazeRaceEnd.ENDLESS);
            fx(() -> {
                game.getMobiles().clear();
                game.step(STEP);
                final List<GazeRace.Mobile> cars = game.getCars();
                assertTrue(cars.size() >= 1 && cars.size() <= level.getMostCars(), level + ": " + cars.size());
                for (int i = 0; i < 50; i++) {
                    final GazeRace.Mobile car = game.addCar();
                    if (car == null) {
                        continue;
                    }
                    // On its side, covering the line in the middle by a quarter of its width at most
                    final double cover = car.getWidth() * GazeRace.LINE_COVER - 1e-9;
                    if (car.getDirection() > 0) {
                        assertTrue(car.getAlong() < 0, "overtaking: from behind");
                        assertTrue(car.getAcross() <= game.getAcross() / 2 - cover, "on its side");
                    } else {
                        assertTrue(car.getAlong() > game.getLength(), "coming the other way: from ahead");
                        assertTrue(car.getAcross() >= game.getAcross() / 2 + cover, "on its side");
                    }
                    assertTrue(car.getAcross() - car.getWidth() / 2 > game.getAcross() * GazeRace.EDGE);
                    assertTrue(car.getAcross() + car.getWidth() / 2 < game.getAcross() * (1 - GazeRace.EDGE));
                }
                final List<GazeRace.Mobile> all = game.getCars();
                for (int i = 0; i < all.size(); i++) {
                    for (int j = i + 1; j < all.size(); j++) {
                        assertFalse(all.get(i).touches(all.get(j)), "never on one another");
                    }
                }
                return null;
            });
        }
    }

    @Test
    void shouldAimAlongTheVerticalRoadFromTheBottom() throws Exception {
        final GazeRace game = launched(GazeRaceVariant.VERTICAL, GazeRaceLevel.EASY, GazeRaceEnd.ENDLESS, 1);
        fx(() -> {
            assertEquals(game.getHeight(), game.getLength(), 1e-9);
            assertEquals(game.getWidth(), game.getAcross(), 1e-9);
            game.aimAt(300, 100);
            assertEquals(game.getHeight() - 100, game.getAimAlong(), 1e-9, "the top of the screen is far ahead");
            assertEquals(300, game.getAimAcross(), 1e-9);
            return null;
        });
    }

    @Test
    void shouldStopAfterDispose() throws Exception {
        final GazeRace game = launched(GazeRaceLevel.EASY, GazeRaceEnd.ENDLESS);
        fx(() -> {
            assertFalse(gameArea.getChildren().isEmpty());
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertEquals(GazeRace.Phase.OVER, game.getPhase());
            game.step(STEP);
            return null;
        });
    }

    @Test
    void shouldReplayWithTheSameCars() throws Exception {
        final double[][] places = new double[2][];
        for (int i = 0; i < 2; i++) {
            final GazeRace game = launched(GazeRaceVariant.HORIZONTAL, GazeRaceLevel.HARD, GazeRaceEnd.ENDLESS, 42);
            places[i] = fx(() -> {
                game.step(STEP);
                return game.getCars().stream().mapToDouble(GazeRace.Mobile::getAcross).toArray();
            });
        }
        assertTrue(places[0].length > 0);
        assertArrayEquals(places[0], places[1]);
    }

    @Test
    void shouldGiveAllTheVariants() {
        final Set<IGameVariant> variants = new GazeRaceVariantGenerator().getVariants();
        assertEquals(18, variants.size());
        final IGameVariant variant = new GroupedGameVariant(GazeRaceVariant.VERTICAL.getKey(),
            GazeRaceLevel.HARD.getKey(), GazeRaceEnd.TIME_3.getKey(), null);
        assertTrue(variants.contains(variant));
        assertEquals(GazeRaceVariant.VERTICAL, GazeRaceLauncher.roadOf(variant));
        assertEquals(GazeRaceLevel.HARD, GazeRaceLauncher.levelOf(variant));
        assertEquals(GazeRaceEnd.TIME_3, GazeRaceLauncher.endOf(variant));
    }

    @Test
    void shouldPlayTheOldVariantsOnTheirRoadAtTheNormalLevelWithoutEnd() {
        final IGameVariant old = IGameVariant.toGameVariant(
            "EnumGameVariant:net.gazeplay.games.gazeRace.GazeRaceVariant:VERTICAL");
        assertInstanceOf(EnumGameVariant.class, old);
        assertEquals(GazeRaceVariant.VERTICAL, GazeRaceLauncher.roadOf(old));
        assertEquals(GazeRaceLevel.NORMAL, GazeRaceLauncher.levelOf(old));
        assertEquals(GazeRaceEnd.ENDLESS, GazeRaceLauncher.endOf(old));
        assertEquals(GazeRaceVariant.HORIZONTAL, GazeRaceLauncher.roadOf(null));
    }

    @Test
    void shouldMakeTheEasyLevelEasierThanTheHardOne() {
        assertTrue(GazeRaceLevel.EASY.getMostCars() < GazeRaceLevel.NORMAL.getMostCars());
        assertTrue(GazeRaceLevel.NORMAL.getMostCars() < GazeRaceLevel.HARD.getMostCars());
        assertTrue(GazeRaceLevel.EASY.getStartSpeed() < GazeRaceLevel.NORMAL.getStartSpeed());
        assertTrue(GazeRaceLevel.NORMAL.getTopSpeed() < GazeRaceLevel.HARD.getTopSpeed());
        // The normal level is the game of before the review, on a 1920 pixels wide road
        assertEquals(300, GazeRaceLevel.NORMAL.getStartSpeed() * 1920, 1);
        assertEquals(900, GazeRaceLevel.NORMAL.getTopSpeed() * 1920, 3);
    }
}

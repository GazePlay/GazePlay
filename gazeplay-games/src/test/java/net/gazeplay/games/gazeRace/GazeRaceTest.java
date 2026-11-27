package net.gazeplay.games.gazeRace;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
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
 * at the start, the points, the lives and the invincibility, the barriers, the ends, the variants; since 27/11/2026
 * (tester's feedback, point 54), the stars caught, the cars avoided, "Super !" and the very easy level.
 */
@ExtendWith(ApplicationExtension.class)
class GazeRaceTest {

    /** The games launched by a test, closed after it (18/11/2026). */
    @org.junit.jupiter.api.extension.RegisterExtension
    final net.gazeplay.games.LaunchedGames launchedGames = new net.gazeplay.games.LaunchedGames();

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
            launchedGames.add(game).launch();
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

    /**
     * Point 54: no more point every 3 seconds; nothing on the road, no point.
     */
    @Test
    void shouldGiveNoPointForTheTimeAlone() throws Exception {
        final GazeRace game = launched(GazeRaceLevel.EASY, GazeRaceEnd.ENDLESS);
        fx(() -> {
            emptySteps(game, 9.1);
            return null;
        });
        assertEquals(0, game.getScore());
        verify(stats, never()).incrementNumberOfGoalsReached();
    }

    /**
     * Point 54: stars come ahead every {@link GazeRace#STAR_EVERY} seconds, go by with the road, and are caught by
     * driving on them: a point, a sound, "+1"; they do not hurt.
     */
    @Test
    void shouldCatchTheStarsTheCarDrivesOn() throws Exception {
        final GazeRace game = launched(GazeRaceLevel.EASY, GazeRaceEnd.ENDLESS);
        fx(() -> {
            game.getMobiles().clear();
            final GazeRace.Mobile star = game.addStar();
            assertNotNull(star);
            assertEquals(GazeRace.Kind.STAR, star.getKind());
            assertTrue(star.getAlong() > game.getLength(), "comes from ahead");
            assertEquals(-1, star.getDirection());
            star.moveTo(game.getPlayer().getAlong(), game.getPlayer().getAcross());
            game.step(STEP);
            assertTrue(game.getStars().isEmpty(), "caught");
            assertEquals(1, game.getStarsCaught());
            assertEquals(1, game.getScore());
            assertEquals(GazeRace.LIVES, game.getLives(), "a star does not hurt");
            assertEquals(1, game.rewardsShown(), "+1");
            steps(game, GazeRace.REWARD + 0.1);
            assertEquals(0, game.rewardsShown(), "gone");
            return null;
        });
        verify(gameContext.getSoundManager()).add(GazeRace.STAR_SOUND);
        verify(stats).incrementNumberOfGoalsReached();
        // The stars come by themselves
        final GazeRace other = launched(GazeRaceLevel.EASY, GazeRaceEnd.ENDLESS);
        fx(() -> {
            for (double t = 0; t < GazeRace.STAR_EVERY; t += STEP) {
                other.getMobiles().removeIf(mobile -> mobile.getKind() == GazeRace.Kind.CAR);
                other.step(STEP);
            }
            assertFalse(other.getStars().isEmpty());
            return null;
        });
    }

    /**
     * Point 54: each car passing the car of the player without touching it gives a point; every
     * {@link GazeRace#SUPER_EVERY} cars avoided, "Super !" with a burst of stars and a sound. A car hit gives nothing.
     */
    @Test
    void shouldRewardTheCarsAvoided() throws Exception {
        final GazeRace game = launched(GazeRaceLevel.NORMAL, GazeRaceEnd.ENDLESS);
        fx(() -> {
            final GazeRace.Mobile player = game.getPlayer();
            for (int i = 1; i <= GazeRace.SUPER_EVERY; i++) {
                game.getMobiles().clear();
                GazeRace.Mobile car = null;
                while (car == null || car.getDirection() < 0) {
                    game.getMobiles().clear();
                    car = game.addCar();
                }
                // Overtaking far from the player, then just ahead of it: passed
                final double away = player.getAcross() > game.getAcross() / 2 ? game.getAcross() * 0.2
                    : game.getAcross() * 0.8;
                car.moveTo(player.getAlong() - 1, away);
                game.step(STEP);
                car.moveTo(player.getAlong() + game.getLength() * 0.05, away);
                game.step(STEP);
                assertEquals(i, game.getCarsAvoided());
            }
            assertEquals(GazeRace.SUPER_EVERY, game.getScore());
            // "+1" and "Super !" with its 8 stars
            assertTrue(game.rewardsShown() >= 1 + 1 + 8, "rewards: " + game.rewardsShown());
            // A car hit is not avoided
            game.getMobiles().clear();
            final GazeRace.Mobile hit = game.addCar();
            hit.moveTo(player.getAlong(), player.getAcross());
            game.step(STEP);
            assertEquals(GazeRace.SUPER_EVERY, game.getCarsAvoided());
            assertEquals(GazeRace.LIVES - 1, game.getLives());
            return null;
        });
        verify(gameContext.getSoundManager()).add(GazeRace.SUPER_SOUND);
        verify(stats, times(GazeRace.SUPER_EVERY)).incrementNumberOfGoalsReached();
    }

    /**
     * Point 54: the very easy level, a car every 2.5 seconds, all overtaking, slowly, no barrier.
     */
    @Test
    void shouldBringOnlyFewOvertakingCarsAndNoBarrierAtTheVeryEasyLevel() throws Exception {
        assertSame(GazeRaceLevel.VERY_EASY, GazeRaceLevel.values()[0], "the easiest first");
        assertTrue(GazeRaceLevel.VERY_EASY.getStartSpeed() < GazeRaceLevel.EASY.getStartSpeed());
        assertTrue(GazeRaceLevel.VERY_EASY.getTopSpeed() < GazeRaceLevel.EASY.getTopSpeed());
        final GazeRace game = launched(GazeRaceLevel.VERY_EASY, GazeRaceEnd.ENDLESS);
        fx(() -> {
            for (int i = 0; i < 100; i++) {
                game.getMobiles().clear();
                final GazeRace.Mobile car = game.addCar();
                assertTrue(car.getDirection() > 0, "overtaking");
            }
            game.getMobiles().clear();
            int arrivals = 0;
            boolean barrier = false;
            for (double t = 0; t < GazeRace.BARRIER_EVERY * 2; t += STEP) {
                final int before = game.getCars().size();
                game.step(STEP);
                arrivals += game.getCars().size() > before ? 1 : 0;
                barrier |= game.getMobiles().stream().anyMatch(m -> m.getKind() == GazeRace.Kind.BARRIER);
                // Keep the player out of the way
                game.getMobiles().removeIf(m -> m.getKind() == GazeRace.Kind.CAR && m.getAlong() > 0);
            }
            assertFalse(barrier, "no barrier");
            assertEquals(GazeRace.BARRIER_EVERY * 2 / GazeRaceLevel.VERY_EASY.getCarInterval(), arrivals, 1);
            return null;
        });
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
        // With or without hearts (17/11/2026)
        // With or without hearts (17/11/2026), 4 levels since the very easy one (27/11/2026)
        assertEquals(2 * 4 * 3 * 2, variants.size());
        final IGameVariant variant = new GroupedGameVariant(GazeRaceVariant.VERTICAL.getKey(),
            GazeRaceLevel.HARD.getKey(), GazeRaceEnd.TIME_3.getKey(), null, LivesVariant.WITH);
        assertTrue(variants.contains(variant));
        assertEquals(GazeRaceVariant.VERTICAL, GazeRaceLauncher.roadOf(variant));
        assertEquals(GazeRaceLevel.HARD, GazeRaceLauncher.levelOf(variant));
        assertEquals(GazeRaceEnd.TIME_3, GazeRaceLauncher.endOf(variant));
        final IGameVariant endlessWithout = new GroupedGameVariant(GazeRaceVariant.HORIZONTAL.getKey(),
            GazeRaceLevel.EASY.getKey(), GazeRaceEnd.ENDLESS.getKey(), null, LivesVariant.WITHOUT);
        assertTrue(variants.contains(endlessWithout));
        assertFalse(LivesVariant.withLives(endlessWithout));
        // The buttons of the end: 1 minute, 3 minutes, then without end on the right (rule of 17/11/2026)
        assertEquals(List.of(GazeRaceEnd.TIME_1, GazeRaceEnd.TIME_3, GazeRaceEnd.ENDLESS),
            List.of(GazeRaceEnd.values()));
    }

    /**
     * "Without hearts" (maintainer's choice of 17/11/2026): no hearts, and the first hit loses the game ("Lost!", then
     * the statistics); the hit is counted as a mistake.
     */
    @Test
    void shouldLoseAtTheFirstHitWithoutHearts() throws Exception {
        final GazeRace game = fx(() -> {
            gameArea.getChildren().clear();
            final GazeRace created = new GazeRace(gameContext, stats, GazeRaceVariant.HORIZONTAL, GazeRaceLevel.EASY,
                GazeRaceEnd.TIME_3, 1);
            created.setWithLives(false);
            created.setManual();
            launchedGames.add(created).launch();
            return created;
        });
        fx(() -> {
            assertFalse(game.isWithLives());
            emptySteps(game, 1);
            game.hit(game.addCar());
            assertEquals(GazeRace.Phase.OVER, game.getPhase());
            assertEquals(0, game.getLives());
            game.dispose();
            return null;
        });
        verify(stats).incrementNumberOfMistakes();
        verify(gameContext, never()).playWinTransition(anyLong(), any());
        assertTrue(GazeRaceLauncher.withLives(game, new GroupedGameVariant(GazeRaceVariant.HORIZONTAL.getKey(),
            GazeRaceLevel.EASY.getKey(), GazeRaceEnd.ENDLESS.getKey(), null, LivesVariant.WITH)).isWithLives());
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
        assertTrue(GazeRaceLevel.VERY_EASY.getMostCars() <= GazeRaceLevel.EASY.getMostCars());
        assertTrue(GazeRaceLevel.EASY.getMostCars() < GazeRaceLevel.NORMAL.getMostCars());
        assertTrue(GazeRaceLevel.NORMAL.getMostCars() < GazeRaceLevel.HARD.getMostCars());
        assertTrue(GazeRaceLevel.EASY.getStartSpeed() < GazeRaceLevel.NORMAL.getStartSpeed());
        assertTrue(GazeRaceLevel.NORMAL.getTopSpeed() < GazeRaceLevel.HARD.getTopSpeed());
        // The normal level is the game of before the review, on a 1920 pixels wide road
        assertEquals(300, GazeRaceLevel.NORMAL.getStartSpeed() * 1920, 1);
        assertEquals(900, GazeRaceLevel.NORMAL.getTopSpeed() * 1920, 3);
    }
}

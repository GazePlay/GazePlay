package net.gazeplay.games.rushhour;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Embouteillage" (Traffic Jam, name code RushHour; reviewed on 9/11/2026, maintainer's rules): the parkings of the file
 * and their fewest moves, choosing a vehicle then a free box with a fixation of the length of the settings, the restart
 * button, the red car leaving, the ends, the layout, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class RushHourTest {

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
    Stats stats;

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
        when(configuration.getFixationLength()).thenReturn(1000);
        screen(1920, 1080);
    }

    private void screen(final double width, final double height) {
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(width, height));
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private RushHour launched(final RushHourLevel level, final RushHourEnd end, final double seed) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final RushHour game = new RushHour(gameContext, stats, level, end, seed);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private static void steps(final RushHour game, final double seconds) {
        for (double t = 0; t < seconds; t += STEP) {
            game.step(STEP);
        }
    }

    private RushHour playing(final RushHourLevel level, final RushHourEnd end, final double seed) throws Exception {
        final RushHour game = launched(level, end, seed);
        fx(() -> {
            steps(game, 0.05);
            return null;
        });
        assertEquals(RushHour.Phase.PLAY, game.getPhase());
        return game;
    }

    /**
     * @return the moves (vehicle, position) of a shortest way to bring the red car to the exit, by a search in breadth
     */
    private static List<int[]> solution(final String boxes) {
        final Map<String, String> parents = new HashMap<>();
        final Map<String, int[]> lastMove = new HashMap<>();
        final Deque<String> queue = new ArrayDeque<>();
        parents.put(boxes, null);
        queue.add(boxes);
        while (!queue.isEmpty()) {
            final String state = queue.poll();
            final Parking parking = new Parking(state, 0);
            if (parking.isSolved()) {
                final List<int[]> moves = new ArrayList<>();
                for (String at = state; parents.get(at) != null; at = parents.get(at)) {
                    moves.add(lastMove.get(at));
                }
                Collections.reverse(moves);
                return moves;
            }
            final List<Parking.Vehicle> vehicles = parking.getVehicles();
            for (int index = 0; index < vehicles.size(); index++) {
                final Parking.Vehicle vehicle = vehicles.get(index);
                final int from = vehicle.position;
                for (final int position : parking.reachable(vehicle)) {
                    vehicle.position = position;
                    final String next = parking.boxes();
                    vehicle.position = from;
                    if (!parents.containsKey(next)) {
                        parents.put(next, state);
                        lastMove.put(next, new int[]{index, position});
                        queue.add(next);
                    }
                }
            }
        }
        throw new AssertionError("No way out: " + boxes);
    }

    /**
     * Brings the red car to the exit with fixations of the length of the settings: the vehicle, then its free box.
     */
    private void solve(final RushHour game) throws Exception {
        for (final int[] move : solution(game.getParking().boxes())) {
            fx(() -> {
                final Parking.Vehicle vehicle = game.getParking().getVehicles().get(move[0]);
                if (game.getSelected() != vehicle) {
                    game.lookAt(game.carOf(vehicle));
                    steps(game, 1.05);
                    assertSame(vehicle, game.getSelected());
                }
                final Object mark = game.markOf(move[1]);
                assertNotNull(mark);
                game.lookAt(mark);
                steps(game, 1.05);
                assertEquals(move[1], vehicle.position);
                return null;
            });
        }
    }

    @Test
    void shouldGiveThirtyParkingsOfEachLevelAllDifferentWithTheirFewestMovesInTheRange() {
        final Map<String, List<String[]>> parkings = RushHour.parkings();
        final Set<String> seen = new HashSet<>();
        final int[][] ranges = {{3, 8}, {9, 16}, {17, 30}};
        for (final RushHourLevel level : RushHourLevel.values()) {
            final List<String[]> list = parkings.get(level.getFileName());
            assertEquals(30, list.size(), level.name());
            for (final String[] parking : list) {
                final int moves = Integer.parseInt(parking[0]);
                assertTrue(moves >= ranges[level.ordinal()][0] && moves <= ranges[level.ordinal()][1],
                    level + " " + parking[1]);
                assertTrue(seen.add(parking[1]), parking[1]);
                // Every vehicle inside, none over another, the red car not at the exit already
                final Parking read = new Parking(parking[1], moves);
                assertEquals(parking[1], read.boxes());
                assertFalse(read.isSolved());
            }
        }
    }

    @Test
    void shouldFindTheFewestMovesOfEveryParkingByAnotherSearch() {
        for (final List<String[]> list : RushHour.parkings().values()) {
            for (final String[] parking : list) {
                assertEquals(Integer.parseInt(parking[0]), solution(parking[1]).size(), parking[1]);
            }
        }
    }

    @Test
    void shouldSlideAVehicleByAsManyFreeBoxesAsWantedButNeverThroughAnother() {
        // A: the red car; B: a truck standing in column 4; C: a car lying on the last line
        final Parking parking = new Parking(""
            + "......"
            + "....B."
            + "AA..B."
            + "....B."
            + "......"
            + "CC....", 2);
        final Parking.Vehicle red = parking.red();
        assertEquals(List.of(1, 2), parking.reachable(red));
        assertFalse(parking.slide(red, 3));
        final Parking.Vehicle truck = parking.getVehicles().get(1);
        assertEquals(List.of(0, 2, 3), parking.reachable(truck));
        assertTrue(parking.slide(truck, 3));
        assertTrue(parking.slide(red, 4));
        assertTrue(parking.isSolved());
        parking.restart();
        assertEquals(0, red.position);
        assertEquals(1, truck.position);
        // The box lit for a position: the last box the vehicle enters
        assertArrayEquals(new int[]{5, 4}, Parking.markOf(truck, 3));
        assertArrayEquals(new int[]{0, 4}, Parking.markOf(truck, 0));
    }

    @Test
    void shouldWaitForTheDelayOfTheSettings() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(2000);
        final RushHour game = launched(RushHourLevel.EASY, RushHourEnd.ENDLESS, 3);
        final Parking.Vehicle red = game.getParking().red();
        fx(() -> {
            game.lookAt(game.carOf(red));
            steps(game, 1.5);
            return null;
        });
        assertEquals(RushHour.Phase.WAIT, game.getPhase());
        assertNull(game.getSelected());
        verify(stats, never()).notifyNewRoundReady();
        fx(() -> {
            steps(game, 0.6);
            return null;
        });
        assertEquals(RushHour.Phase.PLAY, game.getPhase());
        verify(stats).notifyNewRoundReady();
        verify(stats).incrementNumberOfGoalsToReach();
    }

    @Test
    void shouldChooseAVehicleAfterAFixationOfTheLengthOfTheSettingsReadAtEachStep() throws Exception {
        final RushHour game = playing(RushHourLevel.NORMAL, RushHourEnd.ENDLESS, 5);
        final Parking.Vehicle red = game.getParking().red();
        fx(() -> {
            game.lookAt(game.carOf(red));
            steps(game, 0.5);
            return null;
        });
        assertNull(game.getSelected());
        assertEquals(0.5, game.getProgress(), 0.03);
        when(configuration.getFixationLength()).thenReturn(600);
        fx(() -> {
            steps(game, 0.25);
            return null;
        });
        assertNull(game.getSelected());
        fx(() -> {
            steps(game, 0.07);
            return null;
        });
        assertSame(red, game.getSelected());
    }

    @Test
    void shouldLightTheFreeBoxesOfTheChosenVehicleAndSlideItToTheOneFixated() throws Exception {
        final RushHour game = playing(RushHourLevel.HARD, RushHourEnd.ENDLESS, 7);
        Parking.Vehicle movable = null;
        for (final Parking.Vehicle vehicle : game.getParking().getVehicles()) {
            if (!game.getParking().reachable(vehicle).isEmpty()) {
                movable = vehicle;
                break;
            }
        }
        assertNotNull(movable);
        final Parking.Vehicle vehicle = movable;
        final List<Integer> reachable = game.getParking().reachable(vehicle);
        final int target = reachable.get(reachable.size() - 1);
        fx(() -> {
            game.choose(vehicle);
            return null;
        });
        for (final int position : reachable) {
            assertNotNull(game.markOf(position));
        }
        assertNull(game.markOf(vehicle.position));
        fx(() -> {
            game.lookAt(game.markOf(target));
            steps(game, 0.9);
            return null;
        });
        assertNotEquals(target, vehicle.position);
        fx(() -> {
            steps(game, 0.15);
            return null;
        });
        assertEquals(target, vehicle.position);
        assertEquals(1, game.getMoves());
        assertSame(vehicle, game.getSelected());
    }

    @Test
    void shouldNotKeepAFixationLookedAway() throws Exception {
        final RushHour game = playing(RushHourLevel.EASY, RushHourEnd.ENDLESS, 9);
        final Object red = game.carOf(game.getParking().red());
        fx(() -> {
            game.lookAt(red);
            steps(game, 0.7);
            game.lookAway(red);
            steps(game, 0.2);
            game.lookAt(red);
            steps(game, 0.7);
            return null;
        });
        assertNull(game.getSelected());
    }

    @Test
    void shouldPutTheParkingBackAsItWasWithTheRestartButton() throws Exception {
        final RushHour game = playing(RushHourLevel.NORMAL, RushHourEnd.ENDLESS, 11);
        final String start = game.getParking().boxes();
        final int[] first = solution(start).get(0);
        fx(() -> {
            game.choose(game.getParking().getVehicles().get(first[0]));
            game.slide(first[1]);
            return null;
        });
        assertNotEquals(start, game.getParking().boxes());
        fx(() -> {
            game.lookAt(game.getRestartButton());
            steps(game, 1.05);
            return null;
        });
        assertEquals(start, game.getParking().boxes());
        assertNull(game.getSelected());
        assertEquals(0, game.getMoves());
    }

    @Test
    void shouldLetTheRedCarOutCountTheParkingThenGiveANewOne() throws Exception {
        final RushHour game = playing(RushHourLevel.EASY, RushHourEnd.ENDLESS, 13);
        final String first = game.getParking().boxes();
        solve(game);
        assertEquals(RushHour.Phase.LEAVE, game.getPhase());
        assertEquals(1, game.getScore());
        verify(stats).incrementNumberOfGoalsReached();
        fx(() -> {
            steps(game, RushHour.LEAVE + RushHour.JOY + 0.1);
            return null;
        });
        assertEquals(RushHour.Phase.PLAY, game.getPhase());
        assertNotEquals(first, game.getParking().boxes());
        verify(stats, times(2)).incrementNumberOfGoalsToReach();
        verify(gameContext, never()).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldEndWithBravoAfterTheLastParking() throws Exception {
        final RushHour game = playing(RushHourLevel.EASY, RushHourEnd.SOLVED_1, 15);
        solve(game);
        fx(() -> {
            steps(game, RushHour.LEAVE + RushHour.JOY + 0.1);
            return null;
        });
        assertEquals(RushHour.Phase.OVER, game.getPhase());
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldGoOnWithoutEnd() throws Exception {
        final RushHour game = playing(RushHourLevel.EASY, RushHourEnd.ENDLESS, 17);
        for (int round = 0; round < 3; round++) {
            solve(game);
            fx(() -> {
                steps(game, RushHour.LEAVE + RushHour.JOY + 0.1);
                return null;
            });
        }
        assertEquals(3, game.getScore());
        verify(gameContext, never()).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldPutTheParkingInTheMiddleAndItsButtonAtItsLeftInsideTheScreen() throws Exception {
        for (final double[] screen : new double[][]{{1920, 1080}, {1366, 768}, {1024, 768}, {800, 1000}}) {
            screen(screen[0], screen[1]);
            final RushHour game = launched(RushHourLevel.NORMAL, RushHourEnd.ENDLESS, 1);
            final double[] layout = game.layout();
            final String name = screen[0] + " x " + screen[1];
            assertEquals(layout[0], screen[0] - layout[2], 1, name);
            assertTrue(layout[1] >= screen[1] * 0.11 && layout[3] < screen[1], name);
            // The button inside the screen, left of the parking and its walls
            assertTrue(layout[4] - layout[6] / 2 > 0, name);
            assertTrue(layout[4] + layout[6] / 2 < layout[0] - game.getCell() * 0.2, name);
            // The arrow of the exit inside the screen
            assertTrue(layout[2] + game.getCell() * 0.95 < screen[0], name);
            fx(() -> {
                game.dispose();
                return null;
            });
        }
    }

    /**
     * Maintainer's request after his trial of 9/11/2026: the car to take out and the boxes to reach are marked.
     */
    @Test
    void shouldMarkTheRedCarAndTheBoxesToReach() throws Exception {
        final RushHour game = launched(RushHourLevel.EASY, RushHourEnd.ENDLESS, 4);
        for (final Parking.Vehicle vehicle : game.getParking().getVehicles()) {
            assertEquals(vehicle.name == Parking.RED, game.hasStar(game.carOf(vehicle)), String.valueOf(vehicle.name));
        }
        final double cell = game.getCell();
        final double[] board = game.layout();
        final double[] target = game.targetBounds();
        // The two last boxes of the exit line, where the red car ends
        assertEquals(board[2] - 2 * cell, target[0], cell * 0.1);
        assertEquals(board[2], target[2], cell * 0.1);
        assertEquals(board[1] + Parking.EXIT_LINE * cell, target[1], cell * 0.1);
        assertEquals(board[1] + (Parking.EXIT_LINE + 1) * cell, target[3], cell * 0.1);
    }

    @Test
    void shouldReplayWithTheSameParking() throws Exception {
        final String boxes = playing(RushHourLevel.HARD, RushHourEnd.ENDLESS, 21).getParking().boxes();
        assertEquals(boxes, playing(RushHourLevel.HARD, RushHourEnd.ENDLESS, 21).getParking().boxes());
    }

    @Test
    void shouldStopAfterDispose() throws Exception {
        final RushHour game = playing(RushHourLevel.NORMAL, RushHourEnd.ENDLESS, 2);
        fx(() -> {
            game.choose(game.getParking().red());
            game.dispose();
            steps(game, 2);
            return null;
        });
        assertEquals(RushHour.Phase.OVER, game.getPhase());
        assertTrue(gameArea.getChildren().isEmpty());
        verify(gameContext.getGazeDeviceManager(), atLeast(game.getParking().getVehicles().size() + 1))
            .removeEventFilter(any());
    }

    @Test
    void shouldGiveAllTheVariants() {
        final Set<IGameVariant> variants = new RushHourGameVariant().getVariants();
        assertEquals(12, variants.size());
        final IGameVariant variant = new GroupedGameVariant(RushHourGameVariant.GROUP, RushHourLevel.HARD.getKey(),
            RushHourEnd.SOLVED_3.getKey(), null);
        assertTrue(variants.contains(variant));
        assertEquals(RushHourLevel.HARD, RushHourGameLauncher.levelOf(variant));
        assertEquals(RushHourEnd.SOLVED_3, RushHourGameLauncher.endOf(variant));
    }

    @Test
    void shouldPlayTheOldVariantsByThirdsWithoutEnd() {
        assertEquals(RushHourLevel.EASY, RushHourGameLauncher.levelOf(IGameVariant.toGameVariant("IntGameVariant:1")));
        assertEquals(RushHourLevel.EASY, RushHourGameLauncher.levelOf(IGameVariant.toGameVariant("IntGameVariant:11")));
        assertEquals(RushHourLevel.NORMAL, RushHourGameLauncher.levelOf(IGameVariant.toGameVariant("IntGameVariant:12")));
        assertEquals(RushHourLevel.HARD, RushHourGameLauncher.levelOf(IGameVariant.toGameVariant("IntGameVariant:33")));
        assertEquals(RushHourEnd.ENDLESS, RushHourGameLauncher.endOf(IGameVariant.toGameVariant("IntGameVariant:5")));
    }
}

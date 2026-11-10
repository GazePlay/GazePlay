package net.gazeplay.games.trainSwitches;

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

import java.util.ArrayList;
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
 * "Aiguillages" (Train Switches; reviewed on 10/11/2026, maintainer's rules): the circuits of the file, the rails with
 * rounded corners, the trains taking the way of the switches, the switches changed by a fixation of the length of the
 * settings, the three rhythms, the circuits changed every 5 trains, the ends, the layout, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class TrainSwitchesTest {

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

    private TrainSwitches launched(final TrainSwitchesLevel level, final TrainSwitchesRhythm rhythm,
                                   final TrainSwitchesEnd end, final double seed) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final TrainSwitches game = new TrainSwitches(gameContext, stats, level, rhythm, end, seed);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private TrainSwitches playing(final TrainSwitchesLevel level, final TrainSwitchesRhythm rhythm,
                                  final TrainSwitchesEnd end, final double seed) throws Exception {
        final TrainSwitches game = launched(level, rhythm, end, seed);
        steps(game, 0.05);
        assertEquals(TrainSwitches.Phase.PLAY, game.getPhase());
        return game;
    }

    private static void steps(final TrainSwitches game, final double seconds) throws Exception {
        fx(() -> {
            for (double t = 0; t < seconds; t += STEP) {
                game.step(STEP);
            }
            return null;
        });
    }

    /**
     * @return the ways to give to the switches (switch, index of its stretch out) to go from the tunnel to a station
     */
    private static Map<String, Integer> waysTo(final Circuit circuit, final int station) {
        final Map<String, Integer> ways = search(circuit, circuit.getFirst(), station);
        assertNotNull(ways, "No way to the station " + station);
        return ways;
    }

    private static Map<String, Integer> search(final Circuit circuit, final Circuit.Stretch stretch,
                                               final int station) {
        if (stretch.toJunction() == null) {
            return stretch.toStation() == station ? new HashMap<>() : null;
        }
        final Circuit.Junction junction = circuit.getJunctions().get(stretch.toJunction());
        for (int way = 0; way < 2; way++) {
            final Map<String, Integer> ways = search(circuit, junction.getOuts().get(way), station);
            if (ways != null) {
                ways.put(junction.getName(), way);
                return ways;
            }
        }
        return null;
    }

    /**
     * @return the station of a color in the circuit of the game
     */
    private static int stationOf(final TrainSwitches game, final TrainColor color) {
        for (int station = 0; station < game.getCircuit().getStations().size(); station++) {
            if (game.colorOf(station) == color) {
                return station;
            }
        }
        throw new AssertionError("No station " + color);
    }

    /**
     * Sets the switches (directly) on the way to a station.
     */
    private static void setWays(final TrainSwitches game, final int station) throws Exception {
        fx(() -> {
            waysTo(game.getCircuit(), station).forEach((junction, way) -> {
                if (game.wayOf(junction) != way) {
                    game.change(junction);
                }
            });
            return null;
        });
    }

    /**
     * Steps until a train is out, then sets the switches on the way to the right station (or to a wrong one), and steps
     * until it arrives.
     */
    private static void sendOne(final TrainSwitches game, final boolean toTheRightStation) throws Exception {
        final int arrived = game.getArrived();
        for (int i = 0; i < 1200 && game.getTrains().isEmpty(); i++) {
            steps(game, STEP);
        }
        final TrainSwitches.Train train = game.getTrains().get(0);
        int station = stationOf(game, train.getColor());
        if (!toTheRightStation) {
            station = (station + 1) % game.getCircuit().getStations().size();
        }
        setWays(game, station);
        for (int i = 0; i < 6000 && game.getArrived() == arrived; i++) {
            steps(game, STEP);
        }
        assertEquals(arrived + 1, game.getArrived());
    }

    @Test
    void shouldGiveThreeGoodCircuitsOfEachLevel() {
        final Map<String, List<Circuit>> all = Circuit.all();
        for (final TrainSwitchesLevel level : TrainSwitchesLevel.values()) {
            final List<Circuit> circuits = all.get(level.getFileName());
            assertEquals(3, circuits.size(), level.name());
            for (final Circuit circuit : circuits) {
                final String name = level + " " + circuits.indexOf(circuit);
                assertEquals(level.getStations(), circuit.getStations().size(), name);
                assertEquals(level.getStations() - 1, circuit.getJunctions().size(), name);
                assertNotNull(circuit.getFirst(), name);
                // Each switch has two stretches out, in two other directions than the one it is entered by
                for (final Circuit.Junction junction : circuit.getJunctions().values()) {
                    assertEquals(2, junction.getOuts().size(), name + " " + junction.getName());
                    final int[] in = Circuit.endDirection(circuit.arrivingAt(junction.getName()));
                    final int[] a = Circuit.startDirection(junction.getOuts().get(0));
                    final int[] b = Circuit.startDirection(junction.getOuts().get(1));
                    assertFalse(a[0] == b[0] && a[1] == b[1], name + " " + junction.getName());
                    for (final int[] out : new int[][]{a, b}) {
                        assertFalse(out[0] == -in[0] && out[1] == -in[1], name + " " + junction.getName());
                    }
                }
                // Each station is reached by one way
                for (int station = 0; station < circuit.getStations().size(); station++) {
                    assertNotNull(circuit.getStations().get(station), name + " " + station);
                    waysTo(circuit, station);
                }
                checkRails(circuit, name);
            }
        }
    }

    /**
     * The rails go in straight lines inside the grid, never twice between two boxes, and never cross: a box has rails
     * to 1 neighbor (the tunnel, a station), 3 (a switch) or 2 (any other box).
     */
    private static void checkRails(final Circuit circuit, final String name) {
        final Set<String> links = new HashSet<>();
        final Map<String, Integer> degrees = new HashMap<>();
        for (final Circuit.Stretch stretch : circuit.getStretches()) {
            final List<int[]> boxes = stretch.boxes();
            for (int i = 1; i < boxes.size(); i++) {
                final int[] from = boxes.get(i - 1);
                final int[] to = boxes.get(i);
                assertTrue(from[0] == to[0] ^ from[1] == to[1], name + ": not a straight line");
                final int dx = Integer.signum(to[0] - from[0]);
                final int dy = Integer.signum(to[1] - from[1]);
                int x = from[0];
                int y = from[1];
                while (x != to[0] || y != to[1]) {
                    assertTrue(x >= 0 && y >= 0 && x < circuit.getColumns() && y < circuit.getLines(), name);
                    final String a = x + "," + y;
                    final String b = (x + dx) + "," + (y + dy);
                    assertTrue(links.add(a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a), name + ": twice " + a);
                    degrees.merge(a, 1, Integer::sum);
                    degrees.merge(b, 1, Integer::sum);
                    x += dx;
                    y += dy;
                }
                assertTrue(to[0] < circuit.getColumns() && to[1] < circuit.getLines(), name);
            }
        }
        final Set<String> junctions = new HashSet<>();
        circuit.getJunctions().values().forEach(junction -> junctions.add(junction.getBox()[0] + ","
            + junction.getBox()[1]));
        final Set<String> ends = new HashSet<>();
        circuit.getStations().forEach(box -> ends.add(box[0] + "," + box[1]));
        ends.add(circuit.getTunnel()[0] + "," + circuit.getTunnel()[1]);
        degrees.forEach((box, degree) -> {
            final int expected = junctions.contains(box) ? 3 : ends.contains(box) ? 1 : 2;
            assertEquals(expected, degree, name + " " + box);
        });
    }

    @Test
    void shouldRefuseABadCircuit() {
        assertThrows(IllegalArgumentException.class, () -> Circuit.read(List.of("T 1,0 0")));
        assertThrows(IllegalArgumentException.class, () -> Circuit.read(List.of("circuit easy 3 3 0,0", "A 1,0 0")));
        assertThrows(IllegalArgumentException.class, () -> Circuit.read(List.of("circuit easy 3 3 0,0", "T 0")));
    }

    @Test
    void shouldRoundTheCornersOfTheRails() {
        final Route route = new Route(List.of(new int[]{0, 0}, new int[]{2, 0}, new int[]{2, 2}));
        // Two straight lines shortened by the corner, and a quarter of a circle
        assertEquals(4 - 2 * Route.CORNER + Math.PI / 2 * Route.CORNER, route.length(), 1e-9);
        assertArrayEquals(new double[]{1, 0, 0}, route.at(1), 1e-9);
        final double[] middle = route.at(2 - Route.CORNER + Math.PI / 4 * Route.CORNER);
        assertEquals(45, middle[2], 1e-6);
        assertTrue(middle[0] < 2 && middle[1] > 0);
        final double[] last = route.at(route.length());
        assertArrayEquals(new double[]{2, 2, 90}, last, 1e-9);
        // Before the start, straight backwards
        assertArrayEquals(new double[]{-1, 0, 0}, route.at(-1), 1e-9);
        // A stretch added at the end: the corner of the last box is rounded
        route.append(List.of(new int[]{2, 2}, new int[]{0, 2}));
        assertEquals(6 - 4 * Route.CORNER + Math.PI * Route.CORNER, route.length(), 1e-9);
        assertEquals(180, Math.abs(route.at(route.length())[2]), 1e-9);
    }

    @Test
    void shouldWaitForTheDelayOfTheSettings() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(2000);
        final TrainSwitches game = launched(TrainSwitchesLevel.EASY, TrainSwitchesRhythm.CONTINUOUS,
            TrainSwitchesEnd.ENDLESS, 3);
        final String junction = game.getCircuit().getJunctions().keySet().iterator().next();
        fx(() -> {
            game.lookAt(game.switchOf(junction));
            return null;
        });
        steps(game, 1.5);
        assertEquals(TrainSwitches.Phase.WAIT, game.getPhase());
        assertEquals(0, game.wayOf(junction));
        assertTrue(game.getTrains().isEmpty());
        verify(stats, never()).notifyNewRoundReady();
        steps(game, 0.6);
        assertEquals(TrainSwitches.Phase.PLAY, game.getPhase());
        verify(stats).notifyNewRoundReady();
    }

    @Test
    void shouldGoOneBoxAtMostDuringAFixationAndAReactionAndGiveTwoFixationsToGetReady() {
        // The fixation of the settings by default (2 s): 0.4 box per second, 5 seconds to get ready
        assertEquals(0.4, TrainSwitches.speedFor(2000), 1e-9);
        assertEquals(5, TrainSwitches.readyFor(2000), 1e-9);
        assertEquals(1 / 1.5, TrainSwitches.speedFor(1000), 1e-9);
        assertEquals(3, TrainSwitches.readyFor(1000), 1e-9);
        // Short fixations: never faster than the fastest trains
        assertEquals(TrainSwitches.SPEED, TrainSwitches.speedFor(300), 1e-9);
        assertEquals(TrainSwitches.SPEED, TrainSwitches.speedFor(0), 1e-9);
        assertEquals(1 / 5.5, TrainSwitches.speedFor(5000), 1e-9);
    }

    @Test
    void shouldSendTheFirstTrainOutOfTheTunnelWhenTheTimeToGetReadyIsOver() throws Exception {
        final TrainSwitches game = playing(TrainSwitchesLevel.NORMAL, TrainSwitchesRhythm.CONTINUOUS,
            TrainSwitchesEnd.ENDLESS, 5);
        steps(game, TrainSwitches.readyFor(1000) - 0.1);
        assertTrue(game.getTrains().isEmpty());
        steps(game, 0.15);
        assertEquals(1, game.getTrains().size());
        final TrainSwitches.Train train = game.getTrains().get(0);
        // Hidden in the tunnel at the start, then out
        assertFalse(train.isShown());
        verify(gameContext.getSoundManager()).add("data/trainSwitches/sounds/whistle.wav");
        steps(game, 1.5);
        assertTrue(train.isShown());
        final int[] tunnel = game.getCircuit().getTunnel();
        final double[] board = game.layout();
        final double cell = game.getCell();
        final double[] center = train.center();
        final int[] direction = Circuit.startDirection(game.getCircuit().getFirst());
        // At the speed of the settings, along the rails out of the tunnel
        final double expected = train.getPosition();
        assertEquals(TrainSwitches.START + 1.55 * TrainSwitches.speedFor(1000), expected, 0.06);
        assertEquals(board[0] + (tunnel[0] + 0.5 + direction[0] * expected) * cell, center[0], cell * 0.01);
        assertEquals(board[1] + (tunnel[1] + 0.5 + direction[1] * expected) * cell, center[1], cell * 0.01);
    }

    @Test
    void shouldCountATrainAtTheRightStationAndAMistakeAtAWrongOne() throws Exception {
        final TrainSwitches game = playing(TrainSwitchesLevel.HARD, TrainSwitchesRhythm.ONE_AT_A_TIME,
            TrainSwitchesEnd.ENDLESS, 7);
        sendOne(game, true);
        assertEquals(1, game.getRight());
        verify(stats).incrementNumberOfGoalsToReach();
        verify(stats).incrementNumberOfGoalsReached();
        verify(gameContext.getSoundManager()).add("data/trainSwitches/sounds/correct.wav");
        sendOne(game, false);
        assertEquals(1, game.getRight());
        assertEquals(2, game.getArrived());
        verify(stats, times(2)).incrementNumberOfGoalsToReach();
        verify(stats).incrementNumberOfMistakes();
        verify(gameContext.getSoundManager()).add("data/trainSwitches/sounds/wrong.wav");
    }

    @Test
    void shouldChangeASwitchAfterAFixationOfTheLengthOfTheSettingsReadAtEachStep() throws Exception {
        final TrainSwitches game = playing(TrainSwitchesLevel.NORMAL, TrainSwitchesRhythm.ONE_AT_A_TIME,
            TrainSwitchesEnd.ENDLESS, 9);
        final String junction = game.getCircuit().getJunctions().keySet().iterator().next();
        fx(() -> {
            game.lookAt(game.switchOf(junction));
            return null;
        });
        steps(game, 0.5);
        assertEquals(0, game.wayOf(junction));
        assertEquals(0.5, game.getProgress(), 0.03);
        // Half of a fixation of 600 ms is left: 0.3 second
        when(configuration.getFixationLength()).thenReturn(600);
        steps(game, 0.25);
        assertEquals(0, game.wayOf(junction));
        steps(game, 0.07);
        assertEquals(1, game.wayOf(junction));
        verify(gameContext.getSoundManager()).add("data/trainSwitches/sounds/click.wav");
        // Looked at again: the way changes back
        fx(() -> {
            game.lookAway(game.switchOf(junction));
            game.lookAt(game.switchOf(junction));
            return null;
        });
        steps(game, 0.62);
        assertEquals(0, game.wayOf(junction));
    }

    @Test
    void shouldNotKeepAFixationLookedAway() throws Exception {
        final TrainSwitches game = playing(TrainSwitchesLevel.EASY, TrainSwitchesRhythm.ONE_AT_A_TIME,
            TrainSwitchesEnd.ENDLESS, 11);
        final Object view = game.switchOf(game.getCircuit().getJunctions().keySet().iterator().next());
        fx(() -> {
            game.lookAt(view);
            return null;
        });
        steps(game, 0.7);
        fx(() -> {
            game.lookAway(view);
            return null;
        });
        steps(game, 0.2);
        fx(() -> {
            game.lookAt(view);
            return null;
        });
        steps(game, 0.7);
        assertEquals(0, game.getProgress() > 0.9 ? 1 : 0);
        assertEquals(0, game.wayOf(game.getCircuit().getJunctions().keySet().iterator().next()));
    }

    @Test
    void shouldTakeTheWayOfEachSwitchToEveryStation() throws Exception {
        for (final TrainSwitchesLevel level : TrainSwitchesLevel.values()) {
            for (int seed = 1; seed <= 6; seed++) {
                final TrainSwitches game = playing(level, TrainSwitchesRhythm.ONE_AT_A_TIME,
                    TrainSwitchesEnd.ENDLESS, seed);
                for (int i = 0; i < 2; i++) {
                    final int right = game.getRight();
                    sendOne(game, true);
                    assertEquals(right + 1, game.getRight(), level + " " + seed);
                }
                fx(() -> {
                    game.dispose();
                    return null;
                });
            }
        }
    }

    @Test
    void shouldSendTheNextTrainWhenTheLastOneHasArrivedOneAtATime() throws Exception {
        final TrainSwitches game = playing(TrainSwitchesLevel.EASY, TrainSwitchesRhythm.ONE_AT_A_TIME,
            TrainSwitchesEnd.ENDLESS, 13);
        steps(game, TrainSwitches.readyFor(1000) + 0.05);
        assertEquals(1, game.getSent());
        steps(game, 3);
        assertEquals(1, game.getSent());
        sendOne(game, true);
        assertTrue(game.getTrains().isEmpty());
        steps(game, TrainSwitches.readyFor(1000) - 0.1);
        assertEquals(1, game.getSent());
        steps(game, 0.15);
        assertEquals(2, game.getSent());
    }

    @Test
    void shouldKeepTheTrainsApartByTheSameBoxesAtAnySpeed() throws Exception {
        final TrainSwitches game = playing(TrainSwitchesLevel.HARD, TrainSwitchesRhythm.CONTINUOUS,
            TrainSwitchesEnd.ENDLESS, 15);
        final double interval = TrainSwitches.INTERVAL / TrainSwitches.speedFor(1000);
        steps(game, TrainSwitches.readyFor(1000) + 0.05);
        assertEquals(1, game.getSent());
        steps(game, interval - 0.1);
        assertEquals(1, game.getSent());
        steps(game, 0.1);
        assertEquals(2, game.getSent());
        assertEquals(TrainSwitches.START + TrainSwitches.INTERVAL, game.getTrains().get(0).getPosition(), 0.15);
        // Twice the speed of the settings: twice as far in a second, the next train twice as soon
        final double position = game.getTrains().get(0).getPosition();
        when(configuration.getAnimationSpeedRatio()).thenReturn(2.0);
        steps(game, 1);
        assertEquals(position + 2 * TrainSwitches.speedFor(1000), game.getTrains().get(0).getPosition(), 0.05);
        int steps = 0;
        while (game.getSent() < 3 && steps < 2000) {
            steps(game, STEP);
            steps++;
        }
        assertEquals((interval - 2) / 2, steps * STEP, 0.1);
    }

    @Test
    void shouldFollowAFixationLengthChangedDuringTheGame() throws Exception {
        final TrainSwitches game = playing(TrainSwitchesLevel.HARD, TrainSwitchesRhythm.CONTINUOUS,
            TrainSwitchesEnd.ENDLESS, 27);
        steps(game, TrainSwitches.readyFor(1000) + 0.05);
        final TrainSwitches.Train train = game.getTrains().get(0);
        double position = train.getPosition();
        steps(game, 1);
        assertEquals(position + TrainSwitches.speedFor(1000), train.getPosition(), 0.02);
        // Changed in the menu of the game: the trains go slower at once, and the next one waits longer
        final double before = game.timeToNext();
        when(configuration.getFixationLength()).thenReturn(3000);
        position = train.getPosition();
        steps(game, 1);
        assertEquals(position + TrainSwitches.speedFor(3000), train.getPosition(), 0.02);
        assertTrue(game.timeToNext() > before);
        // Shorter: faster at once
        when(configuration.getFixationLength()).thenReturn(400);
        position = train.getPosition();
        steps(game, 1);
        assertEquals(position + TrainSwitches.speedFor(400), train.getPosition(), 0.02);
    }

    @Test
    void shouldShowTheNextTrainOnTheTunnelWithTheTimeLeft() throws Exception {
        final TrainSwitches game = playing(TrainSwitchesLevel.NORMAL, TrainSwitchesRhythm.ONE_AT_A_TIME,
            TrainSwitchesEnd.TRAINS_5, 29);
        assertTrue(game.isPreviewShown());
        final TrainColor next = game.getNextColor();
        assertNotNull(next);
        assertEquals("3", game.previewSeconds());
        steps(game, 1.2);
        assertEquals("2", game.previewSeconds());
        steps(game, TrainSwitches.readyFor(1000) - 1.2);
        // The train shown leaves; the next one is shown, pale and without time until this one has arrived
        assertEquals(1, game.getTrains().size());
        assertSame(next, game.getTrains().get(0).getColor());
        assertNotSame(next, game.getNextColor());
        assertTrue(game.isPreviewShown());
        assertEquals("", game.previewSeconds());
        final TrainColor second = game.getNextColor();
        sendOne(game, true);
        steps(game, STEP);
        assertEquals("3", game.previewSeconds());
        steps(game, TrainSwitches.readyFor(1000) + 0.05);
        assertSame(second, game.getTrains().get(0).getColor());
        // After the last train of the game, nothing is shown
        for (int i = 0; i < 3; i++) {
            sendOne(game, true);
        }
        for (int i = 0; i < 1200 && game.getSent() < 5; i++) {
            steps(game, STEP);
        }
        assertEquals(5, game.getSent());
        steps(game, STEP);
        assertFalse(game.isPreviewShown());
    }

    @Test
    void shouldStopEverythingBeforeEachSwitchUntilResumeIsFixated() throws Exception {
        final TrainSwitches game = playing(TrainSwitchesLevel.NORMAL, TrainSwitchesRhythm.PAUSE,
            TrainSwitchesEnd.ENDLESS, 17);
        assertFalse(game.isResumeShown());
        for (int i = 0; i < 1200 && game.getPaused() == null; i++) {
            steps(game, STEP);
        }
        final TrainSwitches.Train train = game.getPaused();
        assertNotNull(train);
        assertTrue(game.isResumeShown());
        final String junction = train.getToJunction();
        final double position = train.getPosition();
        steps(game, 6);
        // Nothing moves, no other train comes, but the switch can be changed
        assertEquals(position, train.getPosition(), 1e-9);
        assertEquals(1, game.getSent());
        fx(() -> {
            game.lookAt(game.switchOf(junction));
            return null;
        });
        steps(game, 1.05);
        assertEquals(1, game.wayOf(junction));
        // The resume button after a fixation
        fx(() -> {
            game.lookAt(game.getResumeButton());
            return null;
        });
        steps(game, 0.9);
        assertNotNull(game.getPaused());
        steps(game, 0.15);
        assertNull(game.getPaused());
        assertFalse(game.isResumeShown());
        // The train goes through the switch, by the way changed, and stops again before the next switch
        final Circuit.Stretch way = game.getCircuit().getJunctions().get(junction).getOuts().get(1);
        for (int i = 0; i < 1200 && game.getPaused() == null && game.getArrived() == 0; i++) {
            steps(game, STEP);
        }
        if (way.toJunction() != null) {
            assertSame(train, game.getPaused());
            assertEquals(way.toJunction(), train.getToJunction());
        } else {
            assertEquals(1, game.getArrived());
        }
    }

    @Test
    void shouldGiveAnotherCircuitEveryFiveTrains() throws Exception {
        final TrainSwitches game = playing(TrainSwitchesLevel.EASY, TrainSwitchesRhythm.ONE_AT_A_TIME,
            TrainSwitchesEnd.ENDLESS, 19);
        final List<Circuit> seen = new ArrayList<>();
        for (int circuit = 0; circuit < 3; circuit++) {
            seen.add(game.getCircuit());
            for (int train = 0; train < TrainSwitches.PER_CIRCUIT; train++) {
                sendOne(game, true);
            }
            assertEquals(TrainSwitches.Phase.CHANGE, game.getPhase());
            steps(game, TrainSwitches.JOY + 0.05);
            assertEquals(TrainSwitches.Phase.PLAY, game.getPhase());
            assertNotSame(seen.get(seen.size() - 1), game.getCircuit());
        }
        verify(stats, times(4)).notifyNewRoundReady();
        verify(gameContext, never()).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldEndWithBravoAfterTheLastTrain() throws Exception {
        final TrainSwitches game = playing(TrainSwitchesLevel.EASY, TrainSwitchesRhythm.CONTINUOUS,
            TrainSwitchesEnd.TRAINS_5, 21);
        for (int i = 0; i < 5; i++) {
            sendOne(game, i % 2 == 0);
        }
        assertEquals(5, game.getSent());
        assertEquals(TrainSwitches.Phase.CHANGE, game.getPhase());
        steps(game, TrainSwitches.JOY + 0.05);
        assertEquals(TrainSwitches.Phase.OVER, game.getPhase());
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldGiveStationsOfDifferentColorsAndTrainsOfTheirColorsNeverTwiceTheSame() throws Exception {
        final TrainSwitches game = playing(TrainSwitchesLevel.HARD, TrainSwitchesRhythm.CONTINUOUS,
            TrainSwitchesEnd.ENDLESS, 23);
        final Set<TrainColor> colors = new HashSet<>();
        for (int station = 0; station < 8; station++) {
            colors.add(game.colorOf(station));
        }
        assertEquals(8, colors.size());
        // The color of each train at its start (the first ones may have arrived when the last ones leave)
        final List<TrainColor> sent = new ArrayList<>();
        for (int i = 0; i < 3000 && sent.size() < 5; i++) {
            steps(game, STEP);
            if (game.getSent() > sent.size()) {
                sent.add(game.getTrains().get(game.getTrains().size() - 1).getColor());
            }
        }
        assertEquals(5, sent.size());
        for (int i = 0; i < sent.size(); i++) {
            assertTrue(colors.contains(sent.get(i)));
            if (i > 0) {
                assertNotSame(sent.get(i - 1), sent.get(i));
            }
        }
    }

    @Test
    void shouldPutTheCircuitInTheMiddleAndTheResumeButtonAtItsRightInsideTheScreen() throws Exception {
        for (final double[] screen : new double[][]{{1920, 1080}, {1366, 768}, {1024, 768}, {800, 1000}}) {
            screen(screen[0], screen[1]);
            for (final TrainSwitchesLevel level : TrainSwitchesLevel.values()) {
                // Seeds far apart, until the 3 circuits of the level are seen
                final Set<Circuit> circuits = new HashSet<>();
                for (int seed = 1; seed <= 60 && circuits.size() < 3; seed++) {
                    final TrainSwitches game = launched(level, TrainSwitchesRhythm.PAUSE, TrainSwitchesEnd.ENDLESS,
                        seed * 7919.37);
                    final double[] layout = game.layout();
                    final String name = screen[0] + " x " + screen[1] + " " + level;
                    assertEquals(layout[0], screen[0] - layout[2], 1, name);
                    assertTrue(layout[1] >= screen[1] * 0.11 && layout[3] < screen[1], name);
                    assertTrue(layout[4] - layout[6] / 2 > layout[2], name);
                    assertTrue(layout[4] + layout[6] / 2 < screen[0], name);
                    // The switches are large enough for the gaze
                    assertTrue(game.getCell() >= Math.min(screen[0], screen[1]) / 11, name);
                    // The next train on the hill of the tunnel, inside the screen, under the band of the score
                    final double[] preview = game.previewCircle();
                    assertTrue(preview[0] - preview[2] > 0 && preview[0] + preview[2] < screen[0], name);
                    assertTrue(preview[1] - preview[2] > screen[1] * 0.08 && preview[1] + preview[2] < screen[1],
                        name);
                    // The hill behind the mouth of the tunnel, whatever way the tunnel is turned; the next train on it
                    final int[] tunnel = game.getCircuit().getTunnel();
                    final int[] direction = Circuit.startDirection(game.getCircuit().getFirst());
                    final double cell = game.getCell();
                    final double[] hill = game.hillCenter();
                    assertEquals(layout[0] + (tunnel[0] + 0.5 - 0.42 * direction[0]) * cell, hill[0], 1, name);
                    assertEquals(layout[1] + (tunnel[1] + 0.5 - 0.42 * direction[1]) * cell, hill[1], 1, name);
                    assertTrue(Math.hypot(preview[0] - hill[0], preview[1] - hill[1]) + preview[2] <= cell * 0.55,
                        name);
                    circuits.add(game.getCircuit());
                    fx(() -> {
                        game.dispose();
                        return null;
                    });
                }
                assertEquals(3, circuits.size(), level.name());
            }
        }
    }

    @Test
    void shouldReplayWithTheSameCircuitAndColors() throws Exception {
        final TrainSwitches first = playing(TrainSwitchesLevel.HARD, TrainSwitchesRhythm.CONTINUOUS,
            TrainSwitchesEnd.ENDLESS, 25);
        final TrainSwitches second = playing(TrainSwitchesLevel.HARD, TrainSwitchesRhythm.CONTINUOUS,
            TrainSwitchesEnd.ENDLESS, 25);
        assertSame(first.getCircuit(), second.getCircuit());
        for (int station = 0; station < 8; station++) {
            assertEquals(first.colorOf(station), second.colorOf(station));
        }
    }

    @Test
    void shouldStopAfterDispose() throws Exception {
        final TrainSwitches game = playing(TrainSwitchesLevel.NORMAL, TrainSwitchesRhythm.CONTINUOUS,
            TrainSwitchesEnd.ENDLESS, 2);
        fx(() -> {
            game.dispose();
            return null;
        });
        steps(game, 3);
        assertEquals(TrainSwitches.Phase.OVER, game.getPhase());
        assertTrue(gameArea.getChildren().isEmpty());
        verify(gameContext.getGazeDeviceManager(), atLeast(game.getCircuit().getJunctions().size() + 1))
            .removeEventFilter(any());
    }

    @Test
    void shouldGiveAllTheVariants() {
        final Set<IGameVariant> variants = new TrainSwitchesGameVariantGenerator().getVariants();
        assertEquals(36, variants.size());
        final IGameVariant variant = new GroupedGameVariant(TrainSwitchesGameVariantGenerator.GROUP,
            TrainSwitchesLevel.HARD.getKey(), TrainSwitchesEnd.TRAINS_20.getKey(), null,
            TrainSwitchesRhythm.PAUSE.getKey());
        assertTrue(variants.contains(variant));
        assertEquals(TrainSwitchesLevel.HARD, TrainSwitchesGameLauncher.levelOf(variant));
        assertEquals(TrainSwitchesEnd.TRAINS_20, TrainSwitchesGameLauncher.endOf(variant));
        assertEquals(TrainSwitchesRhythm.PAUSE, TrainSwitchesGameLauncher.rhythmOf(variant));
        assertEquals(variant, IGameVariant.toGameVariant(variant.toString()));
    }

    @Test
    void shouldPlayTheOldVariants() {
        final IGameVariant easy = IGameVariant.toGameVariant("IntStringGameVariant:3:ClassicTrain:12");
        assertEquals(TrainSwitchesLevel.EASY, TrainSwitchesGameLauncher.levelOf(easy));
        assertEquals(TrainSwitchesRhythm.CONTINUOUS, TrainSwitchesGameLauncher.rhythmOf(easy));
        assertEquals(TrainSwitchesEnd.TRAINS_10, TrainSwitchesGameLauncher.endOf(easy));
        final IGameVariant pause = IGameVariant.toGameVariant("IntStringGameVariant:8:PauseTrain:30");
        assertEquals(TrainSwitchesLevel.NORMAL, TrainSwitchesGameLauncher.levelOf(pause));
        assertEquals(TrainSwitchesRhythm.PAUSE, TrainSwitchesGameLauncher.rhythmOf(pause));
        assertEquals(TrainSwitchesEnd.TRAINS_20, TrainSwitchesGameLauncher.endOf(pause));
        final IGameVariant infinite = IGameVariant.toGameVariant("IntStringGameVariant:13:InfiniteTrain:3");
        assertEquals(TrainSwitchesLevel.HARD, TrainSwitchesGameLauncher.levelOf(infinite));
        assertEquals(TrainSwitchesEnd.ENDLESS, TrainSwitchesGameLauncher.endOf(infinite));
        final IGameVariant unique = IGameVariant.toGameVariant("IntStringGameVariant:13:UniqueTrain:0");
        assertEquals(TrainSwitchesRhythm.ONE_AT_A_TIME, TrainSwitchesGameLauncher.rhythmOf(unique));
        assertEquals(TrainSwitchesEnd.TRAINS_10, TrainSwitchesGameLauncher.endOf(unique));
    }
}

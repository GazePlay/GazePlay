package net.gazeplay.games.sprintFinish;

import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.sprintFinish.SprintMap.Cell;
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
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Sprint Finish (reviewed on 2/11/2026, maintainer's rules): maps made at random and playable, the cat with both
 * keyboards or the gaze, caught then the same map again (without the dogs of before), the maps of the difficulty then
 * the end, the buttons opened by the cat alone or by the gaze with two players, the dogs stopped by the gaze, the
 * variants.
 */
@ExtendWith(ApplicationExtension.class)
class SprintFinishTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    @Mock
    Configuration configuration;

    @Mock
    Translator translator;

    @Mock
    Stats stats;

    @Mock
    Scene scene;

    private final Pane gameArea = new Pane();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1366, 768));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(gameContext.getPrimaryScene()).thenReturn(scene);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getFixationLength()).thenReturn(100);
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private SprintFinish launched(final SprintMode mode, final SprintDifficulty difficulty) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final SprintFinish game = new SprintFinish(gameContext, stats, mode, difficulty, 42);
            game.setManual();
            game.launch();
            game.play();
            return game;
        });
    }

    /**
     * The cat put on its bowl: the map is won; the next one starts at once.
     */
    private void winMap(final SprintFinish game) throws Exception {
        fx(() -> {
            final Cell dish = game.getMap().getDish();
            game.placeCat(dish.column() + 0.5, dish.row() + 0.5);
            game.step(0.01);
            assertEquals(SprintFinish.Phase.WON, game.getPhase());
            game.skipPause();
            if (game.getPhase() == SprintFinish.Phase.COUNTDOWN) {
                game.play();
            }
            return null;
        });
    }

    /**
     * Every map of every difficulty is playable, many games long: the cat on the left, its bowl on the right, the
     * models in the order of the game, the number of dogs growing.
     */
    @Test
    void shouldMakePlayableMaps() {
        for (int seed = 1; seed <= 150; seed++) {
            final ReplayablePseudoRandom random = new ReplayablePseudoRandom(seed);
            for (final SprintDifficulty difficulty : SprintDifficulty.values()) {
                final int count = difficulty.getMaps();
                for (int index = 0; index < count; index++) {
                    final SprintMap map = SprintMapGenerator.generate(index, count, random);
                    final SprintMapGenerator.Model model = SprintMapGenerator.modelOf(index, count);
                    assertTrue(map.isPlayable(), "seed " + seed + ", map " + index + " of " + count);
                    assertTrue(map.getCat().column() <= 1, "the cat starts on the left");
                    assertTrue(map.getDish().column() >= 13, "the bowl is on the right");
                    assertEquals(SprintMapGenerator.dogsOf(index, count), map.getDogs().size());
                    assertEquals(model == SprintMapGenerator.Model.GATE
                        || model == SprintMapGenerator.Model.ENCLOSURE, !map.getButtons().isEmpty(), model.name());
                    assertEquals(model == SprintMapGenerator.Model.LOGS, !map.getLogs().isEmpty(), model.name());
                    if (!map.getButtons().isEmpty()) {
                        // The bowl cannot be reached with the fences closed
                        final int[][] closed = map.distancesFrom(map.getCat(), map.allFences());
                        assertEquals(-1, closed[map.getDish().column()][map.getDish().row()], model.name());
                    }
                }
            }
        }
        assertEquals(SprintMapGenerator.Model.MEADOW, SprintMapGenerator.modelOf(0, 5));
        assertEquals(SprintMapGenerator.Model.LOGS, SprintMapGenerator.modelOf(14, 15));
        assertEquals(List.of(1, 1, 1, 2, 2), List.of(0, 1, 2, 3, 4).stream()
            .map(index -> SprintMapGenerator.dogsOf(index, 5)).toList());
        assertEquals(3, SprintMapGenerator.dogsOf(14, 15));
    }

    /**
     * The cat moves with the arrows, Z Q S D (AZERTY) and W A S D (QWERTY), without a setting; the keys are listened
     * to on the scene, and no longer at the end.
     */
    @Test
    void shouldMoveTheCatWithBothKeyboards() throws Exception {
        final SprintFinish game = launched(SprintMode.SOLO_KEYBOARD, SprintDifficulty.EASY);
        for (final KeyCode[] keys : new KeyCode[][]{{KeyCode.D, KeyCode.RIGHT}, {KeyCode.S, KeyCode.DOWN}}) {
            for (final KeyCode key : keys) {
                final double[] before = fx(() -> new double[]{game.getCatX(), game.getCatY()});
                fx(() -> {
                    game.press(key);
                    game.step(0.1);
                    game.release(key);
                    return null;
                });
                final double[] after = fx(() -> new double[]{game.getCatX(), game.getCatY()});
                assertTrue(after[0] > before[0] || after[1] > before[1], key + " moves the cat");
            }
        }
        for (final KeyCode key : new KeyCode[]{KeyCode.Q, KeyCode.A, KeyCode.LEFT, KeyCode.Z, KeyCode.W,
            KeyCode.UP}) {
            final double[] before = fx(() -> new double[]{game.getCatX(), game.getCatY()});
            fx(() -> {
                game.press(key);
                game.step(0.05);
                game.release(key);
                return null;
            });
            final double[] after = fx(() -> new double[]{game.getCatX(), game.getCatY()});
            assertTrue(after[0] < before[0] || after[1] < before[1], key + " moves the cat");
        }
        verify(scene).addEventFilter(eq(KeyEvent.KEY_PRESSED), any());
        fx(() -> {
            game.dispose();
            return null;
        });
        verify(scene).removeEventFilter(eq(KeyEvent.KEY_PRESSED), any());
        assertTrue(gameArea.getChildren().isEmpty());
    }

    /**
     * Alone with the gaze: the cat stays until the gaze is known, then goes where it is.
     */
    @Test
    void shouldFollowTheGazeAlone() throws Exception {
        final SprintFinish game = launched(SprintMode.SOLO_GAZE, SprintDifficulty.EASY);
        final double[] start = fx(() -> {
            game.step(0.2);
            return new double[]{game.getCatX(), game.getCatY()};
        });
        final double[] after = fx(() -> {
            game.gazeAt(start[0], start[1] + 1);
            game.step(0.1);
            return new double[]{game.getCatX(), game.getCatY()};
        });
        assertTrue(after[1] > start[1], "the cat goes down to the gaze");
        verify(scene, never()).addEventFilter(eq(KeyEvent.KEY_PRESSED), any());
    }

    /**
     * Caught by a dog: the same map starts again, with only its own dogs, at their start (the dogs of before used to
     * keep running, unseen).
     */
    @Test
    void shouldStartTheSameMapAgainWhenCaught() throws Exception {
        final SprintFinish game = launched(SprintMode.DUO, SprintDifficulty.NORMAL);
        final SprintMap map = fx(game::getMap);
        for (int time = 0; time < 3; time++) {
            fx(() -> {
                if (game.getPhase() == SprintFinish.Phase.COUNTDOWN) {
                    game.play();
                }
                game.getDogs().getFirst().placeAt(game.getCatX() + 0.3, game.getCatY());
                game.step(0.01);
                assertEquals(SprintFinish.Phase.CAUGHT, game.getPhase());
                game.skipPause();
                return null;
            });
        }
        fx(() -> {
            assertEquals(SprintFinish.Phase.COUNTDOWN, game.getPhase());
            assertEquals(0, game.getIndex());
            assertSame(map, game.getMap());
            assertEquals(map.getDogs().size(), game.getDogs().size());
            assertEquals(map.getDogs().getFirst().column() + 0.5, game.getDogs().getFirst().getX(), 1e-9);
            return null;
        });
        verify(stats, times(1)).incrementNumberOfGoalsToReach();
        verify(stats, never()).incrementNumberOfGoalsReached();
    }

    /**
     * The 5 maps of the easy difficulty, one after the other, then "Bravo" and the statistics.
     */
    @Test
    void shouldEndAfterTheMapsOfTheDifficulty() throws Exception {
        final SprintFinish game = launched(SprintMode.SOLO_KEYBOARD, SprintDifficulty.EASY);
        for (int map = 0; map < 5; map++) {
            assertEquals(map, (int) fx(game::getIndex));
            if (map == 4) {
                verify(gameContext, never()).playWinTransition(anyLong(), any());
            }
            winMap(game);
        }
        verify(stats, times(5)).incrementNumberOfGoalsToReach();
        verify(stats, times(5)).incrementNumberOfGoalsReached();
        verify(gameContext).playWinTransition(anyLong(), any());
        assertEquals(SprintFinish.Phase.OVER, fx(game::getPhase));
    }

    /**
     * Alone, the fences open when the cat walks on their button; with two players, only by the gaze.
     */
    @Test
    void shouldOpenTheFencesByTheCatAloneAndByTheGazeWithTwoPlayers() throws Exception {
        for (final SprintMode mode : new SprintMode[]{SprintMode.SOLO_KEYBOARD, SprintMode.DUO}) {
            final SprintFinish game = launched(mode, SprintDifficulty.EASY);
            // The third map of an easy game: a hedge with a fence and its button
            winMap(game);
            winMap(game);
            final boolean open = fx(() -> {
                assertEquals(SprintMapGenerator.Model.GATE, SprintMapGenerator.modelOf(game.getIndex(), 5));
                final Cell button = game.getMap().getButtons().getFirst().cell();
                game.placeCat(button.column() + 0.5, button.row() + 0.5);
                game.step(0.01);
                return game.getButtons().getFirst().isOpen();
            });
            assertEquals(mode == SprintMode.SOLO_KEYBOARD, open, mode.name());
            if (mode == SprintMode.DUO) {
                fx(() -> {
                    game.getButtons().getFirst().open();
                    return null;
                });
                assertTrue(fx(() -> game.getButtons().getFirst().isOpen()));
            }
            fx(() -> {
                game.dispose();
                return null;
            });
        }
    }

    /**
     * With two players, a dog looked at stops; it runs again when no longer looked at.
     */
    @Test
    void shouldStopADogLookedAtWithTwoPlayers() throws Exception {
        final SprintFinish game = launched(SprintMode.DUO, SprintDifficulty.EASY);
        final List<Double> places = new ArrayList<>();
        fx(() -> {
            final SprintFinish.Dog dog = game.getDogs().getFirst();
            dog.setLooked(true);
            places.add(dog.getX() + dog.getY());
            game.step(0.3);
            places.add(dog.getX() + dog.getY());
            dog.setLooked(false);
            game.step(0.3);
            places.add(dog.getX() + dog.getY());
            return null;
        });
        assertEquals(places.get(0), places.get(1), 1e-9);
        assertNotEquals(places.get(1), places.get(2), 1e-6);
    }

    /**
     * A dog runs straight to the cat, diagonally, when nothing is in the way (maintainer's request of 2/11/2026); round
     * a hedge, it never cuts its corner.
     */
    @Test
    void shouldRunDiagonallyToTheCat() throws Exception {
        final SprintFinish game = launched(SprintMode.DUO, SprintDifficulty.EASY);
        final double[] moves = fx(() -> {
            final SprintFinish.Dog dog = game.getDogs().getFirst();
            for (final double[] offset : new double[][]{{2, 2}, {2, -2}, {3, 1}, {1, 3}, {3, -1}}) {
                final double x = game.getCatX() + offset[0];
                final double y = game.getCatY() + offset[1];
                if (x < SprintMap.COLUMNS - 1 && y > 0.5 && y < SprintMap.ROWS - 0.5
                    && game.clearLine(x, y, game.getCatX(), game.getCatY())) {
                    dog.placeAt(x, y);
                    game.step(0.2);
                    return new double[]{dog.getX() - x, dog.getY() - y, offset[0], offset[1]};
                }
            }
            return null;
        });
        assertNotNull(moves, "a clear line from a dog to the cat");
        assertTrue(moves[0] * moves[2] < 0 && moves[1] * moves[3] < 0, "towards the cat on both axes");

        // A diagonal is taken only between two free cells
        final int[][] distances = new int[SprintMap.COLUMNS][SprintMap.ROWS];
        for (final int[] column : distances) {
            java.util.Arrays.fill(column, 5);
        }
        distances[1][1] = 0;
        distances[2][1] = 1;
        distances[2][2] = 3;
        // A hedge beside the diagonal (1, 1): the dog goes up first
        distances[1][2] = -1;
        assertArrayEquals(new int[]{2, 1}, SprintFinish.nextCell(2, 2, distances));
        distances[1][2] = 1;
        assertArrayEquals(new int[]{1, 1}, SprintFinish.nextCell(2, 2, distances));
    }

    /**
     * 9 variants: easy, normal, hard, for each way of playing; a variant of before plays alone with the gaze.
     */
    @Test
    void shouldOfferTheDifficultiesForEachWayOfPlaying() {
        final List<IGameVariant> variants = new ArrayList<>(new SprintFinishVariantGenerator().getVariants());
        assertEquals(9, variants.size());
        final GroupedGameVariant first = (GroupedGameVariant) variants.getFirst();
        assertEquals("SprintSoloGaze", first.getSubGroup());
        assertEquals("SprintEasy", first.getValue());
        assertEquals(SprintMode.DUO, SprintFinishLauncher.modeOf(variants.get(7)));
        assertEquals(SprintDifficulty.NORMAL, SprintFinishLauncher.difficultyOf(variants.get(7)));
        assertEquals(SprintMode.SOLO_GAZE, SprintFinishLauncher.modeOf(new IntGameVariant(4)));
        assertEquals(SprintDifficulty.NORMAL, SprintFinishLauncher.difficultyOf(new IntGameVariant(4)));
        assertEquals(15, SprintDifficulty.HARD.getMaps());
        assertTrue(SprintMode.SOLO_GAZE.getDogSpeed() < SprintMode.DUO.getDogSpeed() / 2,
            "alone, the dogs are much slower");
    }
}

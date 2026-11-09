package net.gazeplay.games.slidingpuzzle;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
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
 * The sliding puzzle "Jeu de taquin" (reviewed on 9/11/2026, maintainer's rules): every shuffle can be solved, the
 * picture shown whole before the shuffle and the delay, the fixation of the length of the settings on a piece next to
 * the empty place, the puzzles solved and the ends, the layout, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class SlidingPuzzleTest {

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

    private SlidingPuzzle launched(final PuzzleContent content, final PuzzleLevel level, final PuzzleEnd end,
                                   final double seed) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final SlidingPuzzle game = new SlidingPuzzle(gameContext, stats, content, level, end, seed);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private static void steps(final SlidingPuzzle game, final double seconds) {
        for (double t = 0; t < seconds; t += STEP) {
            game.step(STEP);
        }
    }

    /**
     * @return a game ready to be played: the picture shown, shuffled, the delay over
     */
    private SlidingPuzzle playing(final PuzzleContent content, final PuzzleLevel level, final PuzzleEnd end,
                                  final double seed) throws Exception {
        final SlidingPuzzle game = launched(content, level, end, seed);
        fx(() -> {
            steps(game, SlidingPuzzle.SHOW + SlidingPuzzle.SHUFFLE + 0.1);
            return null;
        });
        assertEquals(SlidingPuzzle.Phase.PLAY, game.getPhase());
        return game;
    }

    /**
     * @return the pieces to slide, in order, to solve a board (search in breadth, for the boards a few moves away)
     */
    private static List<Integer> solution(final int[] start, final int size) {
        final int empty = size * size - 1;
        final Map<String, String> parents = new HashMap<>();
        final Map<String, Integer> movedPiece = new HashMap<>();
        final Deque<int[]> queue = new ArrayDeque<>();
        parents.put(Arrays.toString(start), null);
        queue.add(start);
        while (!queue.isEmpty()) {
            final int[] board = queue.poll();
            final String key = Arrays.toString(board);
            if (SlidingPuzzle.isSolved(board)) {
                final List<Integer> pieces = new ArrayList<>();
                for (String at = key; parents.get(at) != null; at = parents.get(at)) {
                    pieces.add(movedPiece.get(at));
                }
                Collections.reverse(pieces);
                return pieces;
            }
            int emptyPlace = 0;
            while (board[emptyPlace] != empty) {
                emptyPlace++;
            }
            for (final int neighbor : SlidingPuzzle.neighbors(size, emptyPlace)) {
                final int[] next = board.clone();
                next[emptyPlace] = board[neighbor];
                next[neighbor] = empty;
                final String nextKey = Arrays.toString(next);
                if (!parents.containsKey(nextKey)) {
                    parents.put(nextKey, key);
                    movedPiece.put(nextKey, board[neighbor]);
                    queue.add(next);
                }
            }
        }
        throw new AssertionError("No solution");
    }

    /**
     * Solves the puzzle with fixations of the length of the settings.
     */
    private void solve(final SlidingPuzzle game) throws Exception {
        for (final int piece : solution(game.getBoard(), game.getLevel().getSize())) {
            fx(() -> {
                assertTrue(game.isMovable(piece));
                game.lookAt(piece);
                steps(game, 1.05);
                return null;
            });
        }
    }

    @Test
    void shouldTellTheBoardsThatCanBeSolved() {
        assertTrue(SlidingPuzzle.isSolvable(SlidingPuzzle.solved(3), 3));
        assertTrue(SlidingPuzzle.isSolvable(SlidingPuzzle.solved(4), 4));
        // Two pieces swapped: never
        assertFalse(SlidingPuzzle.isSolvable(new int[]{1, 0, 2, 3, 4, 5, 6, 7, 8}, 3));
        assertFalse(SlidingPuzzle.isSolvable(new int[]{1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, 4));
        // The empty place moved up in 4 x 4: the line of the empty place counts
        assertTrue(SlidingPuzzle.isSolvable(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 12, 13, 14, 11}, 4));
        // The board of the capture before the review (3 6 5 / 4 8 _ / 7 2 1): 17 pairs in the wrong order
        assertFalse(SlidingPuzzle.isSolvable(new int[]{2, 5, 4, 3, 7, 8, 6, 1, 0}, 3));
    }

    @Test
    void shouldAlwaysShuffleIntoABoardThatCanBeSolvedAndIsNotSolved() {
        for (final PuzzleLevel level : PuzzleLevel.values()) {
            for (int seed = 1; seed <= 300; seed++) {
                final int size = level.getSize();
                final int[] board = SlidingPuzzle.shuffle(size, level.getMoves(), new ReplayablePseudoRandom(seed));
                final int[] sorted = board.clone();
                Arrays.sort(sorted);
                assertArrayEquals(SlidingPuzzle.solved(size), sorted, level + " " + seed);
                assertTrue(SlidingPuzzle.isSolvable(board, size), level + " " + seed);
                assertFalse(SlidingPuzzle.isSolved(board), level + " " + seed);
            }
        }
    }

    @Test
    void shouldSolveTheEasyShuffleInAtMostItsMoves() {
        for (int seed = 1; seed <= 30; seed++) {
            final int[] board = SlidingPuzzle.shuffle(3, PuzzleLevel.EASY.getMoves(), new ReplayablePseudoRandom(seed));
            final int moves = solution(board, 3).size();
            assertTrue(moves >= 1 && moves <= PuzzleLevel.EASY.getMoves(), seed + ": " + moves);
        }
    }

    @Test
    void shouldShowThePictureWholeThenShuffleItAndWaitForTheDelayOfTheSettings() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(3000);
        final SlidingPuzzle game = launched(PuzzleContent.PICTURES, PuzzleLevel.NORMAL, PuzzleEnd.ENDLESS, 5);
        assertEquals(SlidingPuzzle.Phase.SHOW, game.getPhase());
        assertTrue(SlidingPuzzle.isSolved(game.getBoard()));
        assertEquals(3, game.getWaiting(), 1e-9);
        fx(() -> {
            steps(game, SlidingPuzzle.SHOW + 0.05);
            return null;
        });
        assertFalse(SlidingPuzzle.isSolved(game.getBoard()));
        assertEquals(SlidingPuzzle.Phase.SHOW, game.getPhase());
        for (int piece = 0; piece < 8; piece++) {
            assertFalse(game.isMovable(piece));
        }
        verify(stats, never()).notifyNewRoundReady();
        fx(() -> {
            steps(game, 1.5);
            return null;
        });
        assertEquals(SlidingPuzzle.Phase.PLAY, game.getPhase());
        verify(stats).notifyNewRoundReady();
        verify(stats).incrementNumberOfGoalsToReach();
    }

    @Test
    void shouldSlideAPieceNextToTheEmptyPlaceAfterAFixationOfTheLengthOfTheSettingsReadAtEachStep() throws Exception {
        final SlidingPuzzle game = playing(PuzzleContent.NUMBERS, PuzzleLevel.NORMAL, PuzzleEnd.ENDLESS, 7);
        final int[] before = game.getBoard();
        final int emptyPlace = game.placeOf(8);
        final int piece = before[SlidingPuzzle.neighbors(3, emptyPlace).get(0)];
        final int from = game.placeOf(piece);
        assertTrue(game.isMovable(piece));
        fx(() -> {
            game.lookAt(piece);
            steps(game, 0.5);
            return null;
        });
        assertEquals(from, game.placeOf(piece));
        assertEquals(0.5, game.getProgress(), 0.03);
        // The length is changed during the fixation: the half left now lasts 0.3 s
        when(configuration.getFixationLength()).thenReturn(600);
        fx(() -> {
            steps(game, 0.25);
            return null;
        });
        assertEquals(from, game.placeOf(piece));
        fx(() -> {
            steps(game, 0.07);
            return null;
        });
        assertEquals(emptyPlace, game.placeOf(piece));
        assertEquals(from, game.placeOf(8));
    }

    @Test
    void shouldNeitherSlideAPieceAwayFromTheEmptyPlaceNorKeepAFixationLookedAway() throws Exception {
        final SlidingPuzzle game = playing(PuzzleContent.NUMBERS, PuzzleLevel.NORMAL, PuzzleEnd.ENDLESS, 11);
        final List<Integer> near = SlidingPuzzle.neighbors(3, game.placeOf(8));
        int far = -1;
        for (int place = 0; place < 9; place++) {
            if (place != game.placeOf(8) && !near.contains(place)) {
                far = game.getBoard()[place];
                break;
            }
        }
        final int farPiece = far;
        final int[] before = game.getBoard();
        fx(() -> {
            game.lookAt(farPiece);
            steps(game, 2);
            return null;
        });
        assertArrayEquals(before, game.getBoard());
        final int nearPiece = before[near.get(0)];
        fx(() -> {
            game.lookAt(nearPiece);
            steps(game, 0.7);
            game.lookAway(nearPiece);
            steps(game, 0.2);
            game.lookAt(nearPiece);
            steps(game, 0.7);
            return null;
        });
        assertArrayEquals(before, game.getBoard());
    }

    @Test
    void shouldCountAPuzzleSolvedShowItsLastPieceThenGiveANewOne() throws Exception {
        final SlidingPuzzle game = playing(PuzzleContent.PICTURES, PuzzleLevel.EASY, PuzzleEnd.ENDLESS, 3);
        solve(game);
        assertEquals(SlidingPuzzle.Phase.JOY, game.getPhase());
        assertEquals(1, game.getScore());
        verify(stats).incrementNumberOfGoalsReached();
        fx(() -> {
            steps(game, SlidingPuzzle.JOY + 0.05);
            return null;
        });
        assertEquals(SlidingPuzzle.Phase.SHOW, game.getPhase());
        verify(stats, times(2)).incrementNumberOfGoalsToReach();
        verify(gameContext, never()).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldEndWithBravoAfterTheLastPuzzle() throws Exception {
        final SlidingPuzzle game = playing(PuzzleContent.NUMBERS, PuzzleLevel.EASY, PuzzleEnd.SOLVED_1, 9);
        solve(game);
        fx(() -> {
            steps(game, SlidingPuzzle.JOY + 0.05);
            return null;
        });
        assertEquals(SlidingPuzzle.Phase.OVER, game.getPhase());
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldGoOnWithoutEnd() throws Exception {
        final SlidingPuzzle game = playing(PuzzleContent.PICTURES, PuzzleLevel.EASY, PuzzleEnd.ENDLESS, 13);
        for (int round = 0; round < 3; round++) {
            solve(game);
            fx(() -> {
                steps(game, SlidingPuzzle.JOY + SlidingPuzzle.SHOW + SlidingPuzzle.SHUFFLE + 0.1);
                return null;
            });
        }
        assertEquals(3, game.getScore());
        assertEquals(SlidingPuzzle.Phase.PLAY, game.getPhase());
        verify(gameContext, never()).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldPlayTheHardLevelOnAGridOfFourByFour() throws Exception {
        final SlidingPuzzle game = playing(PuzzleContent.PICTURES, PuzzleLevel.HARD, PuzzleEnd.ENDLESS, 17);
        assertEquals(16, game.getBoard().length);
        assertTrue(SlidingPuzzle.isSolvable(game.getBoard(), 4));
    }

    @Test
    void shouldPutTheBoardAndItsModelSideBySideInTheMiddleOfTheScreen() throws Exception {
        for (final double[] screen : new double[][]{{1920, 1080}, {1366, 768}, {1024, 768}, {800, 1000}}) {
            screen(screen[0], screen[1]);
            final SlidingPuzzle game = launched(PuzzleContent.PICTURES, PuzzleLevel.HARD, PuzzleEnd.ENDLESS, 1);
            final double[] layout = game.layout();
            final String name = screen[0] + " x " + screen[1];
            // Inside the screen, under the band of the score
            assertTrue(layout[0] > 0 && layout[6] < screen[0], name);
            assertTrue(layout[1] >= screen[1] * 0.11 && layout[3] < screen[1], name);
            // The model at the right of the board, not over it
            assertTrue(layout[4] > layout[2], name);
            // Centered: as much room at the left as at the right
            assertEquals(layout[0], screen[0] - layout[6], 1, name);
            fx(() -> {
                game.dispose();
                return null;
            });
        }
    }

    @Test
    void shouldReplayWithTheSameShuffleAndPictures() throws Exception {
        final SlidingPuzzle first = playing(PuzzleContent.PICTURES, PuzzleLevel.NORMAL, PuzzleEnd.ENDLESS, 21);
        final int[] board = first.getBoard();
        final SlidingPuzzle again = playing(PuzzleContent.PICTURES, PuzzleLevel.NORMAL, PuzzleEnd.ENDLESS, 21);
        assertArrayEquals(board, again.getBoard());
    }

    @Test
    void shouldStopAfterDispose() throws Exception {
        final SlidingPuzzle game = playing(PuzzleContent.NUMBERS, PuzzleLevel.NORMAL, PuzzleEnd.ENDLESS, 2);
        fx(() -> {
            game.dispose();
            steps(game, 2);
            return null;
        });
        assertEquals(SlidingPuzzle.Phase.OVER, game.getPhase());
        assertTrue(gameArea.getChildren().isEmpty());
        verify(gameContext.getGazeDeviceManager(), times(8)).removeEventFilter(any());
    }

    @Test
    void shouldGiveAllTheVariants() {
        final Set<IGameVariant> variants = new PuzzleGameVariantGenerator().getVariants();
        assertEquals(36, variants.size());
        final IGameVariant mine = new GroupedGameVariant(PuzzleContent.MY_IMAGES.getKey(), PuzzleLevel.HARD.getKey(),
            PuzzleEnd.SOLVED_3.getKey(), UserImagesVariant.IMAGES);
        assertTrue(variants.contains(mine));
        assertEquals(UserImagesVariant.IMAGES, UserImagesVariant.folderOf(mine));
        assertEquals(PuzzleContent.MY_IMAGES, PuzzleGameLauncher.contentOf(mine));
        assertEquals(PuzzleLevel.HARD, PuzzleGameLauncher.levelOf(mine));
        assertEquals(PuzzleEnd.SOLVED_3, PuzzleGameLauncher.endOf(mine));
        final IGameVariant numbers = new GroupedGameVariant(PuzzleContent.NUMBERS.getKey(), PuzzleLevel.EASY.getKey(),
            PuzzleEnd.ENDLESS.getKey(), null);
        assertTrue(variants.contains(numbers));
        assertNull(UserImagesVariant.folderOf(numbers));
    }

    @Test
    void shouldPlayTheOldVariantsAtTheNormalLevelWithoutEnd() {
        final IGameVariant numbers = IGameVariant.toGameVariant(
            "EnumGameVariant:net.gazeplay.games.slidingpuzzle.PuzzleGameVariantGenerator$PuzzleGameVariant:NUMBERS");
        assertInstanceOf(EnumGameVariant.class, numbers);
        assertEquals(PuzzleContent.NUMBERS, PuzzleGameLauncher.contentOf(numbers));
        assertEquals(PuzzleLevel.NORMAL, PuzzleGameLauncher.levelOf(numbers));
        assertEquals(PuzzleEnd.ENDLESS, PuzzleGameLauncher.endOf(numbers));
        final IGameVariant monaLisa = IGameVariant.toGameVariant(
            "EnumGameVariant:net.gazeplay.games.slidingpuzzle.PuzzleGameVariantGenerator$PuzzleGameVariant:MONA_LISA");
        assertEquals(PuzzleContent.PICTURES, PuzzleGameLauncher.contentOf(monaLisa));
    }

    @Test
    void shouldFindEveryPictureOfTheGame() {
        for (final String picture : SlidingPuzzle.PICTURES) {
            assertNotNull(getClass().getClassLoader().getResource(SlidingPuzzle.DATA + picture), picture);
        }
    }
}

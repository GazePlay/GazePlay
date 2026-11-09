package net.gazeplay.games.labyrinth;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.ui.Translator;
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
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * The Labyrinth (reviewed on 9/11/2026, maintainer's rules): a perfect labyrinth, the goal far from the start, the three
 * ways of moving with a fixation of the length of the settings, the digging and the delay, the ends, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class LabyrinthTest {

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
    LabyrinthStats stats;

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

    private Labyrinth launched(final LabyrinthLevel level, final LabyrinthMove move, final LabyrinthEnd end,
                               final double seed) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final Labyrinth game = new Labyrinth(gameContext, stats, LabyrinthCharacter.MOUSE, level, move, end, seed);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private static void steps(final Labyrinth game, final double seconds) {
        for (double t = 0; t < seconds; t += STEP) {
            game.step(STEP);
        }
    }

    /**
     * @return the directions of the shortest path from the character to the goal
     */
    private static List<Maze.Direction> pathToGoal(final Labyrinth game) {
        final Maze maze = game.getMaze();
        final int[][] distances = maze.distancesFrom(game.getGoalLine(), game.getGoalColumn());
        final List<Maze.Direction> path = new ArrayList<>();
        int line = game.getLine();
        int column = game.getColumn();
        while (distances[line][column] > 0) {
            for (final Maze.Direction direction : Maze.Direction.values()) {
                if (maze.canGo(line, column, direction)
                    && distances[line + direction.lines][column + direction.columns] == distances[line][column] - 1) {
                    path.add(direction);
                    line += direction.lines;
                    column += direction.columns;
                    break;
                }
            }
        }
        return path;
    }

    /**
     * Goes to the goal by fixations on the arrows.
     */
    private static void reachTheGoalWithArrows(final Labyrinth game) {
        for (final Maze.Direction direction : pathToGoal(game)) {
            game.lookAt(game.getArrow(direction));
            steps(game, 1.05);
            game.lookAway(game.getArrow(direction));
        }
    }

    @Test
    void shouldDigAPerfectLabyrinthWhereEveryBoxCanBeReachedByOnePathOnly() {
        for (final LabyrinthLevel level : LabyrinthLevel.values()) {
            for (int seed = 1; seed <= 20; seed++) {
                final Maze maze = new Maze(level.getLines(), level.getColumns(), new ReplayablePseudoRandom(seed));
                final int boxes = level.getLines() * level.getColumns();
                // A tree: one opening less than boxes, and every box reached
                assertEquals(boxes - 1, maze.getOpenings().size(), level.name());
                final int[][] distances = maze.distancesFrom(0, 0);
                for (final int[] row : distances) {
                    for (final int distance : row) {
                        assertTrue(distance >= 0, level + " seed " + seed + ": every box can be reached");
                    }
                }
                // No wall out of the labyrinth can be opened
                for (int j = 0; j < level.getColumns(); j++) {
                    assertFalse(maze.canGo(0, j, Maze.Direction.UP));
                    assertFalse(maze.canGo(level.getLines() - 1, j, Maze.Direction.DOWN));
                }
                for (int i = 0; i < level.getLines(); i++) {
                    assertFalse(maze.canGo(i, 0, Maze.Direction.LEFT));
                    assertFalse(maze.canGo(i, level.getColumns() - 1, Maze.Direction.RIGHT));
                }
            }
        }
    }

    @Test
    void shouldGoBothWaysThroughAnOpenWall() {
        final Maze maze = new Maze(5, 7, new ReplayablePseudoRandom(3));
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 7; j++) {
                for (final Maze.Direction direction : Maze.Direction.values()) {
                    if (maze.canGo(i, j, direction)) {
                        final Maze.Direction back = Maze.Direction.values()[(direction.ordinal() + 2) % 4];
                        assertTrue(maze.canGo(i + direction.lines, j + direction.columns, back));
                    }
                }
            }
        }
    }

    @Test
    void shouldStartInACornerWithTheGoalAtLeastHalfTheLongestPathAway() throws Exception {
        for (final LabyrinthLevel level : LabyrinthLevel.values()) {
            for (int seed = 1; seed <= 10; seed++) {
                final Labyrinth game = launched(level, LabyrinthMove.LOCAL_ARROWS, LabyrinthEnd.ENDLESS, seed);
                fx(() -> {
                    assertTrue(game.getLine() == 0 || game.getLine() == level.getLines() - 1);
                    assertTrue(game.getColumn() == 0 || game.getColumn() == level.getColumns() - 1);
                    final int[][] distances = game.getMaze().distancesFrom(game.getLine(), game.getColumn());
                    int longest = 0;
                    for (final int[] row : distances) {
                        for (final int distance : row) {
                            longest = Math.max(longest, distance);
                        }
                    }
                    final int toGoal = distances[game.getGoalLine()][game.getGoalColumn()];
                    assertTrue(toGoal > 0 && toGoal >= longest / 2.0, level + ": " + toGoal + " of " + longest);
                    return null;
                });
            }
        }
    }

    @Test
    void shouldDigBeforeAnythingCanBeChosenAndWaitForTheDelayOfTheSettings() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(2500);
        final Labyrinth game = launched(LabyrinthLevel.EASY, LabyrinthMove.LOCAL_ARROWS, LabyrinthEnd.ENDLESS, 1);
        fx(() -> {
            assertEquals(Labyrinth.Phase.DIGGING, game.getPhase());
            assertEquals(2.5, game.getWaiting(), 1e-9, "the longest of the digging and of the delay");
            final Maze.Direction way = pathToGoal(game).get(0);
            game.lookAt(game.getArrow(way));
            steps(game, 2.3);
            assertEquals(Labyrinth.Phase.DIGGING, game.getPhase());
            assertEquals(0, game.getProgress(), 1e-9, "no fixation while digging");
            steps(game, 0.3);
            assertEquals(Labyrinth.Phase.PLAY, game.getPhase());
            final int line = game.getLine();
            final int column = game.getColumn();
            steps(game, 1.05);
            assertNotEquals(line * 100 + column, game.getLine() * 100 + game.getColumn(),
                "an arrow looked at during the digging counts once it is over");
            return null;
        });
        verify(stats).notifyNewRoundReady();
    }

    @Test
    void shouldMoveAfterAFixationOfTheLengthOfTheSettingsReadAtEachStep() throws Exception {
        final Labyrinth game = launched(LabyrinthLevel.NORMAL, LabyrinthMove.LOCAL_ARROWS, LabyrinthEnd.ENDLESS, 2);
        fx(() -> {
            steps(game, Labyrinth.DIGGING + 0.05);
            final Maze.Direction way = pathToGoal(game).get(0);
            final int line = game.getLine();
            final int column = game.getColumn();
            game.lookAt(game.getArrow(way));
            // Half of 1 s, then the length becomes 2 s: the other half takes 1 s
            steps(game, 0.5);
            assertEquals(line, game.getLine(), "not yet");
            assertEquals(column, game.getColumn());
            when(configuration.getFixationLength()).thenReturn(2000);
            steps(game, 0.6);
            assertEquals(line, game.getLine(), "the new length counts at once");
            assertEquals(column, game.getColumn());
            steps(game, 0.45);
            assertEquals(line + way.lines, game.getLine());
            assertEquals(column + way.columns, game.getColumn());
            return null;
        });
    }

    @Test
    void shouldNeitherMoveTowardsAWallNorKeepAFixationLookedAway() throws Exception {
        final Labyrinth game = launched(LabyrinthLevel.NORMAL, LabyrinthMove.LOCAL_ARROWS, LabyrinthEnd.ENDLESS, 4);
        fx(() -> {
            steps(game, Labyrinth.DIGGING + 0.05);
            final int line = game.getLine();
            final int column = game.getColumn();
            for (final Maze.Direction direction : Maze.Direction.values()) {
                if (!game.getMaze().canGo(line, column, direction)) {
                    assertFalse(game.isArrowEnabled(direction), direction + ": gray");
                    game.lookAt(game.getArrow(direction));
                    steps(game, 1.5);
                    game.lookAway(game.getArrow(direction));
                }
            }
            assertEquals(line, game.getLine());
            assertEquals(column, game.getColumn());
            final Maze.Direction way = pathToGoal(game).get(0);
            game.lookAt(game.getArrow(way));
            steps(game, 0.6);
            game.lookAway(game.getArrow(way));
            game.lookAt(game.getArrow(way));
            steps(game, 0.6);
            assertEquals(line, game.getLine(), "the fixation starts again after looking away");
            return null;
        });
    }

    @Test
    void shouldReachTheGoalCountItAndDigANewLabyrinth() throws Exception {
        final Labyrinth game = launched(LabyrinthLevel.EASY, LabyrinthMove.LOCAL_ARROWS, LabyrinthEnd.GOALS_3, 5);
        fx(() -> {
            steps(game, Labyrinth.DIGGING + 0.05);
            final Maze first = game.getMaze();
            reachTheGoalWithArrows(game);
            assertEquals(1, game.getScore());
            assertEquals(Labyrinth.Phase.JOY, game.getPhase());
            steps(game, Labyrinth.JOY + 0.05);
            assertEquals(Labyrinth.Phase.DIGGING, game.getPhase());
            assertNotSame(first, game.getMaze(), "a new labyrinth");
            return null;
        });
        verify(stats).incrementNumberOfGoalsReached();
        verify(stats, times(2)).incrementNumberOfGoalsToReach();
    }

    @Test
    void shouldEndWithBravoAfterTheLastGoal() throws Exception {
        final Labyrinth game = launched(LabyrinthLevel.EASY, LabyrinthMove.LOCAL_ARROWS, LabyrinthEnd.GOALS_3, 6);
        fx(() -> {
            for (int goal = 0; goal < 3; goal++) {
                steps(game, Labyrinth.DIGGING + 0.05);
                reachTheGoalWithArrows(game);
                steps(game, Labyrinth.JOY + 0.05);
            }
            assertEquals(3, game.getScore());
            assertEquals(Labyrinth.Phase.OVER, game.getPhase());
            return null;
        });
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldNeverEndWithoutEnd() throws Exception {
        final Labyrinth game = launched(LabyrinthLevel.EASY, LabyrinthMove.LOCAL_ARROWS, LabyrinthEnd.ENDLESS, 7);
        fx(() -> {
            for (int goal = 0; goal < 12; goal++) {
                steps(game, Labyrinth.DIGGING + 0.05);
                reachTheGoalWithArrows(game);
                steps(game, Labyrinth.JOY + 0.05);
            }
            assertEquals(12, game.getScore());
            assertEquals(Labyrinth.Phase.DIGGING, game.getPhase());
            return null;
        });
        verify(gameContext, never()).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldKeepMovingWhileAnArrowAroundTheLabyrinthIsLookedAt() throws Exception {
        final Labyrinth game = launched(LabyrinthLevel.HARD, LabyrinthMove.GLOBAL_ARROWS, LabyrinthEnd.ENDLESS, 8);
        fx(() -> {
            steps(game, Labyrinth.DIGGING + 0.05);
            final List<Maze.Direction> path = pathToGoal(game);
            int same = 1;
            while (same < path.size() && path.get(same) == path.get(0)) {
                same++;
            }
            // Stops before the goal, where the joy would start
            final int moves = Math.min(same, path.size() - 1);
            final int expectedLine = game.getLine() + path.get(0).lines * moves;
            final int expectedColumn = game.getColumn() + path.get(0).columns * moves;
            game.lookAt(game.getArrow(path.get(0)));
            for (int i = 0; i < moves; i++) {
                steps(game, 1.0);
            }
            steps(game, 0.03 * moves);
            assertEquals(expectedLine, game.getLine(), moves + " moves while looking at the arrow");
            assertEquals(expectedColumn, game.getColumn());
            return null;
        });
    }

    @Test
    void shouldPutTheArrowsAroundTheLabyrinthInsideTheScreenAndOutsideTheLabyrinth() throws Exception {
        final double[][] screens = {{1366, 768}, {1920, 1080}, {1280, 1024}, {3840, 2160}};
        for (final double[] size : screens) {
            screen(size[0], size[1]);
            for (final LabyrinthLevel level : LabyrinthLevel.values()) {
                final Labyrinth game = launched(level, LabyrinthMove.GLOBAL_ARROWS, LabyrinthEnd.ENDLESS, 1);
                fx(() -> {
                    final double[] board = game.board();
                    for (final Maze.Direction direction : Maze.Direction.values()) {
                        final double[] arrow = game.arrowCenter(direction);
                        final double half = arrow[2] / 2;
                        assertTrue(arrow[0] - half >= 0 && arrow[0] + half <= size[0], direction + " inside");
                        assertTrue(arrow[1] - half >= size[1] * 0.1 && arrow[1] + half <= size[1], direction + " inside");
                        final boolean outside = arrow[0] + half <= board[0] || arrow[0] - half >= board[2]
                            || arrow[1] + half <= board[1] || arrow[1] - half >= board[3];
                        assertTrue(outside, direction + " outside the labyrinth " + size[0] + " " + level);
                    }
                    assertTrue(game.getCell() > size[1] * 0.07, "boxes large enough: " + game.getCell());
                    return null;
                });
            }
        }
    }

    @Test
    void shouldChooseTheCharacterThenGuideItBoxByBox() throws Exception {
        final Labyrinth game = launched(LabyrinthLevel.NORMAL, LabyrinthMove.SELECT_THEN_GUIDE, LabyrinthEnd.ENDLESS, 9);
        fx(() -> {
            steps(game, Labyrinth.DIGGING + 0.05);
            final Maze.Direction way = pathToGoal(game).get(0);
            final int line = game.getLine();
            final int column = game.getColumn();
            game.lookAtBox(line + way.lines, column + way.columns);
            assertEquals(line, game.getLine(), "not chosen yet");
            game.lookAt(game.getCharacterTarget());
            steps(game, 1.05);
            assertTrue(game.isSelected());
            game.lookAtBox(line + way.lines, column + way.columns);
            assertEquals(line + way.lines, game.getLine(), "goes at once");
            assertEquals(column + way.columns, game.getColumn());
            // Looking further away lets it go
            game.lookAtBox(game.getLine() + 2 >= 5 ? game.getLine() - 2 : game.getLine() + 2, game.getColumn());
            assertFalse(game.isSelected());
            return null;
        });
    }

    @Test
    void shouldNotGuideTheCharacterThroughAHedge() throws Exception {
        final Labyrinth game = launched(LabyrinthLevel.HARD, LabyrinthMove.SELECT_THEN_GUIDE, LabyrinthEnd.ENDLESS, 10);
        fx(() -> {
            steps(game, Labyrinth.DIGGING + 0.05);
            game.lookAt(game.getCharacterTarget());
            steps(game, 1.05);
            final int line = game.getLine();
            final int column = game.getColumn();
            for (final Maze.Direction direction : Maze.Direction.values()) {
                if (game.getMaze().inside(line + direction.lines, column + direction.columns)
                    && !game.getMaze().canGo(line, column, direction)) {
                    game.lookAtBox(line + direction.lines, column + direction.columns);
                }
            }
            assertEquals(line, game.getLine());
            assertEquals(column, game.getColumn());
            assertTrue(game.isSelected(), "a box next to it behind a hedge does not let it go");
            return null;
        });
    }

    @Test
    void shouldStopAfterDispose() throws Exception {
        final Labyrinth game = launched(LabyrinthLevel.EASY, LabyrinthMove.LOCAL_ARROWS, LabyrinthEnd.ENDLESS, 1);
        fx(() -> {
            assertFalse(gameArea.getChildren().isEmpty());
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertEquals(Labyrinth.Phase.OVER, game.getPhase());
            game.step(STEP);
            return null;
        });
        verify(gameContext.getGazeDeviceManager(), atLeast(4)).removeEventFilter(any());
    }

    @Test
    void shouldReplayWithTheSameLabyrinth() throws Exception {
        final List<int[]> first = fx(() -> launched(LabyrinthLevel.HARD, LabyrinthMove.LOCAL_ARROWS,
            LabyrinthEnd.ENDLESS, 42).getMaze().getOpenings());
        final List<int[]> second = fx(() -> launched(LabyrinthLevel.HARD, LabyrinthMove.LOCAL_ARROWS,
            LabyrinthEnd.ENDLESS, 42).getMaze().getOpenings());
        assertEquals(first.size(), second.size());
        for (int i = 0; i < first.size(); i++) {
            assertArrayEquals(first.get(i), second.get(i));
        }
    }

    @Test
    void shouldGiveAllTheVariants() {
        final Set<IGameVariant> variants = new LabyrinthGameVariantGenerator().getVariants();
        assertEquals(72, variants.size());
        final IGameVariant variant = new GroupedGameVariant(LabyrinthCharacter.ANIMAL.getKey(),
            LabyrinthLevel.HARD.getKey(), LabyrinthEnd.GOALS_10.getKey(), null, LabyrinthMove.GLOBAL_ARROWS.getKey());
        assertTrue(variants.contains(variant));
        assertEquals(LabyrinthCharacter.ANIMAL, LabyrinthGameLauncher.characterOf(variant));
        assertEquals(LabyrinthLevel.HARD, LabyrinthGameLauncher.levelOf(variant));
        assertEquals(LabyrinthMove.GLOBAL_ARROWS, LabyrinthGameLauncher.moveOf(variant));
        assertEquals(LabyrinthEnd.GOALS_10, LabyrinthGameLauncher.endOf(variant));
    }

    @Test
    void shouldPlayTheOldVariantsWithTheirWayAndCharacterAtTheNormalLevelWithoutEnd() {
        final IGameVariant old = IGameVariant.toGameVariant(
            "EnumGameVariant:net.gazeplay.games.labyrinth.LabyrinthGameVariant:OTHER_ANSELECT_THEN_LOOK_DESTINATION");
        assertInstanceOf(EnumGameVariant.class, old);
        assertEquals(LabyrinthCharacter.ANIMAL, LabyrinthGameLauncher.characterOf(old));
        assertEquals(LabyrinthMove.SELECT_THEN_GUIDE, LabyrinthGameLauncher.moveOf(old));
        assertEquals(LabyrinthLevel.NORMAL, LabyrinthGameLauncher.levelOf(old));
        assertEquals(LabyrinthEnd.ENDLESS, LabyrinthGameLauncher.endOf(old));
        assertEquals(LabyrinthCharacter.MOUSE, LabyrinthGameLauncher.characterOf(null));
    }
}

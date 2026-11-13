package net.gazeplay.games.follow;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.labyrinth.Maze;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Suivre le chemin" (reviewed on 13/11/2026): the variants, the coins, the keys before their doors (the Biboule walked
 * along the paths), the hedges, the speed, the delay, the end.
 */
@ExtendWith(ApplicationExtension.class)
class FollowTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

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
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1600, 900));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getGameStartDelay()).thenReturn(0);
        when(configuration.getAnimationSpeedRatio()).thenReturn(1.0);
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private Follow launched(final FollowMode mode, final FollowLevel level, final FollowEnd end, final double seed)
        throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final Follow game = new Follow(gameContext, stats, mode, level, end, seed);
            game.setManual();
            game.launch();
            steps(game, Follow.APPEAR + 0.05);
            return game;
        });
    }

    private static void steps(final Follow game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    private static int[] boxOf(final Follow game) {
        final double[] first = game.boxCenter(0, 0);
        final double cell = game.getCell();
        return new int[]{(int) Math.floor((game.getY() - first[1]) / cell + 0.5),
            (int) Math.floor((game.getX() - first[0]) / cell + 0.5)};
    }

    private static int[] boxOf(final Follow game, final double x, final double y) {
        final double[] first = game.boxCenter(0, 0);
        final double cell = game.getCell();
        return new int[]{(int) Math.round((y - first[1]) / cell), (int) Math.round((x - first[0]) / cell)};
    }

    /**
     * @return the boxes from a box to another, by the paths of the labyrinth (doors not seen)
     */
    private static List<int[]> way(final Maze maze, final int[] from, final int[] to) {
        final int[][] distances = maze.distancesFrom(to[0], to[1]);
        final List<int[]> way = new ArrayList<>();
        int[] box = from;
        while (distances[box[0]][box[1]] > 0) {
            for (final Maze.Direction direction : Maze.Direction.values()) {
                final int line = box[0] + direction.lines;
                final int column = box[1] + direction.columns;
                if (maze.canGo(box[0], box[1], direction)
                    && distances[line][column] == distances[box[0]][box[1]] - 1) {
                    box = new int[]{line, column};
                    break;
                }
            }
            way.add(box);
        }
        return way;
    }

    /**
     * Looks at the middle of each box of the way in turn, until the Biboule is there (or the map is finished).
     */
    private static void walk(final Follow game, final int[] to) {
        for (final int[] box : way(game.getMaze(), boxOf(game), to)) {
            final double[] center = game.boxCenter(box[0], box[1]);
            game.aimAt(center[0], center[1]);
            double time = 0;
            while (game.getPhase() == Follow.Phase.PLAY
                && Math.hypot(game.getX() - center[0], game.getY() - center[1]) > 1) {
                game.step(0.02);
                time += 0.02;
                assertFalse(game.isInWall(), "in a hedge");
                assertTrue(time < 5, "stuck before box " + box[0] + ":" + box[1]);
            }
            if (game.getPhase() != Follow.Phase.PLAY) {
                return;
            }
        }
    }

    @Test
    void shouldGiveEachVariant() throws Exception {
        final Set<IGameVariant> variants = new FollowGameVariantGenerator().getVariants();
        assertEquals(FollowMode.values().length * FollowLevel.values().length * FollowEnd.values().length,
            variants.size());
        final Set<String> seen = new HashSet<>();
        for (final IGameVariant variant : variants) {
            final Follow game = FollowGameLauncher.create(gameContext, variant, stats, 1);
            final GroupedGameVariant grouped = (GroupedGameVariant) variant;
            assertEquals(grouped.getGroup(), game.getMode().getKey());
            assertEquals(grouped.getSubGroup(), game.getEnd().getKey());
            assertEquals(grouped.getValue(), game.getLevel().getKey());
            seen.add(game.getMode() + "/" + game.getLevel() + "/" + game.getEnd());
        }
        assertEquals(variants.size(), seen.size());
        final Follow former = FollowGameLauncher.create(gameContext, mock(IGameVariant.class), stats, 1);
        assertEquals(FollowMode.COINS, former.getMode());
        assertEquals(FollowLevel.NORMAL, former.getLevel());
        assertEquals(FollowEnd.MAPS_1, former.getEnd());
    }

    @Test
    void shouldPickUpEveryCoinOfTheMap() throws Exception {
        for (final FollowLevel level : FollowLevel.values()) {
            for (int seed = 1; seed <= 5; seed++) {
                final Follow game = launched(FollowMode.COINS, level, FollowEnd.ENDLESS, seed);
                fx(() -> {
                    final List<Follow.Item> coins = new ArrayList<>(game.getItems());
                    assertEquals(level.getCoins(), coins.size());
                    final int[] start = boxOf(game);
                    final Set<String> boxes = new HashSet<>();
                    for (final Follow.Item coin : coins) {
                        assertEquals(Follow.Kind.COIN, coin.getKind());
                        final int[] box = boxOf(game, coin.getX(), coin.getY());
                        assertFalse(box[0] == start[0] && box[1] == start[1], "a coin on the start");
                        assertTrue(boxes.add(box[0] + ":" + box[1]), "two coins in a box");
                    }
                    for (final Follow.Item coin : coins) {
                        if (!coin.isTaken()) {
                            walk(game, boxOf(game, coin.getX(), coin.getY()));
                        }
                    }
                    assertEquals(level.getCoins(), game.getCoinsTaken());
                    assertEquals(Follow.Phase.JOY, game.getPhase());
                    assertEquals(1, game.getMaps());
                    return null;
                });
            }
        }
        verify(gameContext.getSoundManager(), atLeastOnce()).add("data/follow/sounds/coin.wav");
    }

    @Test
    void shouldOpenEachDoorWithItsKeyUpToTheTreasure() throws Exception {
        for (final FollowLevel level : FollowLevel.values()) {
            for (int seed = 1; seed <= 5; seed++) {
                final Follow game = launched(FollowMode.KEYS, level, FollowEnd.ENDLESS, seed);
                fx(() -> {
                    assertEquals(level.getDoors(), game.getDoors().size());
                    final List<Follow.Item> keys = game.getItems().stream()
                        .filter(item -> item.getKind() == Follow.Kind.KEY)
                        .sorted(Comparator.comparingInt(Follow.Item::getColor)).toList();
                    assertEquals(level.getDoors(), keys.size());
                    final Follow.Item chest = game.getItems().stream()
                        .filter(item -> item.getKind() == Follow.Kind.CHEST).findFirst().orElseThrow();
                    for (final Follow.Item key : keys) {
                        final Follow.Door door = game.getDoors().get(key.getColor());
                        assertFalse(door.isOpen());
                        walk(game, boxOf(game, key.getX(), key.getY()));
                        assertTrue(key.isTaken(), "key " + key.getColor() + " not reached");
                        assertTrue(door.isOpen());
                    }
                    walk(game, boxOf(game, chest.getX(), chest.getY()));
                    assertTrue(chest.isTaken());
                    assertEquals(Follow.Phase.JOY, game.getPhase());
                    return null;
                });
            }
        }
        verify(gameContext.getSoundManager(), atLeastOnce()).add("data/follow/sounds/key.wav");
    }

    @Test
    void shouldStopAtAClosedDoor() throws Exception {
        final Follow game = launched(FollowMode.KEYS, FollowLevel.EASY, FollowEnd.ENDLESS, 3);
        fx(() -> {
            final Follow.Item chest = game.getItems().stream()
                .filter(item -> item.getKind() == Follow.Kind.CHEST).findFirst().orElseThrow();
            // Straight to the treasure, without the key: the door stops the Biboule
            walkAsFarAsPossible(game, boxOf(game, chest.getX(), chest.getY()));
            assertFalse(chest.isTaken());
            assertFalse(game.getDoors().get(0).isOpen());
            assertEquals(Follow.Phase.PLAY, game.getPhase());
            return null;
        });
    }

    private static void walkAsFarAsPossible(final Follow game, final int[] to) {
        for (final int[] box : way(game.getMaze(), boxOf(game), to)) {
            final double[] center = game.boxCenter(box[0], box[1]);
            game.aimAt(center[0], center[1]);
            steps(game, 3);
            assertFalse(game.isInWall());
        }
    }

    @Test
    void shouldNotGoThroughAHedge() throws Exception {
        final Follow game = launched(FollowMode.KEYS, FollowLevel.NORMAL, FollowEnd.ENDLESS, 2);
        fx(() -> {
            final int[] start = boxOf(game);
            final Maze maze = game.getMaze();
            for (final Maze.Direction direction : Maze.Direction.values()) {
                final int line = start[0] + direction.lines;
                final int column = start[1] + direction.columns;
                if (maze.inside(line, column) && !maze.canGo(start[0], start[1], direction)) {
                    final double[] center = game.boxCenter(line, column);
                    game.aimAt(center[0], center[1]);
                    steps(game, 2);
                    assertFalse(game.isInWall());
                    final int[] now = boxOf(game);
                    assertArrayEquals(start, now, "through the hedge towards " + direction);
                }
            }
            return null;
        });
    }

    @Test
    void shouldGoAtTheSameSpeedOnEveryScreen() throws Exception {
        final Follow game = launched(FollowMode.COINS, FollowLevel.EASY, FollowEnd.ENDLESS, 4);
        fx(() -> {
            final double x0 = game.getX();
            final double y0 = game.getY();
            final double direction = boxOf(game)[1] == 0 ? 1 : -1;
            game.aimAt(x0 + direction * game.getCell() * 0.12, y0);
            steps(game, 0.1);
            assertEquals(Follow.SPEED * 900 * 0.1, Math.abs(game.getX() - x0), 0.6);
            assertEquals(y0, game.getY(), 0.01);
            return null;
        });
    }

    @Test
    void shouldWaitForTheDelayOfTheSettings() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(2000);
        final Follow game = launched(FollowMode.COINS, FollowLevel.EASY, FollowEnd.ENDLESS, 5);
        fx(() -> {
            final double x0 = game.getX();
            game.aimAt(x0 + (boxOf(game)[1] == 0 ? 50 : -50), game.getY());
            steps(game, 1);
            assertEquals(Follow.Phase.APPEAR, game.getPhase());
            assertEquals(x0, game.getX(), 0.01);
            steps(game, 1.2);
            assertEquals(Follow.Phase.PLAY, game.getPhase());
            steps(game, 0.2);
            assertNotEquals(x0, game.getX(), 1);
            return null;
        });
    }

    @Test
    void shouldEndAfterTheMapsChosen() throws Exception {
        final Follow game = launched(FollowMode.KEYS, FollowLevel.EASY, FollowEnd.MAPS_1, 6);
        fx(() -> {
            for (final Follow.Item key : game.getItems().stream().filter(item -> item.getKind() == Follow.Kind.KEY)
                .toList()) {
                walk(game, boxOf(game, key.getX(), key.getY()));
            }
            final Follow.Item chest = game.getItems().stream()
                .filter(item -> item.getKind() == Follow.Kind.CHEST).findFirst().orElseThrow();
            walk(game, boxOf(game, chest.getX(), chest.getY()));
            steps(game, Follow.JOY + 0.1);
            assertEquals(Follow.Phase.OVER, game.getPhase());
            return null;
        });
        verify(gameContext).playWinTransition(anyLong(), any());
        verify(stats).incrementNumberOfGoalsReached();

        final Follow endless = launched(FollowMode.COINS, FollowLevel.EASY, FollowEnd.ENDLESS, 7);
        fx(() -> {
            for (final Follow.Item coin : new ArrayList<>(endless.getItems())) {
                if (!coin.isTaken()) {
                    walk(endless, boxOf(endless, coin.getX(), coin.getY()));
                }
            }
            steps(endless, Follow.JOY + 0.1);
            assertEquals(Follow.Phase.APPEAR, endless.getPhase());
            assertEquals(1, endless.getMaps());
            assertEquals(0, endless.getCoinsTaken());
            assertEquals(FollowLevel.EASY.getCoins(), endless.getItems().size());
            return null;
        });
    }

    @Test
    void shouldMakeLoopsForTheCoinsOnly() throws Exception {
        final Follow keys = launched(FollowMode.KEYS, FollowLevel.EASY, FollowEnd.ENDLESS, 8);
        final Follow coins = launched(FollowMode.COINS, FollowLevel.EASY, FollowEnd.ENDLESS, 8);
        // A labyrinth without loops has one passage less than its boxes
        final int boxes = FollowLevel.EASY.getLines() * FollowLevel.EASY.getColumns();
        assertEquals(boxes - 1, passages(keys.getMaze()));
        assertTrue(passages(coins.getMaze()) > boxes - 1);
    }

    private static int passages(final Maze maze) {
        int count = 0;
        for (int i = 0; i < maze.getLines(); i++) {
            for (int j = 0; j < maze.getColumns(); j++) {
                count += maze.canGo(i, j, Maze.Direction.RIGHT) ? 1 : 0;
                count += maze.canGo(i, j, Maze.Direction.DOWN) ? 1 : 0;
            }
        }
        return count;
    }
}

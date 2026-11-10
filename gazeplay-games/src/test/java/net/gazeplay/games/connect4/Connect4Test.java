package net.gazeplay.games.connect4;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
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

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Quatre en ligne" (name code Connect4; reviewed on 10/11/2026, maintainer's rules): the rules of the grid, the 3
 * levels of the computer, a column chosen by a fixation of the length of the settings, the turns, the ends of a game
 * and of the match, two players, the layout, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class Connect4Test {

    private static final double STEP = 1.0 / 60;
    private static final int RED = Connect4.RED;
    private static final int YELLOW = Connect4.YELLOW;

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
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
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

    private Connect4 launched(final Connect4Opponent opponent, final Connect4End end, final double seed)
        throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final Connect4 game = new Connect4(gameContext, stats, opponent, end, seed);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private Connect4 playing(final Connect4Opponent opponent, final Connect4End end, final double seed)
        throws Exception {
        final Connect4 game = launched(opponent, end, seed);
        steps(game, 0.05);
        assertEquals(Connect4.Phase.PLAY, game.getPhase());
        return game;
    }

    private static void steps(final Connect4 game, final double seconds) throws Exception {
        fx(() -> {
            for (double t = 0; t < seconds; t += STEP) {
                game.step(STEP);
            }
            return null;
        });
    }

    /**
     * Plays a column for the player to play, as at the end of a fixation, and waits for the token to land.
     */
    private static void playAndLand(final Connect4 game, final int column) throws Exception {
        fx(() -> {
            game.play(column);
            return null;
        });
        steps(game, 0.8);
    }

    /**
     * Drops tokens in the grid directly (not a move of the game).
     */
    private static void put(final Connect4 game, final int player, final int... columns) throws Exception {
        fx(() -> {
            for (final int column : columns) {
                game.getBoard().drop(column, player);
            }
            return null;
        });
    }

    @Test
    void shouldFindTheLinesOfFourInEveryDirection() {
        final Connect4Board board = new Connect4Board();
        for (int column = 0; column < 3; column++) {
            board.drop(column, RED);
        }
        assertEquals(Connect4Board.EMPTY, board.winner());
        assertTrue(board.wins(3, RED));
        assertFalse(board.wins(3, YELLOW));
        board.drop(3, RED);
        assertArrayEquals(new int[][]{{0, 0}, {1, 0}, {2, 0}, {3, 0}}, board.winningLine());
        // A column
        final Connect4Board column = new Connect4Board();
        for (int k = 0; k < 4; k++) {
            column.drop(5, YELLOW);
        }
        assertEquals(YELLOW, column.winner());
        // Both diagonals: a staircase of yellow tokens under the red ones
        final Connect4Board up = new Connect4Board();
        for (int k = 0; k < 4; k++) {
            for (int below = 0; below < k; below++) {
                up.drop(k, YELLOW);
            }
            up.drop(k, RED);
        }
        assertEquals(RED, up.winner());
        final Connect4Board down = new Connect4Board();
        for (int k = 0; k < 4; k++) {
            for (int below = 0; below < 3 - k; below++) {
                down.drop(k, YELLOW);
            }
            down.drop(k, RED);
        }
        assertEquals(RED, down.winner());
        assertTrue(down.lineThrough(0, 3, RED));
    }

    /**
     * Fills the grid without any line of 4: pairs of rows of alternate colors.
     */
    private static int drawPattern(final int column, final int row) {
        return ((column % 2) ^ ((row / 2) % 2)) == 0 ? RED : YELLOW;
    }

    @Test
    void shouldFillTheGridAndRefuseAFullColumn() {
        final Connect4Board board = new Connect4Board();
        for (int column = 0; column < Connect4Board.COLUMNS; column++) {
            for (int row = 0; row < Connect4Board.ROWS; row++) {
                board.drop(column, drawPattern(column, row));
            }
            assertFalse(board.canPlay(column));
        }
        assertTrue(board.isFull());
        assertTrue(board.playable().isEmpty());
        assertEquals(Connect4Board.EMPTY, board.winner());
        assertThrows(IllegalArgumentException.class, () -> board.drop(0, RED));
        board.undo(6);
        assertEquals(List.of(6), board.playable());
    }

    @Test
    void shouldWinAtOnceAtEveryLevel() {
        for (final Connect4Opponent level : new Connect4Opponent[]{Connect4Opponent.EASY, Connect4Opponent.NORMAL,
            Connect4Opponent.HARD}) {
            for (int seed = 1; seed <= 10; seed++) {
                final Connect4Board board = new Connect4Board();
                board.drop(0, RED);
                board.drop(4, YELLOW);
                board.drop(0, RED);
                board.drop(4, YELLOW);
                board.drop(1, RED);
                board.drop(4, YELLOW);
                board.drop(6, RED);
                assertEquals(4, new Connect4Computer(level, new ReplayablePseudoRandom(seed)).choose(board, YELLOW,
                    RED), level.name());
            }
        }
    }

    /**
     * @return a random grid without any line, where the player to block has exactly one winning column and the
     * computer none, or null
     */
    private static int[] threat(final ReplayablePseudoRandom random, final Connect4Board board) {
        board.clear();
        final int moves = 6 + random.nextInt(20);
        int player = RED;
        for (int k = 0; k < moves; k++) {
            final List<Integer> playable = board.playable();
            final int column = playable.get(random.nextInt(playable.size()));
            if (board.wins(column, player)) {
                return null;
            }
            board.drop(column, player);
            player = player == RED ? YELLOW : RED;
        }
        if (player != YELLOW) {
            board.drop(board.playable().get(0), RED);
            if (board.winner() != Connect4Board.EMPTY) {
                return null;
            }
        }
        int winning = -1;
        int count = 0;
        for (final int column : board.playable()) {
            if (board.wins(column, YELLOW)) {
                return null;
            }
            if (board.wins(column, RED)) {
                winning = column;
                count++;
            }
        }
        return count == 1 ? new int[]{winning} : null;
    }

    @Test
    void shouldAlwaysBlockALineOfFourAtNormalAndHardLevels() {
        final ReplayablePseudoRandom random = new ReplayablePseudoRandom(42);
        final Connect4Board board = new Connect4Board();
        int checked = 0;
        for (int attempt = 0; attempt < 4000 && checked < 60; attempt++) {
            final int[] threat = threat(random, board);
            if (threat == null) {
                continue;
            }
            checked++;
            for (final Connect4Opponent level : new Connect4Opponent[]{Connect4Opponent.NORMAL, Connect4Opponent.HARD}) {
                assertEquals(threat[0], new Connect4Computer(level, new ReplayablePseudoRandom(checked)).choose(board,
                    YELLOW, RED), level.name());
            }
        }
        assertEquals(60, checked);
    }

    @Test
    void shouldNotGiveTheWinOnTopOfItsTokenAtNormalLevel() {
        // Red has three in a row on the second row, from column 1 to 3, column 0 and 4 empty below it: whoever plays
        // column 4 gives red the box above
        final Connect4Board board = new Connect4Board();
        board.drop(1, YELLOW);
        board.drop(2, YELLOW);
        board.drop(3, RED);
        board.drop(1, RED);
        board.drop(2, RED);
        board.drop(3, RED);
        board.drop(5, YELLOW);
        board.drop(6, YELLOW);
        board.drop(6, RED);
        // Column 0 and column 4 each give red the second row
        for (int seed = 1; seed <= 30; seed++) {
            final int column = new Connect4Computer(Connect4Opponent.NORMAL, new ReplayablePseudoRandom(seed))
                .choose(board, YELLOW, RED);
            assertNotEquals(0, column, "seed " + seed);
            assertNotEquals(4, column, "seed " + seed);
        }
    }

    /**
     * @return the winner of a game between two computers (0 for a draw), the first one playing red and first
     */
    private static int match(final Connect4Opponent first, final Connect4Opponent second, final double seed) {
        final ReplayablePseudoRandom random = new ReplayablePseudoRandom(seed);
        final Connect4Computer red = new Connect4Computer(first, random);
        final Connect4Computer yellow = new Connect4Computer(second, random);
        final Connect4Board board = new Connect4Board();
        int player = RED;
        while (!board.isFull()) {
            final int column = (player == RED ? red : yellow).choose(board, player, player == RED ? YELLOW : RED);
            final int row = board.drop(column, player);
            if (board.lineThrough(column, row, player)) {
                return player;
            }
            player = player == RED ? YELLOW : RED;
        }
        return Connect4Board.EMPTY;
    }

    @Test
    void shouldMakeTheHardComputerBeatTheOthers() {
        int hardWins = 0;
        for (int game = 0; game < 10; game++) {
            final boolean hardFirst = game % 2 == 0;
            final int winner = hardFirst ? match(Connect4Opponent.HARD, Connect4Opponent.EASY, game + 1)
                : match(Connect4Opponent.EASY, Connect4Opponent.HARD, game + 1);
            if (winner == (hardFirst ? RED : YELLOW)) {
                hardWins++;
            }
        }
        assertTrue(hardWins >= 9, "hard against easy: " + hardWins + " / 10");
        // Normal against easy, 30 games: the easy computer can still win with two lines at once
        int normalWins = 0;
        int easyWins = 0;
        for (int game = 0; game < 30; game++) {
            final boolean normalFirst = game % 2 == 0;
            final int winner = normalFirst ? match(Connect4Opponent.NORMAL, Connect4Opponent.EASY, game + 1)
                : match(Connect4Opponent.EASY, Connect4Opponent.NORMAL, game + 1);
            if (winner == (normalFirst ? RED : YELLOW)) {
                normalWins++;
            } else if (winner != Connect4Board.EMPTY) {
                easyWins++;
            }
        }
        assertTrue(normalWins >= 2 * easyWins + 10, "normal " + normalWins + ", easy " + easyWins + " / 30");
        int hardAgainstNormal = 0;
        int normalAgainstHard = 0;
        for (int game = 0; game < 10; game++) {
            final boolean hardFirst = game % 2 == 0;
            final int winner = hardFirst ? match(Connect4Opponent.HARD, Connect4Opponent.NORMAL, game + 1)
                : match(Connect4Opponent.NORMAL, Connect4Opponent.HARD, game + 1);
            if (winner == (hardFirst ? RED : YELLOW)) {
                hardAgainstNormal++;
            } else if (winner != Connect4Board.EMPTY) {
                normalAgainstHard++;
            }
        }
        assertTrue(hardAgainstNormal > normalAgainstHard, "hard " + hardAgainstNormal + ", normal "
            + normalAgainstHard);
    }

    @Test
    void shouldWaitForTheDelayOfTheSettings() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(2000);
        final Connect4 game = launched(Connect4Opponent.NORMAL, Connect4End.ENDLESS, 3);
        fx(() -> {
            game.lookAt(3);
            return null;
        });
        steps(game, 1.5);
        assertEquals(Connect4.Phase.WAIT, game.getPhase());
        verify(stats, never()).notifyNewRoundReady();
        steps(game, 0.6);
        assertEquals(Connect4.Phase.PLAY, game.getPhase());
        verify(stats).notifyNewRoundReady();
    }

    @Test
    void shouldDropATokenAfterAFixationOfTheLengthOfTheSettingsReadAtEachStep() throws Exception {
        final Connect4 game = playing(Connect4Opponent.NORMAL, Connect4End.ENDLESS, 5);
        assertEquals(RED, game.getCurrent());
        fx(() -> {
            game.lookAt(2);
            return null;
        });
        assertTrue(game.isGhostShown());
        steps(game, 0.5);
        assertEquals(0, game.getBoard().count());
        assertEquals(0.5, game.getProgress(), 0.03);
        when(configuration.getFixationLength()).thenReturn(600);
        steps(game, 0.25);
        assertEquals(0, game.getBoard().count());
        steps(game, 0.07);
        assertEquals(1, game.getBoard().count());
        assertEquals(RED, game.getBoard().get(2, 0));
        assertEquals(Connect4.Phase.FALL, game.getPhase());
        // The token lands, then the computer thinks and plays
        steps(game, 0.8);
        assertEquals(Connect4.Phase.THINK, game.getPhase());
        assertEquals(YELLOW, game.getCurrent());
        verify(gameContext.getSoundManager()).add("data/connect4/sounds/drop.wav");
        steps(game, Connect4.THINK + 0.8);
        assertEquals(2, game.getBoard().count());
        assertEquals(Connect4.Phase.PLAY, game.getPhase());
        assertEquals(RED, game.getCurrent());
    }

    @Test
    void shouldNotPlayAFullColumnNorDuringTheTurnOfTheComputer() throws Exception {
        final Connect4 game = playing(Connect4Opponent.EASY, Connect4End.ENDLESS, 7);
        put(game, RED, 0, 0, 0);
        put(game, YELLOW, 0, 0, 0);
        fx(() -> {
            game.lookAt(0);
            return null;
        });
        steps(game, 1.2);
        assertEquals(6, game.getBoard().count());
        assertFalse(game.isGhostShown());
        // The computer's turn: looking at a column does nothing
        playAndLand(game, 3);
        assertEquals(Connect4.Phase.THINK, game.getPhase());
        fx(() -> {
            game.lookAt(5);
            return null;
        });
        steps(game, 0.5);
        assertEquals(7, game.getBoard().count());
    }

    @Test
    void shouldCircleTheLineCountTheGameAndChangeWhoStarts() throws Exception {
        final Connect4 game = playing(Connect4Opponent.NORMAL, Connect4End.ENDLESS, 9);
        put(game, RED, 0, 0, 0);
        put(game, YELLOW, 6, 6, 5);
        playAndLand(game, 0);
        assertEquals(Connect4.Phase.SHOW, game.getPhase());
        assertEquals(RED, game.getLastWinner());
        assertEquals(4, game.marks());
        assertEquals(1, game.score(RED));
        verify(stats).incrementNumberOfGoalsToReach();
        verify(stats).incrementNumberOfGoalsReached();
        verify(gameContext.getSoundManager()).add("data/connect4/sounds/win.wav");
        // The next game: an empty grid, the computer first
        steps(game, Connect4.SHOW + 0.05);
        assertEquals(0, game.marks());
        assertEquals(0, game.getBoard().count());
        assertEquals(Connect4.Phase.THINK, game.getPhase());
        assertEquals(YELLOW, game.getCurrent());
        verify(stats, times(2)).notifyNewRoundReady();
        verify(gameContext, never()).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldEndAMatchWonWithBravo() throws Exception {
        final Connect4 game = playing(Connect4Opponent.HARD, Connect4End.GAMES_1, 11);
        put(game, RED, 1, 1, 1);
        put(game, YELLOW, 6, 5, 6);
        playAndLand(game, 1);
        steps(game, Connect4.SHOW + 0.05);
        assertEquals(Connect4.Phase.OVER, game.getPhase());
        assertTrue(game.isWonByThePlayer());
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldEndAMatchLostWithTheStatisticsAtOnce() throws Exception {
        final Connect4 game = playing(Connect4Opponent.EASY, Connect4End.GAMES_1, 13);
        put(game, YELLOW, 6, 6, 6);
        put(game, RED, 0, 0, 3);
        playAndLand(game, 2);
        // The computer wins at once
        steps(game, Connect4.THINK + 0.8);
        assertEquals(Connect4.Phase.SHOW, game.getPhase());
        assertEquals(YELLOW, game.getLastWinner());
        verify(stats).incrementNumberOfGoalsToReach();
        verify(stats, never()).incrementNumberOfGoalsReached();
        verify(gameContext.getSoundManager()).add("data/connect4/sounds/lose.wav");
        steps(game, Connect4.SHOW + 0.05);
        assertEquals(Connect4.Phase.OVER, game.getPhase());
        assertFalse(game.isWonByThePlayer());
        verify(gameContext, never()).playWinTransition(anyLong(), any());
        verify(gameContext).showRoundStats(any(), any());
    }

    @Test
    void shouldEndAFullGridWithADraw() throws Exception {
        final Connect4 game = playing(Connect4Opponent.DUO, Connect4End.ENDLESS, 15);
        fx(() -> {
            for (int column = 0; column < Connect4Board.COLUMNS; column++) {
                for (int row = 0; row < Connect4Board.ROWS; row++) {
                    if (column != 6 || row != 5) {
                        game.getBoard().drop(column, drawPattern(column, row));
                    }
                }
            }
            return null;
        });
        playAndLand(game, 6);
        assertEquals(Connect4.Phase.SHOW, game.getPhase());
        assertEquals(Connect4Board.EMPTY, game.getLastWinner());
        assertEquals(0, game.marks());
        assertEquals(0, game.score(RED) + game.score(YELLOW));
        // With two players, a game ended is a goal reached
        verify(stats).incrementNumberOfGoalsReached();
    }

    @Test
    void shouldLetTwoPlayersPlayInTurnAndEndWithBravo() throws Exception {
        final Connect4 game = playing(Connect4Opponent.DUO, Connect4End.GAMES_1, 17);
        // Red on column 0, yellow on column 1, by fixations, until red has 4 in a column
        for (int move = 0; move < 7; move++) {
            final int player = game.getCurrent();
            assertEquals(move % 2 == 0 ? RED : YELLOW, player);
            final int column = player == RED ? 0 : 1;
            fx(() -> {
                game.lookAt(column);
                return null;
            });
            steps(game, 1.05);
            fx(() -> {
                game.lookAway(column);
                return null;
            });
            steps(game, 0.8);
            if (move < 6) {
                assertEquals(Connect4.Phase.PLAY, game.getPhase());
            }
        }
        assertEquals(Connect4.Phase.SHOW, game.getPhase());
        assertEquals(RED, game.getLastWinner());
        steps(game, Connect4.SHOW + 0.05);
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldPutTheGridInTheMiddleInsideTheScreen() throws Exception {
        for (final double[] screen : new double[][]{{1920, 1080}, {1366, 768}, {1024, 768}, {800, 1000}}) {
            screen(screen[0], screen[1]);
            final Connect4 game = launched(Connect4Opponent.NORMAL, Connect4End.ENDLESS, 1);
            final double[] layout = game.layout();
            final String name = screen[0] + " x " + screen[1];
            assertEquals(layout[0], screen[0] - layout[2], 1, name);
            assertTrue(layout[3] + game.getCell() * 0.25 < screen[1], name);
            // The token waiting above the grid, under the band of the score
            assertTrue(layout[4] - game.getCell() * 0.5 >= screen[1] * 0.11 - 1, name);
            // A column is a large target
            assertTrue(game.zoneOf(3).getHeight() > screen[1] * 0.6, name);
            fx(() -> {
                game.dispose();
                return null;
            });
        }
    }

    @Test
    void shouldReplayTheSameComputer() throws Exception {
        final int[] columns = new int[2];
        for (int k = 0; k < 2; k++) {
            final Connect4 game = playing(Connect4Opponent.EASY, Connect4End.ENDLESS, 19);
            playAndLand(game, 3);
            columns[k] = game.getChosen();
        }
        assertEquals(columns[0], columns[1]);
    }

    @Test
    void shouldStopAfterDispose() throws Exception {
        final Connect4 game = playing(Connect4Opponent.NORMAL, Connect4End.ENDLESS, 2);
        fx(() -> {
            game.dispose();
            return null;
        });
        steps(game, 2);
        assertEquals(Connect4.Phase.OVER, game.getPhase());
        assertTrue(gameArea.getChildren().isEmpty());
        verify(gameContext.getGazeDeviceManager(), times(Connect4Board.COLUMNS)).removeEventFilter(any());
    }

    @Test
    void shouldGiveAllTheVariantsAndPlayTheOldOnes() {
        final Set<IGameVariant> variants = new Connect4GameVariantGenerator().getVariants();
        assertEquals(16, variants.size());
        final IGameVariant variant = new GroupedGameVariant(Connect4GameVariantGenerator.GROUP,
            Connect4Opponent.HARD.getKey(), Connect4End.GAMES_5.getKey(), null);
        assertTrue(variants.contains(variant));
        assertEquals(Connect4Opponent.HARD, Connect4GameLauncher.opponentOf(variant));
        assertEquals(Connect4End.GAMES_5, Connect4GameLauncher.endOf(variant));
        final IGameVariant old = IGameVariant.toGameVariant(
            "EnumGameVariant:net.gazeplay.games.connect4.Connect4GameVariant:AISTART");
        assertEquals(Connect4Opponent.EASY, Connect4GameLauncher.opponentOf(old));
        assertEquals(Connect4End.ENDLESS, Connect4GameLauncher.endOf(old));
    }
}

package net.gazeplay.games.ticTacToe;

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

import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * The Morpion (name code TicTacToe; reviewed on 10/11/2026, maintainer's rules): the rules of the grid, the 3 levels of
 * the computer (the hard one perfect but for its mistakes), a box chosen by a fixation of the length of the settings,
 * the turns, the ends of a game and of the match (no more freeze at a draw), two players, the layout, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class TicTacToeTest {

    private static final double STEP = 1.0 / 60;
    private static final int X = TicTacToe.CROSS;
    private static final int O = TicTacToe.NOUGHT;

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

    private TicTacToe launched(final TicTacToeOpponent opponent, final TicTacToeEnd end, final double seed)
        throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final TicTacToe game = new TicTacToe(gameContext, stats, opponent, end, seed);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private TicTacToe playing(final TicTacToeOpponent opponent, final TicTacToeEnd end, final double seed)
        throws Exception {
        final TicTacToe game = launched(opponent, end, seed);
        steps(game, 0.05);
        assertEquals(TicTacToe.Phase.PLAY, game.getPhase());
        return game;
    }

    private static void steps(final TicTacToe game, final double seconds) throws Exception {
        fx(() -> {
            for (double t = 0; t < seconds; t += STEP) {
                game.step(STEP);
            }
            return null;
        });
    }

    /**
     * Plays a box for the player to play, as at the end of a fixation, and waits for the mark to be drawn.
     */
    private static void playAndDraw(final TicTacToe game, final int box) throws Exception {
        fx(() -> {
            game.play(box);
            return null;
        });
        steps(game, TicTacToe.DRAW + 0.05);
    }

    private static void put(final TicTacToe game, final int player, final int... boxes) throws Exception {
        fx(() -> {
            for (final int box : boxes) {
                game.getBoard().play(box, player);
            }
            return null;
        });
    }

    @Test
    void shouldFindTheLinesOfThree() {
        final TicTacToeBoard board = new TicTacToeBoard();
        board.play(0, X);
        board.play(4, X);
        assertEquals(TicTacToeBoard.EMPTY, board.winner());
        assertTrue(board.wins(8, X));
        assertFalse(board.wins(8, O));
        board.play(8, X);
        assertArrayEquals(new int[]{0, 4, 8}, board.winningLine());
        assertThrows(IllegalArgumentException.class, () -> board.play(4, O));
        for (final int[] line : TicTacToeBoard.LINES) {
            final TicTacToeBoard one = new TicTacToeBoard();
            for (final int box : line) {
                one.play(box, O);
            }
            assertEquals(O, one.winner());
        }
    }

    /**
     * @return whether the computer never loses from this grid, whatever the other player does (the computer to play
     * when "computerToPlay")
     */
    private static boolean neverLoses(final TicTacToeBoard board, final TicTacToeComputer computer,
                                      final boolean computerToPlay) {
        if (board.winner() == X) {
            return false;
        }
        if (board.winner() == O || board.isFull()) {
            return true;
        }
        if (computerToPlay) {
            final int box = computer.choose(board, O, X);
            board.play(box, O);
            final boolean safe = neverLoses(board, computer, false);
            board.undo(box);
            return safe;
        }
        for (final int box : board.free()) {
            board.play(box, X);
            final boolean safe = neverLoses(board, computer, true);
            board.undo(box);
            if (!safe) {
                return false;
            }
        }
        return true;
    }

    @Test
    void shouldMakeThePerfectComputerNeverLose() {
        // Without its mistakes, the hard computer never loses, whoever starts and whatever the player does
        for (int seed = 1; seed <= 5; seed++) {
            final TicTacToeComputer perfect = new TicTacToeComputer(TicTacToeOpponent.HARD,
                new ReplayablePseudoRandom(seed), 0);
            assertTrue(neverLoses(new TicTacToeBoard(), perfect, false), "player first, seed " + seed);
            assertTrue(neverLoses(new TicTacToeBoard(), perfect, true), "computer first, seed " + seed);
        }
    }

    @Test
    void shouldWinAtOnceAndBlockAtTheRightLevels() {
        // O can win in 2; X threatens 8
        final TicTacToeBoard board = new TicTacToeBoard();
        board.play(0, O);
        board.play(1, O);
        board.play(4, X);
        board.play(6, X);
        board.play(5, X);
        for (final TicTacToeOpponent level : new TicTacToeOpponent[]{TicTacToeOpponent.EASY, TicTacToeOpponent.NORMAL,
            TicTacToeOpponent.HARD}) {
            for (int seed = 1; seed <= 10; seed++) {
                assertEquals(2, new TicTacToeComputer(level, new ReplayablePseudoRandom(seed)).choose(board, O, X),
                    level.name());
            }
        }
        // X threatens 2 (0 and 1), O cannot win: the medium and hard computers block
        final TicTacToeBoard threat = new TicTacToeBoard();
        threat.play(0, X);
        threat.play(1, X);
        threat.play(4, O);
        for (int seed = 1; seed <= 10; seed++) {
            assertEquals(2, new TicTacToeComputer(TicTacToeOpponent.NORMAL, new ReplayablePseudoRandom(seed))
                .choose(threat, O, X));
            assertEquals(2, new TicTacToeComputer(TicTacToeOpponent.HARD, new ReplayablePseudoRandom(seed), 0)
                .choose(threat, O, X));
        }
        // The medium computer takes the middle, else a corner
        final TicTacToeBoard empty = new TicTacToeBoard();
        assertEquals(4, new TicTacToeComputer(TicTacToeOpponent.NORMAL, new ReplayablePseudoRandom(1)).choose(empty,
            O, X));
        empty.play(4, X);
        final int corner = new TicTacToeComputer(TicTacToeOpponent.NORMAL, new ReplayablePseudoRandom(1))
            .choose(empty, O, X);
        assertTrue(corner == 0 || corner == 2 || corner == 6 || corner == 8);
    }

    /**
     * @return the winner of a game between two computers (0 for a draw), the first one playing the crosses and first
     */
    private static int match(final TicTacToeComputer crosses, final TicTacToeComputer noughts) {
        final TicTacToeBoard board = new TicTacToeBoard();
        int player = X;
        while (board.winner() == TicTacToeBoard.EMPTY && !board.isFull()) {
            board.play((player == X ? crosses : noughts).choose(board, player, player == X ? O : X), player);
            player = player == X ? O : X;
        }
        return board.winner();
    }

    @Test
    void shouldLetTheHardComputerBeBeatenRarely() {
        // Against a perfect player, over 200 games: the hard computer never wins, loses some games (its mistakes),
        // and draws most of them
        int lost = 0;
        int won = 0;
        for (int game = 0; game < 200; game++) {
            final ReplayablePseudoRandom random = new ReplayablePseudoRandom(game + 1);
            final TicTacToeComputer perfect = new TicTacToeComputer(TicTacToeOpponent.HARD, random, 0);
            final TicTacToeComputer hard = new TicTacToeComputer(TicTacToeOpponent.HARD, random);
            final boolean hardFirst = game % 2 == 0;
            final int winner = hardFirst ? match(hard, perfect) : match(perfect, hard);
            final int hardMark = hardFirst ? X : O;
            if (winner == hardMark) {
                won++;
            } else if (winner != TicTacToeBoard.EMPTY) {
                lost++;
            }
        }
        assertEquals(0, won);
        assertTrue(lost >= 10 && lost <= 120, "lost " + lost + " / 200");
    }

    @Test
    void shouldWaitForTheDelayOfTheSettings() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(2000);
        final TicTacToe game = launched(TicTacToeOpponent.NORMAL, TicTacToeEnd.ENDLESS, 3);
        fx(() -> {
            game.lookAt(4);
            return null;
        });
        steps(game, 1.5);
        assertEquals(TicTacToe.Phase.WAIT, game.getPhase());
        verify(stats, never()).notifyNewRoundReady();
        steps(game, 0.6);
        assertEquals(TicTacToe.Phase.PLAY, game.getPhase());
        verify(stats).notifyNewRoundReady();
    }

    @Test
    void shouldPutAMarkAfterAFixationOfTheLengthOfTheSettingsReadAtEachStep() throws Exception {
        final TicTacToe game = playing(TicTacToeOpponent.NORMAL, TicTacToeEnd.ENDLESS, 5);
        assertEquals(X, game.getCurrent());
        fx(() -> {
            game.lookAt(0);
            return null;
        });
        assertTrue(game.isGhostShown());
        assertTrue(game.isLit(0));
        steps(game, 0.5);
        assertEquals(0, game.getBoard().count());
        when(configuration.getFixationLength()).thenReturn(600);
        steps(game, 0.25);
        assertEquals(0, game.getBoard().count());
        steps(game, 0.07);
        assertEquals(X, game.getBoard().get(0));
        assertEquals(TicTacToe.Phase.DRAW, game.getPhase());
        verify(gameContext.getSoundManager()).add("data/noughtsandcrosses/sounds/place.wav");
        // Drawn, then the computer thinks and plays the middle
        steps(game, TicTacToe.DRAW + 0.05);
        assertEquals(TicTacToe.Phase.THINK, game.getPhase());
        steps(game, TicTacToe.THINK + TicTacToe.DRAW + 0.1);
        assertEquals(O, game.getBoard().get(4));
        assertEquals(TicTacToe.Phase.PLAY, game.getPhase());
    }

    @Test
    void shouldNotPlayATakenBoxNorDuringTheTurnOfTheComputer() throws Exception {
        final TicTacToe game = playing(TicTacToeOpponent.EASY, TicTacToeEnd.ENDLESS, 7);
        put(game, O, 0);
        fx(() -> {
            game.lookAt(0);
            return null;
        });
        steps(game, 1.2);
        assertEquals(1, game.getBoard().count());
        assertFalse(game.isGhostShown());
        assertFalse(game.isLit(0));
        playAndDraw(game, 4);
        assertEquals(TicTacToe.Phase.THINK, game.getPhase());
        // A fixation during the turn of the computer puts nothing
        fx(() -> {
            game.lookAt(8);
            return null;
        });
        steps(game, TicTacToe.THINK * 0.8);
        assertEquals(2, game.getBoard().count());
        assertFalse(game.isLit(8));
    }

    /**
     * The former game froze at a draw against the computer when the player put the ninth mark.
     */
    @Test
    void shouldEndAFullGridWithADrawAgainstTheComputer() throws Exception {
        final TicTacToe game = playing(TicTacToeOpponent.HARD, TicTacToeEnd.ENDLESS, 9);
        // X O X / X O O / O X . : the player puts the ninth mark
        put(game, X, 0, 2, 3, 7);
        put(game, O, 1, 4, 5, 6);
        playAndDraw(game, 8);
        assertEquals(TicTacToe.Phase.SHOW, game.getPhase());
        assertEquals(TicTacToeBoard.EMPTY, game.getLastWinner());
        assertFalse(game.hasLine());
        verify(stats).incrementNumberOfGoalsToReach();
        verify(stats, never()).incrementNumberOfGoalsReached();
        // The next game: the computer first
        steps(game, TicTacToe.SHOW + 0.05);
        assertEquals(TicTacToe.Phase.THINK, game.getPhase());
        assertEquals(O, game.getCurrent());
        assertEquals(0, game.getBoard().count());
    }

    @Test
    void shouldBarTheLineCountTheGameAndEndAMatchWonWithBravo() throws Exception {
        final TicTacToe game = playing(TicTacToeOpponent.NORMAL, TicTacToeEnd.GAMES_1, 11);
        put(game, X, 0, 1);
        put(game, O, 4, 8);
        playAndDraw(game, 2);
        assertEquals(TicTacToe.Phase.SHOW, game.getPhase());
        assertEquals(X, game.getLastWinner());
        assertTrue(game.hasLine());
        assertEquals(1, game.score(X));
        verify(stats).incrementNumberOfGoalsReached();
        verify(gameContext.getSoundManager()).add("data/noughtsandcrosses/sounds/win.wav");
        steps(game, TicTacToe.SHOW + 0.05);
        assertEquals(TicTacToe.Phase.OVER, game.getPhase());
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldEndAMatchLostWithTheStatisticsAtOnce() throws Exception {
        final TicTacToe game = playing(TicTacToeOpponent.EASY, TicTacToeEnd.GAMES_1, 13);
        put(game, O, 0, 1);
        put(game, X, 4);
        playAndDraw(game, 8);
        // The computer wins at once on 2
        steps(game, TicTacToe.THINK + TicTacToe.DRAW + 0.1);
        assertEquals(TicTacToe.Phase.SHOW, game.getPhase());
        assertEquals(O, game.getLastWinner());
        verify(gameContext.getSoundManager()).add("data/noughtsandcrosses/sounds/lose.wav");
        steps(game, TicTacToe.SHOW + 0.05);
        assertEquals(TicTacToe.Phase.OVER, game.getPhase());
        verify(gameContext, never()).playWinTransition(anyLong(), any());
        verify(gameContext).showRoundStats(any(), any());
    }

    @Test
    void shouldLetTwoPlayersPlayInTurnByFixations() throws Exception {
        final TicTacToe game = playing(TicTacToeOpponent.DUO, TicTacToeEnd.GAMES_1, 15);
        // Crosses on 0, 1, 2, noughts on 3, 4
        final int[] moves = {0, 3, 1, 4, 2};
        for (int move = 0; move < moves.length; move++) {
            assertEquals(move % 2 == 0 ? X : O, game.getCurrent());
            final int box = moves[move];
            fx(() -> {
                game.lookAt(box);
                return null;
            });
            steps(game, 1.05);
            fx(() -> {
                game.lookAway(box);
                return null;
            });
            steps(game, TicTacToe.DRAW + 0.05);
        }
        assertEquals(TicTacToe.Phase.SHOW, game.getPhase());
        assertEquals(X, game.getLastWinner());
        steps(game, TicTacToe.SHOW + 0.05);
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldPutASquareGridInTheMiddleInsideTheScreen() throws Exception {
        for (final double[] screen : new double[][]{{1920, 1080}, {1366, 768}, {1024, 768}, {800, 1000}}) {
            screen(screen[0], screen[1]);
            final TicTacToe game = launched(TicTacToeOpponent.NORMAL, TicTacToeEnd.ENDLESS, 1);
            final double[] layout = game.layout();
            final String name = screen[0] + " x " + screen[1];
            assertEquals(layout[0], screen[0] - layout[2], 1, name);
            assertEquals(layout[2] - layout[0], layout[3] - layout[1], 1e-6, name);
            assertTrue(layout[0] > 0 && layout[3] < screen[1], name);
            // The messages between the band of the score and the grid
            assertTrue(layout[4] > screen[1] * 0.11 && layout[4] < layout[1], name);
            assertTrue(game.getCell() > Math.min(screen[0], screen[1]) * 0.2, name);
            fx(() -> {
                game.dispose();
                return null;
            });
        }
    }

    @Test
    void shouldReplayTheSameComputer() throws Exception {
        final int[] boxes = new int[2];
        for (int k = 0; k < 2; k++) {
            final TicTacToe game = playing(TicTacToeOpponent.EASY, TicTacToeEnd.ENDLESS, 19);
            playAndDraw(game, 4);
            boxes[k] = game.getChosen();
        }
        assertEquals(boxes[0], boxes[1]);
    }

    @Test
    void shouldStopAfterDispose() throws Exception {
        final TicTacToe game = playing(TicTacToeOpponent.NORMAL, TicTacToeEnd.ENDLESS, 2);
        fx(() -> {
            game.dispose();
            return null;
        });
        steps(game, 2);
        assertEquals(TicTacToe.Phase.OVER, game.getPhase());
        assertTrue(gameArea.getChildren().isEmpty());
        verify(gameContext.getGazeDeviceManager(), times(9)).removeEventFilter(any());
    }

    @Test
    void shouldGiveAllTheVariantsAndPlayTheOldOnes() {
        final Set<IGameVariant> variants = new TicTacToeGameVariantGenerator().getVariants();
        assertEquals(16, variants.size());
        final IGameVariant variant = new GroupedGameVariant(TicTacToeGameVariantGenerator.GROUP,
            TicTacToeOpponent.HARD.getKey(), TicTacToeEnd.GAMES_5.getKey(), null);
        assertTrue(variants.contains(variant));
        assertEquals(TicTacToeOpponent.HARD, TicTacToeGameLauncher.opponentOf(variant));
        assertEquals(TicTacToeEnd.GAMES_5, TicTacToeGameLauncher.endOf(variant));
        final IGameVariant two = IGameVariant.toGameVariant(
            "EnumGameVariant:net.gazeplay.games.ticTacToe.TicTacToeGameVariant:P2");
        assertEquals(TicTacToeOpponent.DUO, TicTacToeGameLauncher.opponentOf(two));
        assertEquals(TicTacToeEnd.GAMES_1, TicTacToeGameLauncher.endOf(two));
        final IGameVariant computer = IGameVariant.toGameVariant(
            "EnumGameVariant:net.gazeplay.games.ticTacToe.TicTacToeGameVariant:IA");
        assertEquals(TicTacToeOpponent.EASY, TicTacToeGameLauncher.opponentOf(computer));
    }
}

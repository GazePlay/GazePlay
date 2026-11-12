package net.gazeplay.games.goosegame;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
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
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Jeu de l'oie" (reviewed on 12/11/2026, maintainer's rules): the way of a pawn (the end reached exactly), the special
 * squares with the classic and the simple rules, the turns (a person with the button, the computer by itself), the
 * end ("Bravo" only when a person wins), the variants and those of before the review.
 */
@ExtendWith(ApplicationExtension.class)
class GooseGameTest {

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
        when(configuration.getFixationLength()).thenReturn(500);
        when(configuration.getGameStartDelay()).thenReturn(0);
        when(configuration.getAnimationSpeedRatio()).thenReturn(1.0);
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private GooseGame launched(final GooseRules rules, final GoosePlayers players) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final GooseGame game = new GooseGame(gameContext, stats, rules, players, 9);
            game.setManual();
            game.launch();
            game.step(0.02);
            return game;
        });
    }

    /**
     * Runs the game until a person has to roll, or until the end.
     */
    private static void untilReady(final GooseGame game) {
        for (int i = 0; i < 2000; i++) {
            game.step(0.05);
            if (game.getPhase() == GooseGame.Phase.READY || game.getPhase() == GooseGame.Phase.OVER) {
                return;
            }
        }
        fail("never ready: " + game.getPhase());
    }

    private static GooseGame.Pawn pawn(final GooseGame game, final int index) {
        return game.getPawns().get(index);
    }

    @Test
    void shouldMoveBackPastTheEnd() {
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7), GooseBoard.way(0, 7));
        assertEquals(List.of(61, 62, 63, 62, 61), GooseBoard.way(60, 5));
        assertEquals(List.of(60, 61, 62, 63), GooseBoard.way(59, 4));
    }

    @Test
    void shouldKeepOnlyTheGeeseAndTheEndWithTheSimpleRules() {
        for (int square = 0; square <= GooseBoard.END; square++) {
            final GooseBoard.Kind simple = GooseBoard.kind(square, GooseRules.SIMPLE);
            assertTrue(simple == GooseBoard.Kind.NORMAL || simple == GooseBoard.Kind.GOOSE
                || simple == GooseBoard.Kind.END, square + " " + simple);
        }
        assertEquals(GooseBoard.Kind.BRIDGE, GooseBoard.kind(7, GooseRules.CLASSIC));
        assertEquals(GooseBoard.Kind.INN, GooseBoard.kind(19, GooseRules.CLASSIC));
        assertEquals(GooseBoard.Kind.WELL, GooseBoard.kind(31, GooseRules.CLASSIC));
        assertEquals(GooseBoard.Kind.PRISON, GooseBoard.kind(52, GooseRules.CLASSIC));
        assertEquals(GooseBoard.Kind.BACK, GooseBoard.kind(58, GooseRules.CLASSIC));
        assertEquals(12, GooseBoard.GEESE.size());
    }

    @Test
    void shouldRollAfterTheFixationOfTheSettingsThenMoveThePawn() throws Exception {
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.PLAYERS_2);
        fx(() -> {
            assertEquals(GooseGame.Phase.READY, game.getPhase());
            verify(stats).incrementNumberOfGoalsToReach();
            game.lookAtButton(true);
            for (int i = 0; i < 20; i++) {
                game.step(0.02);
            }
            assertEquals(GooseGame.Phase.READY, game.getPhase());
            for (int i = 0; i < 8; i++) {
                game.step(0.02);
            }
            assertEquals(GooseGame.Phase.ROLLING, game.getPhase());
            verify(stats).incrementNumberOfGoalsReached();
            final int thrown = game.getLastThrow();
            assertTrue(thrown >= 2 && thrown <= 12);
            game.lookAtButton(false);
            untilReady(game);
            // The second player's turn; the first pawn moved by the throw, or on further after a goose or the bridge
            assertEquals(1, game.getCurrent());
            assertTrue(pawn(game, 0).getSquare() >= thrown);
            return null;
        });
    }

    @Test
    void shouldMoveAgainOnAGoose() throws Exception {
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.PLAYERS_2);
        fx(() -> {
            pawn(game, 0).setSquare(1);
            // 1 + 4 = 5 (goose), 9 (goose), 13 (goose), 17
            game.rollWith(4);
            untilReady(game);
            assertEquals(17, pawn(game, 0).getSquare());
            return null;
        });
    }

    @Test
    void shouldTakeTheBridgeAndSendBackToTheStart() throws Exception {
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.PLAYERS_2);
        fx(() -> {
            pawn(game, 0).setSquare(3);
            game.rollWith(4);
            untilReady(game);
            assertEquals(GooseBoard.BRIDGE_END, pawn(game, 0).getSquare());
            pawn(game, 1).setSquare(55);
            game.rollWith(3);
            untilReady(game);
            assertEquals(GooseBoard.START, pawn(game, 1).getSquare());
            return null;
        });
    }

    @Test
    void shouldMakeAPawnSleepAtTheInn() throws Exception {
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.PLAYERS_2);
        fx(() -> {
            pawn(game, 0).setSquare(15);
            game.rollWith(4);
            untilReady(game);
            assertEquals(GooseBoard.INN, pawn(game, 0).getSquare());
            assertEquals(GooseBoard.INN_TURNS, pawn(game, 0).getAsleep());
            // The second player twice in a row, the first one asleep
            pawn(game, 1).setSquare(40);
            game.rollWith(2);
            untilReady(game);
            assertEquals(1, game.getCurrent());
            assertEquals(1, pawn(game, 0).getAsleep());
            game.rollWith(2);
            untilReady(game);
            assertEquals(1, game.getCurrent());
            game.rollWith(2);
            untilReady(game);
            assertEquals(0, game.getCurrent());
            return null;
        });
    }

    @Test
    void shouldFreeAPawnWaitingInTheWellWhenAnotherPasses() throws Exception {
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.PLAYERS_2);
        fx(() -> {
            pawn(game, 0).setSquare(27);
            game.rollWith(4);
            untilReady(game);
            assertEquals(GooseBoard.WELL, pawn(game, 0).getSquare());
            assertTrue(pawn(game, 0).isWaiting());
            pawn(game, 1).setSquare(29);
            game.rollWith(4);
            untilReady(game);
            assertFalse(pawn(game, 0).isWaiting());
            assertEquals(0, game.getCurrent());
            return null;
        });
    }

    @Test
    void shouldFreeAllThePawnsWhenAllWait() throws Exception {
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.PLAYERS_2);
        fx(() -> {
            pawn(game, 0).setSquare(27);
            game.rollWith(4);
            untilReady(game);
            pawn(game, 1).setSquare(50);
            game.rollWith(2);
            untilReady(game);
            assertEquals(GooseBoard.PRISON, pawn(game, 1).getSquare());
            assertFalse(pawn(game, 0).isWaiting());
            assertFalse(pawn(game, 1).isWaiting());
            verify(translator, atLeastOnce()).translate("GooseAllFree");
            return null;
        });
    }

    @Test
    void shouldIgnoreTheTrapsWithTheSimpleRules() throws Exception {
        final GooseGame game = launched(GooseRules.SIMPLE, GoosePlayers.PLAYERS_2);
        fx(() -> {
            pawn(game, 0).setSquare(27);
            game.rollWith(4);
            untilReady(game);
            assertEquals(GooseBoard.WELL, pawn(game, 0).getSquare());
            assertFalse(pawn(game, 0).isWaiting());
            return null;
        });
    }

    @Test
    void shouldReachTheEndExactlyOnlyAndSayBravoWhenAPersonWins() throws Exception {
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.VS_COMPUTER_1);
        fx(() -> {
            pawn(game, 0).setSquare(60);
            game.rollWith(5);
            untilReady(game);
            assertEquals(61, pawn(game, 0).getSquare());
            assertEquals(0, game.getCurrent());
            game.rollWith(2);
            untilReady(game);
            assertEquals(GooseGame.Phase.OVER, game.getPhase());
            assertSame(pawn(game, 0), game.getWinner());
            verify(gameContext).playWinTransition(anyLong(), any());
            return null;
        });
    }

    @Test
    void shouldLetTheComputerPlayAndWinWithoutBravo() throws Exception {
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.VS_COMPUTER_1);
        fx(() -> {
            // The person, then the computer rolls by itself; its pawn is put just as far from the end as its throw
            game.rollWith(2);
            for (int i = 0; i < 400 && game.getCurrent() == 0; i++) {
                game.step(0.05);
            }
            assertEquals(1, game.getCurrent());
            assertFalse(pawn(game, 1).isHuman());
            for (int i = 0; i < 400 && game.getPhase() != GooseGame.Phase.ROLLING; i++) {
                game.step(0.05);
            }
            assertEquals(GooseGame.Phase.ROLLING, game.getPhase());
            pawn(game, 1).setSquare(GooseBoard.END - game.getLastThrow());
            untilReady(game);
            assertEquals(GooseGame.Phase.OVER, game.getPhase());
            assertSame(pawn(game, 1), game.getWinner());
            verify(gameContext, never()).playWinTransition(anyLong(), any());
            verify(gameContext).showRoundStats(any(), any());
            return null;
        });
    }

    /**
     * Maintainer's remark of 12/11/2026: the Biboule was above the center of its base (its wings take the top of its
     * picture).
     */
    @Test
    void shouldCenterTheBodyOfTheBibouleOnItsBase() throws Exception {
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.VS_COMPUTER_3);
        for (final GooseGame.Pawn pawn : game.getPawns()) {
            final javafx.scene.shape.Circle base = (javafx.scene.shape.Circle) pawn.getNode().getChildren().get(0);
            final javafx.scene.image.ImageView view = (javafx.scene.image.ImageView) pawn.getNode().getChildren().get(1);
            final javafx.geometry.Bounds bounds = view.getBoundsInParent();
            assertEquals(base.getCenterX(), bounds.getMinX() + bounds.getWidth() / 2, 1);
            assertEquals(base.getCenterY(), bounds.getMinY() + bounds.getHeight() * GooseGame.BODY_CENTER, 1);
        }
    }

    @Test
    void shouldRemoveEverythingWhenDisposed() throws Exception {
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.PLAYERS_3);
        fx(() -> {
            game.rollWith(3);
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertEquals(GooseGame.Phase.OVER, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldReadTheVariantsAndThoseOfBeforeTheReview() {
        assertEquals(2 * 6, new GooseGameVariantGenerator().getVariants().size());
        final GooseGame grouped = GooseGameLauncher.create(gameContext,
            new GroupedGameVariant("GooseSimple", GooseGameVariantGenerator.BOARD, "GooseVsComputer3", null), stats, 1);
        assertEquals(GooseRules.SIMPLE, grouped.getRules());
        assertEquals(GoosePlayers.VS_COMPUTER_3, grouped.getPlayers());
        assertEquals(4, GoosePlayers.VS_COMPUTER_3.getPawns());
        assertEquals(1, GoosePlayers.VS_COMPUTER_3.getHumans());
        // "IntGameVariant:5" of before the review (5 persons): 4 persons, the classic rules
        final GooseGame former = GooseGameLauncher.create(gameContext, new IntGameVariant(5), stats, 1);
        assertEquals(GoosePlayers.PLAYERS_4, former.getPlayers());
        assertEquals(GooseRules.CLASSIC, former.getRules());
    }
}

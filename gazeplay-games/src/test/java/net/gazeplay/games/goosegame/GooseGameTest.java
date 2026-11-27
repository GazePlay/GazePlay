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
 * end ("Bravo" only when a person wins), the variants and those of before the review; since 27/11/2026 (tester's
 * feedback, point 65), the softened rules and the help of the dice.
 */
@ExtendWith(ApplicationExtension.class)
class GooseGameTest {

    /** The games launched by a test, closed after it (18/11/2026). */
    @org.junit.jupiter.api.extension.RegisterExtension
    final net.gazeplay.games.LaunchedGames launchedGames = new net.gazeplay.games.LaunchedGames();

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
            launchedGames.add(game).launch();
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
        // Point 65: no more arrow back to the start
        assertEquals(GooseBoard.Kind.NORMAL, GooseBoard.kind(58, GooseRules.CLASSIC));
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

    /**
     * Point 22 (23/11/2026): the "Roll" button coming back for another turn asks for the delay of the settings; looked
     * at, it rolls nothing while it runs, then after the fixation counted from its end.
     */
    @Test
    void shouldWaitForTheDelayWhenTheRollButtonComesBack() throws Exception {
        final GooseGame game = launched(GooseRules.SIMPLE, GoosePlayers.PLAYERS_2);
        fx(() -> {
            verify(gameContext, never()).onGameStarted();
            game.rollWith(3);
            when(gameContext.isSelectionBlocked()).thenReturn(true);
            untilReady(game);
            assertEquals(1, game.getCurrent());
            verify(gameContext).onGameStarted();
            game.lookAtButton(true);
            for (int i = 0; i < 100; i++) {
                game.step(0.02);
            }
            assertEquals(GooseGame.Phase.READY, game.getPhase(), "nothing rolled during the delay");
            when(gameContext.isSelectionBlocked()).thenReturn(false);
            for (int i = 0; i < 20; i++) {
                game.step(0.02);
            }
            assertEquals(GooseGame.Phase.READY, game.getPhase(), "the fixation counts from the end of the delay");
            for (int i = 0; i < 8; i++) {
                game.step(0.02);
            }
            assertEquals(GooseGame.Phase.ROLLING, game.getPhase());
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
    void shouldTakeTheBridgeAndNeverSendBackToTheStart() throws Exception {
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.PLAYERS_2);
        fx(() -> {
            pawn(game, 0).setSquare(3);
            game.rollWith(4);
            untilReady(game);
            assertEquals(GooseBoard.BRIDGE_END, pawn(game, 0).getSquare());
            pawn(game, 1).setSquare(55);
            game.rollWith(3);
            untilReady(game);
            assertEquals(58, pawn(game, 1).getSquare(), "58 is an ordinary square (point 65)");
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
            // One turn only (point 65; two before)
            assertEquals(1, pawn(game, 0).getAsleep());
            // The second player twice in a row, the first one missing one turn
            pawn(game, 1).setSquare(40);
            game.rollWith(2);
            untilReady(game);
            assertEquals(1, game.getCurrent());
            assertEquals(0, pawn(game, 0).getAsleep());
            game.rollWith(2);
            untilReady(game);
            assertEquals(0, game.getCurrent());
            return null;
        });
    }

    /**
     * Point 65: the well and the prison make a pawn miss one turn (before, it waited until another pawn came).
     */
    @Test
    void shouldMissOneTurnInTheWellAndThePrison() throws Exception {
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.PLAYERS_2);
        fx(() -> {
            pawn(game, 0).setSquare(27);
            game.rollWith(4);
            untilReady(game);
            assertEquals(GooseBoard.WELL, pawn(game, 0).getSquare());
            assertEquals(1, pawn(game, 0).getAsleep());
            pawn(game, 1).setSquare(50);
            game.rollWith(2);
            untilReady(game);
            assertEquals(GooseBoard.PRISON, pawn(game, 1).getSquare());
            // Both miss one turn: the first one is free first, then the second one
            assertEquals(0, game.getCurrent());
            assertEquals(0, pawn(game, 0).getAsleep());
            assertEquals(0, pawn(game, 1).getAsleep());
            return null;
        });
        verify(translator, atLeastOnce()).translate("GooseWell");
        verify(translator, atLeastOnce()).translate("GoosePrison");
    }

    /**
     * Point 65: the third throw of a person that can reach the end exactly does it, with any players.
     */
    @Test
    void shouldReachTheEndOnTheThirdTry() throws Exception {
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.PLAYERS_2);
        fx(() -> {
            pawn(game, 0).setSquare(57);
            assertEquals(0, game.helpedThrow(pawn(game, 0)), "first try");
            assertEquals(0, game.helpedThrow(pawn(game, 0)), "second try");
            assertEquals(6, game.helpedThrow(pawn(game, 0)), "third try: the throw to the end");
            pawn(game, 0).setSquare(30);
            assertEquals(0, game.helpedThrow(pawn(game, 0)), "far from the end, between persons: no help");
            pawn(game, 0).setSquare(62);
            assertEquals(0, game.helpedThrow(pawn(game, 0)), "1 square cannot be thrown with two dice");
            return null;
        });
    }

    /**
     * Point 65: against the computer, when one of its pawns is ahead, the throw of the person is helped: a goose or the
     * bridge, else 12 or 11 without a trap; the computer itself is never helped.
     */
    @Test
    void shouldHelpThePersonWhenTheComputerIsAhead() throws Exception {
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.VS_COMPUTER_1);
        fx(() -> {
            pawn(game, 1).setSquare(20);
            pawn(game, 0).setSquare(10);
            // From 10: the farthest goose is 18 (8); the bridge is behind
            assertEquals(8, game.helpedThrow(pawn(game, 0)));
            pawn(game, 0).setSquare(0);
            // From 0: geese at 5 and 9, the bridge at 7: the farthest, 9
            assertEquals(9, game.helpedThrow(pawn(game, 0)));
            pawn(game, 1).setSquare(5);
            assertEquals(9, game.helpedThrow(pawn(game, 0)));
            pawn(game, 0).setSquare(10);
            assertEquals(0, game.helpedThrow(pawn(game, 0)), "the person ahead: no help");
            assertEquals(0, game.helpedThrow(pawn(game, 1)), "never for the computer");
            pawn(game, 1).setSquare(60);
            pawn(game, 0).setSquare(55);
            assertEquals(8, game.helpedThrow(pawn(game, 0)), "the end, in reach");
            return null;
        });
    }

    @Test
    void shouldRollTheHelpedThrow() throws Exception {
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.VS_COMPUTER_1);
        fx(() -> {
            pawn(game, 1).setSquare(20);
            pawn(game, 0).setSquare(10);
            game.lookAtButton(true);
            for (int i = 0; i < 30 && game.getPhase() == GooseGame.Phase.READY; i++) {
                game.step(0.02);
            }
            assertEquals(GooseGame.Phase.ROLLING, game.getPhase());
            assertEquals(8, game.getLastThrow());
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
            assertEquals(0, pawn(game, 0).getAsleep());
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

    /**
     * Point 64: the pawn whose turn it is stands in a halo that follows it, the "Roll" button has its halo while it
     * waits, the pawns are bigger than the squares (1.8 squares).
     */
    @Test
    void shouldShowAHaloOnThePawnWhoseTurnItIsAndOnTheButton() throws Exception {
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.PLAYERS_2);
        fx(() -> {
            assertEquals(GooseGame.Phase.READY, game.getPhase());
            assertTrue(game.getHalo().isVisible());
            assertEquals(pawn(game, 0).getNode().getLayoutX(), game.getHalo().getCenterX(), 1e-9);
            assertEquals(pawn(game, 0).getNode().getLayoutY(), game.getHalo().getCenterY(), 1e-9);
            assertTrue(game.getButtonHalo().isVisible());
            final double before = game.getHalo().getScaleX();
            game.step(GooseGame.PULSE / 2);
            assertNotEquals(before, game.getHalo().getScaleX(), "it pulses");
            game.rollWith(3);
            assertFalse(game.getButtonHalo().isVisible(), "the button is gone, its halo too");
            // During the move, the halo follows the pawn
            for (int i = 0; i < 40 && game.getPhase() != GooseGame.Phase.MOVING; i++) {
                game.step(0.05);
            }
            game.step(0.1);
            assertEquals(pawn(game, 0).getNode().getLayoutX(), game.getHalo().getCenterX(), 1e-9);
            untilReady(game);
            assertEquals(1, game.getCurrent());
            assertEquals(pawn(game, 1).getNode().getLayoutX(), game.getHalo().getCenterX(), 1e-9);
            final javafx.scene.image.ImageView view = (javafx.scene.image.ImageView) pawn(game, 0).getNode()
                .getChildren().get(1);
            assertEquals(game.getPawnSize(), view.getFitWidth(), 1e-9);
            return null;
        });
    }

    /**
     * Point 64: the simple rules have their own board, without the special squares that do nothing.
     */
    @Test
    void shouldShowTheBoardOfTheRules() throws Exception {
        for (final GooseRules rules : GooseRules.values()) {
            launched(rules, GoosePlayers.VS_COMPUTER_1);
            final String url = fx(() -> ((javafx.scene.Group) gameArea.getChildren().getFirst()).getChildren().stream()
                .filter(javafx.scene.image.ImageView.class::isInstance)
                .map(node -> ((javafx.scene.image.ImageView) node).getImage().getUrl()).findFirst().orElseThrow());
            assertEquals(rules == GooseRules.SIMPLE, url.endsWith("gooseboard-simple.png"), rules + ": " + url);
        }
    }

    private GooseGame launchedChoosing(final GoosePlayers players) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final GooseGame game = new GooseGame(gameContext, stats, GooseRules.CLASSIC, players, 9);
            game.setManual();
            game.setChooseColors(true);
            launchedGames.add(game).launch();
            game.step(0.02);
            return game;
        });
    }

    /**
     * Point 66: the person chooses the color of their Biboule by a fixation on its card, after the delay of the
     * settings; the computer takes the colors left, in their order.
     */
    @Test
    void shouldLetThePersonChooseTheColorOfTheirBiboule() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(400);
        final GooseGame game = launchedChoosing(GoosePlayers.VS_COMPUTER_2);
        fx(() -> {
            assertEquals(GooseGame.Phase.CHOOSE, game.getPhase());
            assertTrue(game.getPawns().isEmpty());
            verify(translator, atLeastOnce()).translate("GooseChoose");
            game.lookAtCard(2);
            for (int i = 0; i < 20; i++) {
                game.step(0.02);
            }
            assertEquals(GooseGame.Phase.CHOOSE, game.getPhase(), "nothing chosen during the delay");
            for (int i = 0; i < 30; i++) {
                game.step(0.02);
            }
            assertNotEquals(GooseGame.Phase.CHOOSE, game.getPhase());
            assertEquals(3, game.getPawns().size());
            assertEquals(2, pawn(game, 0).getColorIndex(), "the green Biboule chosen");
            assertTrue(pawn(game, 0).isHuman());
            assertEquals(0, pawn(game, 1).getColorIndex(), "the computer: the colors left");
            assertEquals(1, pawn(game, 2).getColorIndex());
            return null;
        });
    }

    /**
     * Point 66: with several persons, each chooses in turn; a color taken cannot be chosen again.
     */
    @Test
    void shouldLetEachPersonChooseInTurn() throws Exception {
        final GooseGame game = launchedChoosing(GoosePlayers.PLAYERS_2);
        fx(() -> {
            game.chooseColor(3);
            assertEquals(GooseGame.Phase.CHOOSE, game.getPhase(), "the second person chooses");
            verify(translator, atLeastOnce()).translate("GoosePlayerChooses");
            game.chooseColor(3);
            assertEquals(GooseGame.Phase.CHOOSE, game.getPhase(), "taken");
            game.chooseColor(0);
            assertEquals(2, game.getPawns().size());
            assertEquals(3, pawn(game, 0).getColorIndex());
            assertEquals(0, pawn(game, 1).getColorIndex());
            untilReady(game);
            assertEquals(GooseGame.Phase.READY, game.getPhase());
            return null;
        });
    }

    /**
     * Point 66: the pawn moves slowly (0.5 s a square); once thrown, the dice are drawn flat and a pip fills at each
     * square; a goose counts the same throw again.
     */
    @Test
    void shouldFillAPipOfTheDiceAtEachSquare() throws Exception {
        assertEquals(0.5, GooseGame.HOP, 1e-9);
        final GooseGame game = launched(GooseRules.CLASSIC, GoosePlayers.PLAYERS_2);
        fx(() -> {
            pawn(game, 0).setSquare(10);
            game.rollWith(7);
            assertArrayEquals(new int[]{6, 1}, game.getFaces());
            for (int i = 0; i < 100 && game.getPhase() != GooseGame.Phase.MOVING; i++) {
                game.step(0.05);
            }
            assertTrue(game.isFlatDiceShown());
            assertEquals(7, game.getPips().size());
            assertEquals(0, game.getPipsFilled());
            for (int i = 0; i < 9; i++) {
                game.step(0.05);
            }
            assertEquals(0, game.getPipsFilled(), "not yet a square");
            game.step(0.06);
            assertEquals(1, game.getPipsFilled(), "one square after half a second");
            assertEquals(1, game.getPips().stream().filter(pip -> !pip.getFill().equals(game.getPips().get(6)
                .getFill())).count());
            untilReady(game);
            assertEquals(17, pawn(game, 0).getSquare());
            // A goose: the same throw counted again
            pawn(game, 1).setSquare(1);
            game.rollWith(4);
            for (int i = 0; i < 100 && game.getPhase() != GooseGame.Phase.MOVING; i++) {
                game.step(0.05);
            }
            for (int i = 0; i < 200 && pawn(game, 1).getSquare() != 5; i++) {
                game.step(0.05);
            }
            assertEquals(4, game.getPipsFilled());
            for (int i = 0; i < 200 && game.getPipsFilled() != 1; i++) {
                game.step(0.05);
            }
            assertEquals(1, game.getPipsFilled(), "counting again after the goose");
            untilReady(game);
            assertFalse(game.isFlatDiceShown(), "gone at the next turn");
            return null;
        });
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

package net.gazeplay.games.rockPaperScissors;

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

import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Pierre papier ciseaux" (Rock Paper Scissors; reviewed on 10/11/2026, maintainer's rules): the rule, a sign chosen by
 * a fixation of the length of the settings, a round against the computer (count, both signs shown), the computer able
 * to win the match, the mode learning the rule (a wrong choice greyed and explained), the ends, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class RockPaperScissorsTest {

    /** The games launched by a test, closed after it (18/11/2026). */
    @org.junit.jupiter.api.extension.RegisterExtension
    final net.gazeplay.games.LaunchedGames launchedGames = new net.gazeplay.games.LaunchedGames();

    private static final double STEP = 1.0 / 60;

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
        when(configuration.getAnimationSpeedRatio()).thenReturn(1.0);
        when(configuration.getGameStartDelay()).thenReturn(0);
        when(configuration.getFixationLength()).thenReturn(1000);
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1920, 1080));
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private RockPaperScissorsGame playing(final RockPaperScissorsMode mode, final RockPaperScissorsEnd end,
                                          final double seed) throws Exception {
        final RockPaperScissorsGame game = fx(() -> {
            gameArea.getChildren().clear();
            final RockPaperScissorsGame created = new RockPaperScissorsGame(gameContext, stats, mode, end, seed);
            created.setManual();
            launchedGames.add(created).launch();
            return created;
        });
        steps(game, 0.05);
        assertEquals(RockPaperScissorsGame.Phase.PLAY, game.getPhase());
        return game;
    }

    private static void steps(final RockPaperScissorsGame game, final double seconds) throws Exception {
        fx(() -> {
            for (double t = 0; t < seconds; t += STEP) {
                game.step(STEP);
            }
            return null;
        });
    }

    private static void choose(final RockPaperScissorsGame game, final HandSign sign) throws Exception {
        fx(() -> {
            game.choose(sign);
            return null;
        });
    }

    /**
     * The result of a round stays (26/11/2026: it went by after a fixed time), then the arrow is chosen.
     */
    private static void next(final RockPaperScissorsGame game) throws Exception {
        steps(game, 3);
        assertEquals(RockPaperScissorsGame.Phase.REVEAL, game.getPhase(), "the result stays");
        assertNotNull(game.getNextButton());
        // "Next round" written under the arrow
        assertTrue(game.getNextLabel().getBoundsInParent().getMinY() > game.getNextButton().getBoundsInParent()
            .getMaxY());
        assertTrue(game.getNextLabel().getBoundsInParent().getMaxX() <= 1920);
        // The arrow centered above it
        assertEquals(game.getNextLabel().getBoundsInParent().getCenterX(),
            game.getNextButton().getLayoutX() + game.getNextButton().getBoundsInLocal().getWidth() / 2, 2);
        fx(() -> {
            game.nextRound();
            return null;
        });
        assertNull(game.getNextButton());
    }

    /**
     * Plays a round against the computer with a sign, until the next round (or the end).
     */
    private static int round(final RockPaperScissorsGame game, final HandSign sign) throws Exception {
        choose(game, sign);
        steps(game, 3 * RockPaperScissorsGame.WORD + 0.05);
        assertEquals(RockPaperScissorsGame.Phase.REVEAL, game.getPhase());
        final int result = sign.fight(game.getComputerSign());
        next(game);
        return result;
    }

    @Test
    void shouldFollowTheRule() {
        assertEquals(1, HandSign.ROCK.fight(HandSign.SCISSORS));
        assertEquals(1, HandSign.PAPER.fight(HandSign.ROCK));
        assertEquals(1, HandSign.SCISSORS.fight(HandSign.PAPER));
        assertEquals(-1, HandSign.ROCK.fight(HandSign.PAPER));
        assertEquals(0, HandSign.PAPER.fight(HandSign.PAPER));
        for (final HandSign sign : HandSign.values()) {
            assertEquals(1, sign.winner().fight(sign));
            assertEquals(1, sign.fight(sign.beaten()));
        }
    }

    @Test
    void shouldChooseASignAfterAFixationOfTheLengthOfTheSettingsReadAtEachStep() throws Exception {
        final RockPaperScissorsGame game = playing(RockPaperScissorsMode.VERSUS, RockPaperScissorsEnd.ENDLESS, 3);
        fx(() -> {
            game.lookAt(HandSign.PAPER);
            return null;
        });
        steps(game, 0.5);
        assertNull(game.getPlayerSign());
        when(configuration.getFixationLength()).thenReturn(600);
        steps(game, 0.25);
        assertNull(game.getPlayerSign());
        steps(game, 0.07);
        assertEquals(HandSign.PAPER, game.getPlayerSign());
        assertEquals(RockPaperScissorsGame.Phase.COUNT, game.getPhase());
    }

    /**
     * Point 22 (23/11/2026): the pointer left on the card just chosen used to play it again at once in the next round;
     * each round after the first asks for the delay of the settings, during which nothing is chosen.
     */
    @Test
    void shouldWaitForTheDelayAtEachNewRound() throws Exception {
        final RockPaperScissorsGame game = playing(RockPaperScissorsMode.VERSUS, RockPaperScissorsEnd.ENDLESS, 3);
        verify(gameContext, never()).onGameStarted();
        fx(() -> {
            game.lookAt(HandSign.PAPER);
            return null;
        });
        steps(game, 1.05);
        assertEquals(RockPaperScissorsGame.Phase.COUNT, game.getPhase());
        steps(game, 3 * RockPaperScissorsGame.WORD + 0.05);
        when(gameContext.isSelectionBlocked()).thenReturn(true);
        next(game);
        assertEquals(RockPaperScissorsGame.Phase.PLAY, game.getPhase());
        verify(gameContext).onGameStarted();
        steps(game, 2);
        assertNull(game.getPlayerSign(), "nothing chosen during the delay");
        when(gameContext.isSelectionBlocked()).thenReturn(false);
        steps(game, 0.9);
        assertNull(game.getPlayerSign(), "the fixation counts from the end of the delay");
        steps(game, 0.15);
        assertEquals(HandSign.PAPER, game.getPlayerSign());
    }

    /**
     * Tester's feedback of 24/11/2026, point 71: what a hand did is seen without the sound nor the text: a wrong choice
     * is crossed out in red and shakes, a right one checked and circled in green; against the computer the winning hand
     * checked, the losing one crossed out, a draw "=".
     */
    @Test
    void shouldMarkTheHandsWithACrossOrACheck() throws Exception {
        final RockPaperScissorsGame learn = playing(RockPaperScissorsMode.LEARN, RockPaperScissorsEnd.ENDLESS, 3);
        final HandSign shown = learn.getComputerSign();
        choose(learn, shown.beaten());
        assertTrue(hasCross(learn.getPlayerSlot()), "a wrong choice crossed out");
        steps(learn, 0.1);
        assertNotEquals(0, learn.getPlayerSlot().getTranslateX(), 1e-9, "and shaking");
        steps(learn, RockPaperScissorsGame.EXPLAIN);
        assertFalse(hasCross(learn.getPlayerSlot()), "gone with the explanation");
        choose(learn, shown.winner());
        assertTrue(hasCheck(learn.getPlayerSlot()), "a right choice checked");

        final RockPaperScissorsGame versus = playing(RockPaperScissorsMode.VERSUS, RockPaperScissorsEnd.ENDLESS, 5);
        choose(versus, HandSign.ROCK);
        // "Rock, paper, scissors!" between both hands too
        final javafx.geometry.Bounds counted = versus.getMessage().getChildren().getFirst().getBoundsInParent();
        assertEquals(versus.getPlayerSlot().getLayoutY(), counted.getCenterY(), 1);
        steps(versus, 3 * RockPaperScissorsGame.WORD + 0.05);
        final int result = HandSign.ROCK.fight(versus.getComputerSign());
        // The result between both hands, at their height
        final javafx.geometry.Bounds said = versus.getMessage().getChildren().getFirst().getBoundsInParent();
        assertEquals(versus.getPlayerSlot().getLayoutY(), said.getCenterY(), 1);
        assertTrue(said.getMinX() > versus.getPlayerSlot().getLayoutX()
            && said.getMaxX() < versus.getComputerSlot().getLayoutX());
        assertEquals(1920 / 2.0, said.getCenterX(), 1);
        if (result > 0) {
            assertTrue(hasCheck(versus.getPlayerSlot()) && hasCross(versus.getComputerSlot()));
        } else if (result < 0) {
            assertTrue(hasCross(versus.getPlayerSlot()) && hasCheck(versus.getComputerSlot()));
        } else {
            assertFalse(hasCross(versus.getPlayerSlot()) || hasCheck(versus.getPlayerSlot()));
            assertTrue(versus.getPlayerSlot().getChildren().size() > 1, "a draw badge");
        }
    }

    private static boolean hasCross(final javafx.scene.Group slot) {
        return slot.getChildren().stream().anyMatch(node -> node instanceof javafx.scene.shape.Line line
            && javafx.scene.paint.Color.rgb(214, 48, 48).equals(line.getStroke()));
    }

    private static boolean hasCheck(final javafx.scene.Group slot) {
        return slot.getChildren().stream().anyMatch(node -> node instanceof javafx.scene.shape.Circle circle
            && javafx.scene.paint.Color.web(net.gazeplay.components.GameStyle.SUCCESS_COLOR).equals(circle.getStroke()));
    }

    @Test
    void shouldCountThenShowBothSignsAndScoreTheRound() throws Exception {
        final RockPaperScissorsGame game = playing(RockPaperScissorsMode.VERSUS, RockPaperScissorsEnd.ENDLESS, 5);
        choose(game, HandSign.ROCK);
        assertNull(game.getComputerSign());
        // Nothing else can be chosen during the count
        choose(game, HandSign.PAPER);
        assertEquals(HandSign.ROCK, game.getPlayerSign());
        steps(game, 3 * RockPaperScissorsGame.WORD - 0.1);
        assertEquals(RockPaperScissorsGame.Phase.COUNT, game.getPhase());
        verify(gameContext.getSoundManager(), times(3)).add("data/rockPaperScissors/sounds/tick.wav");
        steps(game, 0.15);
        assertEquals(RockPaperScissorsGame.Phase.REVEAL, game.getPhase());
        assertNotNull(game.getComputerSign());
        final int result = HandSign.ROCK.fight(game.getComputerSign());
        assertEquals(result > 0 ? 1 : 0, game.getPlayerWins());
        assertEquals(result < 0 ? 1 : 0, game.getComputerWins());
        verify(stats).incrementNumberOfGoalsToReach();
        verify(stats, times(result > 0 ? 1 : 0)).incrementNumberOfGoalsReached();
        next(game);
        assertEquals(RockPaperScissorsGame.Phase.PLAY, game.getPhase());
        assertNull(game.getComputerSign());
    }

    @Test
    void shouldLetTheComputerWinTheMatchWithoutBravo() throws Exception {
        // Always the same sign, until one of them has 2 rounds won
        for (int seed = 1; seed <= 40; seed++) {
            final RockPaperScissorsGame game = playing(RockPaperScissorsMode.VERSUS, RockPaperScissorsEnd.WINS_2,
                seed);
            while (game.getPhase() == RockPaperScissorsGame.Phase.PLAY) {
                round(game, HandSign.ROCK);
            }
            if (game.getComputerWins() == 2) {
                assertEquals(RockPaperScissorsGame.Phase.SHOW, game.getPhase());
                steps(game, RockPaperScissorsGame.SHOW + 0.05);
                assertEquals(RockPaperScissorsGame.Phase.OVER, game.getPhase());
                assertFalse(game.isWon());
                verify(gameContext, never()).playWinTransition(anyLong(), any());
                verify(gameContext).showRoundStats(any(), any());
                return;
            }
            fx(() -> {
                game.dispose();
                return null;
            });
            reset(gameContext.getSoundManager());
        }
        fail("the computer never won a match in 40 games");
    }

    @Test
    void shouldEndWithBravoWhenThePlayerWinsTheMatch() throws Exception {
        for (int seed = 1; seed <= 40; seed++) {
            final RockPaperScissorsGame game = playing(RockPaperScissorsMode.VERSUS, RockPaperScissorsEnd.WINS_2,
                seed);
            while (game.getPhase() == RockPaperScissorsGame.Phase.PLAY) {
                round(game, HandSign.PAPER);
            }
            if (game.getPlayerWins() == 2) {
                steps(game, RockPaperScissorsGame.SHOW + 0.05);
                assertTrue(game.isWon());
                verify(gameContext).playWinTransition(anyLong(), any());
                return;
            }
            fx(() -> {
                game.dispose();
                return null;
            });
        }
        fail("the player never won a match in 40 games");
    }

    @Test
    void shouldGreyAndExplainAWrongChoiceWhenLearningTheRule() throws Exception {
        final RockPaperScissorsGame game = playing(RockPaperScissorsMode.LEARN, RockPaperScissorsEnd.ANSWERS_5, 7);
        final HandSign shown = game.getComputerSign();
        assertNotNull(shown);
        // A wrong choice: the sign it beats
        choose(game, shown.beaten());
        assertTrue(game.isGreyed(shown.beaten()));
        assertEquals(RockPaperScissorsGame.Phase.PLAY, game.getPhase());
        verify(gameContext.getSoundManager()).add("data/rockPaperScissors/sounds/lose.wav");
        // The same sign: a draw, also wrong
        choose(game, shown);
        assertTrue(game.isGreyed(shown));
        // The right one: counted, but not as a goal reached at the first try
        choose(game, shown.winner());
        assertEquals(1, game.getRightAnswers());
        verify(stats, never()).incrementNumberOfGoalsReached();
        // The two wrong choices are mistakes (17/11/2026: they were not counted), counted in this mode
        verify(stats, times(2)).incrementNumberOfMistakes();
        verify(stats).countMistakes();
        next(game);
        assertEquals(RockPaperScissorsGame.Phase.PLAY, game.getPhase());
        // At the first try: a goal reached
        choose(game, game.getComputerSign().winner());
        verify(stats).incrementNumberOfGoalsReached();
    }

    @Test
    void shouldEndWithBravoAfterTheRightAnswers() throws Exception {
        final RockPaperScissorsGame game = playing(RockPaperScissorsMode.LEARN, RockPaperScissorsEnd.ANSWERS_5, 9);
        for (int answer = 0; answer < 5; answer++) {
            choose(game, game.getComputerSign().winner());
            next(game);
        }
        assertEquals(RockPaperScissorsGame.Phase.OVER, game.getPhase());
        assertTrue(game.isWon());
        verify(gameContext).playWinTransition(anyLong(), any());
        verify(stats, times(5)).incrementNumberOfGoalsReached();
    }

    @Test
    void shouldPutTheThreeCardsSideBySideInsideTheScreen() throws Exception {
        final RockPaperScissorsGame game = playing(RockPaperScissorsMode.VERSUS, RockPaperScissorsEnd.ENDLESS, 1);
        double right = 0;
        for (final HandSign sign : HandSign.values()) {
            final double[] bounds = game.cardBounds(sign);
            assertTrue(bounds[0] > right && bounds[2] < 1920 && bounds[3] < 1080, sign.name());
            assertTrue(bounds[1] > 1080 * 0.5, sign.name());
            right = bounds[2];
        }
    }

    @Test
    void shouldStopAfterDispose() throws Exception {
        final RockPaperScissorsGame game = playing(RockPaperScissorsMode.VERSUS, RockPaperScissorsEnd.ENDLESS, 2);
        fx(() -> {
            game.dispose();
            return null;
        });
        steps(game, 2);
        assertEquals(RockPaperScissorsGame.Phase.OVER, game.getPhase());
        assertTrue(gameArea.getChildren().isEmpty());
        verify(gameContext.getGazeDeviceManager(), times(3)).removeEventFilter(any());
    }

    @Test
    void shouldGiveTheVariantsOfEachModeAndPlayTheOldOnes() {
        final Set<IGameVariant> variants = new RockPaperScissorsGameVariantGenerator().getVariants();
        assertEquals(7, variants.size());
        final IGameVariant variant = new GroupedGameVariant(RockPaperScissorsGameVariantGenerator.GROUP,
            RockPaperScissorsMode.LEARN.getKey(), RockPaperScissorsEnd.ANSWERS_10.getKey(), null);
        assertTrue(variants.contains(variant));
        assertFalse(variants.contains(new GroupedGameVariant(RockPaperScissorsGameVariantGenerator.GROUP,
            RockPaperScissorsMode.LEARN.getKey(), RockPaperScissorsEnd.WINS_3.getKey(), null)));
        assertEquals(RockPaperScissorsMode.LEARN, RockPaperScissorsLauncher.modeOf(variant));
        assertEquals(RockPaperScissorsEnd.ANSWERS_10, RockPaperScissorsLauncher.endOf(variant));
        final IGameVariant old = IGameVariant.toGameVariant(
            "EnumGameVariant:net.gazeplay.games.rockPaperScissors.RockPaperScissorsGameVariant:HIDE_BO5");
        assertEquals(RockPaperScissorsMode.VERSUS, RockPaperScissorsLauncher.modeOf(old));
        assertEquals(RockPaperScissorsEnd.WINS_3, RockPaperScissorsLauncher.endOf(old));
    }
}

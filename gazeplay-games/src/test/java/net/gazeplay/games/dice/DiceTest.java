package net.gazeplay.games.dice;

import javafx.event.Event;
import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
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
 * "Dés" (reviewed on 12/11/2026, maintainer's rules): counting (the right total among close wrong ones, looking again
 * or 3 lives, the end) and throwing (all the dice or one, never while they roll); the fixation of the settings, after
 * the delay; the variants, and those of before the review.
 */
@ExtendWith(ApplicationExtension.class)
class DiceTest {

    private static final double WIDTH = 1600;
    private static final double HEIGHT = 900;

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
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(WIDTH, HEIGHT));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getFixationLength()).thenReturn(500);
        when(configuration.getGameStartDelay()).thenReturn(0);
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private Dice launched(final DiceMode mode, final int dice, final DiceEnd end, final DiceMistakes mistakes)
        throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final Dice game = new Dice(gameContext, stats, mode, dice, end, mistakes, 7);
            game.setManual();
            game.launch();
            game.step(0.02);
            return game;
        });
    }

    private static void steps(final Dice game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    private static void enter(final Dice.Spot spot) {
        Event.fireEvent(spot.getNode(), new GazeEvent(GazeEvent.GAZE_ENTERED, 1, 0, 0));
    }

    private static void exit(final Dice.Spot spot) {
        Event.fireEvent(spot.getNode(), new GazeEvent(GazeEvent.GAZE_EXITED, -1, 0, 0));
    }

    /**
     * Rolls with a fixation on the button and waits for the dice to settle.
     */
    private static void roll(final Dice game) {
        enter(game.getRollSpot());
        steps(game, 0.6);
        exit(game.getRollSpot());
        steps(game, Dice.ROLL + 0.1);
    }

    @Test
    void shouldGiveTwoCloseWrongTotalsNeverOnBothSidesByTheSameAmount() {
        final ReplayablePseudoRandom random = new ReplayablePseudoRandom(3);
        for (int dice = 1; dice <= 3; dice++) {
            for (int total = dice; total <= dice * 6; total++) {
                for (int i = 0; i < 50; i++) {
                    final List<Integer> wrong = Dice.wrongTotals(total, dice, dice * 6, random);
                    assertEquals(2, wrong.size());
                    assertNotEquals(wrong.get(0), wrong.get(1));
                    for (final int value : wrong) {
                        assertNotEquals(total, value);
                        assertTrue(value >= dice && value <= dice * 6, dice + " dice, " + value);
                        assertTrue(Math.abs(value - total) <= 4, total + " / " + value);
                    }
                    assertNotEquals(total - wrong.get(0), wrong.get(1) - total, total + " " + wrong);
                }
            }
        }
    }

    @Test
    void shouldRollAfterTheFixationOfTheSettingsAndShowThreeCards() throws Exception {
        final Dice game = launched(DiceMode.COUNT, 2, DiceEnd.ENDLESS, DiceMistakes.SEARCH_AGAIN);
        fx(() -> {
            assertEquals(Dice.Phase.READY, game.getPhase());
            enter(game.getRollSpot());
            steps(game, 0.4);
            assertEquals(Dice.Phase.READY, game.getPhase());
            steps(game, 0.15);
            assertEquals(Dice.Phase.ROLLING, game.getPhase());
            assertTrue(game.getTotal() >= 2 && game.getTotal() <= 12);
            assertEquals(game.getFaces()[0] + game.getFaces()[1], game.getTotal());
            steps(game, Dice.ROLL + 0.05);
            assertEquals(Dice.Phase.ANSWER, game.getPhase());
            assertEquals(3, game.getCards().size());
            assertEquals(1, game.getCards().stream().filter(Dice.Card::isRight).count());
            assertEquals(game.getTotal(), game.getCards().stream().filter(Dice.Card::isRight).findFirst()
                .orElseThrow().getValue());
            verify(stats).incrementNumberOfGoalsToReach();
            return null;
        });
    }

    @Test
    void shouldWaitForTheDelayOfTheSettings() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final Dice game = launched(DiceMode.COUNT, 1, DiceEnd.ENDLESS, DiceMistakes.SEARCH_AGAIN);
        fx(() -> {
            enter(game.getRollSpot());
            steps(game, 0.9);
            assertEquals(Dice.Phase.WAIT, game.getPhase());
            steps(game, 0.7);
            assertEquals(Dice.Phase.ROLLING, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldGreyAWrongCardAndLookAgain() throws Exception {
        final Dice game = launched(DiceMode.COUNT, 1, DiceEnd.ENDLESS, DiceMistakes.SEARCH_AGAIN);
        fx(() -> {
            roll(game);
            final Dice.Card wrong = game.getCards().stream().filter(card -> !card.isRight()).findFirst()
                .orElseThrow();
            game.choose(wrong);
            assertTrue(wrong.isGreyed());
            assertEquals(Dice.Phase.ANSWER, game.getPhase());
            verify(stats).incrementNumberOfMistakes();
            game.choose(wrong);
            verify(stats, times(1)).incrementNumberOfMistakes();
            game.choose(game.getCards().stream().filter(Dice.Card::isRight).findFirst().orElseThrow());
            assertEquals(Dice.Phase.FOUND, game.getPhase());
            assertEquals(1, game.getFound());
            verify(stats).incrementNumberOfGoalsReached();
            steps(game, Dice.FOUND + 0.05);
            assertEquals(Dice.Phase.READY, game.getPhase());
            assertTrue(game.getCards().isEmpty());
            return null;
        });
    }

    @Test
    void shouldLoseTheLivesThenEndWithoutBravo() throws Exception {
        final Dice game = launched(DiceMode.COUNT, 2, DiceEnd.ENDLESS, DiceMistakes.LIVES);
        fx(() -> {
            for (int life = Dice.LIVES; life > 0; life--) {
                roll(game);
                game.choose(game.getCards().stream().filter(card -> !card.isRight()).findFirst().orElseThrow());
                assertEquals(life - 1, game.getLives());
                assertEquals(Dice.Phase.REVEAL, game.getPhase());
                assertTrue(game.getCards().stream().filter(Dice.Card::isRight).findFirst().orElseThrow().isDone());
                steps(game, Dice.REVEAL + 0.05);
            }
            assertEquals(Dice.Phase.SHOW, game.getPhase());
            steps(game, Dice.SHOW + 0.05);
            assertEquals(Dice.Phase.OVER, game.getPhase());
            assertFalse(game.isBravo());
            verify(gameContext, never()).playWinTransition(anyLong(), any());
            verify(gameContext).showRoundStats(any(), any());
            return null;
        });
    }

    @Test
    void shouldEndWithBravoAfterTheTotalsOfTheVariant() throws Exception {
        final Dice game = launched(DiceMode.COUNT, 3, DiceEnd.FOUND_5, DiceMistakes.SEARCH_AGAIN);
        fx(() -> {
            for (int i = 0; i < 5; i++) {
                roll(game);
                assertTrue(game.getTotal() >= 3 && game.getTotal() <= 18);
                game.choose(game.getCards().stream().filter(Dice.Card::isRight).findFirst().orElseThrow());
                steps(game, Dice.FOUND + 0.05);
            }
            assertEquals(Dice.Phase.OVER, game.getPhase());
            assertTrue(game.isBravo());
            verify(gameContext).playWinTransition(anyLong(), any());
            return null;
        });
    }

    @Test
    void shouldGiveEachFaceAndEachTotalOfOneDie() throws Exception {
        final Dice game = launched(DiceMode.COUNT, 1, DiceEnd.ENDLESS, DiceMistakes.SEARCH_AGAIN);
        fx(() -> {
            final Set<Integer> totals = new HashSet<>();
            for (int i = 0; i < 60; i++) {
                roll(game);
                totals.add(game.getTotal());
                game.choose(game.getCards().stream().filter(Dice.Card::isRight).findFirst().orElseThrow());
                steps(game, Dice.FOUND + 0.05);
            }
            assertEquals(Set.of(1, 2, 3, 4, 5, 6), totals);
            return null;
        });
    }

    /**
     * Before the review, a die could be rolled again while all were rolling.
     */
    @Test
    void shouldRollNothingWhileTheDiceRoll() throws Exception {
        final Dice game = launched(DiceMode.THROW, 4, DiceEnd.ENDLESS, DiceMistakes.SEARCH_AGAIN);
        fx(() -> {
            game.rollAll();
            assertEquals(Dice.Phase.ROLLING, game.getPhase());
            final int[] faces = game.getFaces().clone();
            game.rollOne(2);
            game.rollAll();
            assertArrayEquals(faces, game.getFaces());
            assertEquals(1, game.getRolls());
            steps(game, Dice.ROLL + 0.05);
            assertEquals(Dice.Phase.READY, game.getPhase());
            // The total shown
            assertEquals(1, game.getMessage().getChildren().size());
            return null;
        });
    }

    @Test
    void shouldRollOneDieAloneWhenThrowing() throws Exception {
        final Dice game = launched(DiceMode.THROW, 3, DiceEnd.ENDLESS, DiceMistakes.SEARCH_AGAIN);
        fx(() -> {
            final int first = game.getFaces()[0];
            final int third = game.getFaces()[2];
            enter(game.getDieSpots().get(1));
            steps(game, 0.6);
            assertEquals(Dice.Phase.ROLLING, game.getPhase());
            assertEquals(first, game.getFaces()[0]);
            assertEquals(third, game.getFaces()[2]);
            assertEquals(game.getFaces()[0] + game.getFaces()[1] + game.getFaces()[2], game.getTotal());
            verify(stats).incrementNumberOfGoalsToReach();
            verify(stats).incrementNumberOfGoalsReached();
            return null;
        });
    }

    @Test
    void shouldNotRollADieAloneWhenCounting() throws Exception {
        final Dice game = launched(DiceMode.COUNT, 2, DiceEnd.ENDLESS, DiceMistakes.SEARCH_AGAIN);
        fx(() -> {
            game.rollOne(0);
            assertEquals(Dice.Phase.READY, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldReadTheFixationLengthAtEachStep() throws Exception {
        final Dice game = launched(DiceMode.THROW, 2, DiceEnd.ENDLESS, DiceMistakes.SEARCH_AGAIN);
        fx(() -> {
            enter(game.getRollSpot());
            steps(game, 0.3);
            when(configuration.getFixationLength()).thenReturn(2000);
            steps(game, 0.5);
            assertEquals(Dice.Phase.READY, game.getPhase());
            when(configuration.getFixationLength()).thenReturn(100);
            steps(game, 0.04);
            assertEquals(Dice.Phase.ROLLING, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldRemoveEverythingWhenDisposed() throws Exception {
        final Dice game = launched(DiceMode.COUNT, 2, DiceEnd.ENDLESS, DiceMistakes.LIVES);
        fx(() -> {
            roll(game);
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertEquals(Dice.Phase.OVER, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldOfferBothModes() {
        final Set<IGameVariant> variants = new DiceGameVariantGenerator().getVariants();
        // Counting: 4 ends x 2 ways x 3 levels; throwing: 1 to 6 dice
        assertEquals(4 * 2 * 3 + 6, variants.size());
        final long throwing = variants.stream().map(GroupedGameVariant.class::cast)
            .filter(variant -> variant.getGroup().equals(DiceMode.THROW.getKey())).count();
        assertEquals(6, throwing);
    }

    @Test
    void shouldReadTheVariantsAndThoseOfBeforeTheReview() {
        final Dice counting = DiceGameLauncher.create(gameContext,
            new GroupedGameVariant("DiceCount", "DiceEnd10", "DiceHard", null, "DiceLives"), stats, 1);
        assertEquals(DiceMode.COUNT, counting.getMode());
        assertEquals(3, counting.getDiceCount());
        assertEquals(DiceEnd.FOUND_10, counting.getEnd());
        assertEquals(DiceMistakes.LIVES, counting.getMistakes());
        final Dice throwing = DiceGameLauncher.create(gameContext,
            new GroupedGameVariant("DiceThrow", "DiceEndless", "Dice5", null, "DiceSearchAgain"), stats, 1);
        assertEquals(DiceMode.THROW, throwing.getMode());
        assertEquals(5, throwing.getDiceCount());
        // "IntGameVariant:4" of before the review: 4 dice thrown
        final Dice former = DiceGameLauncher.create(gameContext, new IntGameVariant(4), stats, 1);
        assertEquals(DiceMode.THROW, former.getMode());
        assertEquals(4, former.getDiceCount());
    }
}

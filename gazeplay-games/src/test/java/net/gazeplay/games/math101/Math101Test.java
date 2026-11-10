package net.gazeplay.games.math101;

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
 * "Apprends les Maths" (Math101; reviewed on 10/11/2026, maintainer's rules): the operations of each topic and level,
 * never a 0, the wrong answers close to the right one, the choice by a fixation of the length of the settings, a
 * wrong answer looked again or costing a life, the ends, the areas of interest, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class Math101Test {

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

    private Math101 launched(final MathTopic topic, final MathLevel level, final MathEnd end,
                             final MathMistakes mistakes, final double seed) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final Math101 game = new Math101(gameContext, stats, topic, level, end, mistakes, seed);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private static void steps(final Math101 game, final double seconds) throws Exception {
        fx(() -> {
            for (double t = 0; t < seconds; t += STEP) {
                game.step(STEP);
            }
            return null;
        });
    }

    private static void untilPlay(final Math101 game) throws Exception {
        for (int i = 0; i < 2000 && game.getPhase() != Math101.Phase.PLAY; i++) {
            steps(game, STEP);
        }
        assertEquals(Math101.Phase.PLAY, game.getPhase());
    }

    private static Math101.Card card(final Math101 game, final boolean right) {
        return game.getCards().stream().filter(card -> card.isRight() == right).findFirst().orElseThrow();
    }

    private static void choose(final Math101 game, final Math101.Card card) throws Exception {
        fx(() -> {
            game.choose(card);
            return null;
        });
    }

    private static void solve(final Math101 game) throws Exception {
        untilPlay(game);
        choose(game, card(game, true));
        assertEquals(Math101.Phase.FOUND, game.getPhase());
        steps(game, Math101.FOUND + 0.05);
    }

    @Test
    void shouldDrawTheOperationsOfEachLevelWithoutZeroAndWrongAnswersCloseToTheRightOne() {
        final ReplayablePseudoRandom random = new ReplayablePseudoRandom(12345.678);
        for (final MathTopic topic : MathTopic.values()) {
            for (final MathLevel level : topic.levels()) {
                for (int i = 0; i < 400; i++) {
                    final MathQuestion question = MathQuestion.draw(topic, level, random);
                    assertTrue(topic.getOperations().contains(question.getOperation()), topic + " " + level);
                    assertTrue(question.getFirst() >= 1 && question.getSecond() >= 1 && question.getAnswer() >= 1,
                        question.text());
                    switch (question.getOperation()) {
                        case PLUS -> {
                            assertEquals(question.getAnswer(), question.getFirst() + question.getSecond());
                            assertTrue(question.getAnswer() <= level.getUpTo(), question.text());
                        }
                        case MINUS -> {
                            assertEquals(question.getAnswer(), question.getFirst() - question.getSecond());
                            assertTrue(question.getFirst() <= level.getUpTo(), question.text());
                        }
                        case MULTIPLY -> {
                            assertEquals(question.getAnswer(), question.getFirst() * question.getSecond());
                            assertTrue(contains(level.getTables(), question.getTable()), question.text());
                            assertTrue(question.getFirst() == question.getTable()
                                || question.getSecond() == question.getTable(), question.text());
                            assertTrue(question.getAnswer() / question.getTable() <= MathLevel.FACTORS);
                        }
                        case DIVID -> {
                            assertEquals(question.getFirst(), question.getAnswer() * question.getSecond());
                            assertEquals(question.getTable(), question.getSecond());
                            assertTrue(contains(level.getTables(), question.getTable()), question.text());
                            assertTrue(question.getAnswer() <= MathLevel.FACTORS);
                        }
                    }
                    final List<Integer> wrong = question.wrongAnswers(random);
                    assertEquals(2, wrong.size());
                    assertNotEquals(wrong.get(0), wrong.get(1));
                    // The right answer is never the one in the middle of the three, by as much on each side
                    assertNotEquals(2 * question.getAnswer(), wrong.get(0) + wrong.get(1), question.text());
                    for (final int value : wrong) {
                        assertTrue(value >= 1 && value != question.getAnswer(), question.text() + " " + value);
                        final int distance = Math.abs(value - question.getAnswer());
                        switch (question.getOperation()) {
                            case PLUS, MINUS -> assertTrue(distance <= 3 || distance == 10, question.text());
                            case MULTIPLY -> assertTrue(distance <= Math.max(question.getTable(),
                                question.getAnswer() / question.getTable()) + 1, question.text() + " " + value);
                            case DIVID -> assertTrue(distance <= 2 || value == question.getTable(), question.text());
                        }
                    }
                }
            }
        }
        assertEquals("7 + 5 =", new MathQuestion(7, MathOperation.PLUS, 5, 12, 0).text());
        assertEquals("28 ÷ 7 =", new MathQuestion(28, MathOperation.DIVID, 7, 4, 7).text());
    }

    private static boolean contains(final int[] values, final int value) {
        for (final int v : values) {
            if (v == value) {
                return true;
            }
        }
        return false;
    }

    @Test
    void shouldChooseACardAfterAFixationOfTheLengthOfTheSettingsAfterTheDelay() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final Math101 game = launched(MathTopic.ADDITION, MathLevel.UP_TO_10, MathEnd.ENDLESS,
            MathMistakes.SEARCH_AGAIN, 3 * 7919.37);
        steps(game, 0.5);
        assertEquals(Math101.Phase.WAIT, game.getPhase());
        assertTrue(game.getCards().isEmpty());
        steps(game, 0.6);
        assertEquals(Math101.Phase.PLAY, game.getPhase());
        assertEquals(3, game.getCards().size());
        assertEquals(1, game.getCards().stream().filter(Math101.Card::isRight).count());
        verify(stats).notifyNewRoundReady();
        verify(stats).incrementNumberOfGoalsToReach();
        // The three cards are different
        assertEquals(3, new HashSet<>(game.getCards().stream().map(Math101.Card::getValue).toList()).size());

        final Math101.Card right = card(game, true);
        fx(() -> {
            game.lookAt(right);
            return null;
        });
        steps(game, 0.5);
        assertEquals(Math101.Phase.PLAY, game.getPhase());
        // The length of the settings is read at each step: half done, the rest twice as fast
        when(configuration.getFixationLength()).thenReturn(500);
        steps(game, 0.2);
        assertEquals(Math101.Phase.PLAY, game.getPhase());
        steps(game, 0.08);
        assertEquals(Math101.Phase.FOUND, game.getPhase());
        assertEquals(1, game.getSolved());
        verify(stats).incrementNumberOfGoalsReached();
        verify(gameContext.getSoundManager()).add("data/math101/sounds/right.wav");
        // A new operation, never the same as the previous one
        final String before = game.getQuestion().text();
        steps(game, Math101.FOUND + 0.05);
        assertEquals(Math101.Phase.PLAY, game.getPhase());
        assertNotEquals(before, game.getQuestion().text());
    }

    @Test
    void shouldGreyAWrongCardAndLookAgainWithoutLives() throws Exception {
        final Math101 game = launched(MathTopic.MULTIPLICATION, MathLevel.TABLE_7, MathEnd.OPERATIONS_5,
            MathMistakes.SEARCH_AGAIN, 5 * 7919.37);
        untilPlay(game);
        final Math101.Card wrong = card(game, false);
        choose(game, wrong);
        assertTrue(wrong.isGreyed());
        assertEquals(Math101.Phase.PLAY, game.getPhase());
        assertEquals(Math101.LIVES, game.getLives());
        verify(stats).incrementNumberOfMistakes();
        verify(gameContext.getSoundManager()).add("data/math101/sounds/wrong.wav");
        // A greyed card cannot be chosen again
        choose(game, wrong);
        verify(stats, times(1)).incrementNumberOfMistakes();
        choose(game, card(game, true));
        assertEquals(Math101.Phase.FOUND, game.getPhase());
        assertEquals(1, game.getSolved());
    }

    @Test
    void shouldLoseALifeShowTheRightAnswerAndEndLostAfterThreeMistakes() throws Exception {
        final Math101 game = launched(MathTopic.SUBTRACTION, MathLevel.UP_TO_20, MathEnd.OPERATIONS_10,
            MathMistakes.LIVES, 7 * 7919.37);
        for (int mistake = 1; mistake <= Math101.LIVES; mistake++) {
            untilPlay(game);
            final Math101.Card right = card(game, true);
            choose(game, card(game, false));
            assertEquals(Math101.Phase.REVEAL, game.getPhase());
            assertEquals(Math101.LIVES - mistake, game.getLives());
            // The right answer is shown
            assertTrue(right.isDone());
            steps(game, Math101.REVEAL + 0.05);
        }
        assertEquals(Math101.Phase.SHOW, game.getPhase());
        verify(translator).translate("MathLost");
        steps(game, Math101.SHOW + 0.05);
        assertEquals(Math101.Phase.OVER, game.getPhase());
        assertFalse(game.isBravo());
        verify(gameContext, never()).playWinTransition(anyLong(), any());
        verify(gameContext).showRoundStats(any(), any());
        verify(stats, times(3)).incrementNumberOfMistakes();
        verify(stats, never()).incrementNumberOfGoalsReached();
    }

    @Test
    void shouldEndWithBravoAfterTheOperationsToSolve() throws Exception {
        final Math101 game = launched(MathTopic.ALL, MathLevel.ALL_MEDIUM, MathEnd.OPERATIONS_5, MathMistakes.LIVES,
            11 * 7919.37);
        for (int i = 0; i < 5; i++) {
            solve(game);
        }
        assertEquals(Math101.Phase.OVER, game.getPhase());
        assertTrue(game.isBravo());
        verify(stats, times(5)).incrementNumberOfGoalsReached();
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldGoOnWithoutEnd() throws Exception {
        final Math101 game = launched(MathTopic.DIVISION, MathLevel.TABLES_ALL, MathEnd.ENDLESS,
            MathMistakes.SEARCH_AGAIN, 13 * 7919.37);
        for (int i = 0; i < 25; i++) {
            solve(game);
        }
        assertEquals(Math101.Phase.PLAY, game.getPhase());
        assertEquals(25, game.getSolved());
    }

    @Test
    void shouldRemoveEverythingAndGiveTheAreasOfInterestAtTheEnd() throws Exception {
        final Math101 game = launched(MathTopic.ADD_SUB, MathLevel.UP_TO_100, MathEnd.ENDLESS,
            MathMistakes.SEARCH_AGAIN, 17 * 7919.37);
        solve(game);
        untilPlay(game);
        fx(() -> {
            game.dispose();
            return null;
        });
        assertTrue(gameArea.getChildren().isEmpty());
        assertTrue(game.getCards().isEmpty());
        verify(stats).setTargetAOIList(argThat(list -> list.size() == 2 && list.stream()
            .allMatch(aoi -> aoi.getTimeEnded() > 0)));
    }

    @Test
    void shouldGiveAllTheVariantsAndPlayTheOthers() {
        final Set<IGameVariant> variants = new Math101GameVariantGenerator().getVariants();
        int expected = 0;
        for (final MathTopic topic : MathTopic.values()) {
            expected += topic.levels().size() * MathEnd.values().length * MathMistakes.values().length;
        }
        assertEquals(expected, variants.size());
        final MathGameLauncher launcher = new MathGameLauncher();
        final Math101 game = (Math101) launcher.createNewGame(gameContext, new GroupedGameVariant("MathMultDiv",
            "MathOperations20", "MathTable8", null, "MathLives"), stats);
        assertEquals(MathTopic.MULT_DIV, game.getTopic());
        assertEquals(MathLevel.TABLE_8, game.getLevel());
        assertEquals(MathEnd.OPERATIONS_20, game.getEnd());
        assertEquals(MathMistakes.LIVES, game.getMistakes());
        // A level of another topic: the first level of the topic
        final Math101 mismatched = (Math101) launcher.replayGame(gameContext, new GroupedGameVariant("MathAddition",
            "MathEndless", "MathTable8", null, "MathSearchAgain"), stats, 4.5);
        assertEquals(MathLevel.UP_TO_10, mismatched.getLevel());
        // A variant of before the review: the additions up to 10, without end, looking again
        final Math101 old = (Math101) launcher.createNewGame(gameContext,
            IGameVariant.toGameVariant("MathGameVariant:0:12"), stats);
        assertEquals(MathTopic.ADDITION, old.getTopic());
        assertEquals(MathLevel.UP_TO_10, old.getLevel());
        assertEquals(MathEnd.ENDLESS, old.getEnd());
        assertEquals(MathMistakes.SEARCH_AGAIN, old.getMistakes());
    }
}

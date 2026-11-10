package net.gazeplay.games.cups;

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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Gobelets Pass Pass" (Cups and Balls, name code CupsBalls; reviewed on 10/11/2026, maintainer's rules): the ball shown,
 * the exchanges two by two at the speed of the settings, one try per round, the lives, the ends, the areas of interest
 * of the ball, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class CupsAndBallsTest {

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

    private CupsAndBalls launched(final CupsLevel level, final CupsEnd end, final double seed) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final CupsAndBalls game = new CupsAndBalls(gameContext, stats, level, end, seed);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private static void steps(final CupsAndBalls game, final double seconds) throws Exception {
        fx(() -> {
            for (double t = 0; t < seconds; t += STEP) {
                game.step(STEP);
            }
            return null;
        });
    }

    private static void untilPlay(final CupsAndBalls game) throws Exception {
        for (int i = 0; i < 4000 && game.getPhase() != CupsAndBalls.Phase.PLAY; i++) {
            steps(game, STEP);
        }
        assertEquals(CupsAndBalls.Phase.PLAY, game.getPhase());
    }

    private static void choose(final CupsAndBalls game, final CupsAndBalls.Cup cup) throws Exception {
        fx(() -> {
            game.choose(cup);
            return null;
        });
        steps(game, CupsAndBalls.REVEAL + 0.05);
    }

    private static CupsAndBalls.Cup wrongCup(final CupsAndBalls game) {
        return game.getCups().stream().filter(cup -> cup != game.getBallCup()).findFirst().orElseThrow();
    }

    @Test
    void shouldShowTheBallThenExchangeTheCupsTwoByTwoAtTheSpeedOfTheSettings() throws Exception {
        final CupsAndBalls game = launched(CupsLevel.NORMAL, CupsEnd.ENDLESS, 3);
        steps(game, 0.05);
        assertEquals(CupsAndBalls.Phase.SHOW_BALL, game.getPhase());
        assertEquals(4, game.getCups().size());
        steps(game, CupsAndBalls.LIFT + 0.1);
        assertTrue(game.getBallCup().isLifted());
        steps(game, CupsAndBalls.LIFT + CupsAndBalls.HOLD + CupsAndBalls.BEFORE);
        assertEquals(CupsAndBalls.Phase.EXCHANGE, game.getPhase());
        // Nothing can be chosen while the cups move
        fx(() -> {
            game.choose(game.getBallCup());
            return null;
        });
        assertEquals(0, game.getFound());
        // 8 exchanges of 0.7 second, twice as fast at the speed 2
        steps(game, CupsLevel.NORMAL.getExchangeSeconds() * 2);
        assertTrue(game.getExchangesDone() >= 1 && game.getExchangesDone() <= 2);
        when(configuration.getAnimationSpeedRatio()).thenReturn(2.0);
        final int done = game.getExchangesDone();
        steps(game, CupsLevel.NORMAL.getExchangeSeconds() * 2 / 2);
        assertEquals(done + 2, game.getExchangesDone(), 1);
        untilPlay(game);
        assertEquals(8, game.getExchangesDone());
        verify(stats).incrementNumberOfGoalsToReach();
        // The cups are still on their places, one per place
        final Set<Integer> slots = new HashSet<>();
        game.getCups().forEach(cup -> slots.add(cup.getSlot()));
        assertEquals(4, slots.size());
    }

    @Test
    void shouldFollowTheBallInItsCupAndGiveItsPlacesToTheStatistics() throws Exception {
        final CupsAndBalls game = launched(CupsLevel.HARD, CupsEnd.ENDLESS, 5);
        steps(game, 0.05);
        final CupsAndBalls.Cup ballCup = game.getBallCup();
        untilPlay(game);
        assertSame(ballCup, game.getBallCup());
        // An area of interest at each place of the ball, the last one where its cup stands now
        assertFalse(game.getTargetAOIList().isEmpty());
        final double lastX = game.getTargetAOIList().get(game.getTargetAOIList().size() - 1).getXValue();
        assertEquals(ballCup.getX(), lastX, 1e-6);
        fx(() -> {
            game.dispose();
            return null;
        });
        verify(stats).setTargetAOIList(game.getTargetAOIList());
        game.getTargetAOIList().forEach(aoi -> assertTrue(aoi.getTimeEnded() >= aoi.getTimeStarted()));
    }

    @Test
    void shouldChooseACupAfterAFixationOfTheLengthOfTheSettingsReadAtEachStep() throws Exception {
        final CupsAndBalls game = launched(CupsLevel.EASY, CupsEnd.ENDLESS, 7);
        steps(game, 0.05);
        untilPlay(game);
        final CupsAndBalls.Cup cup = game.getBallCup();
        fx(() -> {
            game.lookAt(cup);
            return null;
        });
        steps(game, 0.5);
        assertEquals(CupsAndBalls.Phase.PLAY, game.getPhase());
        when(configuration.getFixationLength()).thenReturn(600);
        steps(game, 0.25);
        assertEquals(CupsAndBalls.Phase.PLAY, game.getPhase());
        steps(game, 0.07);
        assertEquals(CupsAndBalls.Phase.REVEAL, game.getPhase());
        assertEquals(1, game.getFound());
        verify(stats).incrementNumberOfGoalsReached();
        verify(gameContext.getSoundManager()).add("data/cups/sounds/found.wav");
    }

    @Test
    void shouldShowTheBallAfterAWrongCupAndCostALife() throws Exception {
        final CupsAndBalls game = launched(CupsLevel.EASY, CupsEnd.ENDLESS, 9);
        steps(game, 0.05);
        untilPlay(game);
        final CupsAndBalls.Cup ballCup = game.getBallCup();
        final CupsAndBalls.Cup wrong = wrongCup(game);
        fx(() -> {
            game.choose(wrong);
            return null;
        });
        assertEquals(CupsAndBalls.LIVES - 1, game.getLives());
        verify(gameContext.getSoundManager()).add("data/cups/sounds/missed.wav");
        // The wrong cup lifts, then the right one shows the ball
        steps(game, CupsAndBalls.LIFT + 0.05);
        assertTrue(wrong.isLifted());
        steps(game, CupsAndBalls.LIFT);
        assertTrue(ballCup.isLifted());
        verify(stats, never()).incrementNumberOfGoalsReached();
        steps(game, CupsAndBalls.REVEAL);
        // Only one try: a new round
        assertEquals(CupsAndBalls.Phase.SHOW_BALL, game.getPhase());
    }

    @Test
    void shouldEndWithLostAfterThreeMistakesAndNoBravo() throws Exception {
        final CupsAndBalls game = launched(CupsLevel.EASY, CupsEnd.ENDLESS, 11);
        steps(game, 0.05);
        for (int mistake = 0; mistake < CupsAndBalls.LIVES; mistake++) {
            untilPlay(game);
            choose(game, wrongCup(game));
        }
        assertEquals(CupsAndBalls.Phase.SHOW, game.getPhase());
        steps(game, CupsAndBalls.SHOW + 0.05);
        assertFalse(game.isBravo());
        verify(gameContext, never()).playWinTransition(anyLong(), any());
        verify(gameContext).showRoundStats(any(), any());
    }

    @Test
    void shouldEndWithBravoAfterTheBallsToFind() throws Exception {
        final CupsAndBalls game = launched(CupsLevel.EASY, CupsEnd.FOUND_3, 13);
        steps(game, 0.05);
        for (int round = 0; round < 3; round++) {
            untilPlay(game);
            choose(game, game.getBallCup());
        }
        assertEquals(CupsAndBalls.Phase.OVER, game.getPhase());
        assertTrue(game.isBravo());
        verify(gameContext).playWinTransition(anyLong(), any());
        verify(stats, times(3)).notifyNewRoundReady();
    }

    @Test
    void shouldPutTheCupsInARowInsideTheScreen() throws Exception {
        for (final CupsLevel level : CupsLevel.values()) {
            final CupsAndBalls game = launched(level, CupsEnd.ENDLESS, 1);
            steps(game, 0.05);
            final double half = game.getCupWidth() / 2;
            double right = 0;
            for (int slot = 0; slot < level.getCups(); slot++) {
                final int place = slot;
                final CupsAndBalls.Cup cup = game.getCups().stream().filter(c -> c.getSlot() == place)
                    .findFirst().orElseThrow();
                assertTrue(cup.getX() - half > right, level.name());
                right = cup.getX() + half;
            }
            assertTrue(right < 1920, level.name());
            assertTrue(game.getCupWidth() > 1080 * 0.18, level.name());
            fx(() -> {
                game.dispose();
                return null;
            });
        }
    }

    @Test
    void shouldReplayTheSameExchanges() throws Exception {
        final int[] slots = new int[2];
        for (int k = 0; k < 2; k++) {
            final CupsAndBalls game = launched(CupsLevel.HARD, CupsEnd.ENDLESS, 17);
            steps(game, 0.05);
            untilPlay(game);
            slots[k] = game.getBallCup().getSlot();
        }
        assertEquals(slots[0], slots[1]);
    }

    @Test
    void shouldGiveAllTheVariantsAndPlayTheOldOnes() {
        final Set<IGameVariant> variants = new CupsBallsGameVariantGenerator().getVariants();
        assertEquals(12, variants.size());
        final IGameVariant variant = new GroupedGameVariant(CupsBallsGameVariantGenerator.GROUP,
            CupsLevel.HARD.getKey(), CupsEnd.FOUND_10.getKey(), null);
        assertTrue(variants.contains(variant));
        assertEquals(CupsLevel.HARD, CupsBallsGameLauncher.levelOf(variant));
        assertEquals(CupsEnd.FOUND_10, CupsBallsGameLauncher.endOf(variant));
        assertEquals(CupsLevel.EASY, CupsBallsGameLauncher.levelOf(IGameVariant.toGameVariant("IntGameVariant:3")));
        assertEquals(CupsLevel.HARD, CupsBallsGameLauncher.levelOf(IGameVariant.toGameVariant("IntGameVariant:5")));
        assertEquals(CupsEnd.ENDLESS, CupsBallsGameLauncher.endOf(IGameVariant.toGameVariant("IntGameVariant:5")));
    }
}

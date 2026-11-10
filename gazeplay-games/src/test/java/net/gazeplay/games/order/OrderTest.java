package net.gazeplay.games.order;

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

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Ordre" (Order; reviewed on 10/11/2026, maintainer's rules): the targets appearing one after the other, never
 * overlapping, fixated in order, the right answer shown after a mistake, the lives, the progressive level, the ends,
 * the variants.
 */
@ExtendWith(ApplicationExtension.class)
class OrderTest {

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
        screen(1920, 1080);
    }

    private void screen(final double width, final double height) {
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(width, height));
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private Order launched(final OrderLevel level, final OrderEnd end, final double seed) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final Order game = new Order(gameContext, stats, level, end, seed);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private static void steps(final Order game, final double seconds) throws Exception {
        fx(() -> {
            for (double t = 0; t < seconds; t += STEP) {
                game.step(STEP);
            }
            return null;
        });
    }

    /**
     * Steps until the targets can be chosen.
     */
    private static void untilPlay(final Order game) throws Exception {
        for (int i = 0; i < 2000 && game.getPhase() != Order.Phase.PLAY; i++) {
            steps(game, STEP);
        }
        assertEquals(Order.Phase.PLAY, game.getPhase());
    }

    private static void choose(final Order game, final int index) throws Exception {
        fx(() -> {
            game.choose(game.getTargets().get(index));
            return null;
        });
    }

    private static void winRound(final Order game) throws Exception {
        untilPlay(game);
        for (int i = 0; i < game.getCount(); i++) {
            choose(game, i);
        }
        assertEquals(Order.Phase.WON, game.getPhase());
        steps(game, Order.WON + 0.05);
    }

    @Test
    void shouldMakeTheTargetsAppearOneAfterTheOtherAtTheSpeedOfTheSettings() throws Exception {
        final Order game = launched(OrderLevel.FIVE, OrderEnd.ENDLESS, 3);
        steps(game, 0.05);
        assertEquals(Order.Phase.APPEAR, game.getPhase());
        assertEquals(5, game.getTargets().size());
        steps(game, Order.BEFORE + 0.1);
        assertEquals(1, game.getTargets().stream().filter(Order.Target::isShown).count());
        steps(game, Order.APPEAR);
        assertEquals(2, game.getTargets().stream().filter(Order.Target::isShown).count());
        verify(gameContext.getSoundManager(), times(2)).add("data/order/sounds/appear.wav");
        // Nothing can be chosen while they appear
        choose(game, 0);
        assertFalse(game.getTargets().get(0).isFound());
        // Twice as fast with the speed of the settings
        when(configuration.getAnimationSpeedRatio()).thenReturn(2.0);
        steps(game, Order.APPEAR / 2);
        assertEquals(3, game.getTargets().stream().filter(Order.Target::isShown).count());
        untilPlay(game);
        verify(stats).notifyNewRoundReady();
        verify(stats).incrementNumberOfGoalsToReach();
    }

    @Test
    void shouldNeverPutTwoTargetsOverEachOtherNorOutOfTheScreen() throws Exception {
        for (final double[] screen : new double[][]{{1920, 1080}, {1024, 768}, {800, 1000}, {1280, 600}}) {
            screen(screen[0], screen[1]);
            final Order game = launched(OrderLevel.PROGRESSIVE, OrderEnd.ENDLESS, 1);
            final double radius = game.getRadius();
            for (int draw = 0; draw < 30; draw++) {
                final List<double[]> places = fx(() -> game.places(OrderLevel.MOST));
                for (int i = 0; i < places.size(); i++) {
                    final double[] a = places.get(i);
                    assertTrue(a[0] - radius * 1.25 >= 0 && a[0] + radius * 1.25 <= screen[0]);
                    assertTrue(a[1] - radius * 1.25 >= screen[1] * 0.12 && a[1] + radius * 1.25 <= screen[1]);
                    for (int j = i + 1; j < places.size(); j++) {
                        assertTrue(Math.hypot(a[0] - places.get(j)[0], a[1] - places.get(j)[1]) >= radius * 2.6);
                    }
                }
            }
            fx(() -> {
                game.dispose();
                return null;
            });
        }
    }

    @Test
    void shouldChooseATargetAfterAFixationOfTheLengthOfTheSettingsReadAtEachStep() throws Exception {
        final Order game = launched(OrderLevel.THREE, OrderEnd.ENDLESS, 5);
        steps(game, 0.05);
        untilPlay(game);
        final Order.Target first = game.getTargets().get(0);
        fx(() -> {
            game.lookAt(first);
            return null;
        });
        steps(game, 0.5);
        assertFalse(first.isFound());
        when(configuration.getFixationLength()).thenReturn(600);
        steps(game, 0.25);
        assertFalse(first.isFound());
        steps(game, 0.07);
        assertTrue(first.isFound());
        assertTrue(first.showsNumber());
        assertEquals(1, game.getNext());
        verify(gameContext.getSoundManager()).add("data/order/sounds/right.wav");
    }

    @Test
    void shouldWinARoundInOrderAndCountIt() throws Exception {
        final Order game = launched(OrderLevel.FIVE, OrderEnd.ENDLESS, 7);
        steps(game, 0.05);
        winRound(game);
        assertEquals(1, game.getWon());
        verify(stats).incrementNumberOfGoalsReached();
        assertEquals(Order.Phase.APPEAR, game.getPhase());
        assertEquals(Order.LIVES, game.getLives());
    }

    @Test
    void shouldShowTheRightAnswerAfterAMistakeCostALifeAndStartANewRound() throws Exception {
        final Order game = launched(OrderLevel.FIVE, OrderEnd.ENDLESS, 9);
        steps(game, 0.05);
        untilPlay(game);
        final double[] firstPlace = {game.getTargets().get(0).getX(), game.getTargets().get(0).getY()};
        choose(game, 0);
        choose(game, 3);
        assertEquals(Order.Phase.REVEAL, game.getPhase());
        assertEquals(Order.LIVES - 1, game.getLives());
        verify(gameContext.getSoundManager()).add("data/order/sounds/wrong.wav");
        // Every target shows its number: the right answer
        for (final Order.Target target : game.getTargets()) {
            assertTrue(target.showsNumber(), "target " + target.getIndex());
        }
        verify(stats, never()).incrementNumberOfGoalsReached();
        steps(game, Order.REVEAL + 0.05);
        assertEquals(Order.Phase.APPEAR, game.getPhase());
        assertEquals(0, game.getNext());
        assertNotEquals(firstPlace[0], game.getTargets().get(0).getX());
    }

    @Test
    void shouldEndWithLostAfterThreeMistakesAndNoBravo() throws Exception {
        final Order game = launched(OrderLevel.THREE, OrderEnd.ENDLESS, 11);
        steps(game, 0.05);
        for (int mistake = 0; mistake < Order.LIVES; mistake++) {
            untilPlay(game);
            choose(game, 2);
            steps(game, Order.REVEAL + 0.05);
        }
        assertEquals(Order.Phase.SHOW, game.getPhase());
        steps(game, Order.SHOW + 0.05);
        assertEquals(Order.Phase.OVER, game.getPhase());
        assertFalse(game.isBravo());
        verify(gameContext, never()).playWinTransition(anyLong(), any());
        verify(gameContext).showRoundStats(any(), any());
    }

    @Test
    void shouldEndWithBravoAfterTheRoundsToWin() throws Exception {
        final Order game = launched(OrderLevel.THREE, OrderEnd.ROUNDS_3, 13);
        steps(game, 0.05);
        for (int round = 0; round < 3; round++) {
            winRound(game);
        }
        assertEquals(Order.Phase.OVER, game.getPhase());
        assertTrue(game.isBravo());
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldAddATargetAfterEachRoundWonAtTheProgressiveLevelButNotAfterAMistake() throws Exception {
        final Order game = launched(OrderLevel.PROGRESSIVE, OrderEnd.ENDLESS, 15);
        steps(game, 0.05);
        assertEquals(3, game.getCount());
        winRound(game);
        assertEquals(4, game.getCount());
        winRound(game);
        assertEquals(5, game.getCount());
        untilPlay(game);
        choose(game, 1);
        steps(game, Order.REVEAL + 0.05);
        assertEquals(5, game.getCount());
        assertEquals(5, game.getTargets().size());
    }

    @Test
    void shouldWaitForTheDelayOfTheSettings() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(2000);
        final Order game = launched(OrderLevel.THREE, OrderEnd.ENDLESS, 17);
        steps(game, 1.5);
        assertEquals(Order.Phase.WAIT, game.getPhase());
        assertTrue(game.getTargets().isEmpty());
        steps(game, 0.6);
        assertEquals(Order.Phase.APPEAR, game.getPhase());
    }

    @Test
    void shouldReplayTheSamePlaces() throws Exception {
        final double[] x = new double[2];
        for (int k = 0; k < 2; k++) {
            final Order game = launched(OrderLevel.SEVEN, OrderEnd.ENDLESS, 19);
            steps(game, 0.05);
            x[k] = game.getTargets().get(6).getX();
        }
        assertEquals(x[0], x[1], 1e-9);
    }

    @Test
    void shouldStopAfterDispose() throws Exception {
        final Order game = launched(OrderLevel.THREE, OrderEnd.ENDLESS, 2);
        steps(game, 0.05);
        untilPlay(game);
        fx(() -> {
            game.dispose();
            return null;
        });
        steps(game, 2);
        assertEquals(Order.Phase.OVER, game.getPhase());
        assertTrue(gameArea.getChildren().isEmpty());
        verify(gameContext.getGazeDeviceManager(), times(3)).removeEventFilter(any());
    }

    @Test
    void shouldGiveAllTheVariantsAndPlayTheOldOnes() {
        final Set<IGameVariant> variants = new OrdersGameVariantGenerator().getVariants();
        assertEquals(16, variants.size());
        final IGameVariant variant = new GroupedGameVariant(OrdersGameVariantGenerator.GROUP,
            OrderLevel.PROGRESSIVE.getKey(), OrderEnd.ROUNDS_10.getKey(), null);
        assertTrue(variants.contains(variant));
        assertEquals(OrderLevel.PROGRESSIVE, OrderGameLauncher.levelOf(variant));
        assertEquals(OrderEnd.ROUNDS_10, OrderGameLauncher.endOf(variant));
        final IGameVariant old = IGameVariant.toGameVariant("IntGameVariant:7");
        assertEquals(OrderLevel.SEVEN, OrderGameLauncher.levelOf(old));
        assertEquals(OrderEnd.ENDLESS, OrderGameLauncher.endOf(old));
    }
}

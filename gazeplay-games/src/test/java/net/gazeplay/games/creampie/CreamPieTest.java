package net.gazeplay.games.creampie;

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
 * "Tartes à la crème" (reviewed on 12/11/2026, maintainer's rules): the delay, the throw after the fixation or at once,
 * the pie on its arc and the cream on the picture, the next target elsewhere, the moving targets in their space, the
 * areas of interest, the end, the variants and the old shortcut.
 */
@ExtendWith(ApplicationExtension.class)
class CreamPieTest {

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
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private CreamPie launched(final CreamPieThrow way, final CreamPieMotion motion, final CreamPieEnd end)
        throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final CreamPie game = new CreamPie(gameContext, stats, CreamPiePictures.ANIMALS, CreamPieSize.MEDIUM, way,
                motion, end, 7);
            game.setManual();
            game.launch();
            steps(game, CreamPie.APPEAR + 0.02);
            return game;
        });
    }

    private static void steps(final CreamPie game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    /**
     * Throws a pie at once and lets it splat, then the target fade and the next one appear.
     */
    private static void onePie(final CreamPie game) {
        game.lookAt();
        steps(game, 0.6);
        steps(game, CreamPie.THROW + CreamPie.SPLAT + CreamPie.FADE + CreamPie.APPEAR + 0.1);
    }

    @Test
    void shouldThrowNothingDuringTheStartDelay() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final CreamPie game = launched(CreamPieThrow.LOOK, CreamPieMotion.STILL, CreamPieEnd.ENDLESS);
        fx(() -> {
            game.lookAt();
            steps(game, 0.5);
            assertEquals(CreamPie.Phase.WAIT, game.getPhase());
            steps(game, 0.6);
            assertEquals(CreamPie.Phase.THROW, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldThrowAfterTheFixationOnly() throws Exception {
        final CreamPie game = launched(CreamPieThrow.FIXATION, CreamPieMotion.STILL, CreamPieEnd.ENDLESS);
        fx(() -> {
            assertEquals(CreamPie.Phase.AIM, game.getPhase());
            game.lookAt();
            steps(game, 0.3);
            game.lookAway();
            steps(game, 0.5);
            assertEquals(CreamPie.Phase.AIM, game.getPhase());
            game.lookAt();
            steps(game, 0.3);
            assertEquals(CreamPie.Phase.AIM, game.getPhase());
            steps(game, 0.24);
            assertEquals(CreamPie.Phase.THROW, game.getPhase());
            verify(gameContext.getSoundManager()).add("data/creampie/sounds/throw.wav");
            return null;
        });
    }

    @Test
    void shouldSplatThePieOnThePictureThenShowAnotherTargetElsewhere() throws Exception {
        final CreamPie game = launched(CreamPieThrow.LOOK, CreamPieMotion.STILL, CreamPieEnd.ENDLESS);
        fx(() -> {
            final double x = game.getTargetX();
            final double y = game.getTargetY();
            verify(stats, times(1)).incrementNumberOfGoalsToReach();
            game.lookAt();
            game.step(0.02);
            assertEquals(CreamPie.Phase.THROW, game.getPhase());
            // Half way, the pie flies above the straight line, and grows
            steps(game, CreamPie.THROW / 2);
            final double pieCenterY = game.getPie().getLayoutY() + game.getPie().getFitHeight() / 2;
            assertTrue(pieCenterY < y, "on an arc");
            assertTrue(game.getPie().getScaleX() > 1);
            steps(game, CreamPie.THROW / 2 + 0.02);
            assertEquals(CreamPie.Phase.SPLAT, game.getPhase());
            assertFalse(game.getPie().isVisible());
            assertTrue(game.getCream().getChildren().size() > 10);
            assertEquals(1, game.getPies());
            verify(stats).incrementNumberOfGoalsReached();
            verify(gameContext.getSoundManager()).add("data/creampie/sounds/splat.wav");
            assertTrue(game.getTargetAOIList().get(0).getTimeEnded() > 0);
            steps(game, CreamPie.SPLAT + CreamPie.FADE + 0.04);
            assertEquals(CreamPie.Phase.APPEAR, game.getPhase());
            assertTrue(Math.hypot(game.getTargetX() - x, game.getTargetY() - y) > game.getRadius() * 2.5);
            assertTrue(game.getCream().getChildren().isEmpty());
            assertTrue(game.getPie().isVisible());
            verify(stats, times(2)).incrementNumberOfGoalsToReach();
            assertEquals(2, game.getTargetAOIList().size());
            return null;
        });
    }

    @Test
    void shouldKeepTheTargetsAboveTheHandAndUnderTheBand() throws Exception {
        final CreamPie game = launched(CreamPieThrow.FIXATION, CreamPieMotion.MOVING, CreamPieEnd.ENDLESS);
        fx(() -> {
            final double x = game.getTargetX();
            final double y = game.getTargetY();
            steps(game, 0.5);
            assertTrue(Math.hypot(game.getTargetX() - x, game.getTargetY() - y) > 1, "it glides");
            for (int i = 0; i < 1000; i++) {
                game.step(0.02);
                final double r = game.getRadius();
                assertTrue(game.getTargetX() - r >= 0 && game.getTargetX() + r <= 1600);
                assertTrue(game.getTargetY() - r >= 900 * 0.12 - 1e-6 && game.getTargetY() <= game.getMaxY() + 1e-6);
            }
            assertTrue(game.getMaxY() + game.getRadius() < 900 * 0.8);
            return null;
        });
    }

    @Test
    void shouldStopAMovingTargetWhenThePieIsThrown() throws Exception {
        final CreamPie game = launched(CreamPieThrow.LOOK, CreamPieMotion.MOVING, CreamPieEnd.ENDLESS);
        fx(() -> {
            game.lookAt();
            game.step(0.02);
            final double x = game.getTargetX();
            final double y = game.getTargetY();
            steps(game, CreamPie.THROW + 0.5);
            assertEquals(x, game.getTargetX(), 1e-9);
            assertEquals(y, game.getTargetY(), 1e-9);
            return null;
        });
    }

    @Test
    void shouldEndWithBravoAfterThePiesOfTheVariant() throws Exception {
        final CreamPie game = launched(CreamPieThrow.LOOK, CreamPieMotion.STILL, CreamPieEnd.PIES_10);
        fx(() -> {
            for (int i = 0; i < 10; i++) {
                onePie(game);
            }
            assertEquals(10, game.getPies());
            assertEquals(CreamPie.Phase.OVER, game.getPhase());
            verify(gameContext).playWinTransition(anyLong(), any());
            verify(stats, times(10)).incrementNumberOfGoalsReached();
            return null;
        });
    }

    @Test
    void shouldStopAndLeaveTheGameAreaWithItsAreasOfInterest() throws Exception {
        final CreamPie game = launched(CreamPieThrow.FIXATION, CreamPieMotion.STILL, CreamPieEnd.ENDLESS);
        fx(() -> {
            assertFalse(gameArea.getChildren().isEmpty());
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertEquals(CreamPie.Phase.OVER, game.getPhase());
            verify(stats).setTargetAOIList(any());
            return null;
        });
    }

    @Test
    void shouldReadTheVariantsAndTheOldShortcut() {
        final Set<IGameVariant> variants = new CreamPieGameVariantGenerator().getVariants();
        assertEquals(2 * 2 * 2 * 4 * 3, variants.size());
        final CreamPie grouped = CreampieGameLauncher.create(gameContext,
            new GroupedGameVariant("PieMyImages", "PieAtOnce", "PieLarge", "images", "PieMoving", "PieEnd20"), stats, 1);
        assertEquals(CreamPiePictures.MY_IMAGES, grouped.getPictures());
        assertEquals(CreamPieThrow.LOOK, grouped.getWay());
        assertEquals(CreamPieSize.LARGE, grouped.getSize());
        assertEquals(CreamPieMotion.MOVING, grouped.getMotion());
        assertEquals(CreamPieEnd.PIES_20, grouped.getEnd());
        final CreamPie old = CreampieGameLauncher.create(gameContext, null, stats, 1);
        assertEquals(CreamPiePictures.ANIMALS, old.getPictures());
        assertEquals(CreamPieSize.MEDIUM, old.getSize());
        assertEquals(CreamPieThrow.FIXATION, old.getWay());
        assertEquals(CreamPieMotion.STILL, old.getMotion());
        assertEquals(CreamPieEnd.ENDLESS, old.getEnd());
    }
}

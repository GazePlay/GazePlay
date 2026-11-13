package net.gazeplay.games.bottle;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
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
 * "Bouteille" (reviewed on 13/11/2026, maintainer's rules): the bottles on the shelves, the delay and the fixation,
 * several balls at once, a series broken then another, the right color (a wrong bottle shakes, a mistake), the sizes,
 * the end, the variants and those of before.
 */
@ExtendWith(ApplicationExtension.class)
class BottleGameTest {

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

    private BottleGame launched(final BottleMode mode, final BottleCount count, final BottleSize size,
                                final BottleEnd end) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final BottleGame game = new BottleGame(gameContext, stats, mode, count, size, end, 8);
            game.setManual();
            game.launch();
            game.step(0.02);
            return game;
        });
    }

    private static void steps(final BottleGame game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    /**
     * Breaks the bottles to break of the series of now: all of them, or those of the color asked.
     */
    private static void breakSeries(final BottleGame game) {
        for (final BottleGame.Bottle bottle : List.copyOf(game.getBottles())) {
            if (game.getMode() == BottleMode.ALL || bottle.getColor() == game.getTarget()) {
                game.choose(bottle);
            }
        }
        steps(game, BottleGame.THROW + 0.04);
    }

    @Test
    void shouldPutTheBottlesOnTwoShelves() throws Exception {
        for (final BottleCount count : BottleCount.values()) {
            final BottleGame game = launched(BottleMode.ALL, count, BottleSize.LARGE, BottleEnd.ENDLESS);
            fx(() -> {
                assertEquals(count.getBottles(), game.getBottles().size());
                assertEquals(count.getBottles(), game.getToBreak());
                return null;
            });
        }
    }

    @Test
    void shouldChooseNothingDuringTheDelayThenAfterTheFixationOnly() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final BottleGame game = launched(BottleMode.ALL, BottleCount.EIGHT, BottleSize.MEDIUM, BottleEnd.ENDLESS);
        fx(() -> {
            final BottleGame.Bottle bottle = game.getBottles().get(0);
            game.choose(bottle);
            assertEquals(BottleGame.State.STANDING, bottle.getState());
            steps(game, 1.02);
            game.lookAt(bottle);
            steps(game, 0.3);
            game.lookAway(bottle);
            steps(game, 0.5);
            assertEquals(BottleGame.State.STANDING, bottle.getState());
            game.lookAt(bottle);
            steps(game, 0.52);
            assertEquals(BottleGame.State.TARGETED, bottle.getState());
            return null;
        });
    }

    @Test
    void shouldThrowSeveralBallsAtOnceAndBreakTheBottles() throws Exception {
        final BottleGame game = launched(BottleMode.ALL, BottleCount.EIGHT, BottleSize.MEDIUM, BottleEnd.ENDLESS);
        fx(() -> {
            final BottleGame.Bottle first = game.getBottles().get(0);
            final BottleGame.Bottle second = game.getBottles().get(5);
            game.choose(first);
            game.choose(second);
            assertEquals(2, game.getBallsInFlight());
            // A bottle aimed at is not chosen again
            game.choose(first);
            assertEquals(2, game.getBallsInFlight());
            steps(game, BottleGame.THROW + 0.04);
            assertEquals(BottleGame.State.BROKEN, first.getState());
            assertEquals(BottleGame.State.BROKEN, second.getState());
            assertEquals(2, game.getBroken());
            verify(stats, times(2)).incrementNumberOfGoalsReached();
            verify(gameContext.getSoundManager(), times(2)).add("data/bottle/sounds/glass.wav");
            return null;
        });
    }

    @Test
    void shouldStartAnotherSeriesWhenAllAreBroken() throws Exception {
        final BottleGame game = launched(BottleMode.ALL, BottleCount.FOUR, BottleSize.SMALL, BottleEnd.ENDLESS);
        fx(() -> {
            breakSeries(game);
            assertEquals(BottleGame.Phase.REWARD, game.getPhase());
            assertEquals(1, game.getSeries());
            verify(gameContext.getSoundManager()).add("data/goosegame/sounds/win.wav");
            steps(game, BottleGame.REWARD + 0.04);
            assertEquals(BottleGame.Phase.PLAY, game.getPhase());
            assertEquals(4, game.getBottles().size());
            assertTrue(game.getBottles().stream().allMatch(b -> b.getState() == BottleGame.State.STANDING));
            verify(stats, times(2)).incrementNumberOfGoalsToReach(4);
            return null;
        });
    }

    @Test
    void shouldBreakOnlyTheColorAskedAndShakeTheOthers() throws Exception {
        final BottleGame game = launched(BottleMode.COLOR, BottleCount.TWELVE, BottleSize.MEDIUM, BottleEnd.ENDLESS);
        fx(() -> {
            final BottleColor first = game.getTarget();
            assertNotNull(first);
            assertEquals(3, game.getToBreak());
            assertEquals(3, game.getBottles().stream().filter(b -> b.getColor() == first).count());
            final BottleGame.Bottle wrong = game.getBottles().stream().filter(b -> b.getColor() != first)
                .findFirst().orElseThrow();
            game.choose(wrong);
            assertEquals(0, game.getBallsInFlight());
            assertTrue(wrong.isShaking());
            assertEquals(BottleGame.State.STANDING, wrong.getState());
            verify(stats).incrementNumberOfMistakes();
            verify(gameContext.getSoundManager()).add("data/dice/sounds/wrong.wav");
            steps(game, BottleGame.SHAKE + 0.04);
            assertFalse(wrong.isShaking());
            breakSeries(game);
            assertEquals(BottleGame.Phase.REWARD, game.getPhase());
            steps(game, BottleGame.REWARD + 0.04);
            // Another color
            assertNotEquals(first, game.getTarget());
            return null;
        });
    }

    @Test
    void shouldEndWithBravoAfterTheSeriesOfTheVariant() throws Exception {
        final BottleGame game = launched(BottleMode.COLOR, BottleCount.EIGHT, BottleSize.LARGE, BottleEnd.SERIES_3);
        fx(() -> {
            for (int i = 0; i < 3; i++) {
                breakSeries(game);
                steps(game, BottleGame.REWARD + 0.04);
            }
            assertEquals(3, game.getSeries());
            assertEquals(BottleGame.Phase.OVER, game.getPhase());
            verify(gameContext).playWinTransition(anyLong(), any());
            return null;
        });
    }

    @Test
    void shouldStopAndLeaveTheGameArea() throws Exception {
        final BottleGame game = launched(BottleMode.ALL, BottleCount.EIGHT, BottleSize.MEDIUM, BottleEnd.ENDLESS);
        fx(() -> {
            game.choose(game.getBottles().get(0));
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertTrue(game.getBottles().isEmpty());
            assertEquals(0, game.getBallsInFlight());
            assertEquals(BottleGame.Phase.OVER, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldReadTheVariantsAndThoseOfBeforeTheReview() {
        final Set<IGameVariant> variants = new BottleGameVariantGenerator().getVariants();
        assertEquals(2 * 4 * 3 * 4, variants.size());
        final BottleGame color = BottleGameLauncher.create(gameContext,
            new GroupedGameVariant("BottleColorMode", "BottleEnd5", "Bottles12", null, "BottleLarge"), stats, 1);
        assertEquals(BottleMode.COLOR, color.getMode());
        assertEquals(BottleEnd.SERIES_5, color.getEnd());
        assertEquals(BottleCount.TWELVE, color.getCount());
        assertEquals(BottleSize.LARGE, color.getSize());
        final BottleGame infinity = BottleGameLauncher.create(gameContext,
            new IntStringGameVariant(16, "InfinityBottles"), stats, 1);
        assertEquals(BottleMode.ALL, infinity.getMode());
        assertEquals(BottleEnd.ENDLESS, infinity.getEnd());
        assertEquals(BottleCount.SIXTEEN, infinity.getCount());
        assertEquals(BottleSize.MEDIUM, infinity.getSize());
        final BottleGame tiny = BottleGameLauncher.create(gameContext, new IntStringGameVariant(4, "TinyBottles"),
            stats, 1);
        assertEquals(BottleSize.SMALL, tiny.getSize());
        assertEquals(BottleEnd.SERIES_1, tiny.getEnd());
    }
}

package net.gazeplay.games.colorblend;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.colorblend.PaintMix.Pot;
import net.gazeplay.games.colorblend.PaintMix.Target;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Mélanger les couleurs" (reviewed on 13/11/2026, maintainer's rules): the mix of a painter, the delay and the
 * fixation, a color made in any order, a wrong mix (a mistake, the pot emptied, the same color, the hint), the free
 * mode, the end, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class ColorBlendTest {

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

    private ColorBlend launched(final ColorBlendMode mode, final ColorBlendLevel level, final ColorBlendEnd end)
        throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final ColorBlend game = new ColorBlend(gameContext, stats, mode, level, end, 8);
            game.setManual();
            game.launch();
            game.step(0.02);
            return game;
        });
    }

    private static void steps(final ColorBlend game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    /**
     * Pours a pot and waits for the paint to be mixed.
     */
    private static void pour(final ColorBlend game, final Pot pot) {
        game.choose(game.choice(pot));
        steps(game, ColorBlend.POUR + 0.04);
    }

    private static Pot wrongPot(final ColorBlend game, final List<Pot> recipe) {
        return game.getLevel().getPots().stream().filter(p -> !recipe.contains(p)).findFirst().orElseThrow();
    }

    @Test
    void shouldMixAsAPainter() {
        final Color green = PaintMix.mix(List.of(Pot.YELLOW, Pot.BLUE));
        assertTrue(green.getGreen() > green.getRed() && green.getGreen() > green.getBlue(), green.toString());
        final Color orange = PaintMix.mix(List.of(Pot.RED, Pot.YELLOW));
        assertTrue(orange.getRed() > orange.getGreen() && orange.getGreen() > orange.getBlue(), orange.toString());
        final Color purple = PaintMix.mix(List.of(Pot.RED, Pot.BLUE));
        assertTrue(purple.getRed() > purple.getGreen() && purple.getBlue() > purple.getGreen(), purple.toString());
        // The order does not matter; white lightens, black darkens
        assertEquals(green, PaintMix.mix(List.of(Pot.BLUE, Pot.YELLOW)));
        final Color pink = PaintMix.mix(List.of(Pot.RED, Pot.WHITE));
        assertTrue(pink.getBrightness() >= PaintMix.color(Pot.RED).getBrightness());
        assertTrue(pink.getSaturation() < PaintMix.color(Pot.RED).getSaturation());
        assertTrue(PaintMix.mix(List.of(Pot.RED, Pot.BLACK)).getBrightness()
            < PaintMix.color(Pot.RED).getBrightness());
        final Color grey = PaintMix.mix(List.of(Pot.WHITE, Pot.BLACK));
        assertEquals(grey.getRed(), grey.getBlue(), 1e-9);
        // Each color to make differs from the others
        for (final Target a : Target.values()) {
            for (final Target b : Target.values()) {
                if (a != b) {
                    assertNotEquals(a.color(), b.color(), a + " " + b);
                }
            }
            assertEquals(a.getKey(), PaintMix.name(a.getRecipe()));
        }
        assertNull(PaintMix.name(List.of(Pot.YELLOW, Pot.YELLOW, Pot.BLUE)));
    }

    @Test
    void shouldChooseNothingDuringTheDelayThenAfterTheFixationOnly() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final ColorBlend game = launched(ColorBlendMode.CHALLENGE, ColorBlendLevel.EASY, ColorBlendEnd.ENDLESS);
        fx(() -> {
            final ColorBlend.Choice pot = game.choice(Pot.RED);
            game.choose(pot);
            assertTrue(game.getMix().isEmpty());
            steps(game, 1.02);
            assertEquals(ColorBlend.Phase.CHOOSE, game.getPhase());
            game.lookAt(pot);
            steps(game, 0.3);
            game.lookAway(pot);
            steps(game, 0.5);
            assertTrue(game.getMix().isEmpty());
            game.lookAt(pot);
            steps(game, 0.52);
            assertEquals(List.of(Pot.RED), game.getMix());
            assertEquals(ColorBlend.Phase.POUR, game.getPhase());
            // Nothing is chosen while the pot is poured
            game.choose(game.choice(Pot.BLUE));
            assertEquals(1, game.getMix().size());
            return null;
        });
    }

    @Test
    void shouldMakeTheColorInAnyOrderThenAskAnother() throws Exception {
        final ColorBlend game = launched(ColorBlendMode.CHALLENGE, ColorBlendLevel.EASY, ColorBlendEnd.ENDLESS);
        fx(() -> {
            final Target first = game.getTarget();
            assertTrue(ColorBlendLevel.EASY.getTargets().contains(first));
            final List<Pot> reversed = new ArrayList<>(first.getRecipe());
            java.util.Collections.reverse(reversed);
            pour(game, reversed.get(0));
            assertEquals(ColorBlend.Phase.CHOOSE, game.getPhase());
            assertEquals(1, game.choice(reversed.get(0)).getCount());
            pour(game, reversed.get(1));
            assertEquals(ColorBlend.Phase.REWARD, game.getPhase());
            assertEquals(first.color(), game.getPotColor());
            assertEquals(1, game.getDone());
            verify(stats).incrementNumberOfGoalsReached();
            verify(gameContext.getSoundManager()).add("data/goosegame/sounds/win.wav");
            game.step(0.02);
            assertTrue(game.getMessages().contains("ColorBlendWell"));
            steps(game, ColorBlend.REWARD + 0.04);
            assertEquals(ColorBlend.Phase.CHOOSE, game.getPhase());
            // Another color, never the same twice in a row, and an empty pot
            assertNotEquals(first, game.getTarget());
            assertTrue(game.getMix().isEmpty());
            verify(stats, times(2)).incrementNumberOfGoalsToReach();
            return null;
        });
    }

    @Test
    void shouldCountAWrongMixShowItThenAskTheSameColorWithAHintAfterTwo() throws Exception {
        final ColorBlend game = launched(ColorBlendMode.CHALLENGE, ColorBlendLevel.HARD, ColorBlendEnd.ENDLESS);
        fx(() -> {
            final Target target = game.getTarget();
            final List<Pot> recipe = target.getRecipe();
            assertEquals(3, recipe.size());
            for (int attempt = 1; attempt <= 2; attempt++) {
                pour(game, recipe.get(0));
                pour(game, recipe.get(1));
                pour(game, wrongPot(game, recipe));
                assertEquals(ColorBlend.Phase.RESULT, game.getPhase());
                assertTrue(game.getMessages().contains("ColorBlendNotYet"));
                assertNotEquals(target.color(), game.getPotColor());
                verify(stats, times(attempt)).incrementNumberOfMistakes();
                steps(game, ColorBlend.RESULT + 0.04);
                assertEquals(ColorBlend.Phase.CHOOSE, game.getPhase());
                assertTrue(game.getMix().isEmpty());
                assertEquals(target, game.getTarget());
            }
            // After two mistakes, the pots of the recipe are framed, the others not
            for (final Pot pot : game.getLevel().getPots()) {
                assertEquals(recipe.contains(pot), game.choice(pot).isHinted(), pot.toString());
            }
            verify(gameContext.getSoundManager(), times(2)).add("data/dice/sounds/wrong.wav");
            verify(stats, never()).incrementNumberOfGoalsReached();
            return null;
        });
    }

    @Test
    void shouldNameAWrongMixWhenItIsAKnownColor() throws Exception {
        final ColorBlend game = launched(ColorBlendMode.CHALLENGE, ColorBlendLevel.EASY, ColorBlendEnd.ENDLESS);
        fx(() -> {
            final Target other = ColorBlendLevel.EASY.getTargets().stream().filter(t -> t != game.getTarget())
                .findFirst().orElseThrow();
            pour(game, other.getRecipe().get(0));
            pour(game, other.getRecipe().get(1));
            assertEquals(ColorBlend.Phase.RESULT, game.getPhase());
            assertTrue(game.getMessages().contains(other.getKey()), game.getMessages().toString());
            return null;
        });
    }

    @Test
    void shouldMixFreelyNameKnownColorsAndEmptyThePot() throws Exception {
        final ColorBlend game = launched(ColorBlendMode.FREE, ColorBlendLevel.FREE, ColorBlendEnd.COLORS_5);
        fx(() -> {
            assertEquals(ColorBlendEnd.ENDLESS, game.getEnd());
            assertEquals(5, game.getLevel().getPots().size());
            assertNull(game.getTarget());
            assertNotNull(game.getEmpty());
            pour(game, Pot.YELLOW);
            assertEquals(List.of("ColorBlendYellow"), game.getMessages());
            pour(game, Pot.BLUE);
            assertEquals(List.of("ColorBlendGreen"), game.getMessages());
            pour(game, Pot.YELLOW);
            assertTrue(game.getMessages().isEmpty());
            assertEquals(2, game.choice(Pot.YELLOW).getCount());
            assertEquals(PaintMix.mix(List.of(Pot.YELLOW, Pot.BLUE, Pot.YELLOW)), game.getPotColor());
            assertEquals(ColorBlend.Phase.CHOOSE, game.getPhase());
            game.choose(game.getEmpty());
            assertTrue(game.getMix().isEmpty());
            assertEquals(0, game.choice(Pot.YELLOW).getCount());
            verify(stats, never()).incrementNumberOfMistakes();
            verify(stats, never()).incrementNumberOfGoalsToReach();
            return null;
        });
    }

    @Test
    void shouldEndWithBravoAfterTheColorsOfTheVariant() throws Exception {
        final ColorBlend game = launched(ColorBlendMode.CHALLENGE, ColorBlendLevel.NORMAL, ColorBlendEnd.COLORS_5);
        fx(() -> {
            for (int i = 0; i < 5; i++) {
                for (final Pot pot : game.getTarget().getRecipe()) {
                    pour(game, pot);
                }
                steps(game, ColorBlend.REWARD + 0.04);
            }
            assertEquals(5, game.getDone());
            assertEquals(ColorBlend.Phase.OVER, game.getPhase());
            verify(gameContext).playWinTransition(anyLong(), any());
            return null;
        });
    }

    @Test
    void shouldStopAndLeaveTheGameArea() throws Exception {
        final ColorBlend game = launched(ColorBlendMode.CHALLENGE, ColorBlendLevel.EASY, ColorBlendEnd.ENDLESS);
        fx(() -> {
            assertFalse(gameArea.getChildren().isEmpty());
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertTrue(game.getChoices().isEmpty());
            assertEquals(ColorBlend.Phase.OVER, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldReadTheVariantsAndALaunchWithoutVariant() {
        final Set<IGameVariant> variants = new ColorBlendVariantGenerator().getVariants();
        assertEquals(3 * 4 + 1, variants.size());
        final ColorBlend hard = ColorBlendLauncher.create(gameContext,
            new GroupedGameVariant("ColorBlendChallenge", "ColorBlendEnd10", "ColorBlendHard", null), stats, 1);
        assertEquals(ColorBlendMode.CHALLENGE, hard.getMode());
        assertEquals(ColorBlendLevel.HARD, hard.getLevel());
        assertEquals(ColorBlendEnd.COLORS_10, hard.getEnd());
        // The game had no variant before its review: the free mode
        final ColorBlend before = ColorBlendLauncher.create(gameContext, null, stats, 1);
        assertEquals(ColorBlendMode.FREE, before.getMode());
        assertEquals(ColorBlendLevel.FREE, before.getLevel());
    }
}

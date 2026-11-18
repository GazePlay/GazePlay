package net.gazeplay.games.colorblend;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
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
            launchedGames.add(game).launch();
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

    /**
     * 3 lives in a challenge with an end (maintainer's request of 16/11/2026): a wrong mix costs one, the hint still
     * comes after two; the third loses the game: "Lost!", then the statistics.
     */
    @Test
    void shouldLoseALifeAtEachWrongMixThenTheGame() throws Exception {
        final ColorBlend game = launched(ColorBlendMode.CHALLENGE, ColorBlendLevel.EASY, ColorBlendEnd.COLORS_10);
        fx(() -> {
            assertTrue(game.hasLives());
            assertEquals(ColorBlend.LIVES, game.getLives());
            final List<Pot> recipe = game.getTarget().getRecipe();
            for (int attempt = 1; attempt <= 3; attempt++) {
                pour(game, recipe.get(0));
                pour(game, wrongPot(game, recipe));
                assertEquals(ColorBlend.LIVES - attempt, game.getLives());
                steps(game, ColorBlend.RESULT + 0.04);
                if (attempt == 2) {
                    assertTrue(game.choice(recipe.get(0)).isHinted(), "the hint after two mistakes");
                }
            }
            assertEquals(ColorBlend.Phase.LOST, game.getPhase());
            assertTrue(game.getMessages().contains("ColorBlendLost"));
            verify(gameContext, never()).showRoundStats(any(), any());
            steps(game, ColorBlend.LOST_SHOWN + 0.04);
            assertEquals(ColorBlend.Phase.OVER, game.getPhase());
            verify(gameContext).showRoundStats(stats, game);
            return null;
        });
    }

    /**
     * Without end too since 17/11/2026: with the hearts, the third wrong mix loses the game; without them, never.
     */
    @Test
    void shouldHaveLivesWithoutEndToo() throws Exception {
        final ColorBlend endless = launched(ColorBlendMode.CHALLENGE, ColorBlendLevel.EASY, ColorBlendEnd.ENDLESS);
        assertTrue(endless.hasLives());
        fx(() -> {
            final List<Pot> recipe = endless.getTarget().getRecipe();
            for (int attempt = 1; attempt <= 3; attempt++) {
                pour(endless, recipe.get(0));
                pour(endless, wrongPot(endless, recipe));
                steps(endless, ColorBlend.RESULT + 0.04);
            }
            assertEquals(ColorBlend.Phase.LOST, endless.getPhase());
            return null;
        });
        final ColorBlend without = fx(() -> {
            gameArea.getChildren().clear();
            final ColorBlend noHearts = new ColorBlend(gameContext, stats, ColorBlendMode.CHALLENGE,
                ColorBlendLevel.EASY, ColorBlendEnd.ENDLESS, 3);
            noHearts.setWithLives(false);
            noHearts.setManual();
            launchedGames.add(noHearts).launch();
            noHearts.step(0.02);
            return noHearts;
        });
        assertFalse(without.hasLives());
        fx(() -> {
            final List<Pot> recipe = without.getTarget().getRecipe();
            for (int attempt = 1; attempt <= 4; attempt++) {
                pour(without, recipe.get(0));
                pour(without, wrongPot(without, recipe));
                steps(without, ColorBlend.RESULT + 0.04);
            }
            assertEquals(ColorBlend.Phase.CHOOSE, without.getPhase());
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
        // One mode (the free one removed on 17/11/2026), the hearts with or without with every end (17/11/2026)
        assertEquals(3 * 4 * 2, variants.size());
        assertTrue(ColorBlendLauncher.create(gameContext,
            new GroupedGameVariant("ColorBlendChallenge", "ColorBlendEndless", "ColorBlendHard", null,
                LivesVariant.WITH), stats, 1).hasLives());
        assertEquals(List.of(ColorBlendMode.CHALLENGE), List.of(ColorBlendMode.values()));
        assertEquals(List.of(ColorBlendLevel.EASY, ColorBlendLevel.NORMAL, ColorBlendLevel.HARD),
            List.of(ColorBlendLevel.values()));
        // A variant of the free mode of before: played as a challenge, easy (the level "the 5 pots" is no more)
        final ColorBlend free = ColorBlendLauncher.create(gameContext,
            new GroupedGameVariant("ColorBlendFree", "ColorBlendEndless", "ColorBlendAllPots", null), stats, 1);
        assertEquals(ColorBlendMode.CHALLENGE, free.getMode());
        assertEquals(ColorBlendLevel.EASY, free.getLevel());
        assertTrue(ColorBlendLauncher.create(gameContext,
            new GroupedGameVariant("ColorBlendChallenge", "ColorBlendEnd10", "ColorBlendHard", null,
                LivesVariant.WITH), stats, 1).hasLives());
        assertFalse(ColorBlendLauncher.create(gameContext,
            new GroupedGameVariant("ColorBlendChallenge", "ColorBlendEnd10", "ColorBlendHard", null,
                LivesVariant.WITHOUT), stats, 1).hasLives());
        final ColorBlend hard = ColorBlendLauncher.create(gameContext,
            new GroupedGameVariant("ColorBlendChallenge", "ColorBlendEnd10", "ColorBlendHard", null), stats, 1);
        assertEquals(ColorBlendMode.CHALLENGE, hard.getMode());
        assertEquals(ColorBlendLevel.HARD, hard.getLevel());
        assertEquals(ColorBlendEnd.COLORS_10, hard.getEnd());
        // The game had no variant before its review: the challenge, easy, without end
        final ColorBlend before = ColorBlendLauncher.create(gameContext, null, stats, 1);
        assertEquals(ColorBlendMode.CHALLENGE, before.getMode());
        assertEquals(ColorBlendLevel.EASY, before.getLevel());
        assertEquals(ColorBlendEnd.ENDLESS, before.getEnd());
    }
}

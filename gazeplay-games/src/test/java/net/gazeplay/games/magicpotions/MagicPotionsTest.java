package net.gazeplay.games.magicpotions;

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

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Magic Potions" (name code Potions; reviewed on 10/11/2026, maintainer's rules): the recipes, a potion poured at the
 * speed of the settings after a fixation of the length of the settings, the cauldron taking the color of the mixture,
 * the client served or the cauldron exploding, the lives, the ends, the recipe shown or not, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class MagicPotionsTest {

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
        when(configuration.isBackgroundEnabled()).thenReturn(true);
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1920, 1080));
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private MagicPotions playing(final PotionsLevel level, final PotionsEnd end, final double seed) throws Exception {
        final MagicPotions game = fx(() -> {
            gameArea.getChildren().clear();
            final MagicPotions created = new MagicPotions(gameContext, stats, level, end, seed);
            created.setManual();
            created.launch();
            return created;
        });
        steps(game, 0.05);
        assertEquals(MagicPotions.Phase.PLAY, game.getPhase());
        return game;
    }

    private static void steps(final MagicPotions game, final double seconds) throws Exception {
        fx(() -> {
            for (double t = 0; t < seconds; t += STEP) {
                game.step(STEP);
            }
            return null;
        });
    }

    private static void pour(final MagicPotions game, final PotionMix.Primary primary) throws Exception {
        fx(() -> {
            game.choose(primary);
            return null;
        });
        steps(game, MagicPotions.POUR + 0.05);
    }

    /**
     * Serves the client its potion.
     */
    private static void serve(final MagicPotions game) throws Exception {
        for (final PotionMix.Primary primary : game.getRequest().getRecipe()) {
            pour(game, primary);
        }
        assertEquals(MagicPotions.Phase.SERVED, game.getPhase());
        steps(game, MagicPotions.RESULT + 0.05);
    }

    private static PotionMix.Primary outOfRecipe(final MagicPotions game) {
        for (final PotionMix.Primary primary : PotionMix.Primary.values()) {
            if (!game.getRequest().getRecipe().contains(primary)) {
                return primary;
            }
        }
        return null;
    }

    @Test
    void shouldMixTheColorsAsPaints() {
        assertEquals(PotionMix.ORANGE, PotionMix.of(EnumSet.of(PotionMix.Primary.RED, PotionMix.Primary.YELLOW)));
        assertEquals(PotionMix.GREEN, PotionMix.of(EnumSet.of(PotionMix.Primary.BLUE, PotionMix.Primary.YELLOW)));
        assertEquals(PotionMix.PURPLE, PotionMix.of(EnumSet.of(PotionMix.Primary.RED, PotionMix.Primary.BLUE)));
        assertEquals(PotionMix.BROWN, PotionMix.of(EnumSet.allOf(PotionMix.Primary.class)));
        assertEquals(PotionMix.BLUE, PotionMix.of(EnumSet.of(PotionMix.Primary.BLUE)));
        assertNull(PotionMix.of(EnumSet.noneOf(PotionMix.Primary.class)));
    }

    @Test
    void shouldPourAPotionAfterAFixationOfTheLengthOfTheSettingsReadAtEachStep() throws Exception {
        final MagicPotions game = playing(PotionsLevel.WITH_RECIPE, PotionsEnd.ENDLESS, 3);
        final PotionMix.Primary first = game.getRequest().getRecipe().iterator().next();
        fx(() -> {
            game.lookAt(first);
            return null;
        });
        steps(game, 0.5);
        assertEquals(MagicPotions.Phase.PLAY, game.getPhase());
        when(configuration.getFixationLength()).thenReturn(600);
        steps(game, 0.25);
        assertEquals(MagicPotions.Phase.PLAY, game.getPhase());
        steps(game, 0.07);
        assertEquals(MagicPotions.Phase.POUR, game.getPhase());
        // Poured in the middle of its way, at the speed of the settings
        assertTrue(game.getMixture().isEmpty());
        when(configuration.getAnimationSpeedRatio()).thenReturn(2.0);
        steps(game, MagicPotions.POUR / 4 + 0.05);
        assertTrue(game.getMixture().contains(first));
        verify(gameContext.getSoundManager()).add("data/potions/sounds/pour.wav");
        steps(game, MagicPotions.POUR / 4);
        assertNotEquals(MagicPotions.Phase.POUR, game.getPhase());
    }

    @Test
    void shouldServeTheClientWhenTheRecipeIsCompleteInAnyOrder() throws Exception {
        for (int seed = 1; seed <= 30; seed++) {
            final MagicPotions game = playing(PotionsLevel.WITH_RECIPE, PotionsEnd.ENDLESS, seed * 7919.37);
            if (game.getRequest().getRecipe().size() < 2) {
                continue;
            }
            final PotionMix asked = game.getRequest();
            serve(game);
            assertEquals(1, game.getServed());
            verify(stats).incrementNumberOfGoalsReached();
            verify(gameContext.getSoundManager()).add("data/potions/sounds/served.wav");
            assertNotSame(asked, game.getRequest());
            assertEquals(MagicPotions.Phase.PLAY, game.getPhase());
            return;
        }
        fail("no mixture asked for in 30 games");
    }

    @Test
    void shouldColorTheCauldronWithTheMixture() throws Exception {
        for (int seed = 1; seed <= 30; seed++) {
            final MagicPotions game = playing(PotionsLevel.WITH_RECIPE, PotionsEnd.ENDLESS, seed * 7919.37);
            if (game.getRequest() != PotionMix.BROWN) {
                continue;
            }
            pour(game, PotionMix.Primary.RED);
            assertEquals(PotionMix.RED.getColor(), game.potColor());
            pour(game, PotionMix.Primary.BLUE);
            assertEquals(PotionMix.PURPLE.getColor(), game.potColor());
            pour(game, PotionMix.Primary.YELLOW);
            assertEquals(PotionMix.BROWN.getColor(), game.potColor());
            assertEquals(MagicPotions.Phase.SERVED, game.getPhase());
            return;
        }
        fail("brown never asked for in 30 games");
    }

    @Test
    void shouldExplodeWithAPotionOutOfTheRecipeAndCostALife() throws Exception {
        for (int seed = 1; seed <= 30; seed++) {
            final MagicPotions game = playing(PotionsLevel.WITHOUT_RECIPE, PotionsEnd.ENDLESS, seed * 7919.37);
            final PotionMix.Primary wrong = outOfRecipe(game);
            if (wrong == null) {
                continue;
            }
            pour(game, wrong);
            assertEquals(MagicPotions.Phase.FAILED, game.getPhase());
            assertTrue(game.isExploding());
            assertEquals(MagicPotions.LIVES - 1, game.getLives());
            verify(gameContext.getSoundManager()).add("data/potions/sounds/boom.wav");
            verify(stats, never()).incrementNumberOfGoalsReached();
            steps(game, MagicPotions.RESULT + 0.05);
            assertEquals(MagicPotions.Phase.PLAY, game.getPhase());
            assertTrue(game.getMixture().isEmpty());
            verify(stats, times(2)).incrementNumberOfGoalsToReach();
            return;
        }
        fail("only brown asked for in 30 games");
    }

    @Test
    void shouldNotPourAPotionTwiceNorWhileAnotherIsPoured() throws Exception {
        for (int seed = 1; seed <= 30; seed++) {
            final MagicPotions game = playing(PotionsLevel.WITH_RECIPE, PotionsEnd.ENDLESS, seed * 7919.37);
            if (game.getRequest().getRecipe().size() < 2) {
                continue;
            }
            final PotionMix.Primary first = game.getRequest().getRecipe().iterator().next();
            fx(() -> {
                game.choose(first);
                game.choose(PotionMix.Primary.values()[(first.ordinal() + 1) % 3]);
                return null;
            });
            steps(game, MagicPotions.POUR + 0.05);
            assertEquals(Set.of(first), game.getMixture());
            pour(game, first);
            assertEquals(Set.of(first), game.getMixture());
            assertEquals(MagicPotions.Phase.PLAY, game.getPhase());
            return;
        }
        fail("no mixture asked for in 30 games");
    }

    @Test
    void shouldEndWithLostAfterThreeExplosionsAndNoBravo() throws Exception {
        final MagicPotions game = playing(PotionsLevel.WITHOUT_RECIPE, PotionsEnd.ENDLESS, 11);
        int explosions = 0;
        for (int round = 0; round < 40 && explosions < MagicPotions.LIVES; round++) {
            final PotionMix.Primary wrong = outOfRecipe(game);
            if (wrong == null) {
                serve(game);
                continue;
            }
            pour(game, wrong);
            steps(game, MagicPotions.RESULT + 0.05);
            explosions++;
        }
        assertEquals(MagicPotions.Phase.SHOW, game.getPhase());
        steps(game, MagicPotions.SHOW + 0.05);
        assertFalse(game.isBravo());
        verify(gameContext, never()).playWinTransition(anyLong(), any());
        verify(gameContext).showRoundStats(any(), any());
    }

    @Test
    void shouldEndWithBravoAfterThePotionsToServe() throws Exception {
        final MagicPotions game = playing(PotionsLevel.WITH_RECIPE, PotionsEnd.SERVED_3, 13);
        for (int potion = 0; potion < 3; potion++) {
            serve(game);
        }
        assertEquals(MagicPotions.Phase.OVER, game.getPhase());
        assertTrue(game.isBravo());
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldShowTheRecipeOnlyWithTheRecipe() throws Exception {
        final MagicPotions with = playing(PotionsLevel.WITH_RECIPE, PotionsEnd.ENDLESS, 15);
        assertEquals(with.getRequest().getRecipe().size(), with.recipeShown());
        final MagicPotions without = playing(PotionsLevel.WITHOUT_RECIPE, PotionsEnd.ENDLESS, 15);
        assertEquals(0, without.recipeShown());
    }

    /**
     * Maintainer's remark after his trial of 10/11/2026: only the round body of a flask reacted to the gaze.
     */
    @Test
    void shouldLookAtTheWholeFlaskItsNeckToo() throws Exception {
        final MagicPotions game = playing(PotionsLevel.WITH_RECIPE, PotionsEnd.ENDLESS, 3);
        double right = 0;
        for (final PotionMix.Primary primary : PotionMix.Primary.values()) {
            final var zone = game.zoneOf(primary).getBoundsInParent();
            final var picture = game.potionOf(primary).getBoundsInParent();
            assertTrue(zone.contains(picture), primary.name());
            // The neck, at the top middle of the picture
            assertTrue(zone.contains(picture.getMinX() + picture.getWidth() / 2, picture.getMinY() + 2),
                primary.name());
            assertTrue(game.potionOf(primary).isMouseTransparent());
            assertTrue(zone.getMinX() > right, primary.name());
            right = zone.getMaxX();
        }
    }

    @Test
    void shouldStopAfterDispose() throws Exception {
        final MagicPotions game = playing(PotionsLevel.WITH_RECIPE, PotionsEnd.ENDLESS, 2);
        fx(() -> {
            game.dispose();
            return null;
        });
        steps(game, 2);
        assertEquals(MagicPotions.Phase.OVER, game.getPhase());
        assertTrue(gameArea.getChildren().isEmpty());
        verify(gameContext.getGazeDeviceManager(), times(3)).removeEventFilter(any());
    }

    @Test
    void shouldGiveAllTheVariantsAndPlayAnOldGame() {
        final Set<IGameVariant> variants = new PotionsGameVariantGenerator().getVariants();
        assertEquals(8, variants.size());
        final IGameVariant variant = new GroupedGameVariant(PotionsGameVariantGenerator.GROUP,
            PotionsLevel.WITHOUT_RECIPE.getKey(), PotionsEnd.SERVED_10.getKey(), null);
        assertTrue(variants.contains(variant));
        assertEquals(PotionsLevel.WITHOUT_RECIPE, PotionsGameLauncher.levelOf(variant));
        assertEquals(PotionsEnd.SERVED_10, PotionsGameLauncher.endOf(variant));
        assertEquals(PotionsLevel.WITH_RECIPE, PotionsGameLauncher.levelOf(null));
        assertEquals(PotionsEnd.ENDLESS, PotionsGameLauncher.endOf(null));
    }
}

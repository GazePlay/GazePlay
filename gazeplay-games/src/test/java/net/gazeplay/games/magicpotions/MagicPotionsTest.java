package net.gazeplay.games.magicpotions;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
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
 * "Recettes magiques" (name code Potions; Magic Potions until 24/11/2026, maintainer's rules): the recipes of 2, 3 or 4
 * ingredients and the shelf, an ingredient flying into the cauldron after a fixation of the length of the settings, the
 * ingredients out of the recipe, the explosion only for a recipe complete, the lives, the ends, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class MagicPotionsTest {

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
        when(configuration.isBackgroundEnabled()).thenReturn(true);
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1920, 1080));
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private MagicPotions playing(final RecipeSize size, final PotionsEnd end, final double seed) throws Exception {
        final MagicPotions game = fx(() -> {
            gameArea.getChildren().clear();
            final MagicPotions created = new MagicPotions(gameContext, stats, size, end, seed);
            created.setManual();
            launchedGames.add(created).launch();
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

    private static void choose(final MagicPotions game, final Ingredient ingredient) throws Exception {
        fx(() -> {
            game.choose(ingredient);
            return null;
        });
    }

    private static void put(final MagicPotions game, final Ingredient ingredient) throws Exception {
        choose(game, ingredient);
        steps(game, MagicPotions.FLY + 0.05);
    }

    /**
     * Makes the recipe and serves the client.
     */
    private static void serve(final MagicPotions game) throws Exception {
        for (final Ingredient ingredient : List.copyOf(game.getRecipe())) {
            put(game, ingredient);
        }
        assertEquals(MagicPotions.Phase.FUSION, game.getPhase());
        steps(game, MagicPotions.FUSION + MagicPotions.DRINK + MagicPotions.SERVED + 0.1);
    }

    private static List<Ingredient> outOfRecipe(final MagicPotions game) {
        return game.getShelf().stream().filter(i -> !game.getRecipe().contains(i)).toList();
    }

    private static void refuse(final MagicPotions game, final Ingredient ingredient) throws Exception {
        choose(game, ingredient);
        steps(game, MagicPotions.SHAKE + 0.05);
    }

    @Test
    void shouldShowARecipeOfTheSizeChosenAndTwoOtherIngredientsOnTheShelf() throws Exception {
        for (final RecipeSize size : RecipeSize.values()) {
            final MagicPotions game = playing(size, PotionsEnd.ENDLESS, 5);
            assertEquals(size.getIngredients(), game.getRecipe().size());
            assertEquals(size.getIngredients(), new HashSet<>(game.getRecipe()).size(), "no ingredient twice");
            assertEquals(size.getIngredients() + 2, game.getShelf().size());
            assertEquals(game.getShelf().size(), new HashSet<>(game.getShelf()).size());
            assertTrue(game.getShelf().containsAll(game.getRecipe()));
            assertEquals(2, outOfRecipe(game).size());
            assertEquals(size.getIngredients(), game.recipeShown());
            assertEquals(0, game.ticked());
        }
    }

    @Test
    void shouldPutAnIngredientAfterAFixationOfTheLengthOfTheSettingsReadAtEachStep() throws Exception {
        final MagicPotions game = playing(RecipeSize.TWO, PotionsEnd.ENDLESS, 3);
        final Ingredient first = game.getRecipe().getFirst();
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
        assertEquals(MagicPotions.Phase.FLY, game.getPhase());
        assertTrue(game.getAdded().isEmpty(), "in the cauldron at the end of its flight");
        when(configuration.getAnimationSpeedRatio()).thenReturn(2.0);
        steps(game, MagicPotions.FLY / 2 + 0.05);
        assertEquals(Set.of(first), game.getAdded());
        assertEquals(1, game.ticked());
        // An empty card stays on the shelf
        assertFalse(game.cardOf(first).getChildren().get(1).isVisible());
        verify(gameContext.getSoundManager()).add("data/potions/sounds/pour.wav");
        assertEquals(MagicPotions.Phase.PLAY, game.getPhase());
        assertFalse(game.isExploding(), "no explosion before the recipe is complete");
    }

    /**
     * Maintainer's request of 24/11/2026: the ring of the fixation along the edge of the card, as in Memory.
     */
    @Test
    void shouldFillTheRingAlongTheEdgeOfTheCardLookedAt() throws Exception {
        for (final RecipeSize size : RecipeSize.values()) {
            final MagicPotions game = playing(size, PotionsEnd.ENDLESS, 3);
            assertFalse(game.getRing().isVisible());
            for (final Ingredient ingredient : game.getShelf()) {
                fx(() -> {
                    game.lookAt(ingredient);
                    return null;
                });
                steps(game, 0.5);
                assertTrue(game.getRing().isVisible());
                // The geometry, not the bounds: those of a dashed line only hold its filled part
                final Rectangle ring = game.getRing();
                final Rectangle card = (Rectangle) game.cardOf(ingredient).getChildren().getFirst();
                assertEquals(card.getX(), ring.getLayoutX() + ring.getX(), 0.01);
                assertEquals(card.getY(), ring.getLayoutY() + ring.getY(), 0.01);
                assertEquals(card.getWidth(), ring.getWidth(), 0.01);
                assertEquals(card.getHeight(), ring.getHeight(), 0.01);
                assertEquals(card.getArcWidth(), ring.getArcWidth(), 0.01);
                final List<Double> dashes = game.getRing().getStrokeDashArray();
                assertEquals(0.5, dashes.get(0) / (dashes.get(0) + dashes.get(1)), 0.03, "half filled");
                final int index = game.getShelf().indexOf(ingredient);
                fx(() -> {
                    game.lookAway(index);
                    return null;
                });
                assertFalse(game.getRing().isVisible());
            }
        }
    }

    /**
     * Point 22: the delay of the settings at the start of each new recipe, the first one included, and nowhere else.
     */
    @Test
    void shouldWaitForTheDelayAtTheStartOfEachNewRecipe() throws Exception {
        final MagicPotions game = playing(RecipeSize.TWO, PotionsEnd.ENDLESS, 3);
        verify(gameContext, times(1)).onGameStarted();
        for (final Ingredient ingredient : List.copyOf(game.getRecipe())) {
            verify(gameContext, times(1)).onGameStarted();
            put(game, ingredient);
        }
        when(gameContext.isSelectionBlocked()).thenReturn(true);
        steps(game, MagicPotions.FUSION + MagicPotions.DRINK + MagicPotions.SERVED + 0.1);
        assertEquals(MagicPotions.Phase.PLAY, game.getPhase());
        verify(gameContext, times(2)).onGameStarted();
        final Ingredient first = game.getRecipe().getFirst();
        fx(() -> {
            game.lookAt(first);
            return null;
        });
        steps(game, 2);
        assertEquals(MagicPotions.Phase.PLAY, game.getPhase(), "nothing chosen during the delay");
        when(gameContext.isSelectionBlocked()).thenReturn(false);
        steps(game, 0.9);
        assertEquals(MagicPotions.Phase.PLAY, game.getPhase(), "the fixation counts from the end of the delay");
        steps(game, 0.15);
        assertEquals(MagicPotions.Phase.FLY, game.getPhase());
    }

    /**
     * Maintainer's decision of 24/11/2026: the explosion only when the whole recipe is right (the ingredients merged),
     * then the Biboule drinks the potion.
     */
    @Test
    void shouldExplodeThenServeWhenTheRecipeIsCompleteInAnyOrder() throws Exception {
        final MagicPotions game = playing(RecipeSize.THREE, PotionsEnd.ENDLESS, 7);
        final List<Ingredient> asked = List.copyOf(game.getRecipe());
        for (final Ingredient ingredient : asked.reversed()) {
            put(game, ingredient);
        }
        assertEquals(MagicPotions.Phase.FUSION, game.getPhase());
        assertTrue(game.isExploding());
        verify(gameContext.getSoundManager()).add("data/potions/sounds/boom.wav");
        verify(stats, never()).incrementNumberOfGoalsReached();
        steps(game, MagicPotions.FUSION + 0.05);
        assertEquals(MagicPotions.Phase.DRINK, game.getPhase());
        assertFalse(game.isExploding());
        steps(game, MagicPotions.DRINK + 0.05);
        assertEquals(MagicPotions.Phase.SERVED, game.getPhase());
        assertEquals(1, game.getServed());
        verify(stats).incrementNumberOfGoalsReached();
        verify(gameContext.getSoundManager()).add("data/potions/sounds/served.wav");
        steps(game, MagicPotions.SERVED + 0.05);
        assertEquals(MagicPotions.Phase.PLAY, game.getPhase());
        assertNotEquals(new HashSet<>(asked), new HashSet<>(game.getRecipe()), "another recipe");
        assertEquals(0, game.ticked());
        verify(stats, times(2)).incrementNumberOfGoalsToReach();
    }

    @Test
    void shouldShakeAnIngredientOutOfTheRecipeCostALifeAndNotExplode() throws Exception {
        final MagicPotions game = playing(RecipeSize.TWO, PotionsEnd.ENDLESS, 9);
        final Ingredient wrong = outOfRecipe(game).getFirst();
        choose(game, wrong);
        assertEquals(MagicPotions.Phase.WRONG, game.getPhase());
        assertEquals(MagicPotions.LIVES - 1, game.getLives());
        verify(stats).incrementNumberOfMistakes();
        verify(gameContext.getSoundManager()).add("data/dice/sounds/wrong.wav");
        steps(game, MagicPotions.SHAKE / 4);
        assertNotEquals(0, game.cardOf(wrong).getTranslateX(), "it shakes");
        steps(game, MagicPotions.SHAKE);
        assertEquals(MagicPotions.Phase.PLAY, game.getPhase());
        assertEquals(0, game.cardOf(wrong).getTranslateX());
        assertFalse(game.isExploding());
        assertTrue(game.getAdded().isEmpty());
        verify(gameContext.getSoundManager(), never()).add("data/potions/sounds/boom.wav");
        // Greyed, it can no longer be chosen
        assertTrue(game.cardOf(wrong).getOpacity() < 1);
        choose(game, wrong);
        assertEquals(MagicPotions.Phase.PLAY, game.getPhase());
        assertEquals(MagicPotions.LIVES - 1, game.getLives());
        verify(stats, times(1)).incrementNumberOfGoalsToReach();
    }

    @Test
    void shouldNotPutAnIngredientTwiceNorWhileAnotherFlies() throws Exception {
        final MagicPotions game = playing(RecipeSize.THREE, PotionsEnd.ENDLESS, 4);
        final Ingredient first = game.getRecipe().get(0);
        final Ingredient second = game.getRecipe().get(1);
        fx(() -> {
            game.choose(first);
            game.choose(second);
            return null;
        });
        steps(game, MagicPotions.FLY + 0.05);
        assertEquals(Set.of(first), game.getAdded());
        put(game, first);
        assertEquals(Set.of(first), game.getAdded());
        assertEquals(MagicPotions.Phase.PLAY, game.getPhase());
    }

    @Test
    void shouldEndWithLostAfterThreeMistakesAndNoBravo() throws Exception {
        final MagicPotions game = playing(RecipeSize.TWO, PotionsEnd.ENDLESS, 11);
        for (final Ingredient wrong : outOfRecipe(game)) {
            refuse(game, wrong);
        }
        assertEquals(MagicPotions.Phase.PLAY, game.getPhase());
        // The third mistake on the next recipe (the greyed ingredients come back on the shelf)
        serve(game);
        refuse(game, outOfRecipe(game).getFirst());
        assertEquals(MagicPotions.Phase.SHOW, game.getPhase());
        steps(game, MagicPotions.SHOW + 0.05);
        assertFalse(game.isBravo());
        verify(gameContext, never()).playWinTransition(anyLong(), any());
        verify(gameContext).showRoundStats(any(), any());
    }

    /**
     * "Without hearts": no life lost and the game never lost, the ingredients out of the recipe counted as mistakes.
     */
    @Test
    void shouldNeverLoseWithoutHearts() throws Exception {
        final MagicPotions game = fx(() -> {
            gameArea.getChildren().clear();
            final MagicPotions created = new MagicPotions(gameContext, stats, RecipeSize.TWO, PotionsEnd.ENDLESS, 11);
            created.setWithLives(false);
            created.setManual();
            launchedGames.add(created).launch();
            return created;
        });
        steps(game, 0.05);
        for (int round = 0; round < 2; round++) {
            for (final Ingredient wrong : outOfRecipe(game)) {
                refuse(game, wrong);
            }
            serve(game);
        }
        assertEquals(MagicPotions.LIVES, game.getLives());
        assertEquals(MagicPotions.Phase.PLAY, game.getPhase());
        verify(stats, times(4)).incrementNumberOfMistakes();
        verify(gameContext, never()).showRoundStats(any(), any());
    }

    @Test
    void shouldEndWithBravoAfterTheRecipesToMake() throws Exception {
        final MagicPotions game = playing(RecipeSize.FOUR, PotionsEnd.SERVED_3, 13);
        for (int recipe = 0; recipe < 3; recipe++) {
            serve(game);
        }
        assertEquals(MagicPotions.Phase.OVER, game.getPhase());
        assertTrue(game.isBravo());
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldStopAfterDispose() throws Exception {
        final MagicPotions game = playing(RecipeSize.TWO, PotionsEnd.ENDLESS, 2);
        fx(() -> {
            game.dispose();
            return null;
        });
        steps(game, 2);
        assertEquals(MagicPotions.Phase.OVER, game.getPhase());
        assertTrue(gameArea.getChildren().isEmpty());
        // The cards of the shelf
        verify(gameContext.getGazeDeviceManager(), times(4)).removeEventFilter(any());
    }

    @Test
    void shouldGiveAllTheVariantsTheEasiestFirst() {
        final Set<IGameVariant> variants = new PotionsGameVariantGenerator().getVariants();
        // 3 sizes, 4 ends, with or without hearts (maintainer's choices of 24/11/2026)
        assertEquals(3 * 4 * 2, variants.size());
        assertEquals(new GroupedGameVariant(PotionsGameVariantGenerator.GROUP, PotionsEnd.SERVED_3.getKey(),
            RecipeSize.TWO.getKey(), null, LivesVariant.WITHOUT), variants.iterator().next());
        final IGameVariant variant = new GroupedGameVariant(PotionsGameVariantGenerator.GROUP,
            PotionsEnd.SERVED_10.getKey(), RecipeSize.FOUR.getKey(), null, LivesVariant.WITH);
        assertTrue(variants.contains(variant));
        assertEquals(RecipeSize.FOUR, PotionsGameLauncher.sizeOf(variant));
        assertEquals(PotionsEnd.SERVED_10, PotionsGameLauncher.endOf(variant));
        final IGameVariant endlessWithout = new GroupedGameVariant(PotionsGameVariantGenerator.GROUP,
            PotionsEnd.ENDLESS.getKey(), RecipeSize.THREE.getKey(), null, LivesVariant.WITHOUT);
        assertTrue(variants.contains(endlessWithout));
        assertTrue(PotionsGameLauncher.withLives(
            new MagicPotions(gameContext, stats, RecipeSize.TWO, PotionsEnd.ENDLESS, 1), variant).isWithLives());
        assertFalse(PotionsGameLauncher.withLives(
            new MagicPotions(gameContext, stats, RecipeSize.TWO, PotionsEnd.ENDLESS, 1), endlessWithout)
            .isWithLives());
        assertEquals(RecipeSize.TWO, PotionsGameLauncher.sizeOf(null));
        assertEquals(PotionsEnd.ENDLESS, PotionsGameLauncher.endOf(null));
    }
}

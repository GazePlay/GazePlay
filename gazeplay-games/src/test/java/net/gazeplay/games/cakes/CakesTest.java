package net.gazeplay.games.cakes;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
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
 * "La Fabrique à gâteaux" (reviewed on 12/11/2026, maintainer's rules): the delay, the fixation, the steps of a layer, a
 * choice that differs from the model (a mistake, the step again), the layers of the model, the hidden model and its
 * button, the free mode, the cake finished, the end, the variants and those of before the review.
 */
@ExtendWith(ApplicationExtension.class)
class CakesTest {

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

    private CakeFactory launched(final CakesMode mode, final CakesLevel level, final CakesEnd end) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final CakeFactory game = new CakeFactory(gameContext, stats, mode, level, end, 8);
            game.setManual();
            game.launch();
            game.step(0.02);
            return game;
        });
    }

    private static void steps(final CakeFactory game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    /**
     * Makes the cake of the model, right at each step, waiting for the layers to arrive.
     */
    private static void copyModel(final CakeFactory game) {
        for (int i = 0; i < game.getModel().size(); i++) {
            final CakeFactory.Layer part = game.getModel().get(i);
            game.choose(game.choice(part.sponge));
            game.choose(game.choice(part.cream));
            if (i < game.getModel().size() - 1) {
                game.choose(game.choice(part.accessory));
                steps(game, CakeFactory.ARRIVE + 0.04);
            } else {
                game.choose(game.choice(part.sweets));
            }
        }
        game.choose(game.choice(game.getModelDecoration()));
    }

    @Test
    void shouldChooseNothingDuringTheDelayThenAfterTheFixationOnly() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final CakeFactory game = launched(CakesMode.COPY, CakesLevel.EASY, CakesEnd.ENDLESS);
        fx(() -> {
            final CakeFactory.Choice choice = game.choice(game.getModel().get(0).sponge);
            game.choose(choice);
            assertEquals(-1, game.getLayers().get(0).sponge);
            steps(game, 1.02);
            assertEquals(CakeFactory.Phase.CHOOSE, game.getPhase());
            game.lookAt(choice);
            steps(game, 0.3);
            game.lookAway(choice);
            steps(game, 0.5);
            assertEquals(-1, game.getLayers().get(0).sponge);
            game.lookAt(choice);
            steps(game, 0.52);
            assertEquals(game.getModel().get(0).sponge, game.getLayers().get(0).sponge);
            assertEquals(CakeFactory.Step.CREAM, game.getStep());
            return null;
        });
    }

    @Test
    void shouldShowEachChoiceOfAStep() throws Exception {
        final CakeFactory game = launched(CakesMode.FREE, CakesLevel.FREE, CakesEnd.ENDLESS);
        fx(() -> {
            assertEquals(CakeFactory.SPONGES, game.getChoices().size());
            game.choose(game.choice(0));
            assertEquals(CakeFactory.CREAMS, game.getChoices().size());
            game.choose(game.choice(0));
            assertEquals(CakeFactory.Step.NEXT, game.getStep());
            assertEquals(2, game.getChoices().size());
            game.choose(game.choice(0));
            assertEquals(CakeFactory.ACCESSORIES, game.getChoices().size());
            return null;
        });
    }

    @Test
    void shouldCountAChoiceThatDiffersFromTheModelAndAskAgain() throws Exception {
        final CakeFactory game = launched(CakesMode.COPY, CakesLevel.EASY, CakesEnd.ENDLESS);
        fx(() -> {
            final int right = game.getModel().get(0).sponge;
            final int wrong = (right + 1) % CakeFactory.SPONGES;
            game.choose(game.choice(wrong));
            // Put on the cake anyway, a mistake, the same step
            assertEquals(wrong, game.getLayers().get(0).sponge);
            assertEquals(CakeFactory.Step.SPONGE, game.getStep());
            verify(stats).incrementNumberOfMistakes();
            verify(gameContext.getSoundManager()).add("data/dice/sounds/wrong.wav");
            game.choose(game.choice(right));
            assertEquals(CakeFactory.Step.CREAM, game.getStep());
            verify(stats, times(1)).incrementNumberOfMistakes();
            return null;
        });
    }

    @Test
    void shouldCopyTheThreeLayersOfTheModelThenStartAnotherCake() throws Exception {
        final CakeFactory game = launched(CakesMode.COPY, CakesLevel.NORMAL, CakesEnd.ENDLESS);
        fx(() -> {
            assertEquals(3, game.getModel().size());
            // Sweets on the top layer only (on the others they would be hidden), an accessory on the others
            assertEquals(-1, game.getModel().get(0).sweets);
            assertEquals(-1, game.getModel().get(1).sweets);
            assertTrue(game.getModel().get(2).sweets >= 0);
            assertEquals(-1, game.getModel().get(2).accessory);
            final CakeFactory.Layer first = game.getModel().get(0);
            game.choose(game.choice(first.sponge));
            game.choose(game.choice(first.cream));
            assertEquals(CakeFactory.Step.ACCESSORY, game.getStep());
            game.choose(game.choice(first.accessory));
            // The second layer arrives: nothing is chosen meanwhile
            assertEquals(2, game.getLayers().size());
            assertEquals(CakeFactory.Phase.ARRIVE, game.getPhase());
            game.choose(game.choice(game.getModel().get(1).sponge));
            assertEquals(-1, game.getLayers().get(1).sponge);
            steps(game, CakeFactory.ARRIVE + 0.04);
            final CakeFactory.Layer second = game.getModel().get(1);
            game.choose(game.choice(second.sponge));
            game.choose(game.choice(second.cream));
            game.choose(game.choice(second.accessory));
            steps(game, CakeFactory.ARRIVE + 0.04);
            final CakeFactory.Layer third = game.getModel().get(2);
            game.choose(game.choice(third.sponge));
            game.choose(game.choice(third.cream));
            assertEquals(CakeFactory.Step.SWEETS, game.getStep());
            game.choose(game.choice(third.sweets));
            // The decoration only on the last layer
            assertEquals(CakeFactory.Step.DECORATION, game.getStep());
            assertEquals(3, game.getLayers().size());
            game.choose(game.choice(game.getModelDecoration()));
            assertEquals(CakeFactory.Phase.REWARD, game.getPhase());
            assertEquals(1, game.getCakes());
            verify(stats).incrementNumberOfGoalsReached();
            verify(gameContext.getSoundManager()).add("data/goosegame/sounds/win.wav");
            steps(game, CakeFactory.REWARD + 0.04);
            assertEquals(CakeFactory.Phase.CHOOSE, game.getPhase());
            assertEquals(1, game.getLayers().size());
            assertEquals(-1, game.getLayers().get(0).sponge);
            verify(stats, times(2)).incrementNumberOfGoalsToReach();
            return null;
        });
    }

    @Test
    void shouldHideTheModelAndShowItAgainByItsButton() throws Exception {
        final CakeFactory game = launched(CakesMode.COPY, CakesLevel.HARD, CakesEnd.ENDLESS);
        fx(() -> {
            assertTrue(game.isModelVisible());
            steps(game, CakeFactory.MODEL_SHOWN);
            assertFalse(game.isModelVisible());
            assertNotNull(game.getShowModel());
            game.choose(game.getShowModel());
            game.step(0.02);
            assertTrue(game.isModelVisible());
            steps(game, CakeFactory.MODEL_AGAIN);
            assertFalse(game.isModelVisible());
            // A mistake shows it while it blinks
            final int wrong = (game.getModel().get(0).sponge + 1) % CakeFactory.SPONGES;
            game.choose(game.choice(wrong));
            game.step(0.02);
            assertTrue(game.isModelVisible());
            return null;
        });
    }

    @Test
    void shouldMakeOnesOwnCakeOfUpToThreeLayersInTheFreeMode() throws Exception {
        final CakeFactory game = launched(CakesMode.FREE, CakesLevel.FREE, CakesEnd.ENDLESS);
        fx(() -> {
            assertNull(game.getShowModel());
            for (int layer = 0; layer < 2; layer++) {
                game.choose(game.choice(1));
                game.choose(game.choice(2));
                assertEquals(CakeFactory.Step.NEXT, game.getStep());
                game.choose(game.choice(0));
                assertEquals(CakeFactory.Step.ACCESSORY, game.getStep());
                game.choose(game.choice(layer));
                steps(game, CakeFactory.ARRIVE + 0.04);
            }
            assertEquals(3, game.getLayers().size());
            assertEquals(1, game.getLayers().get(1).accessory);
            game.choose(game.choice(3));
            game.choose(game.choice(4));
            // The third layer is the last one: no more choice, the sweets
            assertEquals(CakeFactory.Step.SWEETS, game.getStep());
            game.choose(game.choice(1));
            // The third layer is the last one: the decoration
            assertEquals(CakeFactory.Step.DECORATION, game.getStep());
            game.choose(game.choice(1));
            assertEquals(CakeFactory.Phase.REWARD, game.getPhase());
            verify(stats, never()).incrementNumberOfMistakes();
            return null;
        });
    }

    @Test
    void shouldDecorateAfterOneLayerInTheFreeMode() throws Exception {
        final CakeFactory game = launched(CakesMode.FREE, CakesLevel.FREE, CakesEnd.ENDLESS);
        fx(() -> {
            game.choose(game.choice(0));
            game.choose(game.choice(0));
            game.choose(game.choice(1));
            assertEquals(CakeFactory.Step.SWEETS, game.getStep());
            game.choose(game.choice(2));
            assertEquals(CakeFactory.Step.DECORATION, game.getStep());
            assertEquals(1, game.getLayers().size());
            assertEquals(-1, game.getLayers().get(0).accessory);
            game.choose(game.choice(0));
            assertEquals(0, game.getDecoration());
            assertEquals(1, game.getCakes());
            return null;
        });
    }

    @Test
    void shouldEndWithBravoAfterTheCakesOfTheVariant() throws Exception {
        final CakeFactory game = launched(CakesMode.COPY, CakesLevel.EASY, CakesEnd.CAKES_3);
        fx(() -> {
            for (int i = 0; i < 3; i++) {
                copyModel(game);
                steps(game, CakeFactory.REWARD + 0.04);
            }
            assertEquals(3, game.getCakes());
            assertEquals(CakeFactory.Phase.OVER, game.getPhase());
            verify(gameContext).playWinTransition(anyLong(), any());
            return null;
        });
    }

    @Test
    void shouldStopAndLeaveTheGameArea() throws Exception {
        final CakeFactory game = launched(CakesMode.COPY, CakesLevel.NORMAL, CakesEnd.ENDLESS);
        fx(() -> {
            assertFalse(gameArea.getChildren().isEmpty());
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertTrue(game.getChoices().isEmpty());
            assertEquals(CakeFactory.Phase.OVER, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldReadTheVariantsAndThoseOfBeforeTheReview() {
        final Set<IGameVariant> variants = new CakesGameVariantGenerator().getVariants();
        assertEquals(3 * 4 + 4, variants.size());
        final CakeFactory free = CakesGameLauncher.create(gameContext,
            new GroupedGameVariant("CakesFree", "CakesEnd3", "CakesFreeLayers", null), stats, 1);
        assertEquals(CakesMode.FREE, free.getMode());
        assertEquals(CakesEnd.CAKES_3, free.getEnd());
        final CakeFactory hard = CakesGameLauncher.create(gameContext,
            new GroupedGameVariant("CakesCopy", "CakesEndless", "CakesHard", null), stats, 1);
        assertEquals(CakesLevel.HARD, hard.getLevel());
        for (final CakeGameVariant old : CakeGameVariant.values()) {
            final CakeFactory game = CakesGameLauncher.create(gameContext,
                new EnumGameVariant<>(old, CakeGameVariant::getLabel), stats, 1);
            assertEquals(CakesMode.COPY, game.getMode());
            assertEquals(CakesEnd.ENDLESS, game.getEnd());
            assertEquals(switch (old) {
                case ONELAYER -> CakesLevel.EASY;
                case THREELAYERS -> CakesLevel.NORMAL;
                case THREELAYERSHIDEN -> CakesLevel.HARD;
            }, game.getLevel());
        }
    }
}

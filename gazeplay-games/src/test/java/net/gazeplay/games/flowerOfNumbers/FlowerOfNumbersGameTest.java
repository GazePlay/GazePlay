package net.gazeplay.games.flowerOfNumbers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GameStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Flower Of Numbers (reviewed on 2/11/2026, rules of the maintainer): the values of each variant, the parts of the
 * guided mode, each value put once and taken back, a petal over its part or right, the win in the 3 modes, the total,
 * the layout and the variants.
 */
@ExtendWith(ApplicationExtension.class)
class FlowerOfNumbersGameTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    @Mock
    Configuration configuration;

    @Mock
    Translator translator;

    @Mock
    Stats stats;

    private final Pane gameArea = new Pane();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1366, 768));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty())
            .thenReturn(new SimpleDoubleProperty(100));
        when(configuration.getFixationLength()).thenReturn(100);
        when(configuration.getLanguage()).thenReturn("fra");
    }

    private FlowerOfNumbersGame launched(final int maxNumber, final FlowerMode mode, final double seed)
        throws Exception {
        return WaitForAsyncUtils.asyncFx(() -> {
            gameArea.getChildren().clear();
            final FlowerOfNumbersGame game = new FlowerOfNumbersGame(gameContext, stats, maxNumber, mode, seed);
            game.launch();
            return game;
        }).get();
    }

    private static ValueTarget choice(final FlowerOfNumbersGame game, final Petal petal, final int value) {
        return game.getChoices().get(petal).stream().filter(target -> target.getValue() == value).findFirst()
            .orElseThrow();
    }

    /**
     * Chooses a value of a petal, and waits for it to land.
     */
    private void choose(final FlowerOfNumbersGame game, final Petal petal, final int value) throws Exception {
        WaitForAsyncUtils.asyncFx(() -> choice(game, petal, value).select()).get();
        WaitForAsyncUtils.sleep(120, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private static String text(final Group pill) {
        return ((Text) pill.getChildren().get(1)).getText();
    }

    /**
     * @return different values of a petal making the sum, as many as its places at most, or null
     */
    private static List<Integer> partsOf(final FlowerOfNumbersGame game, final Petal petal, final int sum) {
        return find(game.valuesOf(petal), 0, petal.getCapacity(), sum);
    }

    private static List<Integer> find(final int[] values, final int from, final int places, final int sum) {
        if (sum == 0) {
            return new ArrayList<>();
        }
        if (places == 0) {
            return null;
        }
        for (int i = values.length - 1; i >= from; i--) {
            if (values[i] <= sum) {
                final List<Integer> rest = find(values, i + 1, places - 1, sum - values[i]);
                if (rest != null) {
                    rest.addFirst(values[i]);
                    return rest;
                }
            }
        }
        return null;
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 20, 30, 40, 50})
    void shouldProposeTheValuesAndTheNumberOfTheVariant(final int maxNumber) throws Exception {
        final FlowerOfNumbersGame game = launched(maxNumber, FlowerMode.GUIDED, 3);

        assertEquals(9, game.getChoices().get(Petal.DIGITS).size());
        assertEquals(Math.min(maxNumber, 20), game.getChoices().get(Petal.WORDS).size());
        assertEquals(5, game.getChoices().get(Petal.FINGERS).size());
        assertEquals(6, game.getChoices().get(Petal.DICE).size());
        assertEquals(List.of(1, 2, 5, 10),
            game.getChoices().get(Petal.MONEY).stream().map(ValueTarget::getValue).toList());
        final int number = game.getFlower().getPistil();
        assertTrue(number >= 5 && number <= maxNumber, String.valueOf(number));
        verify(stats).incrementNumberOfGoalsToReach();
    }

    /**
     * Guided: the number shared out among the 5 petals, at least 1 each, each part a sum its petal can make with
     * different values (the money cannot make 4, 9 or 14).
     */
    @ParameterizedTest
    @ValueSource(ints = {10, 20, 30, 40, 50})
    void shouldShareTheNumberOutAmongThePetals(final int maxNumber) throws Exception {
        for (int seed = 1; seed <= 15; seed++) {
            final FlowerOfNumbersGame game = launched(maxNumber, FlowerMode.GUIDED, seed);
            int total = 0;
            for (final Petal petal : Petal.values()) {
                final int part = game.partOf(petal);
                assertTrue(part >= 1, petal + " " + part);
                assertNotNull(partsOf(game, petal, part), petal + " cannot make " + part);
                assertEquals("0 / " + part, text(game.getSumPills().get(petal)));
                total += part;
            }
            assertEquals(game.getFlower().getPistil(), total);
        }
        final FlowerOfNumbersGame game = launched(10, FlowerMode.GUIDED, 1);
        assertFalse(game.reachableSums(Petal.MONEY).contains(4));
        assertFalse(game.reachableSums(Petal.MONEY).contains(9));
        assertTrue(game.reachableSums(Petal.MONEY).contains(18));
    }

    /**
     * A value put in its petal cannot be put again (dimmed in its card) until it is taken back.
     */
    @Test
    void shouldPutEachValueOnceThenTakeItBack() throws Exception {
        final FlowerOfNumbersGame game = launched(20, FlowerMode.FREE, 7);

        choose(game, Petal.DICE, 1);
        assertEquals(1, game.getFlower().getSum(Petal.DICE));
        assertEquals("1", text(game.getSumPills().get(Petal.DICE)));
        assertFalse(choice(game, Petal.DICE, 1).isEnabled());
        assertTrue(choice(game, Petal.DICE, 2).isEnabled());
        verify(gameContext.getSoundManager()).add(FlowerOfNumbersGame.PUT_SOUND);

        choose(game, Petal.DICE, 1);
        assertEquals(1, game.getFlower().getSum(Petal.DICE), "the die 1 only once");

        final ValueTarget put = game.getPlaced().get(Petal.DICE)[0];
        WaitForAsyncUtils.asyncFx(put::select).get();
        assertEquals(0, game.getFlower().getSum(Petal.DICE));
        assertNull(put.getParent());
        assertTrue(choice(game, Petal.DICE, 1).isEnabled());
    }

    /**
     * A full petal takes no more values; the total over the number turns red.
     */
    @Test
    void shouldStopAFullPetalAndShowTheTotalOver() throws Exception {
        final FlowerOfNumbersGame game = launched(10, FlowerMode.FREE, 7);
        for (final int digit : List.of(9, 8, 7, 6)) {
            choose(game, Petal.DIGITS, digit);
        }
        assertTrue(game.getFlower().petalIsFull(Petal.DIGITS));
        assertTrue(game.getChoices().get(Petal.DIGITS).stream().noneMatch(ValueTarget::isEnabled));
        assertEquals("FlowerTotal 30 / " + game.getFlower().getPistil(), text(game.getTotalPill()));
        assertTrue(((Text) game.getTotalPill().getChildren().get(1)).getStyle().contains("-gp-danger"));
        verify(gameContext, never()).playWinTransition(anyLong(), any());
    }

    /**
     * Guided: a petal over its part turns red; making it, green with a sound; all of them: won.
     */
    @Test
    void shouldWinWhenEachPetalMakesItsPart() throws Exception {
        final FlowerOfNumbersGame game = launched(30, FlowerMode.GUIDED, 11);
        final int digits = game.partOf(Petal.DIGITS);
        final int over = game.getChoices().get(Petal.DIGITS).stream().map(ValueTarget::getValue)
            .filter(value -> value > digits).findFirst().orElse(0);
        if (over > 0) {
            choose(game, Petal.DIGITS, over);
            assertTrue(game.getPetals().get(Petal.DIGITS).getStyle().contains("-gp-danger"));
            WaitForAsyncUtils.asyncFx(() -> game.getPlaced().get(Petal.DIGITS)[0].select()).get();
        }

        for (final Petal petal : Petal.values()) {
            for (final int value : partsOf(game, petal, game.partOf(petal))) {
                choose(game, petal, value);
            }
            assertTrue(game.getPetals().get(petal).getStyle().contains(GameStyle.SUCCESS_COLOR), petal.toString());
        }
        verify(gameContext.getSoundManager(), times(5)).add(FlowerOfNumbersGame.PETAL_SOUND);
        verify(stats).incrementNumberOfGoalsReached();
        verify(gameContext).updateScore(any(), any());
        verify(gameContext).playWinTransition(anyLong(), any());
        assertTrue(game.getChoices().values().stream().flatMap(List::stream).noneMatch(ValueTarget::isEnabled));
    }

    /**
     * Free: the total alone counts, empty petals allowed.
     */
    @Test
    void shouldWinFreeWithTheTotalOnly() throws Exception {
        final FlowerOfNumbersGame game = launched(10, FlowerMode.FREE, 5);
        choose(game, Petal.WORDS, game.getFlower().getPistil());

        verify(gameContext).playWinTransition(anyLong(), any());
        assertTrue(((Text) game.getTotalPill().getChildren().get(1)).getStyle().contains(GameStyle.SUCCESS_COLOR));
    }

    /**
     * All the petals: the total is not enough while a petal is empty (pale).
     */
    @Test
    void shouldWinWithAllThePetalsOnlyWhenNoneIsEmpty() throws Exception {
        final FlowerOfNumbersGame game = launched(10, FlowerMode.ALL_PETALS, 5);
        final int number = game.getFlower().getPistil();
        assertTrue(number >= 5);
        assertTrue(game.getPetals().get(Petal.DICE).getStyle().contains("derive("), "pale");

        choose(game, Petal.WORDS, number);
        verify(gameContext, never()).playWinTransition(anyLong(), any());
        WaitForAsyncUtils.asyncFx(() -> game.getPlaced().get(Petal.WORDS)[0].select()).get();

        choose(game, Petal.DIGITS, 1);
        choose(game, Petal.FINGERS, 1);
        choose(game, Petal.DICE, 1);
        choose(game, Petal.MONEY, 1);
        assertFalse(game.getPetals().get(Petal.DICE).getStyle().contains("derive("), "no longer pale");
        verify(gameContext, never()).playWinTransition(anyLong(), any());
        choose(game, Petal.WORDS, number - 4);
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    /**
     * On a small screen of another shape, the values stay on the screen, away from each other, from the sums, from the
     * total and from the gear at the bottom left.
     */
    @Test
    void shouldLayTheValuesOutWithoutOverlapOnASmallScreen() throws Exception {
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(800, 600));
        final FlowerOfNumbersGame game = launched(50, FlowerMode.GUIDED, 3);

        final List<Group> pills = new ArrayList<>(game.getSumPills().values());
        pills.add(game.getTotalPill());
        final List<Bounds> values = game.getChoices().values().stream().flatMap(List::stream)
            .map(target -> target.localToScene(target.getBoundsInLocal())).toList();
        for (int i = 0; i < values.size(); i++) {
            final Bounds bounds = values.get(i);
            assertTrue(bounds.getMinX() >= 0 && bounds.getMaxX() <= 800 && bounds.getMinY() >= 0
                && bounds.getMaxY() <= 600, bounds.toString());
            assertFalse(bounds.getMinX() < 150 && bounds.getMaxY() > 470, "the gear: " + bounds);
            for (int j = i + 1; j < values.size(); j++) {
                assertFalse(bounds.intersects(values.get(j)), bounds + " " + values.get(j));
            }
            for (final Group pill : pills) {
                assertFalse(bounds.intersects(pill.localToScene(pill.getBoundsInLocal())), "pill: " + bounds);
            }
        }
    }

    @Test
    void shouldGiveTheVariants() {
        final List<GroupedGameVariant> variants = new FlowerOfNumbersVariantGenerator().getVariants().stream()
            .map(GroupedGameVariant.class::cast).toList();
        assertEquals(15, variants.size());
        assertEquals(FlowerMode.GUIDED, FlowerOfNumbersGameLauncher.modeOf(variants.get(0)));
        assertEquals(FlowerMode.FREE, FlowerOfNumbersGameLauncher.modeOf(variants.get(5)));
        assertEquals(FlowerMode.ALL_PETALS, FlowerOfNumbersGameLauncher.modeOf(variants.get(14)));
        assertEquals(List.of(10, 20, 30, 40, 50), variants.subList(0, 5).stream()
            .map(FlowerOfNumbersGameLauncher::maxNumberOf).toList());
        for (final GroupedGameVariant variant : variants) {
            assertEquals(variant, IGameVariant.toGameVariant(variant.toString()));
        }
        // A game saved before the variants
        assertEquals(20, FlowerOfNumbersGameLauncher.maxNumberOf(null));
        assertEquals(FlowerMode.GUIDED, FlowerOfNumbersGameLauncher.modeOf(null));
    }
}

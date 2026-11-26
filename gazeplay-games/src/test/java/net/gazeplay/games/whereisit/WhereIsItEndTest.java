package net.gazeplay.games.whereisit;

import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.whereisit.gamevariantgenerator.WhereIsItGameVariantGenerator;
import net.gazeplay.games.whereisit.gamevariantgenerator.WhereIsTheAnimalGameVariantGenerator;
import net.gazeplay.games.whereisit.gamevariantgenerator.WhereIsTheColorGameVariantGenerator;
import net.gazeplay.games.whereisit.gamevariantgenerator.WhereIsTheLetterGameVariantGenerator;
import net.gazeplay.games.whereisit.gamevariantgenerator.WhereIsTheShapeGameVariantGenerator;
import net.gazeplay.games.whereisit.launcher.WhereIsTheAnimalGameLauncher;
import net.gazeplay.games.whereisit.launcher.WhereIsTheNumberGameLauncher;
import net.gazeplay.games.whereisit.launcher.WhereIsTheShapeGameLauncher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The end of the family "Where is it?" (16/11/2026, when the time and score limiters of the settings were removed): 5,
 * 10 or 15 screens, or without end, in the variants; the variants of before still read, without end.
 */
@ExtendWith(ApplicationExtension.class)
class WhereIsItEndTest {

    private final IGameContext gameContext = mock(IGameContext.class, Answers.RETURNS_DEEP_STUBS);

    private final Stats stats = mock(Stats.class);

    private final Pane gameArea = new Pane();

    @BeforeEach
    void setUp() {
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1600, 900));
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getTranslator().translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldGiveEachThemeAndSizeWithEachEnd() {
        final Set<IGameVariant> variants = new WhereIsTheAnimalGameVariantGenerator().getVariants();
        // 31 themes and sizes before, each with the 4 ends with or without hearts (without end too since 17/11/2026)
        assertEquals(31 * 8, variants.size());
        assertTrue(variants.stream().allMatch(GroupedGameVariant.class::isInstance));
        final List<GroupedGameVariant> grouped = variants.stream().map(GroupedGameVariant.class::cast).toList();
        // The ends in the order of the switches: 5, 10, 15 screens, then without end
        assertEquals(List.of("WhereIsItEnd5", "WhereIsItEnd10", "WhereIsItEnd15", "WhereIsItEndless"),
            List.copyOf(grouped.stream().map(GroupedGameVariant::getSubGroup)
                .collect(Collectors.toCollection(LinkedHashSet::new))));
        assertEquals(List.of("Farm", "Forest", "Savanna", "Birds", "Maritime", "AllAnimals", "Dynamic"),
            List.copyOf(grouped.stream().map(GroupedGameVariant::getGroup)
                .collect(Collectors.toCollection(LinkedHashSet::new))));
        assertTrue(variants.contains(
            IGameVariant.toGameVariant("GroupedGameVariant:Farm:WhereIsItEnd10:2x3:-:LivesWith")));
        assertTrue(variants.contains(
            IGameVariant.toGameVariant("GroupedGameVariant:Farm:WhereIsItEnd10:2x3:-:LivesWithout")));
        // Without end, the hearts to choose too (17/11/2026)
        assertTrue(variants.contains(
            IGameVariant.toGameVariant("GroupedGameVariant:Farm:WhereIsItEndless:2x3:-:LivesWithout")));
        assertTrue(variants.contains(
            IGameVariant.toGameVariant("GroupedGameVariant:Farm:WhereIsItEndless:2x3:-:LivesWith")));
        // Dynamic keeps its single size
        assertEquals(8, grouped.stream().filter(variant -> variant.getGroup().equals("Dynamic")).count());
        // Easy of the colors keeps its 4 sizes (no 3 x 3), each with the 3 examples since 26/11/2026 (point 36)
        assertEquals(4 * 8 * 3, new WhereIsTheColorGameVariantGenerator().getVariants().stream()
            .map(GroupedGameVariant.class::cast).filter(variant -> variant.getGroup().equals("Easy")).count());
    }

    @Test
    void shouldGiveOneThemeToAGameWithoutThemes() {
        final Set<IGameVariant> variants = new WhereIsItGameVariantGenerator("WhereIsTheNumber").getVariants();
        // Each with the 3 examples since 26/11/2026 (point 36)
        assertEquals(5 * 8 * 3, variants.size());
        assertTrue(variants.stream().map(GroupedGameVariant.class::cast)
            .allMatch(variant -> variant.getGroup().equals("WhereIsTheNumber") && variant.isSize()));
    }

    /**
     * Tester's feedback of 23/11/2026, point 36: the example of the question, a switch of the shape, the color and the
     * number (before the hearts, learning first), not of the other games; a variant without it has no example.
     */
    @Test
    void shouldGiveTheExampleSwitchToTheShapeTheColorAndTheNumber() {
        final List<GroupedGameVariant> shapes = new WhereIsTheShapeGameVariantGenerator().getVariants().stream()
            .map(GroupedGameVariant.class::cast).toList();
        assertEquals(List.of("WhereIsItLearning", "WhereIsItTraining", "WhereIsItMastery"),
            List.copyOf(shapes.stream().map(variant -> variant.getExtras().get(0))
                .collect(Collectors.toCollection(LinkedHashSet::new))));
        assertTrue(shapes.stream().allMatch(variant -> variant.getExtras().get(1).startsWith("Lives")));
        assertTrue(new WhereIsTheColorGameVariantGenerator().getVariants().contains(IGameVariant.toGameVariant(
            "GroupedGameVariant:Easy:WhereIsItEnd5:2x2:-:WhereIsItTraining:LivesWith")));
        // Not the letter (maintainer, 26/11/2026: the question already shows the letter), nor the animals
        assertTrue(new WhereIsTheLetterGameVariantGenerator().getVariants().stream()
            .map(GroupedGameVariant.class::cast).allMatch(variant -> variant.getExtras().size() == 1));
        assertTrue(new WhereIsTheAnimalGameVariantGenerator().getVariants().stream()
            .map(GroupedGameVariant.class::cast).allMatch(variant -> variant.getExtras().size() == 1));

        final WhereIsIt shape = (WhereIsIt) new WhereIsTheShapeGameLauncher().createNewGame(gameContext,
            IGameVariant.toGameVariant("GroupedGameVariant:Easy:WhereIsItEnd5:2x2:-:WhereIsItLearning:LivesWith"),
            stats);
        assertEquals(WhereIsItExample.LEARNING, shape.getExample());
        assertTrue(WhereIsItVariants.withLives(IGameVariant.toGameVariant(
            "GroupedGameVariant:Easy:WhereIsItEnd5:2x2:-:WhereIsItLearning:LivesWith")));
        final WhereIsIt number = (WhereIsIt) new WhereIsTheNumberGameLauncher().createNewGame(gameContext,
            IGameVariant.toGameVariant("GroupedGameVariant:WhereIsTheNumber:WhereIsItEnd5:2x2:-:LivesWith"), stats);
        assertEquals(WhereIsItExample.MASTERY, number.getExample());
    }

    /**
     * Point 36: the card of the example at the right of the sentence while the question is asked, then with it in the
     * band, at its right; in the training way it goes away once the pictures are shown; none in the mastery way.
     */
    @Test
    void shouldShowTheExampleBesideTheSentence() throws Exception {
        when(gameContext.getConfiguration().getLanguage()).thenReturn("fra");
        // A short question (an animation of no length does not move anything)
        when(gameContext.getConfiguration().getQuestionLength()).thenReturn(200L);
        for (final WhereIsItExample example : WhereIsItExample.values()) {
            final WhereIsIt game = (WhereIsIt) new WhereIsTheShapeGameLauncher().createNewGame(gameContext,
                IGameVariant.toGameVariant("GroupedGameVariant:Easy:WhereIsItEnd5:2x2:-:" + example.getKey()
                    + ":LivesWithout"), stats);
            final Group[] card = new Group[1];
            WaitForAsyncUtils.asyncFx(() -> {
                gameArea.getChildren().clear();
                game.launch();
                card[0] = game.getExampleCard();
                if (example == WhereIsItExample.MASTERY) {
                    assertNull(card[0]);
                } else {
                    assertNotNull(card[0]);
                    assertTrue(gameArea.getChildren().contains(card[0]));
                    // At the right of the sentence, in the middle of the screen
                    final Bounds sentence = sentenceBounds();
                    assertTrue(card[0].getBoundsInParent().getMinX() > sentence.getMaxX());
                    assertEquals(sentence.getCenterY(), card[0].getBoundsInParent().getCenterY(), 2);
                }
                return null;
            }).get();
            // The question (0.2 s) then its move to the band (0.1 s): the card in the band, at the right of the sentence
            WaitForAsyncUtils.sleep(450, java.util.concurrent.TimeUnit.MILLISECONDS);
            WaitForAsyncUtils.asyncFx(() -> {
                if (example == WhereIsItExample.MASTERY) {
                    assertNull(game.getExampleCard());
                } else {
                    assertTrue(gameArea.getChildren().contains(card[0]));
                    final Bounds sentence = sentenceBounds();
                    assertTrue(card[0].getBoundsInParent().getMinX() >= sentence.getMaxX() - 1);
                    assertEquals(sentence.getCenterY(), card[0].getBoundsInParent().getCenterY(), 2);
                    assertTrue(card[0].getBoundsInParent().getHeight() < 900 * GameSizingComputer.BAND);
                }
                return null;
            }).get();
            // Once the pictures are shown, the training way takes the card away; the learning way keeps it
            WaitForAsyncUtils.sleep((long) (3 * WhereIsIt.TRAINING_FADE),
                java.util.concurrent.TimeUnit.MILLISECONDS);
            WaitForAsyncUtils.asyncFx(() -> {
                switch (example) {
                    case LEARNING -> {
                        assertSame(card[0], game.getExampleCard());
                        assertTrue(gameArea.getChildren().contains(card[0]));
                    }
                    case TRAINING -> {
                        assertNull(game.getExampleCard());
                        assertFalse(gameArea.getChildren().contains(card[0]));
                    }
                    default -> assertNull(game.getExampleCard());
                }
                game.dispose();
                return null;
            }).get();
        }
    }

    /**
     * @return where the sentence of the question is (the pill holding a text)
     */
    private Bounds sentenceBounds() {
        return gameArea.getChildren().stream()
            .filter(node -> node instanceof Group group && group.getChildren().stream().anyMatch(Text.class::isInstance))
            .filter(node -> ((Group) node).getChildren().stream().filter(Text.class::isInstance)
                .anyMatch(text -> ((Text) text).getText().contains("?")))
            .findFirst().orElseThrow().getBoundsInParent();
    }

    @Test
    void shouldPlayTheChosenThemeSizeAndEnd() {
        final WhereIsIt game = (WhereIsIt) new WhereIsTheAnimalGameLauncher().createNewGame(gameContext,
            IGameVariant.toGameVariant("GroupedGameVariant:Forest:WhereIsItEnd15:3x2:-"), stats);
        assertEquals(WhereIsItGameType.ANIMALS_FOREST, game.getGameType());
        assertEquals(3, game.getNbLines());
        assertEquals(2, game.getNbColumns());
        assertEquals(WhereIsItEnd.SCREENS_15, game.getEnd());
    }

    /**
     * A shortcut or a game to replay of before 16/11/2026 keeps its theme and size, without end as then.
     */
    @Test
    void shouldReadTheVariantsOfBeforeWithoutEnd() {
        final WhereIsIt animal = (WhereIsIt) new WhereIsTheAnimalGameLauncher().createNewGame(gameContext,
            IGameVariant.toGameVariant("DimensionDifficultyGameVariant:2:3:Farm"), stats);
        assertEquals(WhereIsItGameType.ANIMALS_FARM, animal.getGameType());
        assertEquals(2, animal.getNbLines());
        assertEquals(3, animal.getNbColumns());
        assertEquals(WhereIsItEnd.ENDLESS, animal.getEnd());
    }

    @Test
    void shouldEndAfterTheScreensOfTheVariant() {
        final WhereIsIt five = new WhereIsIt(WhereIsItGameType.ANIMALS_FARM, 2, 2, gameContext, stats,
            WhereIsItEnd.SCREENS_5);
        for (int i = 1; i < 5; i++) {
            assertFalse(five.screenFound(), "screen " + i);
        }
        assertTrue(five.screenFound(), "the fifth screen ends the game");
        assertEquals(5, five.getScreensFound());

        final WhereIsIt endless = new WhereIsIt(WhereIsItGameType.ANIMALS_FARM, 2, 2, gameContext, stats,
            WhereIsItEnd.ENDLESS);
        for (int i = 1; i <= 50; i++) {
            assertFalse(endless.screenFound(), "screen " + i);
        }
    }

    /**
     * The counter (tester's feedback of 20/11/2026, point 14): the screen being played, out of those of the end, from
     * its start; in the corner on the left of the band, out of the grid; one counter only.
     */
    @Test
    void shouldShowTheScreenBeingPlayedInTheBand() {
        final WhereIsIt game = new WhereIsIt(WhereIsItGameType.ANIMALS_FARM, 2, 2, gameContext, stats,
            WhereIsItEnd.SCREENS_10);
        game.showCounter();
        assertEquals("WhereIsItScreen 1 / 10", text(game.counter()), "the first screen, from its start");
        game.screenFound();
        assertEquals("WhereIsItScreen 1 / 10", text(game.counter()), "unchanged until the next screen");
        game.screenFound();
        game.showCounter();
        assertEquals("WhereIsItScreen 3 / 10", text(game.counter()));
        assertEquals(1, gameArea.getChildren().stream().filter(node -> node == game.counter()).count());
        assertEquals(1, gameArea.getChildren().size());
        final GameSizing sizing = new GameSizingComputer(2, 2).computeGameSizing(new Dimension2D(1600, 900));
        final Bounds bounds = game.counter().getBoundsInParent();
        assertTrue(bounds.getMinY() >= sizing.bandTop(), "in the band: " + bounds);
        assertTrue(bounds.getMaxX() <= sizing.bandLeft(), "on the left of the question: " + bounds);

        final WhereIsIt endless = new WhereIsIt(WhereIsItGameType.ANIMALS_FARM, 2, 2, gameContext, stats,
            WhereIsItEnd.ENDLESS);
        for (int i = 0; i < 6; i++) {
            endless.screenFound();
        }
        endless.showCounter();
        assertEquals("WhereIsItScreen 7", text(endless.counter()));

        final WhereIsIt five = new WhereIsIt(WhereIsItGameType.ANIMALS_FARM, 2, 2, gameContext, stats,
            WhereIsItEnd.SCREENS_5);
        for (int i = 0; i < 5; i++) {
            five.screenFound();
        }
        assertEquals(5, five.currentScreen(), "never beyond the last screen");
    }

    private static String text(final Group pill) {
        return pill.lookupAll("Text").stream().map(Text.class::cast).map(Text::getText).findFirst().orElse(null);
    }

    /**
     * 3 lives (maintainer's request of 16/11/2026): a wrong picture costs one, the third loses the game; without end
     * too since 17/11/2026.
     */
    @Test
    void shouldLoseALifeAtEachWrongPictureWithOrWithoutEnd() throws Exception {
        final WhereIsIt game = new WhereIsIt(WhereIsItGameType.ANIMALS_FARM, 2, 2, gameContext, stats,
            WhereIsItEnd.SCREENS_5);
        assertTrue(game.hasLives());
        assertEquals(WhereIsIt.LIVES, game.getLives());
        assertFalse(game.wrongPictureChosen());
        assertEquals(2, game.getLives());
        assertNotNull(game.livesShown(), "the hearts");
        assertTrue(gameArea.getChildren().contains(game.livesShown()));
        assertFalse(game.wrongPictureChosen());
        assertTrue(game.wrongPictureChosen(), "the third wrong picture loses the game");
        assertEquals(0, game.getLives());
        assertTrue(game.isLost());

        final WhereIsIt endless = new WhereIsIt(WhereIsItGameType.ANIMALS_FARM, 2, 2, gameContext, stats,
            WhereIsItEnd.ENDLESS);
        assertTrue(endless.hasLives());
        assertFalse(endless.wrongPictureChosen());
        assertNotNull(endless.livesShown(), "the hearts without end too");
        assertEquals(2, endless.getLives());
        assertFalse(endless.wrongPictureChosen());
        assertTrue(endless.wrongPictureChosen(), "the third wrong picture loses the game without end too");
        assertTrue(endless.isLost());
    }

    /**
     * "Without hearts" chosen in the variant (16/11/2026): no hearts, never lost, the mistakes still counted; a variant
     * of before keeps its hearts.
     */
    @Test
    void shouldNeverLoseWithoutHearts() {
        final WhereIsIt game = (WhereIsIt) new WhereIsTheAnimalGameLauncher().createNewGame(gameContext,
            IGameVariant.toGameVariant("GroupedGameVariant:Farm:WhereIsItEnd5:2x2:-:LivesWithout"), stats);
        assertFalse(game.hasLives());
        for (int i = 0; i < 10; i++) {
            assertFalse(game.wrongPictureChosen());
        }
        assertNull(game.livesShown());
        assertFalse(game.isLost());
        // Each wrong picture is a mistake (17/11/2026: it was not counted)
        verify(stats, times(10)).incrementNumberOfMistakes();
        assertTrue(((WhereIsIt) new WhereIsTheAnimalGameLauncher().createNewGame(gameContext,
            IGameVariant.toGameVariant("GroupedGameVariant:Farm:WhereIsItEnd5:2x2:-:LivesWith"), stats)).hasLives());
        assertTrue(((WhereIsIt) new WhereIsTheAnimalGameLauncher().createNewGame(gameContext,
            IGameVariant.toGameVariant("GroupedGameVariant:Farm:WhereIsItEnd5:2x2:-"), stats)).hasLives());
        // Without end (17/11/2026)
        assertTrue(((WhereIsIt) new WhereIsTheAnimalGameLauncher().createNewGame(gameContext,
            IGameVariant.toGameVariant("GroupedGameVariant:Farm:WhereIsItEndless:2x2:-:LivesWith"), stats)).hasLives());
        final WhereIsIt endlessWithout = (WhereIsIt) new WhereIsTheAnimalGameLauncher().createNewGame(gameContext,
            IGameVariant.toGameVariant("GroupedGameVariant:Farm:WhereIsItEndless:2x2:-:LivesWithout"), stats);
        assertFalse(endlessWithout.hasLives());
        for (int i = 0; i < 10; i++) {
            assertFalse(endlessWithout.wrongPictureChosen());
        }
        assertFalse(endlessWithout.isLost());
    }

    /**
     * The game lost: "Lost!", then the statistics.
     */
    @Test
    void shouldShowTheStatisticsWhenTheGameIsLost() throws Exception {
        final WhereIsIt game = new WhereIsIt(WhereIsItGameType.ANIMALS_FARM, 2, 2, gameContext, stats,
            WhereIsItEnd.SCREENS_10);
        WaitForAsyncUtils.asyncFx(() -> {
            game.wrongPictureChosen();
            game.wrongPictureChosen();
            game.wrongPictureChosen();
            game.lose();
            return null;
        }).get();
        assertTrue(gameArea.getChildren().stream().anyMatch(node -> node instanceof Group group
            && group.lookupAll("Text").stream().anyMatch(text -> "WhereIsItLost".equals(((Text) text).getText()))));
        org.mockito.Mockito.verify(gameContext, org.mockito.Mockito.timeout(4000)).showRoundStats(stats, game);
    }

    /**
     * The hearts at the top left, as in the other games: the grid leaves them a row.
     */
    @Test
    void shouldLeaveARowAtTheTopForTheLives() {
        final Dimension2D screen = new Dimension2D(1600, 900);
        final GameSizing without = new GameSizingComputer(2, 3).computeGameSizing(screen);
        final GameSizing with = new GameSizingComputer(2, 3, true).computeGameSizing(screen);
        assertEquals(without.gap(), without.cellY(0), 1e-9);
        assertTrue(with.cellY(0) >= 900 * GameSizingComputer.LIVES_ROW, "under the hearts: " + with.cellY(0));
        // The pictures stay above the band
        assertTrue(with.cellY(1) + with.cellHeight() <= with.bandTop());
    }

    @Test
    void shouldNotShowTheReplayHintWithoutTheLoudspeaker() {
        final WhereIsIt game = new WhereIsIt(WhereIsItGameType.ANIMALS_FARM, 2, 2, gameContext, stats,
            WhereIsItEnd.SCREENS_5);
        game.showReplayHint();
        assertNull(game.replayHint());
    }

    @Test
    void shouldReadAnUnknownEndAsWithoutEnd() {
        assertEquals(WhereIsItEnd.ENDLESS, WhereIsItEnd.of("SomethingElse"));
        assertEquals(WhereIsItEnd.SCREENS_10, WhereIsItEnd.of("WhereIsItEnd10"));
    }
}

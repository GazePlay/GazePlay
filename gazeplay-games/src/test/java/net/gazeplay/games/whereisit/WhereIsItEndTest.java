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
import net.gazeplay.games.whereisit.launcher.WhereIsItGameLauncher;
import net.gazeplay.games.whereisit.launcher.WhereIsTheAnimalGameLauncher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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
        // 31 themes and sizes before, each with the 4 ends
        assertEquals(31 * 4, variants.size());
        assertTrue(variants.stream().allMatch(GroupedGameVariant.class::isInstance));
        final List<GroupedGameVariant> grouped = variants.stream().map(GroupedGameVariant.class::cast).toList();
        // The ends in the order of the switches: 5, 10, 15 screens, then without end
        assertEquals(List.of("WhereIsItEnd5", "WhereIsItEnd10", "WhereIsItEnd15", "WhereIsItEndless"),
            List.copyOf(grouped.stream().map(GroupedGameVariant::getSubGroup)
                .collect(Collectors.toCollection(LinkedHashSet::new))));
        assertEquals(List.of("Farm", "Forest", "Savanna", "Birds", "Maritime", "AllAnimals", "Dynamic"),
            List.copyOf(grouped.stream().map(GroupedGameVariant::getGroup)
                .collect(Collectors.toCollection(LinkedHashSet::new))));
        assertTrue(variants.contains(IGameVariant.toGameVariant("GroupedGameVariant:Farm:WhereIsItEnd10:2x3:-")));
        // Dynamic keeps its single size
        assertEquals(4, grouped.stream().filter(variant -> variant.getGroup().equals("Dynamic")).count());
        // Easy of the colors keeps its 4 sizes (no 3 x 3)
        assertEquals(4 * 4, new WhereIsTheColorGameVariantGenerator().getVariants().stream()
            .map(GroupedGameVariant.class::cast).filter(variant -> variant.getGroup().equals("Easy")).count());
    }

    @Test
    void shouldGiveOneThemeToAGameWithoutThemes() {
        final Set<IGameVariant> variants = new WhereIsItGameVariantGenerator().getVariants();
        assertEquals(5 * 4, variants.size());
        assertTrue(variants.stream().map(GroupedGameVariant.class::cast)
            .allMatch(variant -> variant.getGroup().equals("WhereIsIt") && variant.isSize()));
    }

    @Test
    void shouldPlayTheChosenThemeSizeAndEnd() {
        final WhereIsIt game = (WhereIsIt) new WhereIsTheAnimalGameLauncher().createNewGame(gameContext,
            IGameVariant.toGameVariant("GroupedGameVariant:Forest:WhereIsItEnd15:3x2:-"), stats);
        assertEquals(WhereIsItGameType.ANIMALS_FOREST, game.getGameType());
        assertEquals(3, game.getNbLines());
        assertEquals(2, game.getNbColumns());
        assertEquals(WhereIsItEnd.SCREENS_15, game.getEnd());
        final WhereIsIt replayed = (WhereIsIt) new WhereIsItGameLauncher().replayGame(gameContext,
            IGameVariant.toGameVariant("GroupedGameVariant:WhereIsIt:WhereIsItEnd5:2x2:-"), stats, 42);
        assertEquals(WhereIsItGameType.CUSTOMIZED, replayed.getGameType());
        assertEquals(WhereIsItEnd.SCREENS_5, replayed.getEnd());
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
        final WhereIsIt custom = (WhereIsIt) new WhereIsItGameLauncher().createNewGame(gameContext,
            IGameVariant.toGameVariant("DimensionGameVariant:3:3"), stats);
        assertEquals(3, custom.getNbLines());
        assertEquals(3, custom.getNbColumns());
        assertEquals(WhereIsItEnd.ENDLESS, custom.getEnd());
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
     * The counter, as in the other games: the screens found, out of those of the end; in the corner on the left of the
     * band, out of the grid; one counter only.
     */
    @Test
    void shouldCountTheScreensFoundInTheBand() {
        final WhereIsIt game = new WhereIsIt(WhereIsItGameType.ANIMALS_FARM, 2, 2, gameContext, stats,
            WhereIsItEnd.SCREENS_10);
        game.screenFound();
        game.screenFound();
        assertEquals("WhereIsItScreens 2 / 10", text(game.counter()));
        assertEquals(1, gameArea.getChildren().stream().filter(node -> node == game.counter()).count());
        assertEquals(1, gameArea.getChildren().size());
        final GameSizing sizing = new GameSizingComputer(2, 2).computeGameSizing(new Dimension2D(1600, 900));
        final Bounds bounds = game.counter().getBoundsInParent();
        assertTrue(bounds.getMinY() >= sizing.bandTop(), "in the band: " + bounds);
        assertTrue(bounds.getMaxX() <= sizing.bandLeft(), "on the left of the question: " + bounds);

        final WhereIsIt endless = new WhereIsIt(WhereIsItGameType.ANIMALS_FARM, 2, 2, gameContext, stats,
            WhereIsItEnd.ENDLESS);
        endless.screenFound();
        assertEquals("WhereIsItScreens 1", text(endless.counter()));
    }

    private static String text(final Group pill) {
        return pill.lookupAll("Text").stream().map(Text.class::cast).map(Text::getText).findFirst().orElse(null);
    }

    @Test
    void shouldReadAnUnknownEndAsWithoutEnd() {
        assertEquals(WhereIsItEnd.ENDLESS, WhereIsItEnd.of("SomethingElse"));
        assertEquals(WhereIsItEnd.SCREENS_10, WhereIsItEnd.of("WhereIsItEnd10"));
    }
}

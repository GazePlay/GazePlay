package net.gazeplay;

import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The menu in the alphabetical order of the names shown (24/11/2026), whatever the categories and the former
 * priorities.
 */
class GameSummaryComparatorTest {

    private static List<String> sorted(final Locale locale, final Map<String, String> names,
                                       final List<GameSummary> summaries) {
        final Translator translator = mock(Translator.class);
        when(translator.currentLocale()).thenReturn(locale);
        when(translator.translate(anyString())).thenAnswer(invocation -> names.get((String) invocation.getArgument(0)));
        final List<GameSummary> list = new ArrayList<>(summaries);
        list.sort(new GameSummaryComparator(translator));
        return list.stream().map(summary -> names.get(summary.getNameCode())).toList();
    }

    private static GameSummary game(final String code, final GameCategories.Category category, final int priority) {
        return GameSummary.builder().gameThumbnail("thumbnail.png").nameCode(code).category(category)
            .absolutePriority(priority).build();
    }

    @Test
    void shouldSortByTheNameShownWhateverTheCategoriesAndPriorities() {
        final Map<String, String> names = Map.of("Ninja", "Ninja", "DotToDot", "Point par point", "Creampie",
            "La tarte à la crème", "Bubbles", "Bulles", "Memory", "Memory");
        final List<String> order = sorted(Locale.FRENCH, names, List.of(
            game("Ninja", GameCategories.Category.ACTION_REACTION, 1),
            game("DotToDot", GameCategories.Category.LOGIC_MATHS, 1),
            game("Creampie", GameCategories.Category.ACTION_REACTION, 2),
            game("Memory", GameCategories.Category.MEMORIZATION, 0),
            game("Bubbles", GameCategories.Category.ACTION_REACTION, 0)));

        assertEquals(List.of("Bulles", "La tarte à la crème", "Memory", "Ninja", "Point par point"), order);
    }

    @Test
    void shouldIgnoreTheAccentsTheLigaturesAndTheCapitals() {
        final Map<String, String> names = Map.of("Ladder", "Échelle", "RushHour", "Embouteillage", "Dice", "Dés",
            "Egg", "Œuf", "Order", "ordre", "Opinions", "Opinions");
        final List<String> order = sorted(Locale.FRENCH, names, List.of(
            game("Egg", GameCategories.Category.ACTION_REACTION, 0),
            game("Order", GameCategories.Category.LOGIC_MATHS, 0),
            game("RushHour", GameCategories.Category.LOGIC_MATHS, 0),
            game("Ladder", GameCategories.Category.SELECTION, 0),
            game("Opinions", GameCategories.Category.SELECTION, 0),
            game("Dice", GameCategories.Category.LOGIC_MATHS, 0)));

        assertEquals(List.of("Dés", "Échelle", "Embouteillage", "Œuf", "Opinions", "ordre"), order);
    }

    @Test
    void shouldSortInTheLanguageChosen() {
        final Map<String, String> names = Map.of("Ladder", "Ladder", "RushHour", "Rush hour", "Dice", "Dice");
        final List<String> order = sorted(Locale.ENGLISH, names, List.of(
            game("RushHour", GameCategories.Category.LOGIC_MATHS, 0),
            game("Ladder", GameCategories.Category.SELECTION, 0),
            game("Dice", GameCategories.Category.LOGIC_MATHS, 0)));

        assertEquals(List.of("Dice", "Ladder", "Rush hour"), order);
    }

    @Test
    void shouldGiveTheSameOrderToTheGameSpecsAsTheLocator() {
        final Map<String, String> names = Map.of("B", "Bulles", "A", "Aiguillages");
        final Translator translator = mock(Translator.class);
        when(translator.currentLocale()).thenReturn(Locale.FRENCH);
        when(translator.translate(anyString())).thenAnswer(invocation -> names.get((String) invocation.getArgument(0)));
        final IGameLauncher launcher = mock(IGameLauncher.class);
        final List<GameSpec> specs = new ArrayList<>(List.of(
            new GameSpec(game("B", GameCategories.Category.ACTION_REACTION, 0), launcher),
            new GameSpec(game("A", GameCategories.Category.LOGIC_MATHS, 0), launcher)));

        specs.sort(Comparator.comparing(GameSpec::getGameSummary, new GameSummaryComparator(translator)));

        assertEquals("A", specs.getFirst().getGameSummary().getNameCode());
    }
}

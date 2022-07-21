package net.gazeplay;

import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class GameSummaryComparatorTest {

    @Mock
    private Translator mockTranslator;

    @Mock
    private IGameLauncher mockLauncher;

    private GameSummary actionGameSummary;
    private GameSummary selectionGameSummary;
    private GameSummary memorizationGameSummary;
    private GameSummary logicGameSummary;
    private GameSummary literacyGameSummary;
    private GameSummary multimediaGameSummary;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldSortAbsolutePriorityInCorrectOrder() {
        actionGameSummary = GameSummary.builder().category(GameCategories.Category.ACTION_REACTION).absolutePriority(3).build();
        selectionGameSummary = GameSummary.builder().category(GameCategories.Category.SELECTION).build();
        memorizationGameSummary = GameSummary.builder().category(GameCategories.Category.MEMORIZATION).absolutePriority(2).build();
        logicGameSummary = GameSummary.builder().category(GameCategories.Category.LOGIC_MATHS).absolutePriority(1).build();
        literacyGameSummary = GameSummary.builder().category(GameCategories.Category.LITERACY).build();
        multimediaGameSummary = GameSummary.builder().category(GameCategories.Category.MULTIMEDIA).build();

        // This list is intentionally in the wrong order
        ArrayList<GameSpec> specs = new ArrayList<>(List.of(
            new GameSpec(selectionGameSummary, mockLauncher),
            new GameSpec(actionGameSummary, mockLauncher),
            new GameSpec(logicGameSummary, mockLauncher),
            new GameSpec(memorizationGameSummary, mockLauncher),
            new GameSpec(multimediaGameSummary, mockLauncher),
            new GameSpec(literacyGameSummary, mockLauncher)
        ));

        specs.sort(Comparator.comparing(GameSpec::getGameSummary, new GameSummaryComparator(mockTranslator)));

        assertEquals(actionGameSummary, specs.get(0).getGameSummary());
        assertEquals(memorizationGameSummary, specs.get(1).getGameSummary());
        assertEquals(logicGameSummary, specs.get(2).getGameSummary());
        assertEquals(selectionGameSummary, specs.get(3).getGameSummary());
        assertEquals(literacyGameSummary, specs.get(4).getGameSummary());
        assertEquals(multimediaGameSummary, specs.get(5).getGameSummary());
    }

    @Test
    void shouldSortCategoriesInCorrectOrder() {
        actionGameSummary = GameSummary.builder().category(GameCategories.Category.ACTION_REACTION).build();
        selectionGameSummary = GameSummary.builder().category(GameCategories.Category.SELECTION).build();
        memorizationGameSummary = GameSummary.builder().category(GameCategories.Category.MEMORIZATION).build();
        logicGameSummary = GameSummary.builder().category(GameCategories.Category.LOGIC_MATHS).build();
        literacyGameSummary = GameSummary.builder().category(GameCategories.Category.LITERACY).build();
        multimediaGameSummary = GameSummary.builder().category(GameCategories.Category.MULTIMEDIA).build();

        // This list is intentionally in the wrong order
        ArrayList<GameSpec> specs = new ArrayList<>(List.of(
            new GameSpec(selectionGameSummary, mockLauncher),
            new GameSpec(actionGameSummary, mockLauncher),
            new GameSpec(logicGameSummary, mockLauncher),
            new GameSpec(memorizationGameSummary, mockLauncher),
            new GameSpec(multimediaGameSummary, mockLauncher),
            new GameSpec(literacyGameSummary, mockLauncher)
        ));

        specs.sort(Comparator.comparing(GameSpec::getGameSummary, new GameSummaryComparator(mockTranslator)));

        assertEquals(actionGameSummary, specs.get(0).getGameSummary());
        assertEquals(selectionGameSummary, specs.get(1).getGameSummary());
        assertEquals(memorizationGameSummary, specs.get(2).getGameSummary());
        assertEquals(logicGameSummary, specs.get(3).getGameSummary());
        assertEquals(literacyGameSummary, specs.get(4).getGameSummary());
        assertEquals(multimediaGameSummary, specs.get(5).getGameSummary());
    }

    @Test
    void shouldSortPriorityInCorrectOrder() {
        // Ignore the variable names, these need to be the same category to test the priority
        actionGameSummary = GameSummary.builder().category(GameCategories.Category.ACTION_REACTION).priority(2).build();
        selectionGameSummary = GameSummary.builder().category(GameCategories.Category.ACTION_REACTION).priority(1).build();
        memorizationGameSummary = GameSummary.builder().category(GameCategories.Category.ACTION_REACTION).priority(4).build();
        logicGameSummary = GameSummary.builder().category(GameCategories.Category.ACTION_REACTION).priority(0).build();
        literacyGameSummary = GameSummary.builder().category(GameCategories.Category.ACTION_REACTION).priority(3).build();
        multimediaGameSummary = GameSummary.builder().category(GameCategories.Category.ACTION_REACTION).priority(5).build();

        // This list is intentionally in the wrong order
        ArrayList<GameSpec> specs = new ArrayList<>(List.of(
            new GameSpec(selectionGameSummary, mockLauncher),
            new GameSpec(actionGameSummary, mockLauncher),
            new GameSpec(logicGameSummary, mockLauncher),
            new GameSpec(memorizationGameSummary, mockLauncher),
            new GameSpec(multimediaGameSummary, mockLauncher),
            new GameSpec(literacyGameSummary, mockLauncher)
        ));

        specs.sort(Comparator.comparing(GameSpec::getGameSummary, new GameSummaryComparator(mockTranslator)));

        assertEquals(multimediaGameSummary, specs.get(0).getGameSummary());
        assertEquals(memorizationGameSummary, specs.get(1).getGameSummary());
        assertEquals(literacyGameSummary, specs.get(2).getGameSummary());
        assertEquals(actionGameSummary, specs.get(3).getGameSummary());
        assertEquals(selectionGameSummary, specs.get(4).getGameSummary());
        assertEquals(logicGameSummary, specs.get(5).getGameSummary());
    }

    @Test
    void shouldSortNameCodeInCorrectOrder() {
        // This simply returns whatever we pass in
        when(mockTranslator.translate(anyString())).thenAnswer((Answer<String>) invocation -> {
            Object[] args = invocation.getArguments();
            return (String) args[0];
        });

        // Ignore the variable names, these need to be the same category to test the name code
        actionGameSummary = GameSummary.builder().nameCode("Action").category(GameCategories.Category.ACTION_REACTION).build();
        selectionGameSummary = GameSummary.builder().nameCode("Selection").category(GameCategories.Category.ACTION_REACTION).build();
        memorizationGameSummary = GameSummary.builder().nameCode("Memorization").category(GameCategories.Category.ACTION_REACTION).build();
        logicGameSummary = GameSummary.builder().nameCode("Logic").category(GameCategories.Category.ACTION_REACTION).build();
        literacyGameSummary = GameSummary.builder().nameCode("Literacy").category(GameCategories.Category.ACTION_REACTION).build();
        multimediaGameSummary = GameSummary.builder().nameCode("Multimedia").category(GameCategories.Category.ACTION_REACTION).build();

        // This list is intentionally in the wrong order
        ArrayList<GameSpec> specs = new ArrayList<>(List.of(
            new GameSpec(selectionGameSummary, mockLauncher),
            new GameSpec(actionGameSummary, mockLauncher),
            new GameSpec(logicGameSummary, mockLauncher),
            new GameSpec(memorizationGameSummary, mockLauncher),
            new GameSpec(multimediaGameSummary, mockLauncher),
            new GameSpec(literacyGameSummary, mockLauncher)
        ));

        specs.sort(Comparator.comparing(GameSpec::getGameSummary, new GameSummaryComparator(mockTranslator)));

        assertEquals(actionGameSummary, specs.get(0).getGameSummary());
        assertEquals(literacyGameSummary, specs.get(1).getGameSummary());
        assertEquals(logicGameSummary, specs.get(2).getGameSummary());
        assertEquals(memorizationGameSummary, specs.get(3).getGameSummary());
        assertEquals(multimediaGameSummary, specs.get(4).getGameSummary());
        assertEquals(selectionGameSummary, specs.get(5).getGameSummary());
    }
}

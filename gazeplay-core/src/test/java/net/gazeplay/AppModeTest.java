package net.gazeplay;

import net.gazeplay.commons.ui.Translator;
import net.gazeplay.gameslocator.CatalogBasedGamesLocator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Each mode shows its part of the catalog: the games without GazePlay Eval and GazePlay Eval Test, or these two only.
 */
class AppModeTest {

    private final List<GameSpec> catalog = new CatalogBasedGamesLocator().listGames(translator());

    /**
     * @return a translator giving the keys (the catalog is sorted by translated name)
     */
    private static Translator translator() {
        Translator translator = mock(Translator.class);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        return translator;
    }

    @Test
    void shouldShowTheGamesWithoutTheEvaluations() {
        List<GameSpec> games = AppMode.GAMES.filter(catalog);

        assertEquals(catalog.size() - 2, games.size());
        assertTrue(games.stream().noneMatch(game -> game.getGameSummary().getNameCode().startsWith("GazePlayEval")));
    }

    @Test
    void shouldShowOnlyTheEvaluations() {
        List<GameSpec> evaluations = AppMode.EVALUATIONS.filter(catalog);

        assertEquals(List.of("GazePlayEval", "GazePlayEvalTest"),
            evaluations.stream().map(game -> game.getGameSummary().getNameCode()).sorted().toList());
    }
}

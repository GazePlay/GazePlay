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
 * Each mode shows its part of the catalog: the games without GazePlay Eval, or GazePlay Eval only.
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

        assertEquals(catalog.size() - 1, games.size());
        assertTrue(games.stream().noneMatch(game -> game.getGameSummary().getNameCode().equals("GazePlayEval")));
    }

    @Test
    void shouldShowOnlyTheEvaluations() {
        List<GameSpec> evaluations = AppMode.EVALUATIONS.filter(catalog);

        assertEquals(1, evaluations.size());
        assertEquals("GazePlayEval", evaluations.getFirst().getGameSummary().getNameCode());
    }
}

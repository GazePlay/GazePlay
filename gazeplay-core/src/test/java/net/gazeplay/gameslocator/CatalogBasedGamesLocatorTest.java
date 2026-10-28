package net.gazeplay.gameslocator;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The catalog of the games lists GazePlay Eval with the other games (the BERA edition, which listed the former
 * evaluations instead, was removed in October 2026), and GazePlay Eval Test (added on 28/10/2026 at the request of
 * the maintainer: the evaluation without form nor results), not the former test of the BERA evaluations.
 */
class CatalogBasedGamesLocatorTest {

    @Test
    void shouldListTheGamesOfTheCatalog() {
        List<Class> games = new CatalogBasedGamesLocator().findGameSpecSourceClasses();

        assertTrue(games.size() > 2);
        assertEquals(List.of("GazeplayEvalGameSpecSource", "GazeplayEvalTestGameSpecSource"), games.stream()
            .filter(game -> game.getName().contains("gazeplayEval")).map(Class::getSimpleName).sorted().toList(),
            games.toString());
    }
}

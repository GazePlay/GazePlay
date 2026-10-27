package net.gazeplay.gameslocator;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The catalog of the games lists GazePlay Eval with the other games (the BERA edition, which listed the former
 * evaluations instead, was removed in October 2026), and no longer the former test of the BERA evaluations.
 */
class CatalogBasedGamesLocatorTest {

    @Test
    void shouldListTheGamesOfTheCatalog() {
        List<Class> games = new CatalogBasedGamesLocator().findGameSpecSourceClasses();

        assertTrue(games.size() > 2);
        assertEquals(1, games.stream().filter(game -> game.getName().contains("gazeplayEval")).count(), games.toString());
    }
}

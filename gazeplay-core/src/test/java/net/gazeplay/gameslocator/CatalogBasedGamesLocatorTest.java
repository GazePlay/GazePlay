package net.gazeplay.gameslocator;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The catalog of the games is always used: the BERA edition, which listed the evaluations instead (evals-catalog.txt,
 * kept with the dormant Eval code), and the AFSR edition were removed in October 2026.
 */
class CatalogBasedGamesLocatorTest {

    @Test
    void shouldListTheGamesOfTheCatalog() {
        List<Class> games = new CatalogBasedGamesLocator().findGameSpecSourceClasses();

        assertTrue(games.size() > 2);
        assertFalse(games.stream().anyMatch(game -> game.getName().contains("gazeplayEval")), games.toString());
    }
}

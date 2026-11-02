package net.gazeplay.ui.scenes.gamemenu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The search bar of the menu (2/11/2026, maintainer's request: "oeuf" did not find "Jeu de l'œuf"): case, accents,
 * "œ" and "æ", and the curly apostrophe do not matter.
 */
class HomeMenuSearchTest {

    @Test
    void shouldFindAGameWhateverTheAccentsAndTheLigatures() {
        assertTrue(HomeMenuScreen.searchable("Jeu de l'œuf").contains(HomeMenuScreen.searchable("oeuf")));
        assertTrue(HomeMenuScreen.searchable("Jeu de l’œuf").contains(HomeMenuScreen.searchable("L'OEUF")));
        assertTrue(HomeMenuScreen.searchable("Où est l'animal ?").contains(HomeMenuScreen.searchable("ou est")));
        assertTrue(HomeMenuScreen.searchable("Fleur des nombres").contains(HomeMenuScreen.searchable("NOMBRÉS")));
        assertEquals("ecureuil", HomeMenuScreen.searchable("Écureuil"));
    }
}

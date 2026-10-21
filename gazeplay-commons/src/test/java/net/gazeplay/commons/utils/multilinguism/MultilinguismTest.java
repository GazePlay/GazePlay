package net.gazeplay.commons.utils.multilinguism;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultilinguismTest {

    private Multilinguism multilinguism;

    @BeforeEach
    void setup() {
        String path = "data/multilinguism/multilinguism.csv";
        multilinguism = new Multilinguism(new I18N(path));
    }

    @Test
    void shouldGetFrenchTranslation() {
        // The test fixture is a subset of the real translation file
        String result = multilinguism.getTranslation("Exit FullScreen", "fra");
        assertEquals("Sortir du plein écran", result);
    }

    @Test
    void shouldGetDefaultEnglishTranslation() {
        String result = multilinguism.getTranslation("Exit FullScreen", "wrong");
        assertEquals("Exit FullScreen", result);
    }

    @Test
    void shouldGetDefaultUntranslatedTranslation() {
        String result = multilinguism.getTranslation("wrong", "wrong");
        assertEquals("[untranslated!] wrong", result);
    }

}

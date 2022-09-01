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
        String result = multilinguism.getTranslation("Exit", "fra");
        assertEquals("Quitter", result);
    }

    @Test
    void shouldGetDefaultEnglishTranslation() {
        String result = multilinguism.getTranslation("Exit", "wrong");
        assertEquals("Exit", result);
    }

    @Test
    void shouldGetDefaultUntranslatedTranslation() {
        String result = multilinguism.getTranslation("wrong", "wrong");
        assertEquals("[untranslated!] wrong", result);
    }

}

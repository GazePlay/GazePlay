package net.gazeplay.commons.utils.multilinguism;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class I18NTest {

    private static String FILESEPARATOR = File.separator;

    @Test
    void shouldLoadTranslationsFromResource() {
        Map<I18N.Entry, String> translations = I18N.loadFromFile("data/multilinguism/translation.csv");
        assert translations.size() == 6;
    }

    @Test
    void shouldLoadTranslationsFromLocalFile() {
        String file = System.getProperty("user.dir") +
            FILESEPARATOR + "src" +
            FILESEPARATOR + "test" +
            FILESEPARATOR + "resources" +
            FILESEPARATOR + "data" +
            FILESEPARATOR + "multilinguism" +
            FILESEPARATOR + "translation.csv";
        Map<I18N.Entry, String> translations = I18N.loadFromFile(file);
        assert translations.size() == 6;
    }

    @Test
    void shouldThrowErrorWhenCantReadFromFile() {
        assertThrows(RuntimeException.class, () -> {
            I18N.loadFromFile("some/path/to.csv");
        });
    }

    @Test
    void shouldGetTranslationForExistingKey() {
        I18N translator = new I18N("data/multilinguism/translation.csv");
        assert translator.translate("item1", "eng").equals("English");
    }

    @Test
    void shouldThrowExceptionForNonExistingKey() {
        I18N translator = new I18N("data/multilinguism/translation.csv");
        assert translator.translate("item9", "eng") == null;
    }
}

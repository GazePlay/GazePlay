package net.gazeplay.commons.utils.multilinguism;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguagesTest {

    @Test
    void shouldGetAllLanguageDetails() {
        Collection<LanguageDetails> result = Languages.getAllLanguageDetails();
        LanguageDetails gb = null;

        for (LanguageDetails temp : result) {
            if (temp.getLocale().equals(new Locale("eng", "GB"))) {
                gb = temp;
                break;
            }
        }

        assertEquals(25, result.size());
        assertEquals("English", gb.getLabel());
        assertTrue(gb.getFlags().get(0).contains("Flag_of_the_United_Kingdom"));
    }

    @Test
    void shouldGetAllCodes() {
        Collection<Locale> result = Languages.getAllCodes();
        assertEquals(25, result.size());
    }

    @Test
    void shouldGetValidLocale() {
        LanguageDetails result = Languages.getLocale(new Locale("eng", "GB"));
        assertEquals("English", result.getLabel());
        assertTrue(result.getFlags().get(0).contains("Flag_of_the_United_Kingdom"));
    }

    @Test
    void shouldGetNullLocale() {
        LanguageDetails result = Languages.getLocale(null);
        assertEquals("Français", result.getLabel());
        assertTrue(result.getFlags().get(0).contains("Flag_of_France"));
    }

    @Test
    void shouldGetInvalidLocale() {
        LanguageDetails result = Languages.getLocale(new Locale("invalid", "locale"));
        assertEquals("Français", result.getLabel());
        assertTrue(result.getFlags().get(0).contains("Flag_of_France"));
    }
}

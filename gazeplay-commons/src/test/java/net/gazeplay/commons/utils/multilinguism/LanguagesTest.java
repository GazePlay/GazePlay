package net.gazeplay.commons.utils.multilinguism;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * French and English only since 18/11/2026 (maintainer's decision), one variant of each.
 */
class LanguagesTest {

    @Test
    void shouldOfferFrenchAndEnglishOnly() {
        Collection<LanguageDetails> result = Languages.getAllLanguageDetails();
        assertEquals(List.of(Locale.of("eng", "GB"), Locale.of("fra", "FR")),
            result.stream().map(LanguageDetails::getLocale).toList());
        assertEquals(List.of("English", "Français"), result.stream().map(LanguageDetails::getLabel).toList());
        assertTrue(result.stream().allMatch(language -> language.isLeftAligned()
            && language.isStableTranslationAvailable() && language.getFlags().size() == 1));
    }

    @Test
    void shouldGetAllCodes() {
        assertEquals(List.of(Locale.of("eng", "GB"), Locale.of("fra", "FR")), List.copyOf(Languages.getAllCodes()));
    }

    @Test
    void shouldGetValidLocale() {
        LanguageDetails english = Languages.getLocale(Locale.of("eng", "GB"));
        assertEquals("English", english.getLabel());
        assertTrue(english.getFlags().get(0).contains("Flag_of_the_United_Kingdom"));
        LanguageDetails french = Languages.getLocale(Locale.of("fra", "FR"));
        assertEquals("Français", french.getLabel());
        assertTrue(french.getFlags().get(0).contains("Flag_of_France"));
    }

    /**
     * A language no longer offered, or none, is shown in English (maintainer's decision of 18/11/2026).
     */
    @Test
    void shouldGiveEnglishForALanguageNoLongerOfferedOrNone() {
        for (Locale locale : new Locale[]{null, Locale.of("deu", "DE"), Locale.of("chn"), Locale.of("fra"),
            Locale.of("eng", "US"), Locale.of("invalid", "locale")}) {
            LanguageDetails result = Languages.getLocale(locale);
            assertEquals("English", result.getLabel(), String.valueOf(locale));
            assertTrue(result.getFlags().get(0).contains("Flag_of_the_United_Kingdom"));
        }
    }
}

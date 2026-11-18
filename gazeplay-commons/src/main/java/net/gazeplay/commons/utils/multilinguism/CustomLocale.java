package net.gazeplay.commons.utils.multilinguism;

import java.util.Locale;

/**
 * Locales of the languages of GazePlay, as written in the header of the translation files (ISO 639-3 codes, and a
 * country). Since 18/11/2026, French and English only (maintainer's decision); the translation files keep the columns
 * of the other languages, so that one can come back without translating everything again.
 */
public class CustomLocale {
    public static final Locale FRANCE = Locale.of("fra", "FR");
    public static final Locale UNITED_KINGDOM = Locale.of("eng", "GB");
}

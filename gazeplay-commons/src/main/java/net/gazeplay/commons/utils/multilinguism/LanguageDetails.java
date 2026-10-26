package net.gazeplay.commons.utils.multilinguism;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Locale;

/**
 * A language proposed in the settings, with its flags.
 */
@Builder
public class LanguageDetails {

    /**
     * The ISO 639 code of the language
     */
    @Getter
    private final Locale locale;

    /**
     * The language name
     */
    @Getter
    private final String label;

    /**
     * Whether the translation is complete enough to be proposed as stable.
     */
    @Getter
    private final boolean stableTranslationAvailable;

    /**
     * False for the languages written from right to left (Arabic).
     */
    @Getter
    private final boolean leftAligned;

    /**
     * Flags corresponding to this language
     */
    @Getter
    private final List<String> flags;

}

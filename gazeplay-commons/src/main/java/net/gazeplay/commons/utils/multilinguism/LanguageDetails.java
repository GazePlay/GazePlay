package net.gazeplay.commons.utils.multilinguism;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Locale;

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

    @Getter
    private final boolean stableTranslationAvailable;

    @Getter
    private final boolean leftAligned;

    /**
     * Flags corresponding to this language
     */
    @Getter
    private final List<String> flags;

}

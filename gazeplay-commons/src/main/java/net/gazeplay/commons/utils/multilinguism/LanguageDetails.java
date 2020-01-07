package net.gazeplay.commons.utils.multilinguism;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Locale;

@Builder
public class LanguageDetails {

    // the ISO 639 code of the language
    @Getter
    private final Locale locale;

    // the language name
    @Getter
    private final String label;

    @Getter
    private final boolean stableTranslationAvailable;

    @Getter
    private final boolean leftAligned;

    // flags corresponding to this language
    @Getter
    private final List<String> flags;

}

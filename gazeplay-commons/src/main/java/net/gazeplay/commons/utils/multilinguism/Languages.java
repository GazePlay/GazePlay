package net.gazeplay.commons.utils.multilinguism;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Languages of GazePlay Learning: French (fra) and English (eng), ISO 639-3 codes, one variant of each (France, United
 * Kingdom). Since 18/11/2026 (maintainer's decision), the 19 other languages are no longer offered: they were translated
 * for 12 to 20 % of the texts, the rest shown in English. Their columns stay in the translation files, so that a
 * language can come back once French and English are stable: a line here, its flag in data/common/flags, and
 * {@code Configuration.getLanguage()}.
 * <p>
 * see https://iso639-3.sil.org/code_tables/639/data/c
 */
@Slf4j
public class Languages {

    private static final SortedMap<Locale, LanguageDetails> languageMap = Collections.unmodifiableSortedMap(createLanguagesMap());

    /**
     * English: the language of a profile saved in a language no longer offered (maintainer's decision of 18/11/2026).
     */
    private static final Locale DEFAULT_CODE = CustomLocale.UNITED_KINGDOM;

    private static final String flagsPath = "data/common/flags/";

    private static SortedMap<Locale, LanguageDetails> createLanguagesMap() {
        final SortedMap<Locale, LanguageDetails> result = new TreeMap<>(
            (o1, o2) -> {
                final int languageComparison = o1.getLanguage().compareTo(o2.getLanguage());
                if (languageComparison != 0) {
                    return languageComparison;
                } else {
                    return o1.getCountry().compareTo(o2.getCountry());
                }
            });
        put(result, build(CustomLocale.UNITED_KINGDOM, "English", true, true, "800px-Flag_of_the_United_Kingdom.svg.png"));
        put(result, build(CustomLocale.FRANCE, "Français", true, true, "800px-Flag_of_France.svg.png"));
        return result;
    }

    private static void put(final SortedMap<Locale, LanguageDetails> result, final LanguageDetails languageDetails) {
        result.put(languageDetails.getLocale(), languageDetails);
    }

    private static LanguageDetails build(final Locale locale, final String language, final boolean stableTranslationAvailable, final boolean leftAligned, final String... flags) {
        return LanguageDetails.builder()
            .locale(locale)
            .label(language)
            .stableTranslationAvailable(stableTranslationAvailable)
            .leftAligned(leftAligned)
            .flags(toFlagsLocations(flags))
            .build();
    }

    private static List<String> toFlagsLocations(final String[] flags) {
        final List<String> flagsList = new ArrayList<>(flags.length);
        for (final String flag : flags) {
            flagsList.add(flagsPath + flag);
        }
        return Collections.unmodifiableList(flagsList);
    }

    /**
     * @return the details of a language, or of English when it is null or not a language of GazePlay
     */
    public static LanguageDetails getLocale(Locale locale) {
        Locale result = locale;
        if (result == null) {
            result = DEFAULT_CODE;
        }

        final LanguageDetails localeValue = languageMap.get(result);

        if (localeValue == null) {
            result = DEFAULT_CODE;
        }

        return languageMap.get(result);
    }

    /**
     * @return all the languages, sorted by language code then country
     */
    public static Collection<LanguageDetails> getAllLanguageDetails() {
        return languageMap.values();
    }

    /**
     * @return the locales of all the languages, sorted by language code then country
     */
    public static Collection<Locale> getAllCodes() {
        return languageMap.keySet();
    }

}

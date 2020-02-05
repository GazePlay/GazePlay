package net.gazeplay.commons.utils.multilinguism;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Languages of GazePlay
 * <p>
 * They follow ISO 639-3
 * <p>
 * alb: Albanian
 * <p>
 * ara: Arabic
 * <p>
 * chn: Chinese
 * <p>
 * deu: German
 * <p>
 * eng: English
 * <p>
 * fra: French
 * <p>
 * hrv: Croatian
 * <p>
 * ita: Italian
 * <p>
 * jpn: Japanese
 * <p>
 * pol: Polish
 * <p>
 * por: Portuguese
 * <p>
 * nld: Dutch
 * <p>
 * rus: Russian
 * <p>
 * spa: Spanish
 * <p>
 * vnm: Vietnamese
 * <p>
 * see https://iso639-3.sil.org/code_tables/639/data/c
 */
@Slf4j
public class Languages {

    private static final SortedMap<Locale, LanguageDetails> languageMap = Collections.unmodifiableSortedMap(createLanguagesMap());

    private static final Locale DEFAULT_CODE = new Locale("fra", "FR");

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
        put(result, build(new Locale("alb"), "Shqip", false, true, "800px-Flag_of_Albania.svg.png"));
        put(result, build(new Locale("ara"), "العَرَبِيَّة", false, false, "800px-Flag_of_the_Arab_League.svg.png"));
        put(result, build(new Locale("chn"), "漢語", false, true, "800px-Flag_of_the_People's_Republic_of_China.svg.png"));
        put(result, build(new Locale("deu", "DE"), "Deutsch", true, true, "800px-Flag_of_Germany.svg.png"));
        put(result, build(new Locale("deu", "AT"), "Deutsch", true, true, "800px-Flag_of_Austria.svg.png"));
        put(result, build(new Locale("eng", "GB"), "English", true, true, "800px-Flag_of_the_United_Kingdom.svg.png"));
        put(result, build(new Locale("eng", "US"), "English", true, true, "800px-Flag_of_the_United_States_(Pantone).svg.png"));
        put(result, build(new Locale("ell"), "ελληνικά", false, true, "800px-Flag_of_Greece.svg.png"));
        put(result, build(new Locale("fin"), "Suomi", false, true, "800px-Flag_of_Finland.svg.png"));
        put(result, build(new Locale("fra", "FR"), "Français", true, true, "800px-Flag_of_France.svg.png"));
        put(result, build(new Locale("fra"), "Français", true, true, "Flag_of_La_Francophonie.svg.png"));
        put(result, build(new Locale("hrv"), "Hrvatski", false, true, "800px-Flag_of_Croatia.svg.png"));
        put(result, build(new Locale("ind"), "Bahasa Indonesia", false, true, "800px-Flag_of_Indonesia.svg.png"));
        put(result, build(new Locale("ita"), "Italiano", false, true, "800px-Flag_of_Italy.svg.png"));
        put(result, build(new Locale("jpn"), "日本語", false, true, "800px-Flag_of_Japan.svg.png"));
        put(result, build(new Locale("pol"), "polszczyzna", false, true, "800px-Flag_of_Poland.svg.png"));
        put(result, build(new Locale("por", "PT"), "Português", false, true, "800px-Flag_of_Portugal.svg.png"));
        put(result, build(new Locale("por", "BR"), "Português", false, true, "800px-Flag_of_Brazil.svg.png"));
        put(result, build(new Locale("nld"), "Nederlands", false, true, "800px-Flag_of_the_Netherlands.svg.png"));
        put(result, build(new Locale("rus"), "Pу́сский язы́к", false, true, "800px-Flag_of_Russia.svg.png"));
        put(result, build(new Locale("spa"), "Español", false, true, "800px-Flag_of_Spain.svg.png"));
        put(result, build(new Locale("vnm"), "Tiếng Việt", false, true, "800px-Flag_of_Vietnam.svg.png"));
        put(result, build(new Locale("zsm"), "Bahasa Melayu", false, true, "800px-Flag_of_Malaysia.svg.png"));
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

    public static LanguageDetails getLocale(Locale locale) {
        if (locale == null) {
            locale = DEFAULT_CODE;
        }

        final LanguageDetails localeValue = languageMap.get(locale);

        if (localeValue == null) {
            locale = DEFAULT_CODE;
        }

        return languageMap.get(locale);
    }

    public static Collection<LanguageDetails> getAllLanguageDetails() {
        return languageMap.values();
    }

    public static Collection<Locale> getAllCodes() {
        return languageMap.keySet();
    }

}

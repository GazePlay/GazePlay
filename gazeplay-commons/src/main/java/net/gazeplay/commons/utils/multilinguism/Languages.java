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

    private static final SortedMap<LanguageLocale, LanguageDetails> languageMap = Collections.unmodifiableSortedMap(createLanguagesMap());

    private static final LanguageLocale DEFAULT_CODE = new LanguageLocale("fra","FR");

    private static final String flagsPath = "data/common/flags/";

    private static SortedMap<LanguageLocale, LanguageDetails> createLanguagesMap() {
        SortedMap<LanguageLocale, LanguageDetails> result = new TreeMap<>();
        put(result, build(new LanguageLocale("alb"), "Shqip", false, true, "800px-Flag_of_Albania.svg.png"));
        put(result, build(new LanguageLocale("ara"), "العَرَبِيَّة", false, false, "800px-Flag_of_the_Arab_League.svg.png"));
        put(result, build(new LanguageLocale("chn"), "漢語", false, true, "800px-Flag_of_the_People's_Republic_of_China.svg.png"));
        put(result, build(new LanguageLocale("deu","DE"), "Deutsch", true, true, "800px-Flag_of_Germany.svg.png"));
        put(result, build(new LanguageLocale("deu","AT"), "Deutsch", true, true, "800px-Flag_of_Austria.svg.png"));
        put(result, build(new LanguageLocale("eng","GB"), "English", true, true, "800px-Flag_of_the_United_Kingdom.svg.png"));
        put(result, build(new LanguageLocale("eng","US"), "English", true, true, "800px-Flag_of_the_United_States_(Pantone).svg.png"));
        put(result, build(new LanguageLocale("ell"), "ελληνικά", false, true, "800px-Flag_of_Greece.svg.png"));
        put(result, build(new LanguageLocale("fin"), "Suomi", false, true, "800px-Flag_of_Finland.svg.png"));
        put(result, build(new LanguageLocale("fra","FR"), "Français", true, true, "800px-Flag_of_France.svg.png"));
        put(result, build(new LanguageLocale("fra"), "Français", true, true, "Flag_of_La_Francophonie.svg.png"));
        put(result, build(new LanguageLocale("hrv"), "Hrvatski", false, true, "800px-Flag_of_Croatia.svg.png"));
        put(result, build(new LanguageLocale("ind"), "Bahasa Indonesia", false, true, "800px-Flag_of_Indonesia.svg.png"));
        put(result, build(new LanguageLocale("ita"), "Italiano", false, true, "800px-Flag_of_Italy.svg.png"));
        put(result, build(new LanguageLocale("jpn"), "日本語", false, true, "800px-Flag_of_Japan.svg.png"));
        put(result, build(new LanguageLocale("pol"), "polszczyzna", false, true, "800px-Flag_of_Poland.svg.png"));
        put(result, build(new LanguageLocale("por","PT"), "Português", false, true, "800px-Flag_of_Portugal.svg.png"));
        put(result, build(new LanguageLocale("por","BR"), "Português", false, true, "800px-Flag_of_Brazil.svg.png"));
        put(result, build(new LanguageLocale("nld"), "Nederlands", false, true, "800px-Flag_of_the_Netherlands.svg.png"));
        put(result, build(new LanguageLocale("rus"), "Pу́сский язы́к", false, true, "800px-Flag_of_Russia.svg.png"));
        put(result, build(new LanguageLocale("spa"), "Español", false, true, "800px-Flag_of_Spain.svg.png"));
        put(result, build(new LanguageLocale("vnm"), "Tiếng Việt", false, true, "800px-Flag_of_Vietnam.svg.png"));
        put(result, build(new LanguageLocale("zsm"), "Bahasa Melayu", false, true, "800px-Flag_of_Malaysia.svg.png"));
        return result;
    }

    private static void put(SortedMap<LanguageLocale, LanguageDetails> result, LanguageDetails languageDetails) {
        result.put(languageDetails.getLocale(), languageDetails);
    }

    private static LanguageDetails build(LanguageLocale locale, String language, boolean stableTranslationAvailable, boolean leftAligned, String... flags) {
        return LanguageDetails.builder()
            .locale(locale)
            .label(language)
            .stableTranslationAvailable(stableTranslationAvailable)
            .leftAligned(leftAligned)
            .flags(toFlagsLocations(flags))
            .build();
    }

    private static List<String> toFlagsLocations(String[] flags) {
        List<String> flagsList = new ArrayList<>(flags.length);
        for (String flag : flags) {
            flagsList.add(flagsPath + flag);
        }
        return Collections.unmodifiableList(flagsList);
    }

    public static LanguageDetails getLocale(LanguageLocale locale) {
        if (locale == null) {
            locale = DEFAULT_CODE;
        }

        LanguageDetails localeValue = languageMap.get(locale);

        if (localeValue == null){
            locale = DEFAULT_CODE;
        }

        log.debug(locale.getLanguage() + " ..... " + locale.getCountry());
        return languageMap.get(locale);
    }

    public static Collection<LanguageDetails> getAllLanguageDetails() {
        return languageMap.values();
    }

    public static Collection<LanguageLocale> getAllCodes() {
        return languageMap.keySet();
    }

}

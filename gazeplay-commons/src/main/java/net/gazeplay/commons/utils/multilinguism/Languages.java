package net.gazeplay.commons.utils.multilinguism;

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
public class Languages {

    private static final SortedMap<String, LanguageDetails> languageMap = Collections.unmodifiableSortedMap(createLanguagesMap());

    private static final String DEFAULT_CODE = "fra";

    private static final String flagsPath = "data/common/flags/";

    private static SortedMap<String, LanguageDetails> createLanguagesMap() {
        SortedMap<String, LanguageDetails> result = new TreeMap<>();
        put(result, build("alb", "Shqip", false, true, "800px-Flag_of_Albania.svg.png"));
        put(result, build("ara", "العَرَبِيَّة", false, false, "800px-Flag_of_the_Arab_League.svg.png"));
        put(result, build("chn", "漢語", false, true, "800px-Flag_of_the_People's_Republic_of_China.svg.png"));
        put(result, build("deu", "Deutsch", true, true, "800px-Flag_of_Germany.svg.png", "800px-Flag_of_Austria.svg.png"));
        put(result, build("eng", "English", true, true, "800px-Flag_of_the_United_Kingdom.svg.png", "800px-Flag_of_the_United_States_(Pantone).svg.png"));
        put(result, build("ell", "ελληνικά", false, true, "800px-Flag_of_Greece.svg.png"));
        put(result, build("fin", "Suomi", false, true, "800px-Flag_of_Finland.svg.png"));
        put(result, build("fra", "Français", true, true, "800px-Flag_of_France.svg.png", "Flag_of_La_Francophonie.svg.png"));
        put(result, build("hrv", "Hrvatski", false, true, "800px-Flag_of_Croatia.svg.png"));
        put(result, build("ind", "Bahasa Indonesia", false, true, "800px-Flag_of_Indonesia.svg.png"));
        put(result, build("ita", "Italiano", false, true, "800px-Flag_of_Italy.svg.png"));
        put(result, build("jpn", "日本語", false, true, "800px-Flag_of_Japan.svg.png"));
        put(result, build("pol", "polszczyzna", false, true, "800px-Flag_of_Poland.svg.png"));
        put(result, build("por", "Português", false, true, "800px-Flag_of_Portugal.svg.png", "800px-Flag_of_Brazil.svg.png"));
        put(result, build("nld", "Nederlands", false, true, "800px-Flag_of_the_Netherlands.svg.png"));
        put(result, build("rus", "Pу́сский язы́к", false, true, "800px-Flag_of_Russia.svg.png"));
        put(result, build("spa", "Español", false, true, "800px-Flag_of_Spain.svg.png"));
        put(result, build("vnm", "Tiếng Việt", false, true, "800px-Flag_of_Vietnam.svg.png"));
        put(result, build("zsm", "Bahasa Melayu", false, true, "800px-Flag_of_Malaysia.svg.png"));
        return result;
    }

    private static void put(SortedMap<String, LanguageDetails> result, LanguageDetails languageDetails) {
        result.put(languageDetails.getCode(), languageDetails);
    }

    private static LanguageDetails build(String code, String language, boolean stableTranslationAvailable, boolean leftAligned, String... flags) {
        return LanguageDetails.builder()
            .code(code)
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

    public static LanguageDetails getLanguage(String code) {
        if (code == null) {
            code = DEFAULT_CODE;
        }
        return languageMap.get(code);
    }

    public static Collection<LanguageDetails> getAllLanguageDetails() {
        return languageMap.values();
    }

    public static Collection<String> getAllCodes() {
        return languageMap.keySet();
    }

}

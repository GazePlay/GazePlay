package net.gazeplay.commons.utils.multilinguism;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Languages of GazePlay
 *
 * They follow ISO 639-3
 *
 * ara: Arabic
 *
 * chn: Chinese
 *
 * deu: German
 *
 * eng: English
 *
 * fra: French
 *
 * hrv: Croatian
 *
 * ita: Italian
 *
 * jpn: Japanese
 *
 * pol: Polish
 *
 * por: Portuguese
 *
 * nld: Dutch
 *
 * rus: Russian
 *
 * spa: Spanish
 *
 * vnm: Vietnamese
 *
 * see https://iso639-3.sil.org/code_tables/639/data/c
 *
 */
public class Languages {

    private static final HashMap<String, CodeLanguageFlag> languageMap = new HashMap<>(50);
    private static final HashMap<String, String> languageCodeMap = new HashMap<>(50);
    private static final String DEFAULT_CODE = "fra";

    static {

        put("ara", "العَرَبِيَّة", "data/common/flags/800px-Flag_of_the_Arab_League.svg.png");
        put("chn", "漢語", "data/common/flags/800px-Flag_of_the_People's_Republic_of_China.svg.png");
        put("deu", "Deutsch", "data/common/flags/800px-Flag_of_Germany.svg.png",
                "data/common/flags/800px-Flag_of_Austria.svg.png");
        put("eng", "English", "data/common/flags/800px-Flag_of_the_United_Kingdom.svg.png",
                "data/common/flags/800px-Flag_of_the_United_States_(Pantone).svg.png");
        put("ell", "ελληνικά", "data/common/flags/800px-Flag_of_Greece.svg.png");
        put("fin", "Suomi", "data/common/flags/800px-Flag_of_Finland.svg.png");
        put("fra", "Français", "data/common/flags/Flag_of_La_Francophonie.svg.png",
                "data/common/flags/800px-Flag_of_France.svg.png");
        put("hrv", "Hrvatski", "data/common/flags/800px-Flag_of_Croatia.svg.png");
        put("ind", "Bahasa Indonesia", "data/common/flags/800px-Flag_of_Indonesia.svg.png");
        put("ita", "Italiano", "data/common/flags/800px-Flag_of_Italy.svg.png");
        put("jpn", "日本語", "data/common/flags/800px-Flag_of_Japan.svg.png");
        put("pol", "polszczyzna", "data/common/flags/800px-Flag_of_Poland.svg.png");
        put("por", "Português", "data/common/flags/800px-Flag_of_Portugal.svg.png",
                "data/common/flags/800px-Flag_of_Brazil.svg.png");
        put("nld", "Nederlands", "data/common/flags/800px-Flag_of_the_Netherlands.svg.png");
        put("rus", "Pу́сский язы́к", "data/common/flags/800px-Flag_of_Russia.svg.png");
        put("spa", "Español", "data/common/flags/800px-Flag_of_Spain.svg.png");
        put("vnm", "Tiếng Việt", "data/common/flags/800px-Flag_of_Vietnam.svg.png");
        put("zsm", "Bahasa Melayu", "data/common/flags/800px-Flag_of_Malaysia.svg.png");
    }

    /**
     *
     * 1st: code
     *
     * 2nd: language
     *
     * next: path to flag
     *
     */
    private static void put(String... args) {

        String code = args[0];
        String language = args[1];

        ArrayList<String> flags = new ArrayList(args.length);

        for (int i = 2; i < args.length; i++) {

            flags.add(args[i]);
        }

        languageMap.put(code, new CodeLanguageFlag(code, language, flags));
        languageCodeMap.put(language, code);

    }

    public static String getCode(String language) {

        return languageCodeMap.get(language);
    }

    public static String getLanguage(String code) {

        if (code == null)
            code = DEFAULT_CODE;

        return languageMap.get(code).language;
    }

    public static ArrayList<String> getLanguages() {

        return new ArrayList<String>(languageCodeMap.keySet());
    }

    public static ArrayList<String> getCodes() {

        return new ArrayList<String>(languageMap.keySet());
    }

    public static ArrayList<String> getFlags(String code) {

        return languageMap.get(code).flags;
    }
}

/**
 *
 * String code: the ISO 639 code of the language
 *
 * String language: the language
 *
 * ArrayList<String> flags corresponding to this language
 */

@Slf4j
@AllArgsConstructor
class CodeLanguageFlag {

    public String code;
    public String language;
    public ArrayList<String> flags;
}

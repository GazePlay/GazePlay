package net.gazeplay.commons.utils.multilinguism;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

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

    private static final HashMap<String, CodeLanguagesFlags> languageMap = new HashMap<>(50);
    private static final HashMap<String, String> languageCodeMap = new HashMap<>(50);

    static {

        put("ara", "العَرَبِيَّة",
                "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_the_Arab_League.svg.png");
        put("chn", "Chinese",
                "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_the_People's_Republic_of_China.svg.png");
        put("deu", "Deutsch", "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_Germany.svg.png",
                "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_Austria.svg.png");
        put("eng", "English", "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_the_United_Kingdom.svg.png",
                "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_the_United_States_(Pantone).svg.png");
        put("fra", "Français", "net/gazeplay/commons/utils/multilinguism/flags/Flag_of_La_Francophonie.svg.png",
                "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_France.svg.png");
        put("hrv", "Croatian", "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_Croatia.svg.png");
        put("ita", "Italian",
                "/Users/schwab/Documents/GazePlay/gazeplay-commons/src/main/java/net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_Italy.svg.png");
        put("jpn", "Japanese", "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_Japan.svg.png");
        put("por", "Portuguese", "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_Portugal.svg.png");
        put("nld", "Dutch", "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_the_Netherlands.svg.png");
        put("rus", "Russian", "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_Russia.svg.png");
        put("spa", "Spanish", "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_Spain.svg.png");
        put("vnm", "Vietnamese",
                "/Users/schwab/Documents/GazePlay/gazeplay-commons/src/main/java/net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_Vietnam.svg.png");
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

        languageMap.put(code, new CodeLanguagesFlags(code, language, flags));
        languageCodeMap.put(language, code);

    }

    public static String getCode(String language) {

        return languageCodeMap.get(language);
    }

    public static String getLanguage(String code) {

        return languageMap.get(code).language;
    }

    public static ArrayList<String> values() {

        return new ArrayList<String>(languageCodeMap.keySet());
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
@NoArgsConstructor
@AllArgsConstructor
class CodeLanguagesFlags {

    public String code;
    public String language;
    public ArrayList<String> flags;
}

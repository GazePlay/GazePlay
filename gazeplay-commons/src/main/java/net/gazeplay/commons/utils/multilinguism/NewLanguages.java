package net.gazeplay.commons.utils.multilinguism;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
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
public class NewLanguages {

    private static final HashMap<String, CodeLanguagesFlags> languageMap = new HashMap<>(50);

    private NewLanguages(){

        put("ara", "العَرَبِيَّة", "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_the_Arab_League.svg.png");
        put("deu", "Deutsch", "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_Germany.svg.png", "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_Austria.svg.png");
        put("fra", "Français", "net/gazeplay/commons/utils/multilinguism/flags/Flag_of_La_Francophonie.svg.png", "net/gazeplay/commons/utils/multilinguism/flags/800px-Flag_of_France.svg.png");

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
    private void put(String... args){

        String code = args[0];
        String language = args[1];

        ArrayList<String> flags = new ArrayList(args.length);

        for(int i = 2; i < args.length; i++){

            flags.add(args[i]);
        }

        languageMap.put(code, new CodeLanguagesFlags(code, language, flags));

    }

  //  ara, chn, deu, eng, fra, hrv, ita, jpn, por, nld, rus, spa, vnm
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
class CodeLanguagesFlags{

    public String code;
    public String language;
    public ArrayList<String> flags;




}



package net.gazeplay.commons.utils.multilinguism;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Multilinguism {

    private final static String mainFilePath = "data/multilinguism/multilinguism.csv";

    @Getter
    private static Multilinguism singleton = new Multilinguism(new I18N(mainFilePath));

    private static Map<String, Multilinguism> byResourceLocation = new HashMap<>();

    public static Multilinguism getForResource(String resourceLocation) {
        Multilinguism result = byResourceLocation.get(resourceLocation);
        if (result == null) {
            result = new Multilinguism(new I18N(resourceLocation));
            byResourceLocation.put(resourceLocation, result);
        }
        return result;
    }

    private final I18N i18n;

    private Multilinguism(I18N i18n) {
        this.i18n = i18n;
    }

    public String getTrad(String key, String language) {
        String translate = i18n.translate(key, language);
        if (translate == null) {
            log.warn("No translation found for key '{}'", key);
            return "[untranslated!] " + key;
        }
        return translate;
    }

}

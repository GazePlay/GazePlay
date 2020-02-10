package net.gazeplay.commons.utils.multilinguism;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Multilinguism {

    private final static String mainFilePath = "data/multilinguism/multilinguism.csv";

    @Getter
    private static final Multilinguism singleton = new Multilinguism(new I18N(mainFilePath));

    private static final Map<String, Multilinguism> byResourceLocation = new HashMap<>();

    public static Multilinguism getForResource(final String resourceLocation) {
        Multilinguism result = byResourceLocation.get(resourceLocation);
        if (result == null) {
            result = new Multilinguism(new I18N(resourceLocation));
            byResourceLocation.put(resourceLocation, result);
        }
        return result;
    }

    private final I18N i18n;

    private Multilinguism(final I18N i18n) {
        this.i18n = i18n;
    }

    public String getTrad(final String key, final String language) {
        final String translate = i18n.translate(key, language);
        if (translate == null || "".equals(translate)) {
            log.warn("No translation found for key '{}'", key);

            return getENGTrad(key);
        }
        return translate;
    }

    /**
     * This function is used if no key exist for the language in getTrad(String key, String language) function
     */
    public String getENGTrad(final String key) {
        final String translate = i18n.translate(key, "eng");
        if (translate == null || "".equals(translate)) {
            log.warn("No translation found for key '{}'", key);

            return "[untranslated!] " + key;
        }
        return translate;
    }

}

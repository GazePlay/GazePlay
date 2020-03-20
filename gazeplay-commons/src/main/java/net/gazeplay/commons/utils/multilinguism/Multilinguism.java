package net.gazeplay.commons.utils.multilinguism;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Multilinguism {

    private final I18N i18n;

    Multilinguism(final I18N i18n) {
        this.i18n = i18n;
    }

    public String getTranslation(final String key, final String language) {
        final String translate = i18n.translate(key, language);
        if (translate == null || translate.isBlank()) {
            log.warn("No translation found for key '{}'", key);

            return getEngTranslation(key);
        }
        return translate;
    }

    /**
     * This function is used if no key exist for the language in getTrad(String key, String language) function
     */
    public String getEngTranslation(final String key) {
        final String translate = i18n.translate(key, "eng");
        if (translate == null || "".equals(translate)) {
            log.warn("No translation found for key '{}'", key);

            return "[untranslated!] " + key;
        }
        return translate;
    }

}

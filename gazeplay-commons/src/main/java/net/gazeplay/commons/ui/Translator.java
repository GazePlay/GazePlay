package net.gazeplay.commons.ui;

import java.util.Locale;

/**
 * Translates the texts of the interface into the language of the settings. The translations are the lines of the
 * multilinguism CSV files, the keys being their first column.
 */
public interface Translator {

    /**
     * A component that changes its text when the language changes.
     */
    interface LanguageChangeListener {

        void languageChanged();

    }

    /**
     * @return the translation of a key in the current language, else in English, else "[untranslated!] " and the key
     */
    String translate(String key);

    /**
     * @return the translations of the keys, put end to end
     */
    String translate(String... keys);

    /**
     * Registers a component to tell when the language changes.
     */
    void registerLanguageChangeListener(LanguageChangeListener listener);

    /**
     * Reads the language of the active configuration again and tells it to the registered components.
     */
    void notifyLanguageChanged();

    /**
     * @return the language and country of the settings
     */
    Locale currentLocale();

}

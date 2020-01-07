package net.gazeplay.commons.ui;

import java.util.Locale;

public interface Translator {

    interface LanguageChangeListener {

        void languageChanged();

    }

    String translate(String key);

    String translate(String... keys);

    void registerLanguageChangeListener(LanguageChangeListener listener);

    void notifyLanguageChanged();

    Locale currentLocale();

}

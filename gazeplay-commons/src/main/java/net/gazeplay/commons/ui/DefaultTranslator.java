package net.gazeplay.commons.ui;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.multilinguism.LanguageLocale;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class DefaultTranslator implements Translator {

    private final Multilinguism multilinguism;

    private Configuration config;

    private final List<LanguageChangeListener> languageChangeListeners = new CopyOnWriteArrayList<>();

    public DefaultTranslator(Configuration config, Multilinguism multilinguism) {
        this.config = config;
        this.multilinguism = multilinguism;
    }

    @Override
    public String translate(String key) {
        return multilinguism.getTrad(key, config.getLanguage());
    }

    @Override
    public String translate(String... keys) {
        StringBuilder textBuilder = new StringBuilder();
        for (String key : keys) {
            textBuilder.append(translate(key));
        }
        return textBuilder.toString();
    }

    @Override
    public void registerLanguageChangeListener(LanguageChangeListener listener) {
        languageChangeListeners.add(listener);
    }

    @Override
    public void notifyLanguageChanged() {
        config = ActiveConfigurationContext.getInstance();
        this.notifyAllListeners();
    }

    @Override
    public LanguageLocale currentLocale() {
        return new LanguageLocale(config.getLanguage(),config.getCountry());
    }

    private void notifyAllListeners() {
        for (LanguageChangeListener l : languageChangeListeners) {
            l.languageChanged();
        }
    }

}

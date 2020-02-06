package net.gazeplay.commons.ui;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class DefaultTranslator implements Translator {

    private final Multilinguism multilinguism;

    private Configuration config;

    private final List<LanguageChangeListener> languageChangeListeners = new CopyOnWriteArrayList<>();

    public DefaultTranslator(final Configuration config, final Multilinguism multilinguism) {
        this.config = config;
        this.multilinguism = multilinguism;
    }

    @Override
    public String translate(final String key) {
        return multilinguism.getTrad(key, config.getLanguage());
    }

    @Override
    public String translate(final String... keys) {
        final StringBuilder textBuilder = new StringBuilder();
        for (final String key : keys) {
            textBuilder.append(translate(key));
        }
        return textBuilder.toString();
    }

    @Override
    public void registerLanguageChangeListener(final LanguageChangeListener listener) {
        languageChangeListeners.add(listener);
    }

    @Override
    public void notifyLanguageChanged() {
        config = ActiveConfigurationContext.getInstance();
        this.notifyAllListeners();
    }

    @Override
    public Locale currentLocale() {
        return new Locale(config.getLanguage(), config.getCountry());
    }

    private void notifyAllListeners() {
        for (final LanguageChangeListener l : languageChangeListeners) {
            l.languageChanged();
        }
    }

}

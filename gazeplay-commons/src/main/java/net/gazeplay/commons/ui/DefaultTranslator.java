package net.gazeplay.commons.ui;

import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
        config = ConfigurationBuilder.createFromPropertiesResource().build();
        this.notifyAllListeners();
    }

    private void notifyAllListeners() {
        for (LanguageChangeListener l : languageChangeListeners) {
            l.languageChanged();
        }
    }

}

package net.gazeplay.commons.ui;

import javafx.scene.control.Button;

/**
 * Button whose text is the translation of keys, updated when the language changes.
 */
public class I18NButton extends Button implements Translator.LanguageChangeListener {

    private final String[] textKeys;

    private final Translator translator;

    public I18NButton(Translator translator, String... textKeys) {
        super();
        this.textKeys = textKeys;
        this.translator = translator;
        languageChanged();
        translator.registerLanguageChangeListener(this);
    }

    @Override
    public void languageChanged() {
        if (textKeys != null) {
            this.setText(translator.translate(textKeys));
        }
    }

}

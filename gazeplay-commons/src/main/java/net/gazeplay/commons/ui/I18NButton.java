package net.gazeplay.commons.ui;

import javafx.scene.control.Button;

public class I18NButton extends Button implements Translator.LanguageChangeListener {

    private String[] textKeys;

    private final Translator translator;

    public I18NButton(Translator translator, String... textKeys) {
        super();
        this.textKeys = textKeys;
        this.translator = translator;
        //
        languageChanged();
        //
        translator.registerLanguageChangeListener(this);
    }

    public void setTextKeys(String[] value) {
        this.textKeys = value;
        languageChanged();
    }

    @Override
    public void languageChanged() {
        if (textKeys != null) {
            setText(translator.translate(textKeys));
        }
    }

}

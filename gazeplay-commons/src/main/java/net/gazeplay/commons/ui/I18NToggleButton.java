package net.gazeplay.commons.ui;

import javafx.scene.control.ToggleButton;

public class I18NToggleButton extends ToggleButton implements Translator.LanguageChangeListener {

    private String[] textKeys;

    private final Translator translator;

    public I18NToggleButton(Translator translator, String... textKeys) {
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
            this.setText(translator.translate(textKeys));
        }
    }

}

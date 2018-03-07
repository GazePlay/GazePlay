package net.gazeplay.commons.ui;

import javafx.scene.control.Tooltip;

public class I18NTooltip extends Tooltip implements Translator.LanguageChangeListener {

    private String[] textKeys;

    private final Translator translator;

    public I18NTooltip(Translator translator, String... textKeys) {
        super();
        this.textKeys = textKeys;
        this.translator = translator;
        //
        setText(translator.translate(textKeys));
        //
        translator.registerLanguageChangeListener(this);
    }

    public void setTextKeys(String[] value) {
        this.textKeys = value;
        languageChanged();
    }

    @Override
    public void languageChanged() {
        setText(translator.translate(textKeys));
    }

}

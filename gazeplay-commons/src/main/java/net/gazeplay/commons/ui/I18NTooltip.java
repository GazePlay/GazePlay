package net.gazeplay.commons.ui;

import javafx.scene.control.Tooltip;

/**
 * Tooltip whose text is the translation of keys, updated when the language changes.
 */
public class I18NTooltip extends Tooltip implements Translator.LanguageChangeListener {

    private final String[] textKeys;

    private final Translator translator;

    public I18NTooltip(Translator translator, String... textKeys) {
        super();
        this.textKeys = textKeys;
        this.translator = translator;
        setText(translator.translate(textKeys));
        translator.registerLanguageChangeListener(this);
    }

    @Override
    public void languageChanged() {
        setText(translator.translate(textKeys));
    }

}

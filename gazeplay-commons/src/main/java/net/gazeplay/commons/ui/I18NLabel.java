package net.gazeplay.commons.ui;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

public class I18NLabel extends Label implements Translator.LanguageChangeListener {

    private final String[] textKeys;

    private final Translator translator;

    public I18NLabel(Translator translator, String... textKeys) {
        super();
        this.textKeys = textKeys;
        this.translator = translator;
        //
        String newText = translator.translate(textKeys);
        setText(newText);
        setTooltip(new Tooltip(newText));
        setAccessibleText(newText);
        //
        translator.registerLanguageChangeListener(this);
    }

    @Override
    public void languageChanged() {
        String newText = translator.translate(textKeys);
        setText(newText);
        setTooltip(new Tooltip(newText));
        setAccessibleText(newText);
    }

}

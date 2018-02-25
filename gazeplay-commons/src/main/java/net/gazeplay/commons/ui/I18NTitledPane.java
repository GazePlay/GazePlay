package net.gazeplay.commons.ui;

import javafx.scene.control.TitledPane;

public class I18NTitledPane extends TitledPane implements Translator.LanguageChangeListener {

    private final String[] textKeys;

    private final Translator translator;

    public I18NTitledPane(Translator translator, String... textKeys) {
        super();
        this.textKeys = textKeys;
        this.translator = translator;
        //
        setText(translator.translate(textKeys));
        //
        translator.registerLanguageChangeListener(this);
    }

    @Override
    public void languageChanged() {
        setText(translator.translate(textKeys));
    }

}

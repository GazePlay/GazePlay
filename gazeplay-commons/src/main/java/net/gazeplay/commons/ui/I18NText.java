package net.gazeplay.commons.ui;

import javafx.scene.text.Text;

public class I18NText extends Text implements Translator.LanguageChangeListener {

    private final String[] textKeys;

    private final Translator translator;

    public I18NText(Translator translator, String... textKeys) {
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

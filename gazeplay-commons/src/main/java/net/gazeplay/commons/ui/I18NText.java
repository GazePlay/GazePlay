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
        if (textKeys != null && "EnableGazeMouse".equals(textKeys[0])) {
            String[] labelParts = translator.translate(textKeys).split(";");
            String concatenateLabel = "";
            for (String labels : labelParts) {
                concatenateLabel = concatenateLabel + labels + "\n\t";
            }
            this.setText(concatenateLabel);
        } else {
            setText(translator.translate(textKeys));
        }
    }

}

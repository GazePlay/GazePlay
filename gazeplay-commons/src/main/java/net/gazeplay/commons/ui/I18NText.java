package net.gazeplay.commons.ui;

import javafx.scene.text.Text;

/**
 * Text whose content is the translation of keys, updated when the language changes. The parts of the "EnableGazeMouse"
 * translation separated by ";" are put on separate lines.
 */
public class I18NText extends Text implements Translator.LanguageChangeListener {

    private final String[] textKeys;

    private final Translator translator;

    public I18NText(Translator translator, String... textKeys) {
        super();
        this.textKeys = textKeys;
        this.translator = translator;
        setText(translator.translate(textKeys));
        translator.registerLanguageChangeListener(this);
    }

    @Override
    public void languageChanged() {
        if (textKeys != null && "EnableGazeMouse".equals(textKeys[0])) {
            String[] labelParts = translator.translate(textKeys).split(";");
            StringBuilder concatenateLabel = new StringBuilder();
            for (String labels : labelParts) {
                concatenateLabel.append(labels).append("\n\t");
            }
            this.setText(concatenateLabel.toString());
        } else {
            setText(translator.translate(textKeys));
        }
    }

}

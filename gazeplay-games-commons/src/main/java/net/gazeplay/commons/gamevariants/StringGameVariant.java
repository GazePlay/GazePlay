package net.gazeplay.commons.gamevariants;

import lombok.Data;
import net.gazeplay.commons.ui.Translator;

/**
 * Variant given by a value and the translation key of its label.
 */
@Data
public class StringGameVariant implements IGameVariant {

    private final String label;
    private final String value;

    @Override
    public String getLabel(final Translator translator) {
        return translator.translate(label);
    }

    @Override
    public String toString() {
        return "StringGameVariant:" + label + ":" + value;
    }

}

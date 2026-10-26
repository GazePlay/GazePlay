package net.gazeplay.commons.gamevariants;

import lombok.Getter;
import lombok.Setter;
import net.gazeplay.commons.ui.Translator;

/**
 * Variant given by a number and a translation key, with an optional second number (e.g. the number of trains of Train
 * Switches, chosen in the menu).
 */
public class IntStringGameVariant implements IGameVariant {

    @Getter
    private final int number;
    @Getter
    private final String stringValue;

    @Getter
    @Setter
    private int number2;

    public IntStringGameVariant(int number, String stringValue) {
        this.number = number;
        this.stringValue = stringValue;
    }

    public IntStringGameVariant(int number, String stringValue, int number2) {
        this.number = number;
        this.number2 = number2;
        this.stringValue = stringValue;
    }

    @Override
    public String getLabel(final Translator translator) {
        return number + " " + translator.translate(stringValue);
    }

    @Override
    public String toString() {
        return "IntStringGameVariant:" + number + ":" + stringValue + ":" + number2;
    }
}

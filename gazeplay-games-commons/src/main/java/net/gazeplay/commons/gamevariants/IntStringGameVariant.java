package net.gazeplay.commons.gamevariants;

import lombok.Getter;
import lombok.Setter;
import net.gazeplay.commons.ui.Translator;

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

    // The following commented code will be used later for different positions of EggGame
    public static String[] positions = new String[]{
        "TOPLEFT",
        "TOPCENTER",
        "TOPRIGHT",
        "MIDDLELEFT",
        "MIDDLECENTER",
        "MIDDLERIGHT",
        "BOTTOMLEFT",
        "BOTTOMCENTER",
        "BOTTOMRIGHT"};

    // The following commented code will be used later for High-Contrast Color Game
    public static String[] contrastStyles = new String[]{
        "NORMAL", "HIGH"
    };

    @Override
    public String getLabel(final Translator translator) {
        return number + " " + translator.translate(stringValue);
    }

    @Override
    public String toString() {
        return "IntStringGameVariant:" + number + ":" + stringValue + ":" + number2;
    }
}

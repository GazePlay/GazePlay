package net.gazeplay.commons.gamevariants;

import lombok.Getter;
import net.gazeplay.commons.ui.Translator;

public class IntStringGameVariant implements IGameVariant {

    @Getter
    private final int number;
    @Getter
    private final String stringValue;

    public IntStringGameVariant(int number, String stringValue) {
        this.number = number;
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
        return "IntStringGameVariant:" + number + ":" + stringValue;
    }
}

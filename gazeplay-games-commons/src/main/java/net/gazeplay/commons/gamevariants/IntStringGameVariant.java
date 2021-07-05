package net.gazeplay.commons.gamevariants;

import lombok.Data;
import net.gazeplay.commons.ui.Translator;

@Data
public class IntStringGameVariant implements IGameVariant {

    private final int number;
    private final String type;

    @Override
    public String getLabel(final Translator translator) {
        return number + " " + translator.translate(type);
    }

    @Override
    public String toString() {
        return "IntStringGameVariant:" + number + ":" + type;
    }

    public int getNumber() {
        return number;
    }

    public String getType() {
        return type;
    }
}

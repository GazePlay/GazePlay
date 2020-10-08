package net.gazeplay.commons.gamevariants;

import lombok.Data;
import net.gazeplay.commons.ui.Translator;

@Data
public class IntGameVariant implements IGameVariant {

    private final int number;

    @Override
    public String getLabel(final Translator translator) {
        return Integer.toString(number);
    }

    @Override
    public String toString() {
        return "IntGameVariant:" + number;
    }
}

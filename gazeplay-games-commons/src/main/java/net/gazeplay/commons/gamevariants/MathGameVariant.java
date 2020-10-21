package net.gazeplay.commons.gamevariants;

import lombok.Data;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.ui.Translator;

@Data
public class MathGameVariant implements IGameVariant {

    private final int min;
    private final int max;

    @Override
    public String getLabel(Translator translator) {
        return translator.translate("From") + " " + getMin() + " " + translator.translate("to") + " " + getMax();
    }

    @Override
    public String toString() {
        return "MathGameVariant:" + getMin() + ":" + getMax();
    }

}

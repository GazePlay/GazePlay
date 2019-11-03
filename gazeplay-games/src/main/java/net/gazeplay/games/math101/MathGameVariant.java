package net.gazeplay.games.math101;

import lombok.Data;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.ui.Translator;

@Data
public class MathGameVariant implements GameSpec.GameVariant {

    private final VariableRange variableRange;

    @Override
    public String getLabel(Translator translator) {
        return variableRange.asText();
    }

}

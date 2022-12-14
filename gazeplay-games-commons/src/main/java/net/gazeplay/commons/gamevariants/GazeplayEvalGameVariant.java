package net.gazeplay.commons.gamevariants;

import lombok.Data;
import net.gazeplay.commons.ui.Translator;

@Data
public class GazeplayEvalGameVariant implements IGameVariant{

    private final String nameGame;

    @Override
    public String getLabel(Translator translator) {
        return nameGame;
    }

    @Override
    public String toString() {
        return nameGame;
    }
}

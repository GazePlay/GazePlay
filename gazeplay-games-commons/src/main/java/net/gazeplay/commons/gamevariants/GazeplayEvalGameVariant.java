package net.gazeplay.commons.gamevariants;

import lombok.Data;
import net.gazeplay.commons.ui.Translator;

/**
 * Variant of GazePlay Eval: the name of an imported evaluation (code kept but unreachable).
 */
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

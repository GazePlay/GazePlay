package net.gazeplay.commons.gamevariants;

import lombok.Data;
import net.gazeplay.commons.ui.Translator;

/**
 * Variant of GazePlay Eval: the name of an imported evaluation (its folder in the evaluations folder). Persistent form
 * "GazeplayEvalGameVariant:name" (the names of the website never contain ":").
 */
@Data
public class GazeplayEvalGameVariant implements IGameVariant {

    static final String TYPE = "GazeplayEvalGameVariant";

    private final String nameGame;

    @Override
    public String getLabel(Translator translator) {
        return nameGame;
    }

    @Override
    public String toString() {
        return TYPE + ":" + nameGame;
    }
}

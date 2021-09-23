package net.gazeplay.commons.gamevariants;

import lombok.Data;
import net.gazeplay.commons.ui.Translator;

@Data
public class DimensionDifficultyGameVariant implements IGameVariant {

    private final int width;
    private final int height;
    private final String difficulty;

    @Override
    public String getLabel(final Translator translator) {
        return width + "x" + height + " " + translator.translate(difficulty);
    }

    @Override
    public String toString() {
        return "DimensionDifficultyGameVariant:" + width + ":" + height + ":" + difficulty;
    }
}

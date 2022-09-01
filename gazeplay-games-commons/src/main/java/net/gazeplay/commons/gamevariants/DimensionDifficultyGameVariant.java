package net.gazeplay.commons.gamevariants;

import lombok.Data;
import net.gazeplay.commons.ui.Translator;

@Data
public class DimensionDifficultyGameVariant implements IGameVariant {
    private final int width;
    private final int height;
    private final String variant;

    @Override
    public String getLabel(final Translator translator) {
        return width + "x" + height + " " + translator.translate(variant);
    }

    @Override
    public String toString() {
        return "DimensionDifficultyGameVariant:" + width + ":" + height + ":" + variant;
    }
}

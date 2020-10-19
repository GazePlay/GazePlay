package net.gazeplay.commons.gamevariants;

import lombok.Data;
import net.gazeplay.commons.ui.Translator;

@Data
public class DimensionGameVariant implements IGameVariant {

    private final int width;
    private final int height;

    @Override
    public String getLabel(final Translator translator) {
        return width + "x" + height;
    }

    @Override
    public String toString() {
        return "DimensionGameVariant:" + width + ":" + height;
    }
}

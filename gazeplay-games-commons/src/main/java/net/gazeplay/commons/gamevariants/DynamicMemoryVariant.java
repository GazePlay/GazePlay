package net.gazeplay.commons.gamevariants;

import lombok.Data;
import net.gazeplay.commons.ui.Translator;

@Data
public class DynamicMemoryVariant implements IGameVariant {
    private final int version;

    @Override
    public String getLabel(final Translator translator) {
        return "Variant " + version;
    }

    @Override
    public String toString() {
        return "DynamicMemoryVariant: Variant " + version;
    }
}

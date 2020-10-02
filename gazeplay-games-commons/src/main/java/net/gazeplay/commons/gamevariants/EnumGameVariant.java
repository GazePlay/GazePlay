package net.gazeplay.commons.gamevariants;

import lombok.Data;
import net.gazeplay.commons.ui.Translator;

import java.util.function.Function;

@Data
public class EnumGameVariant<K extends Enum<K>> implements IGameVariant {

    private final K enumValue;
    private final Function<K, String> extractLabelCodeFunction;

    @Override
    public String getLabel(final Translator translator) {
        return translator.translate(extractLabelCodeFunction.apply(enumValue));
    }



}

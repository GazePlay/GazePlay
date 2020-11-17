package net.gazeplay.commons.gamevariants;

import lombok.Data;
import net.gazeplay.commons.ui.Translator;

import java.util.Objects;
import java.util.function.Function;

@Data
public class EnumGameVariant<K extends Enum<K>> implements IGameVariant {

    private final K enumValue;
    private final Function<K, String> extractLabelCodeFunction;

    public EnumGameVariant(K enumValue, Function<K, String> extractLabelCodeFunction) {
        this.enumValue = enumValue;
        this.extractLabelCodeFunction = extractLabelCodeFunction;
    }

    public EnumGameVariant(K enumValue) {
        this.enumValue = enumValue;
        this.extractLabelCodeFunction = Objects::toString;
    }

    @Override
    public String getLabel(final Translator translator) {
        return translator.translate(extractLabelCodeFunction.apply(enumValue));
    }

    @Override
    public String toString() {
        return "EnumGameVariant:" + enumValue.getClass().getName() + ":" + enumValue;
    }

}

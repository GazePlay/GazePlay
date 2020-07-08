package net.gazeplay.commons.gamevariants.generators;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.GameSpec;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

@Data
public class EnumGameVariantGenerator<K extends Enum<K>> implements IGameVariantGenerator {

    private final K[] enumValues;

    private final Function<K, String> extractLabelCodeFunction;

    @Getter
    @Setter
    private String variantChooseText = "Choose Game Variant";

    @Override
    public Set<GameSpec.GameVariant> getVariants() {
        final LinkedHashSet<GameSpec.GameVariant> result = new LinkedHashSet<>();
        for (final K value : enumValues) {
            result.add(new GameSpec.EnumGameVariant<>(value, extractLabelCodeFunction));
        }
        return result;
    }
}

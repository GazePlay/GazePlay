package net.gazeplay.commons.gamevariants.generators;

import lombok.Data;
import net.gazeplay.GameSpec;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class IntRangeVariantGenerator implements IGameVariantGenerator {

    private final String variantChooseText;

    private final int min;

    private final int max;

    @Override
    public Set<GameSpec.GameVariant> getVariants() {
        final LinkedHashSet<GameSpec.GameVariant> result = new LinkedHashSet<>();
        for (int i = min; i <= max; i++) {
            result.add(new GameSpec.IntGameVariant(i));
        }
        return result;
    }
}

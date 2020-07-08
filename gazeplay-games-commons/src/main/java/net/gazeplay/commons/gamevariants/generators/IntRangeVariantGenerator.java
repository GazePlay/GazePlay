package net.gazeplay.commons.gamevariants.generators;

import lombok.Data;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class IntRangeVariantGenerator implements IGameVariantGenerator {

    private final String variantChooseText;

    private final int min;

    private final int max;

    @Override
    public Set<IGameVariant> getVariants() {
        final LinkedHashSet<IGameVariant> result = new LinkedHashSet<>();
        for (int i = min; i <= max; i++) {
            result.add(new IntGameVariant(i));
        }
        return result;
    }
}

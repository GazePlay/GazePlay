package net.gazeplay.commons.gamevariants.generators;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;

import java.util.Set;

public class SquareDimensionVariantGenerator implements IGameVariantGenerator {

    private final int minSize;

    private final int maxSize;

    @Getter
    @Setter
    private String variantChooseText = "Choose size";

    public SquareDimensionVariantGenerator(final int minSize, final int maxSize) {
        super();
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> result = Sets.newLinkedHashSet();
        for (int i = minSize; i <= maxSize; i++) {
            result.add(new DimensionGameVariant(i, i));
        }
        return result;
    }
}

package net.gazeplay.commons.gamevariants;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.GameSpec;

import java.util.Set;

public class SquareDimensionVariantGenerator implements GameSpec.GameVariantGenerator {

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
    public Set<GameSpec.GameVariant> getVariants() {
        final Set<GameSpec.GameVariant> result = Sets.newLinkedHashSet();
        for (int i = minSize; i <= maxSize; i++) {
            result.add(new GameSpec.DimensionGameVariant(i, i));
        }
        return result;
    }
}

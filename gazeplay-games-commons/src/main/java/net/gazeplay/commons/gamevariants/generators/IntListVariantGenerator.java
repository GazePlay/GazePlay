package net.gazeplay.commons.gamevariants.generators;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;

import java.util.LinkedHashSet;
import java.util.Set;

@ToString
@EqualsAndHashCode
public class IntListVariantGenerator implements IGameVariantGenerator {

    @Getter
    private final String variantChooseText;

    @Getter
    private final int[] values;

    public IntListVariantGenerator(final String variantChooseText, final int... values) {
        this.variantChooseText = variantChooseText;
        this.values = values;
    }

    @Override
    public Set<IGameVariant> getVariants() {
        final LinkedHashSet<IGameVariant> result = new LinkedHashSet<>();
        for (final int i : values) {
            result.add(new IntGameVariant(i));
        }
        return result;
    }
}

package net.gazeplay.commons.gamevariants.generators;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;

import java.util.LinkedHashSet;
import java.util.Set;

@ToString
@EqualsAndHashCode
public class IntStringListVariantGenerator implements IGameVariantGenerator {

    @Getter
    private final String variantChooseText;

    @Getter
    private final int[] values;

    @Getter
    private final String[] type;

    public IntStringListVariantGenerator(final String variantChooseText, final int[] values, final String... type) {
        this.variantChooseText = variantChooseText;
        this.values = values;
        this.type = type;
    }

    @Override
    public Set<IGameVariant> getVariants() {
        final LinkedHashSet<IGameVariant> result = new LinkedHashSet<>();
        for (int i=0; i<values.length; i++) {
            result.add(new IntStringGameVariant(values[i], type[i]));
        }
        return result;
    }
}

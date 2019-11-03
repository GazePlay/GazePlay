package net.gazeplay.games.math101;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;

import java.util.Set;

public class Math101AdditionGameVariantGenerator implements GameSpec.GameVariantGenerator {

    @Override
    public Set<GameSpec.GameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new MathGameVariant(new VariableRange(0, 8)),
            new MathGameVariant(new VariableRange(0, 12)),
            new MathGameVariant(new VariableRange(0, 20))
        ));
    }

}

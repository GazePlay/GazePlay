package net.gazeplay.games.math101;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;

import java.util.Set;

public class Math101MultiplicationGameVariantGenerator implements GameSpec.GameVariantGenerator {
    @Override
    public Set<GameSpec.GameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new MathGameVariant(new VariableRange(0, 3)),
            new MathGameVariant(new VariableRange(0, 5)),
            new MathGameVariant(new VariableRange(0, 7)),
            new MathGameVariant(new VariableRange(0, 9)),
            new MathGameVariant(new VariableRange(0, 11)),
            new MathGameVariant(new VariableRange(0, 12))
        ));
    }
}

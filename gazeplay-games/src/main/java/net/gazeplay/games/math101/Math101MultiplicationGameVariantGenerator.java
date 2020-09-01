package net.gazeplay.games.math101;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

public class Math101MultiplicationGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
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

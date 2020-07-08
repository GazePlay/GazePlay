package net.gazeplay.games.math101;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

public class Math101AllOperationsGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<GameSpec.GameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new MathGameVariant(new VariableRange(0, 5)),
            new MathGameVariant(new VariableRange(0, 10)),
            new MathGameVariant(new VariableRange(0, 15)),
            new MathGameVariant(new VariableRange(0, 20))
        ));
    }
}

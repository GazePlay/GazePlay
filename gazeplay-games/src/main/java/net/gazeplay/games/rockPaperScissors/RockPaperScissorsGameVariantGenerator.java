package net.gazeplay.games.rockPaperScissors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Set;

public class RockPaperScissorsGameVariantGenerator implements IGameVariantGenerator {
    @Override
    public Set<IGameVariant> getVariants() {
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new IntStringGameVariant(3, "hide"),
            new IntStringGameVariant(5, "hide"),
            new IntStringGameVariant(7, "hide"),
            new IntStringGameVariant(9, "hide"),
            new IntStringGameVariant(3, "visible"),
            new IntStringGameVariant(5, "visible"),
            new IntStringGameVariant(7, "visible"),
            new IntStringGameVariant(9, "visible")
        ));
    }
}

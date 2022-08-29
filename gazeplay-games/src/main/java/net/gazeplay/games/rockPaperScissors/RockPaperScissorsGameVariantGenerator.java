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
            new IntStringGameVariant(3, "Hide"),
            new IntStringGameVariant(5, "Hide"),
            new IntStringGameVariant(7, "Hide"),
            new IntStringGameVariant(9, "Hide"),
            new IntStringGameVariant(3, "Visible"),
            new IntStringGameVariant(5, "Visible"),
            new IntStringGameVariant(7, "Visible"),
            new IntStringGameVariant(9, "Visible")
        ));
    }
}

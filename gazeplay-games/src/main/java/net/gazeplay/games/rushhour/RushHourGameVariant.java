package net.gazeplay.games.rushhour;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.HashSet;
import java.util.Set;

public class RushHourGameVariant implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {

        Set<IGameVariant> gameVariants = new HashSet<>();

        for (int i = 1; i <= 33; i++){
            gameVariants.add(new IntGameVariant(i));
        }
        return gameVariants;
        /*
        return Sets.newLinkedHashSet(Lists.newArrayList(
            new IntStringGameVariant(1, "Niveau1-5"),
            new IntStringGameVariant(2, "Niveau1-5"),
            new IntStringGameVariant(3, "Niveau1-5"),
            new IntStringGameVariant(4, "Niveau1-5"),
            new IntStringGameVariant(5, "Niveau1-5"),
            new IntStringGameVariant(6, "Niveau6-11"),
            new IntStringGameVariant(7, "Niveau6-11"),
            new IntStringGameVariant(8, "Niveau6-11"),
            new IntStringGameVariant(9, "Niveau6-11"),
            new IntStringGameVariant(10, "Niveau6-11"),
            new IntStringGameVariant(11, "Niveau6-11"),
            new IntStringGameVariant(12, "Niveau12-17"),
            new IntStringGameVariant(13, "Niveau12-17"),
            new IntStringGameVariant(14, "Niveau12-17"),
            new IntStringGameVariant(15, "Niveau12-17"),
            new IntStringGameVariant(16, "Niveau12-17"),
            new IntStringGameVariant(17, "Niveau12-17"),
            new IntStringGameVariant(18, "Niveau18-23"),
            new IntStringGameVariant(19, "Niveau18-23"),
            new IntStringGameVariant(20, "Niveau18-23"),
            new IntStringGameVariant(21, "Niveau18-23"),
            new IntStringGameVariant(22, "Niveau18-23"),
            new IntStringGameVariant(23, "Niveau18-23"),
            new IntStringGameVariant(24, "Niveau24-29"),
            new IntStringGameVariant(25, "Niveau24-29"),
            new IntStringGameVariant(26, "Niveau24-29"),
            new IntStringGameVariant(27, "Niveau24-29"),
            new IntStringGameVariant(28, "Niveau24-29"),
            new IntStringGameVariant(29, "Niveau24-29"),
            new IntStringGameVariant(30, "Niveau30-33"),
            new IntStringGameVariant(31, "Niveau30-33"),
            new IntStringGameVariant(32, "Niveau30-33"),
            new IntStringGameVariant(33, "Niveau30-33")

        ));*/
    }
}

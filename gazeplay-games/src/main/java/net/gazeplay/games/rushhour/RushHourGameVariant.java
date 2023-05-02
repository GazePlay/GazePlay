package net.gazeplay.games.rushhour;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.gamevariants.IntStringGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class RushHourGameVariant implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {

        Set<IGameVariant> gameVariants = new HashSet<>();

        for (int i = 1; i <= 33; i++) {
            gameVariants.add(new IntGameVariant(i));
        }

        return gameVariants.stream()
            .sorted(Comparator.comparingInt(v -> ((IntGameVariant) v).getNumber()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

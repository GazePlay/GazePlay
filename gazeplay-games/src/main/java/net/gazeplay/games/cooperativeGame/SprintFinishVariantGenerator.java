package net.gazeplay.games.cooperativeGame;

import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SprintFinishVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {

        Set<IGameVariant> gameVariants = new HashSet<>();

        for (int i = 1; i <= 17; i++) {
            gameVariants.add(new IntGameVariant(i));
        }

        return gameVariants.stream()
            .sorted(Comparator.comparingInt(v -> ((IntGameVariant) v).getNumber()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

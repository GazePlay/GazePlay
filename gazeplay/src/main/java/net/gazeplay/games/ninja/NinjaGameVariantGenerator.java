package net.gazeplay.games.ninja;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;

import java.util.Set;

public class NinjaGameVariantGenerator implements GameSpec.GameVariantGenerator {
	@Override
	public Set<GameSpec.GameVariant> getVariants() {
		return Sets.newLinkedHashSet(Lists.newArrayList(
				new GameSpec.IntGameVariant(1, "Random"),
				new GameSpec.IntGameVariant(2, "Vertical"),
				new GameSpec.IntGameVariant(3, "Horizontal"),
				new GameSpec.IntGameVariant(4, "Diagonal from upper left to lower right"),
				new GameSpec.IntGameVariant(5, "Diagonal from upper right to lower left")));
	}
}

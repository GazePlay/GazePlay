package net.gazeplay.gameslocator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;

import java.util.Set;

public class Math103GameVariantGenerator implements GameSpec.GameVariantGenerator {
	@Override
	public Set<GameSpec.GameVariant> getVariants() {
		return Sets.newLinkedHashSet(Lists.newArrayList(new GameSpec.IntGameVariant(0, "0 to 3"),
				new GameSpec.IntGameVariant(1, "0 to 5"), new GameSpec.IntGameVariant(2, "0 to 7"),
				new GameSpec.IntGameVariant(3, "0 to 9"), new GameSpec.IntGameVariant(4, "0 to 11"),
				new GameSpec.IntGameVariant(5, "0 to 12")));
	}
}

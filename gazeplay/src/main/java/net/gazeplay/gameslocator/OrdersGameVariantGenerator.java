package net.gazeplay.gameslocator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;

import java.util.Set;

public class OrdersGameVariantGenerator implements GameSpec.GameVariantGenerator {
	@Override
	public Set<GameSpec.GameVariant> getVariants() {
		return Sets.newLinkedHashSet(Lists.newArrayList(

				new GameSpec.TargetsGameVariant(3), new GameSpec.TargetsGameVariant(5),
				new GameSpec.TargetsGameVariant(7)));
	}
}

package net.gazeplay.games.dice;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;

import java.util.Set;

public class DiceGameVariantGenerator implements GameSpec.GameVariantGenerator {
	@Override
	public Set<GameSpec.GameVariant> getVariants() {
		return Sets.newLinkedHashSet(Lists.newArrayList(

				new GameSpec.IntGameVariant(1, "1 die"),

				new GameSpec.IntGameVariant(2, "2 dice"),

				new GameSpec.IntGameVariant(3, "3 dice"),

				new GameSpec.IntGameVariant(4, "4 dice"),

				new GameSpec.IntGameVariant(5, "5 dice"),

				new GameSpec.IntGameVariant(6, "6 dice")

		));
	}
}

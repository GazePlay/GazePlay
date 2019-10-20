package net.gazeplay.games.labyrinth;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.GameSpec;

import java.util.Set;

public class LabyrinthGameVariantGenerator implements GameSpec.GameVariantGenerator {
	@Override
	public Set<GameSpec.GameVariant> getVariants() {
		return Sets.newLinkedHashSet(Lists.newArrayList(

				new GameSpec.IntGameVariant(0, "Look at the destination box to move"),

				new GameSpec.IntGameVariant(2, "Look the movement arrows around the mouse to move"),

				new GameSpec.IntGameVariant(3, "Look the movement arrows around the labyrinth to move"),

				new GameSpec.IntGameVariant(4,
						"Select the mouse then look at the destination box to move")

		));
	}
}

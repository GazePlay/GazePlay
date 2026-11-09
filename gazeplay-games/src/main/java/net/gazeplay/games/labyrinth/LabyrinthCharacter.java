package net.gazeplay.games.labyrinth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Who goes where in the Labyrinth (maintainer's choice of 9/11/2026): the mouse to the cheese, or an animal drawn at
 * random to the house.
 */
@Getter
@RequiredArgsConstructor
public enum LabyrinthCharacter {
    MOUSE("LabyrinthMouse", "LabyrinthCheeses"),
    ANIMAL("LabyrinthAnimal", "LabyrinthHouses");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * Key of translation of the goals reached, shown at the top.
     */
    private final String goalsKey;

    /**
     * @return the character of a key, the mouse for an unknown one
     */
    public static LabyrinthCharacter of(final String key) {
        for (final LabyrinthCharacter character : values()) {
            if (character.key.equals(key)) {
                return character;
            }
        }
        return MOUSE;
    }
}


package net.gazeplay.games.labyrinth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The 16 variants of the Labyrinth before its review of 9/11/2026, kept so that the old variants (saved statistics,
 * shortcuts) are still read: each one is played with its way of moving and its character, at the normal level, without
 * end. "Anime" (the labyrinth dug before the eyes) is no longer a choice: every labyrinth is dug so.
 */
@Getter
@RequiredArgsConstructor
public enum LabyrinthGameVariant {
    LOOK_DESTINATION(LabyrinthMove.LOCAL_ARROWS, LabyrinthCharacter.MOUSE),
    LOOK_LOCAL_ARROWS(LabyrinthMove.LOCAL_ARROWS, LabyrinthCharacter.MOUSE),
    LOOK_GLOBAL_ARROWS(LabyrinthMove.GLOBAL_ARROWS, LabyrinthCharacter.MOUSE),
    SELECT_THEN_LOOK_DESTINATION(LabyrinthMove.SELECT_THEN_GUIDE, LabyrinthCharacter.MOUSE),
    ANLOOK_DESTINATION(LabyrinthMove.LOCAL_ARROWS, LabyrinthCharacter.MOUSE),
    ANLOOK_LOCAL_ARROWS(LabyrinthMove.LOCAL_ARROWS, LabyrinthCharacter.MOUSE),
    ANLOOK_GLOBAL_ARROWS(LabyrinthMove.GLOBAL_ARROWS, LabyrinthCharacter.MOUSE),
    ANSELECT_THEN_LOOK_DESTINATION(LabyrinthMove.SELECT_THEN_GUIDE, LabyrinthCharacter.MOUSE),

    OTHER_LOOK_DESTINATION(LabyrinthMove.LOCAL_ARROWS, LabyrinthCharacter.ANIMAL),
    OTHER_LOOK_LOCAL_ARROWS(LabyrinthMove.LOCAL_ARROWS, LabyrinthCharacter.ANIMAL),
    OTHER_LOOK_GLOBAL_ARROWS(LabyrinthMove.GLOBAL_ARROWS, LabyrinthCharacter.ANIMAL),
    OTHER_SELECT_THEN_LOOK_DESTINATION(LabyrinthMove.SELECT_THEN_GUIDE, LabyrinthCharacter.ANIMAL),
    OTHER_ANLOOK_DESTINATION(LabyrinthMove.LOCAL_ARROWS, LabyrinthCharacter.ANIMAL),
    OTHER_ANLOOK_LOCAL_ARROWS(LabyrinthMove.LOCAL_ARROWS, LabyrinthCharacter.ANIMAL),
    OTHER_ANLOOK_GLOBAL_ARROWS(LabyrinthMove.GLOBAL_ARROWS, LabyrinthCharacter.ANIMAL),
    OTHER_ANSELECT_THEN_LOOK_DESTINATION(LabyrinthMove.SELECT_THEN_GUIDE, LabyrinthCharacter.ANIMAL);

    private final LabyrinthMove move;
    private final LabyrinthCharacter character;
}

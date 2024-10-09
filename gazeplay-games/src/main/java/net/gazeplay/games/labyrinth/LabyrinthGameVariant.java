
package net.gazeplay.games.labyrinth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LabyrinthGameVariant {
    LOOK_LOCAL_ARROWS("Look the movement arrows around the mouse to move");

    @Getter
    private final String label;
}

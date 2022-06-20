package net.gazeplay.games.labyrinth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LabyrinthGameVariant {
    LOOK_DESTINATION("Look at the destination box to move"),
    LOOK_LOCAL_ARROWS("Look the movement arrows around the mouse to move"),
    LOOK_GLOBAL_ARROWS("Look the movement arrows around the labyrinth to move"),
    SELECT_THEN_LOOK_DESTINATION("Select the mouse then look at the destination box to move"),
    ANLOOK_DESTINATION("AnimeLook at the destination box to move"),
    ANLOOK_LOCAL_ARROWS("AnimeLook the movement arrows around the mouse to move"),
    ANLOOK_GLOBAL_ARROWS("AnimeLook the movement arrows around the labyrinth to move"),
    ANSELECT_THEN_LOOK_DESTINATION("AnimeSelect the mouse then look at the destination box to move"),

    OTHER_LOOK_DESTINATION("Look at the house box to move"),
    OTHER_LOOK_LOCAL_ARROWS("Look the movement arrows around the character to move"),
    OTHER_LOOK_GLOBAL_ARROWS("Look the movement arrows around the labyrinth to move the character"),
    OTHER_SELECT_THEN_LOOK_DESTINATION("Select the character then look at the house box to move"),
    OTHER_ANLOOK_DESTINATION("AnimeLook at the house box to move"),
    OTHER_ANLOOK_LOCAL_ARROWS("AnimeLook the movement arrows around the character to move"),
    OTHER_ANLOOK_GLOBAL_ARROWS("AnimeLook the movement arrows around the labyrinth to move the character"),
    OTHER_ANSELECT_THEN_LOOK_DESTINATION("AnimeSelect the character then look at the house box to move");

    @Getter
    private final String label;
}


package net.gazeplay.games.pianosight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PianoGameVariant {
    FREEPLAY("Freeplay"),
    NORMAL("PlayWithInstructions");
    @Getter
    private final String label;
}

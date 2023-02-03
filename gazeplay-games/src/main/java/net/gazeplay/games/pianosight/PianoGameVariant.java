package net.gazeplay.games.pianosight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PianoGameVariant {
    FREEPLAY("Freeplay"),
    NORMAL("Play with instructions");
    @Getter
    private final String label;
}

package net.gazeplay.games.space;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SpaceGameVariant {
    EASY("Easy"),
    NORMAL("Normal"),
    HARD("Hard");
    @Getter
    private final String label;
}

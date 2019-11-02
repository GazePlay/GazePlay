package net.gazeplay.games.cakes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CakeGameVariant {
    FREE("free"),
    NORMAL("normal"),
    EXTREM("extreme");

    @Getter
    private final String label;
}

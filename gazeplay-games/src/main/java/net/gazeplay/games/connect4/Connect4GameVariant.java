package net.gazeplay.games.connect4;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Connect4GameVariant {
    PLAYERSTART("PlayerStart"),
    AISTART("AiStart");
    @Getter
    private final String label;
}

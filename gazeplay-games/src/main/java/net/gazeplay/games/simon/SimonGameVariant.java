package net.gazeplay.games.simon;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SimonGameVariant {

    EASY("Easy"),
    NORMAL("Normal"),
    HARD("Hard");

    @Getter
    private final String label;
}

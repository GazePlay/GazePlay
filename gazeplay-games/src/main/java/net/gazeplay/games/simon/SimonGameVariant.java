package net.gazeplay.games.simon;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SimonGameVariant {

    EASY_CLASSIC("Easy"),
    NORMAL_CLASSIC("Normal"),
    HARD_CLASSIC("Hard"),
    MODE2("Simon copy you"),
    EASY_MULTIPLAYER("Easy multiplayer"),
    NORMAL_MULTIPLAYER("Normal multiplayer"),
    HARD_MULTIPLAYER("Hard multiplayer");

    @Getter
    private final String label;
}

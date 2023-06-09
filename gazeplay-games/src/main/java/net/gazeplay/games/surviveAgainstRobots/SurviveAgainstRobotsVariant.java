package net.gazeplay.games.surviveAgainstRobots;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SurviveAgainstRobotsVariant {
    DIFFICULTY_EASY_KEYBOARD("Easy_KEYBOARD"),
    DIFFICULTY_NORMAL_KEYBOARD("Normal_KEYBOARD"),
    DIFFICULTY_HARD_KEYBOARD("Hard_KEYBOARD"),

    DIFFICULTY_EASY_AUTO_KEYBOARD("Easy_AUTO_KEYBOARD"),
    DIFFICULTY_NORMAL_AUTO_KEYBOARD("Normal_AUTO_KEYBOARD"),
    DIFFICULTY_HARD_AUTO_KEYBOARD("Hard_AUTO_KEYBOARD");
    @Getter
    private final String label;
}

package net.gazeplay.games.surviveAgainstRobots;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor

public enum SurviveAgainstRobotsMouseVariant {

    DIFFICULTY_EASY_MOUSE("Easy_MOUSE"),
    DIFFICULTY_NORMAL_MOUSE("Normal_MOUSE"),
    DIFFICULTY_HARD_MOUSE("Hard_MOUSE"),
    DIFFICULTY_EASY_AUTO_MOUSE("Easy_AUTO_MOUSE"),
    DIFFICULTY_NORMAL_AUTO_MOUSE("Normal_AUTO_MOUSE"),
    DIFFICULTY_HARD_AUTO_MOUSE("Hard_AUTO_MOUSE");

    @Getter
    private final String label;
}

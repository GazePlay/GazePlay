package net.gazeplay.games.surviveAgainstRobots;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SurviveAgainstRobotsVariant {
    DIFFICULTY_EASY("Easy"),
    DIFFICULTY_NORMAL("Normal"),
    DIFFICULTY_HARD("Hard");

    @Getter
    private final String label;
}

package net.gazeplay.games.surviveAgainstRobots;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SurviveAgainstRobotsVariant {
    KEYBOARD("Keyboard"),
    MOUSE("Mouse");

    @Getter
    private final String label;
}

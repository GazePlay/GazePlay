package net.gazeplay.games.surviveAgainstRobots;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How the Biboule of "Survive Against Robots" shoots (maintainer's rules of 3/11/2026): at the nearest robot, or
 * forwards, the way it goes. It goes where the gaze or the mouse is, as the setting of the inputs allows (no choice
 * in the variants: a user with the mouse only could otherwise choose the gaze and be stuck).
 */
@Getter
@RequiredArgsConstructor
public enum SurviveShot {
    AUTOMATIC("SurviveShotAuto"),
    FORWARD("SurviveShotForward");

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * @return the way of shooting of a key, automatic for an unknown one
     */
    public static SurviveShot of(final String key) {
        for (final SurviveShot shot : values()) {
            if (shot.key.equals(key)) {
                return shot;
            }
        }
        return AUTOMATIC;
    }
}

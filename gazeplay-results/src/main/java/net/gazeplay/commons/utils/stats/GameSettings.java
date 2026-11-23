package net.gazeplay.commons.utils.stats;

/**
 * The settings a game was played with, at its start ("settings" of replayData.json, format version 6, 23/11/2026:
 * maintainer's request with the tester's feedback, point 22, the files used to hold none of them). A setting changed
 * during the game in the menu of the game screen (Esc) is not followed here; the delays before a selection actually run
 * are in "selectionDelays".
 *
 * @param fixationLength      length of a fixation, in ms (FIXATION_LENGTH)
 * @param selectionDelay      delay before a selection is possible when new choices appear, in ms (GAME_START_DELAY)
 * @param inputMode           the input of the games (EYE_TRACKER: "mouse_control", "tobii" or "tobii_and_mouse")
 * @param movingFixationKept  whether a fixation on a moving target keeps its progress when the gaze leaves it
 *                            (MOVING_FIXATION_KEPT)
 * @param animationSpeedRatio speed of the elements of the games (ANIMATION_SPEED_RATIO, 1 by default)
 */
public record GameSettings(
    int fixationLength,
    int selectionDelay,
    String inputMode,
    boolean movingFixationKept,
    double animationSpeedRatio
) {
}

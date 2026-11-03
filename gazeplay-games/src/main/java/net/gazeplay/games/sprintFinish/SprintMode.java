package net.gazeplay.games.sprintFinish;

import lombok.Getter;

/**
 * How "Sprint Finish" is played (maintainer's rules of 2/11/2026), chosen at the bottom of the window of the variants.
 * Alone, the cat follows the gaze or the mouse, as the setting of the inputs allows (3/11/2026, maintainer's rule: no
 * choice of the input in the variants; the mode "alone with the keyboard" was removed, a saved one is played alone).
 * Alone, the dogs are much slower, so that the game stays fun without losing too often, and a button opens its fences
 * when the cat walks on it. With two players, one moves the cat with the keyboard and the other helps with the gaze:
 * a dog or a log looked at stops, a button looked at (a fixation) opens its fences.
 */
@Getter
public enum SprintMode {

    SOLO_GAZE("SprintSoloGaze", false, true, 0.7, 1.0),
    DUO("SprintDuo", true, false, 1.6, 1.7);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * Whether the cat moves with the keyboard (arrows, Z Q S D or W A S D).
     */
    private final boolean keyboardCat;

    /**
     * Whether the cat follows the gaze (or the mouse).
     */
    private final boolean gazeCat;

    /**
     * Speed of the dogs, in cells per second.
     */
    private final double dogSpeed;

    /**
     * Speed of the logs, in cells per second.
     */
    private final double logSpeed;

    SprintMode(final String key, final boolean keyboardCat, final boolean gazeCat, final double dogSpeed,
               final double logSpeed) {
        this.key = key;
        this.keyboardCat = keyboardCat;
        this.gazeCat = gazeCat;
        this.dogSpeed = dogSpeed;
        this.logSpeed = logSpeed;
    }

    /**
     * Whether the gaze of a second player stops the dogs and the logs and opens the fences (two players); alone, the
     * cat opens them by walking on the button.
     */
    public boolean isHelperGaze() {
        return this == DUO;
    }

    /**
     * @return the mode of a key, alone with the gaze for an unknown one
     */
    public static SprintMode of(final String key) {
        for (final SprintMode mode : values()) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return SOLO_GAZE;
    }
}

package net.gazeplay.commons.gaze;

/**
 * Values of the EYE_TRACKER setting: which inputs play the games (decision of the maintainer, October 2026).
 * <ul>
 * <li>{@link #mouse_control}: the mouse only, no eye tracker;</li>
 * <li>{@link #tobii}: the gaze of the Tobii eye tracker only. The mouse no longer acts in the games (it used to fill
 * the progress indicators too); it still works in the menus and the configuration panel of the game screen;</li>
 * <li>{@link #tobii_and_mouse}: both, as "tobii" did before October 2026.</li>
 * </ul>
 * EyeTribe (no longer made) was removed in October 2026, when only the Tobii Eye Tracker 5 was in use: a setting still
 * saying "eyetribe" is read as the mouse (see Configuration).
 */
public enum EyeTracker {
    mouse_control, tobii, tobii_and_mouse;

    /**
     * @return whether the games receive the gaze of an eye tracker
     */
    public boolean usesGaze() {
        return this != mouse_control;
    }

    /**
     * @return whether the mouse acts in the games
     */
    public boolean acceptsMouse() {
        return this != tobii;
    }

    /**
     * @return the eye tracker of a value of the setting; the mouse for an unknown value, e.g. of a removed eye tracker
     */
    public static EyeTracker fromConfigValue(final String value) {
        for (final EyeTracker eyeTracker : values()) {
            if (eyeTracker.name().equals(value)) {
                return eyeTracker;
            }
        }
        return mouse_control;
    }
}

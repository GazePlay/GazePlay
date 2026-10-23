package net.gazeplay.commons.gaze;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EyeTrackerTest {

    /**
     * Which inputs play the games: the mouse only, the gaze only, or both (decision of the maintainer, October 2026:
     * with "tobii", the mouse used to fill the progress indicators too).
     */
    @Test
    void shouldTellWhichInputsPlayTheGames() {
        assertTrue(EyeTracker.mouse_control.acceptsMouse());
        assertFalse(EyeTracker.mouse_control.usesGaze());

        assertFalse(EyeTracker.tobii.acceptsMouse());
        assertTrue(EyeTracker.tobii.usesGaze());

        assertTrue(EyeTracker.tobii_and_mouse.acceptsMouse());
        assertTrue(EyeTracker.tobii_and_mouse.usesGaze());
    }

    @Test
    void shouldReadAnUnknownSettingAsTheMouse() {
        assertEquals(EyeTracker.tobii, EyeTracker.fromConfigValue("tobii"));
        assertEquals(EyeTracker.tobii_and_mouse, EyeTracker.fromConfigValue("tobii_and_mouse"));
        assertEquals(EyeTracker.mouse_control, EyeTracker.fromConfigValue("eyetribe"));
        assertEquals(EyeTracker.mouse_control, EyeTracker.fromConfigValue(null));
    }
}

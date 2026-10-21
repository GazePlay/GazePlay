package net.gazeplay.ui.scenes.stats;

import javafx.scene.Scene;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Records the screen for a moment as during a game with video recording enabled, then converts the video as the
 * screen of the areas of interest does. Needs a display (AWT screen capture), like the other video tests.
 */
@ExtendWith(ApplicationExtension.class)
class VideoConverterTest {

    @Test
    void shouldConvertTheRecordedVideoIntoMp4() throws Exception {
        Scene scene = mock(Scene.class);
        when(scene.getWidth()).thenReturn(800d);
        when(scene.getHeight()).thenReturn(600d);
        // user.home is a temporary folder during tests (see build.gradle)
        Stats stats = new Stats(scene, "videoGame");

        stats.startVideoRecording();
        Thread.sleep(1500);
        stats.endVideoRecording();

        assertTrue(stats.awaitVideoRecordingEnd(30, TimeUnit.SECONDS), "The recording did not end");
        File avi = stats.getVideoFile().orElseThrow();
        // Regression test: the name used to be rebuilt with another time format and never matched the real file
        assertTrue(avi.isFile() && avi.length() > 0, "No video file: " + avi);
        assertTrue(avi.getName().matches("ScreenRecording \\d{4}-\\d{2}-\\d{2} at \\d{2}\\.\\d{2}\\.\\d{2}\\.avi"), avi.getName());

        File mp4 = VideoConverter.toMp4(avi);

        assertTrue(mp4.isFile() && mp4.length() > 0, "No MP4 file: " + mp4);
        assertTrue(mp4.getName().endsWith(".mp4"));
        assertTrue(mp4.getParentFile().equals(avi.getParentFile()));
    }
}

package net.gazeplay.commons.soundsmanager;

import net.gazeplay.TestingUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.awaitility.Awaitility.await;

public class SoundManagerTest {

    @Test
    void shouldCreateAndLaunchExecutorServiceAndSoundPlayerOnInit() throws InterruptedException {
        SoundsManager soundsManager = new SoundsManager();
        soundsManager.init();
        Assertions.assertNotNull(soundsManager.getExecutorService());
        Assertions.assertNotNull(soundsManager.getSoundPlayerRunnable());
        Assertions.assertFalse(soundsManager.getExecutorService().isShutdown());
        Assertions.assertFalse(soundsManager.getExecutorService().isTerminated());
        soundsManager.destroy();
    }

    @Test
    void shouldStopExecutorServiceOnDestroy() throws InterruptedException {
        SoundsManager soundsManager = new SoundsManager();
        soundsManager.init();
        soundsManager.destroy();
        Assertions.assertTrue(soundsManager.getExecutorService().isShutdown());
    }

    @Test
    void shouldAddMP3FileNameToTheList() {
        SoundsManager soundsManager = new SoundsManager();
        soundsManager.add("music/hand_sound1.mp3");
        Assertions.assertTrue(soundsManager.getListOfMusicToPlay().contains("music/hand_sound1.mp3"));
    }

    @Test
    void shouldAddWAVFileNameToTheList() {
        SoundsManager soundsManager = new SoundsManager();
        soundsManager.add("music/mvmt0.wav");
        Assertions.assertTrue(soundsManager.getListOfMusicToPlay().contains("music/mvmt0.wav"));
    }

    @Test
    void shouldAddSeveralFileNameToTheList() {
        SoundsManager soundsManager = new SoundsManager();
        soundsManager.add("music/mvmt0.wav");
        soundsManager.add("music/hand_sound1.mp3");
        soundsManager.add("music/song.mp3");
        Assertions.assertTrue(soundsManager.getListOfMusicToPlay().contains("music/mvmt0.wav"));
        Assertions.assertTrue(soundsManager.getListOfMusicToPlay().contains("music/hand_sound1.mp3"));
        Assertions.assertTrue(soundsManager.getListOfMusicToPlay().contains("music/song.mp3"));
    }

    @Test
    void shouldRemoveMP3FileFromListWhenPlayed() throws InterruptedException {
        SoundsManager soundsManager = new SoundsManager();
        soundsManager.add("music/hand_sound1.mp3");
        int initialSize = soundsManager.getListOfMusicToPlay().size();
        soundsManager.init();
        TestingUtils.waitForRunLater();
        await().until(() ->
            soundsManager.getListOfMusicToPlay().size() < initialSize);
        Assertions.assertFalse(soundsManager.getListOfMusicToPlay().contains("music/hand_sound1.mp3"));
        TestingUtils.waitForRunLater();
        soundsManager.destroy();
    }

    @Test
    void shouldRemoveWAVFileFromListWhenPlayed() throws InterruptedException {
        SoundsManager soundsManager = new SoundsManager();
        soundsManager.add("music/mvmt0.wav");
        int initialSize = soundsManager.getListOfMusicToPlay().size();
        soundsManager.init();
        TestingUtils.waitForRunLater();
        await().until(() ->
            soundsManager.getListOfMusicToPlay().size() < initialSize);
        Assertions.assertFalse(soundsManager.getListOfMusicToPlay().contains("music/mvmt0.wav"));
        TestingUtils.waitForRunLater();
        soundsManager.destroy();
    }

    @Test
    void shouldRemoveFilesFromListWhenPlayed() throws InterruptedException {
        SoundsManager soundsManager = new SoundsManager();
        soundsManager.add("music/mvmt0.wav");
        soundsManager.add("music/hand_sound1.mp3");
        int initialSize = soundsManager.getListOfMusicToPlay().size();
        soundsManager.init();
        TestingUtils.waitForRunLater();
        await().until(() ->
            soundsManager.getListOfMusicToPlay().size() <= initialSize - 1);
        Assertions.assertFalse(soundsManager.getListOfMusicToPlay().contains("music/mvmt0.wav"));
        TestingUtils.waitForRunLater();
        await().until(() ->
            soundsManager.getListOfMusicToPlay().size() <= initialSize - 2);
        Assertions.assertFalse(soundsManager.getListOfMusicToPlay().contains("music/hand_sound1.mp3"));
        TestingUtils.waitForRunLater();
        soundsManager.destroy();
    }

}

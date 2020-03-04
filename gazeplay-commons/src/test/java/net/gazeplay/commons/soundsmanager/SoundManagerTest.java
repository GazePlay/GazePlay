package net.gazeplay.commons.soundsmanager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.awaitility.Awaitility.await;

public class SoundManagerTest {

    @Mock
    private SoundsManager soundsManager;


    @BeforeEach
    void setup() {
        soundsManager = new SoundsManager();
    }

    @Test
    void shouldCreateAndLaunchExecutorServiceAndSoundPlayerOnInit() {
        soundsManager.init();
        Assertions.assertNotNull(soundsManager.getExecutorService());
        Assertions.assertNotNull(soundsManager.getSoundPlayerRunnable());
        Assertions.assertFalse(soundsManager.getExecutorService().isShutdown());
        Assertions.assertFalse(soundsManager.getExecutorService().isTerminated());
    }

    @Test
    void shouldStopExecutorServiceOnDestroy() {
        soundsManager.init();
        soundsManager.destroy();
        Assertions.assertTrue(soundsManager.getExecutorService().isShutdown());
        Assertions.assertTrue(soundsManager.getExecutorService().isTerminated());
    }

    @Test
    void shouldAddMP3FileNameToTheList() {
        soundsManager.add("music/hand_sound1.mp3");
        Assertions.assertTrue(soundsManager.getListOfMusicToPlay().contains("music/hand_sound1.mp3"));
    }

    @Test
    void shouldAddWAVFileNameToTheList() {
        soundsManager.add("music/mvmt0.wav");
        Assertions.assertTrue(soundsManager.getListOfMusicToPlay().contains("music/mvmt0.wav"));
    }

    @Test
    void shouldAddSeveralFileNameToTheList() {
        soundsManager.add("music/mvmt0.wav");
        soundsManager.add("music/hand_sound1.mp3");
        soundsManager.add("music/song.mp3");
        Assertions.assertTrue(soundsManager.getListOfMusicToPlay().contains("music/mvmt0.wav"));
        Assertions.assertTrue(soundsManager.getListOfMusicToPlay().contains("music/hand_sound1.mp3"));
        Assertions.assertTrue(soundsManager.getListOfMusicToPlay().contains("music/song.mp3"));
    }

    @Test
    void shouldRemoveMP3FileFromListWhenPlayed() {
        soundsManager.add("music/hand_sound1.mp3");
        int initialSize = soundsManager.getListOfMusicToPlay().size();
        soundsManager.init();
        await().until(() ->
            soundsManager.getListOfMusicToPlay().size() < initialSize);
        Assertions.assertFalse(soundsManager.getListOfMusicToPlay().contains("music/hand_sound1.mp3"));
    }

    @Test
    void shouldRemoveWAVFileFromListWhenPlayed() {
        soundsManager.add("music/mvmt0.wav");
        int initialSize = soundsManager.getListOfMusicToPlay().size();
        soundsManager.init();
        await().until(() ->
            soundsManager.getListOfMusicToPlay().size() < initialSize);
        Assertions.assertFalse(soundsManager.getListOfMusicToPlay().contains("music/mvmt0.wav"));
    }

    @Test
    void shouldRemoveFilesFromListWhenPlayed() {
        soundsManager.add("music/mvmt0.wav");
        soundsManager.add("music/hand_sound1.mp3");
        int initialSize = soundsManager.getListOfMusicToPlay().size();
        soundsManager.init();

        await().until(() ->
            soundsManager.getListOfMusicToPlay().size() <= initialSize - 1);
        Assertions.assertFalse(soundsManager.getListOfMusicToPlay().contains("music/mvmt0.wav"));

        await().until(() ->
            soundsManager.getListOfMusicToPlay().size() <= initialSize - 2);
        Assertions.assertFalse(soundsManager.getListOfMusicToPlay().contains("music/hand_sound1.mp3"));
    }

}

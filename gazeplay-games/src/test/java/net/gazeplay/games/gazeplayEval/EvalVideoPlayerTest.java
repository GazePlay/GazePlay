package net.gazeplay.games.gazeplayEval;

import javafx.application.Platform;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The player of the videos of the evaluations, on a real short video with a silent AAC sound, written here by FFmpeg: the
 * kind of file JavaFX Media could not read under Windows 11 (decision of the maintainer of 5/11/2026).
 */
@ExtendWith(ApplicationExtension.class)
class EvalVideoPlayerTest {

    private static final int WIDTH = 320;
    private static final int HEIGHT = 180;
    private static final int FRAMES_PER_SECOND = 25;
    private static final int SAMPLE_RATE = 44_100;

    @TempDir
    Path folder;

    private File video;
    private final AtomicInteger ends = new AtomicInteger();
    private final AtomicReference<String> error = new AtomicReference<>();
    private EvalVideoPlayer player;

    @BeforeEach
    void writeAVideo() throws Exception {
        video = folder.resolve("video.mp4").toFile();
        writeVideo(video, WIDTH, HEIGHT);
    }

    /**
     * Writes half a second of grey video, with a silent AAC sound.
     */
    static void writeVideo(File file, int width, int height) throws Exception {
        try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(file, width, height, 2)) {
            recorder.setFormat("mp4");
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            recorder.setFrameRate(FRAMES_PER_SECOND);
            recorder.setSampleRate(SAMPLE_RATE);
            recorder.start();
            Frame image = new Frame(width, height, Frame.DEPTH_UBYTE, 3);
            ByteBuffer pixels = (ByteBuffer) image.image[0];
            for (int position = 0; position < pixels.capacity(); position++) {
                pixels.put(position, (byte) 200);
            }
            for (int index = 0; index < FRAMES_PER_SECOND / 2; index++) {
                recorder.record(image);
                recorder.recordSamples(SAMPLE_RATE, 2, ShortBuffer.allocate(2 * SAMPLE_RATE / FRAMES_PER_SECOND));
            }
        }
    }

    @AfterEach
    void stopThePlayer() {
        if (player != null) {
            WaitForAsyncUtils.waitForAsyncFx(5000, player::dispose);
        }
    }

    private EvalVideoPlayer playerOf(File file, double maxWidth, double maxHeight) {
        player = new EvalVideoPlayer(file, maxWidth, maxHeight, ends::incrementAndGet, error::set);
        return player;
    }

    @Test
    void shouldShowTheImagesThenTellTheEnd() throws TimeoutException {
        playerOf(video, 1000, 1000);

        Platform.runLater(player::start);

        WaitForAsyncUtils.waitFor(15, TimeUnit.SECONDS, () -> ends.get() == 1);
        WaitForAsyncUtils.waitForFxEvents();
        assertNull(error.get());
        assertNotNull(player.getView().getImage());
        assertEquals(WIDTH, player.getView().getImage().getWidth());
        assertEquals(HEIGHT, player.getView().getImage().getHeight());
        // The grey of the video, opaque
        var color = player.getView().getImage().getPixelReader().getColor(WIDTH / 2, HEIGHT / 2);
        assertEquals(1, color.getOpacity());
        assertTrue(color.getRed() > 0.5, color.toString());
    }

    @Test
    void shouldReduceTheImagesToTheSizeShown() throws TimeoutException {
        playerOf(video, 160, 500);

        Platform.runLater(player::start);

        WaitForAsyncUtils.waitFor(15, TimeUnit.SECONDS, () -> ends.get() == 1);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(160, player.getView().getImage().getWidth());
        assertEquals(90, player.getView().getImage().getHeight());
    }

    /**
     * The menu of the game pauses the video: nothing goes on behind it.
     */
    @Test
    void shouldWaitDuringAPause() throws Exception {
        playerOf(video, 1000, 1000);

        Platform.runLater(() -> {
            player.pause();
            player.start();
        });

        Thread.sleep(1500);
        assertEquals(0, ends.get());

        Platform.runLater(player::resume);
        WaitForAsyncUtils.waitFor(15, TimeUnit.SECONDS, () -> ends.get() == 1);
    }

    /**
     * The P key of the examiner, also once the video has ended.
     */
    @Test
    void shouldPlayAgainFromTheStart() throws TimeoutException {
        playerOf(video, 1000, 1000);

        Platform.runLater(player::start);
        WaitForAsyncUtils.waitFor(15, TimeUnit.SECONDS, () -> ends.get() == 1);

        Platform.runLater(player::restart);
        WaitForAsyncUtils.waitFor(15, TimeUnit.SECONDS, () -> ends.get() == 2);
        assertNull(error.get());
    }

    @Test
    void shouldTellThatAFileCannotBeRead() throws IOException, TimeoutException {
        Path unreadable = folder.resolve("illisible.mp4");
        Files.writeString(unreadable, "not a video");
        playerOf(unreadable.toFile(), 1000, 1000);

        Platform.runLater(player::start);

        WaitForAsyncUtils.waitFor(15, TimeUnit.SECONDS, () -> error.get() != null);
        assertEquals(0, ends.get());
    }

    @Test
    void shouldCallNothingAfterItsDisposal() throws Exception {
        playerOf(video, 1000, 1000);

        Platform.runLater(() -> {
            player.start();
            player.dispose();
        });

        Thread.sleep(1500);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(0, ends.get());
        assertNull(error.get());
    }
}

package net.gazeplay.games.gazeplayEval;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Plays a video of an evaluation with FFmpeg (JavaCV), image and sound, into an {@link ImageView}. JavaFX Media was left
 * on 5/11/2026 for the videos (GazePlayEval.md section 21): under Windows 11 it fails at random with ERROR_MEDIA_INVALID
 * and could not decode any AAC sound, while the same files were always decoded by FFmpeg.
 * <p>
 * A thread reads the file in its order. The sound goes to a line, which plays it by itself; each image waits for its time
 * on a clock that stops during a pause, and is skipped when it comes too late. The methods are to be called from the
 * JavaFX thread, and the callbacks are called on it, never after {@link #dispose()}.
 */
@Slf4j
final class EvalVideoPlayer {

    /** An image later than this is not shown: the next one is on its way. */
    private static final long TOO_LATE_MICROS = 100_000;

    /** Room for the sound read ahead of the images, as they are not side by side in a file. */
    private static final int SOUND_BUFFER_SECONDS = 2;

    private static final long WAIT_STEP_MILLIS = 2;

    private static final int STEREO = 2;

    private static final String NATIVE_CACHE_PROPERTY = "org.bytedeco.javacpp.cachedir";

    private static CompletableFuture<Void> loaded;

    private final File file;
    private final double maxWidth;
    private final double maxHeight;
    private final Runnable onEnd;
    private final Consumer<String> onError;

    private final ImageView view = new ImageView();

    private final Object clockLock = new Object();
    private long clockStartNanos;
    private long pausedSinceNanos;
    private boolean paused;
    private boolean restartRequested;
    private volatile boolean stopped;
    private volatile SourceDataLine line;

    /** The last image read, waiting for the JavaFX thread. */
    private final Object imageLock = new Object();
    private ByteBuffer pendingImage;
    private int pendingWidth;
    private int pendingHeight;
    private int pendingStride;
    private final AtomicBoolean renderScheduled = new AtomicBoolean();
    private WritableImage image;

    /**
     * @param onEnd   called at the end of the video, once at each play
     * @param onError called when the video cannot be played, with the reason
     */
    EvalVideoPlayer(File file, double maxWidth, double maxHeight, Runnable onEnd, Consumer<String> onError) {
        this.file = file;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.onEnd = onEnd;
        this.onError = onError;
    }

    /**
     * Loads FFmpeg in the background, once: it takes up to some seconds (more when it is unpacked), which must not delay
     * a video on its screen, whose time is already running.
     *
     * @return completed when FFmpeg is loaded, or could not be: the video then says so
     */
    static synchronized CompletableFuture<Void> preload() {
        if (loaded == null) {
            loaded = new CompletableFuture<>();
            Thread thread = new Thread(() -> {
                try {
                    keepTheNativeFilesInTheGazePlayFolder();
                    Loader.load(avcodec.class);
                } catch (LinkageError | RuntimeException e) {
                    log.warn("Cannot load FFmpeg", e);
                } finally {
                    loaded.complete(null);
                }
            }, "EvalVideo-preload");
            thread.setDaemon(true);
            thread.start();
        }
        return loaded;
    }

    private static void keepTheNativeFilesInTheGazePlayFolder() {
        if (System.getProperty(NATIVE_CACHE_PROPERTY) == null) {
            System.setProperty(NATIVE_CACHE_PROPERTY,
                new File(GazePlayDirectories.getGazePlayFolder(), "native-cache").getAbsolutePath());
        }
    }

    ImageView getView() {
        return view;
    }

    void start() {
        Thread thread = new Thread(this::run, "EvalVideo-" + file.getName());
        thread.setDaemon(true);
        thread.start();
    }

    void pause() {
        synchronized (clockLock) {
            if (!paused) {
                paused = true;
                pausedSinceNanos = System.nanoTime();
            }
        }
        SourceDataLine current = line;
        if (current != null) {
            current.stop();
        }
    }

    void resume() {
        synchronized (clockLock) {
            if (paused) {
                paused = false;
                clockStartNanos += System.nanoTime() - pausedSinceNanos;
                clockLock.notifyAll();
            }
        }
        SourceDataLine current = line;
        if (current != null) {
            current.start();
        }
    }

    /**
     * Plays the video again from its start.
     */
    void restart() {
        synchronized (clockLock) {
            restartRequested = true;
            clockLock.notifyAll();
        }
        SourceDataLine current = line;
        if (current != null) {
            current.flush();
        }
    }

    void dispose() {
        stopped = true;
        synchronized (clockLock) {
            clockLock.notifyAll();
        }
        SourceDataLine current = line;
        if (current != null) {
            // Frees the thread if it is blocked writing the sound; the thread closes the line
            current.stop();
            current.flush();
        }
    }

    private void run() {
        keepTheNativeFilesInTheGazePlayFolder();
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
            // FFmpeg writes on the console, out of the log of GazePlay: its errors only
            avutil.av_log_set_level(avutil.AV_LOG_ERROR);
            grabber.setPixelFormat(avutil.AV_PIX_FMT_BGRA);
            grabber.setSampleFormat(avutil.AV_SAMPLE_FMT_S16);
            grabber.setAudioChannels(STEREO);
            grabber.start();
            if (!grabber.hasVideo()) {
                throw new IllegalStateException("No video track");
            }
            fitInTheBox(grabber);
            openTheSound(grabber);
            play(grabber);
        } catch (Exception | LinkageError e) {
            log.warn("Cannot play the video {}", file, e);
            String reason = e.toString();
            onJavaFxThread(() -> onError.accept(reason));
        } finally {
            closeTheSound();
        }
    }

    /**
     * Reduces the images to the size shown, which saves work on the big videos; never enlarged here.
     */
    private void fitInTheBox(FFmpegFrameGrabber grabber) {
        double scale = Math.min(1, Math.min(maxWidth / grabber.getImageWidth(), maxHeight / grabber.getImageHeight()));
        if (scale < 1) {
            int width = (int) Math.round(grabber.getImageWidth() * scale);
            int height = (int) Math.round(grabber.getImageHeight() * scale);
            grabber.setImageWidth(width);
            grabber.setImageHeight(height);
        }
    }

    /**
     * A video without sound output goes on without its sound.
     */
    private void openTheSound(FFmpegFrameGrabber grabber) {
        if (!grabber.hasAudio() || grabber.getSampleRate() <= 0) {
            return;
        }
        int channels = grabber.getAudioChannels();
        AudioFormat format = new AudioFormat(grabber.getSampleRate(), Short.SIZE, channels, true, false);
        try {
            SourceDataLine opened = AudioSystem.getSourceDataLine(format);
            opened.open(format, grabber.getSampleRate() * channels * Short.BYTES * SOUND_BUFFER_SECONDS);
            synchronized (clockLock) {
                if (!paused) {
                    opened.start();
                }
            }
            line = opened;
        } catch (LineUnavailableException | IllegalArgumentException e) {
            log.warn("No sound for the video {}", file, e);
        }
    }

    private void closeTheSound() {
        SourceDataLine current = line;
        line = null;
        if (current != null) {
            current.close();
        }
    }

    private void play(FFmpegFrameGrabber grabber) throws Exception {
        startTheClock();
        boolean ended = false;
        while (!stopped) {
            if (takeRestartRequest()) {
                grabber.setTimestamp(0);
                startTheClock();
                ended = false;
            }
            if (ended) {
                waitForARequest();
                continue;
            }
            waitWhilePaused();
            if (stopped || isRestartRequested()) {
                continue;
            }
            Frame frame = grabber.grab();
            if (frame == null) {
                finishThePlay(grabber);
                ended = !isRestartRequested();
            } else if (frame.samples != null) {
                playTheSound(frame);
            } else if (frame.image != null) {
                showTheImage(frame);
            }
        }
    }

    /**
     * The last image stays until the end of the video and of its sound.
     */
    private void finishThePlay(FFmpegFrameGrabber grabber) {
        waitForTheTime(grabber.getLengthInTime());
        SourceDataLine current = line;
        if (current != null && !stopped && !isRestartRequested()) {
            current.drain();
        }
        if (!stopped && !isRestartRequested()) {
            onJavaFxThread(onEnd);
        }
    }

    private void playTheSound(Frame frame) {
        SourceDataLine current = line;
        if (current == null || !(frame.samples[0] instanceof ShortBuffer samples)) {
            return;
        }
        ShortBuffer source = samples.duplicate();
        byte[] bytes = new byte[source.remaining() * Short.BYTES];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(source);
        current.write(bytes, 0, bytes.length);
    }

    private void showTheImage(Frame frame) {
        waitForTheTime(frame.timestamp);
        if (stopped || isRestartRequested() || clockMicros() - frame.timestamp > TOO_LATE_MICROS) {
            return;
        }
        ByteBuffer source = ((ByteBuffer) frame.image[0]).duplicate();
        source.position(0);
        int bytes = frame.imageStride * frame.imageHeight;
        source.limit(bytes);
        synchronized (imageLock) {
            if (pendingImage == null || pendingImage.capacity() < bytes) {
                pendingImage = ByteBuffer.allocateDirect(bytes);
            }
            pendingImage.clear();
            pendingImage.put(source);
            pendingImage.flip();
            pendingWidth = frame.imageWidth;
            pendingHeight = frame.imageHeight;
            pendingStride = frame.imageStride;
        }
        if (renderScheduled.compareAndSet(false, true)) {
            Platform.runLater(this::render);
        }
    }

    private void render() {
        renderScheduled.set(false);
        if (stopped) {
            return;
        }
        synchronized (imageLock) {
            if (image == null || image.getWidth() != pendingWidth || image.getHeight() != pendingHeight) {
                image = new WritableImage(pendingWidth, pendingHeight);
                view.setImage(image);
            }
            image.getPixelWriter().setPixels(0, 0, pendingWidth, pendingHeight, PixelFormat.getByteBgraPreInstance(),
                pendingImage.duplicate(), pendingStride);
        }
    }

    private void onJavaFxThread(Runnable action) {
        Platform.runLater(() -> {
            if (!stopped) {
                action.run();
            }
        });
    }

    private void startTheClock() {
        synchronized (clockLock) {
            long now = System.nanoTime();
            clockStartNanos = now;
            pausedSinceNanos = now;
        }
    }

    /**
     * @return the time of the video, stopped during a pause
     */
    private long clockMicros() {
        synchronized (clockLock) {
            return ((paused ? pausedSinceNanos : System.nanoTime()) - clockStartNanos) / 1000;
        }
    }

    private void waitForTheTime(long micros) {
        while (!stopped && !isRestartRequested() && clockMicros() < micros) {
            try {
                Thread.sleep(WAIT_STEP_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stopped = true;
            }
        }
    }

    private void waitWhilePaused() {
        waitUntilWoken(() -> paused);
    }

    private void waitForARequest() {
        waitUntilWoken(() -> true);
    }

    /**
     * Waits while the condition holds, until a restart is requested or the player is stopped.
     */
    private void waitUntilWoken(BooleanSupplier condition) {
        synchronized (clockLock) {
            while (condition.getAsBoolean() && !stopped && !restartRequested) {
                try {
                    clockLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    stopped = true;
                }
            }
        }
    }

    private boolean isRestartRequested() {
        synchronized (clockLock) {
            return restartRequested;
        }
    }

    private boolean takeRestartRequest() {
        synchronized (clockLock) {
            boolean requested = restartRequested;
            restartRequested = false;
            return requested;
        }
    }
}

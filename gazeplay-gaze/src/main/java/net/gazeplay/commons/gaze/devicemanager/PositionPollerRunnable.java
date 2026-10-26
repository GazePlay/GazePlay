package net.gazeplay.commons.gaze.devicemanager;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tobii.Tobii;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Reads the gaze position of the Tobii eye tracker every 10 ms, until it is asked to stop, and gives each new position
 * to a consumer as a {@link GazeSample}, in screen coordinates.
 * <p>
 * The wrapper gives a position as two ratios (0 to 1) of the screen width and height: they are converted to pixels of
 * the screen showing GazePlay, then moved by the position of that screen in the desktop (second screen). The "no data"
 * value of the wrapper and the repeated positions are ignored (anomalies R9 and R6).
 */
@Slf4j
public class PositionPollerRunnable implements Runnable {

    /**
     * Value returned by the Tobii wrapper (TobiiStreamEngineForJava 5.0) when it has no gaze data: on error, and
     * continuously when no eye tracker is connected (checked on Windows x64: "Init code error -2", then (0.5, 0.5) at
     * each call). Without this check, GazePlay received a fixed gaze at the center of the screen (anomaly R9).
     * A real measure exactly equal to these two floats is practically impossible.
     */
    static final float NO_GAZE_DATA = 0.5f;

    private final Supplier<Dimension2D> screenDimensionSupplier;

    private final Supplier<Point2D> screenPositionSupplier;

    private final Consumer<GazeSample> sampleConsumer;

    private final Supplier<float[]> gazePositionSource;

    private boolean noGazeDataReported = false;

    /**
     * Ratios of the last position given, to drop the repetitions (R6). Copies: the wrapper fills a shared array.
     */
    private float lastXRatio = Float.NaN;
    private float lastYRatio = Float.NaN;

    @Setter
    private transient boolean stopRequested = false;

    /**
     * @param screenDimensionSupplier dimension of the screen showing GazePlay, read at each poll (the window can move)
     * @param screenPositionSupplier  position of that screen in the desktop
     * @param sampleConsumer          receives the samples, on the polling thread
     */
    public PositionPollerRunnable(final Supplier<Dimension2D> screenDimensionSupplier, final Supplier<Point2D> screenPositionSupplier, final Consumer<GazeSample> sampleConsumer) {
        this(screenDimensionSupplier, screenPositionSupplier, sampleConsumer, Tobii::gazePosition);
    }

    /**
     * Constructor for the tests: the position comes from {@code gazePositionSource} instead of the wrapper.
     */
    PositionPollerRunnable(final Supplier<Dimension2D> screenDimensionSupplier, final Supplier<Point2D> screenPositionSupplier,
                           final Consumer<GazeSample> sampleConsumer, final Supplier<float[]> gazePositionSource) {
        this.screenDimensionSupplier = screenDimensionSupplier;
        this.screenPositionSupplier = screenPositionSupplier;
        this.sampleConsumer = sampleConsumer;
        this.gazePositionSource = gazePositionSource;
    }

    /**
     * Polls until {@link #setStopRequested(boolean)} is called. An exception of a poll is logged and the polling goes
     * on.
     */
    @Override
    public void run() {
        while (!stopRequested) {
            try {
                poll();
            } catch (final RuntimeException e) {
                log.warn("Exception while polling position of gaze", e);
            }

            // sleep is mandatory to avoid too much calls to gazePosition()
            try {
                Thread.sleep(10);
            } catch (InterruptedException | RuntimeException e) {
                log.warn("Exception while sleeping until next poll", e);
            }
        }
    }

    /**
     * Reads one position and gives it to the consumer if it is a new one.
     */
    void poll() {
        // The acquisition time is taken here, on the polling thread, before the hop to the JavaFX thread
        nextGazePoint().ifPresent(point -> sampleConsumer.accept(GazeSample.now(point, GazeSample.Source.GAZE)));
    }

    /**
     * @return the position of the gaze on the screen, or empty when the eye tracker gives no gaze data, or no new one
     */
    Optional<Point2D> nextGazePoint() {
        final float[] pointAsFloatArray = gazePositionSource.get();

        final float xRatio = pointAsFloatArray[0];
        final float yRatio = pointAsFloatArray[1];

        if (xRatio == NO_GAZE_DATA && yRatio == NO_GAZE_DATA) {
            if (!noGazeDataReported) {
                noGazeDataReported = true;
                log.warn("The Tobii eye tracker gives no gaze data (no eye tracker connected?): ignored until it does");
            }
            return Optional.empty();
        }

        // The wrapper returns the last position it received, without its time nor whether it is new: polled every
        // 10 ms, it repeated each position of the eye tracker (about 70 % of the samples with an Eye Tracker 5, R6). A
        // new measure is practically never exactly equal to the previous one, even for a still eye: a repeated pair is
        // no new measure, and is dropped (results format version 4)
        if (xRatio == lastXRatio && yRatio == lastYRatio) {
            return Optional.empty();
        }
        lastXRatio = xRatio;
        lastYRatio = yRatio;

        final Dimension2D screenDimension = screenDimensionSupplier.get();
        final double positionX = xRatio * screenDimension.getWidth();
        final double positionY = yRatio * screenDimension.getHeight();

        final Point2D screenPosition = screenPositionSupplier.get();
        final double offsetX = screenPosition.getX();
        final double offsetY = screenPosition.getY();

        return Optional.of(new Point2D(positionX + offsetX, positionY + offsetY));
    }

}

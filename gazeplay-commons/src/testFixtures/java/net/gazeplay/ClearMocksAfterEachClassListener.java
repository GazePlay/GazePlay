package net.gazeplay;

import javafx.application.Platform;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.mockito.Mockito;
import org.mockito.listeners.MockCreationListener;
import org.mockito.mock.MockCreationSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Frees the mocks of Mockito at the end of the test class that created them, in each JVM of tests of the modules using
 * these fixtures. A mock keeps every call made on it with its arguments: the games given to the mocks of a test (nodes,
 * so the scene and its images) stayed in memory until the end of the JVM. Measured on 16/11/2026 (REVUE_DES_JEUX.md,
 * "Mémoire des tests"): the tests of gazeplay-games went over 4 GB without it.
 * <p>
 * At once since 18/11/2026: every test closes the games it launches ({@code LaunchedGames} in gazeplay-games), so no
 * timer of a game is left to fire after its class on a freed mock (a DisabledMockException in the next class, seen on
 * the CI on 16/11/2026; the mocks were then freed 10 s after the end of their class). Before freeing them, the tasks
 * already queued on the JavaFX thread are run, so that an action still on its way is not the next class's error.
 * <p>
 * Mocks are followed on the thread of the tests and on the JavaFX thread (deep stubs are often created there), with a
 * {@link MockCreationListener}, whose listeners belong to a thread in Mockito; a mock created on another thread is not
 * freed. Found by JUnit through META-INF/services/org.junit.platform.launcher.TestExecutionListener.
 */
public class ClearMocksAfterEachClassListener implements TestExecutionListener {

    /**
     * How long to wait for the tasks queued on the JavaFX thread at the end of a class.
     */
    static final long FX_QUEUE_TIMEOUT_MILLIS = 5_000;

    private final Object lock = new Object();

    private List<Object> current = new ArrayList<>();

    private final MockCreationListener creations = new MockCreationListener() {
        @Override
        public void onMockCreated(final Object mock, final MockCreationSettings settings) {
            synchronized (lock) {
                current.add(mock);
            }
        }
    };

    private boolean followedOnJavaFxThread;

    @Override
    public void testPlanExecutionStarted(final TestPlan testPlan) {
        Mockito.framework().addListener(creations);
    }

    @Override
    public void executionFinished(final TestIdentifier testIdentifier, final TestExecutionResult result) {
        if (testIdentifier.getSource().filter(ClassSource.class::isInstance).isEmpty()) {
            return;
        }
        followOnJavaFxThread();
        runTheQueuedJavaFxTasks();
        final List<Object> ended;
        synchronized (lock) {
            ended = current;
            current = new ArrayList<>();
        }
        for (final Object mock : ended) {
            Mockito.framework().clearInlineMock(mock);
        }
    }

    @Override
    public void testPlanExecutionFinished(final TestPlan testPlan) {
        Mockito.framework().removeListener(creations);
    }

    /**
     * Once JavaFX is started (by the first test class using it), also follows the mocks created on its thread.
     */
    private void followOnJavaFxThread() {
        if (followedOnJavaFxThread) {
            return;
        }
        try {
            Platform.runLater(() -> Mockito.framework().addListener(creations));
            followedOnJavaFxThread = true;
        } catch (final IllegalStateException notStarted) {
            // JavaFX not started yet: tried again at the end of the next class
        }
    }

    /**
     * Waits until the tasks already queued on the JavaFX thread have run, when JavaFX is started.
     */
    private void runTheQueuedJavaFxTasks() {
        if (!followedOnJavaFxThread || Platform.isFxApplicationThread()) {
            return;
        }
        final CountDownLatch done = new CountDownLatch(1);
        try {
            Platform.runLater(done::countDown);
            done.await(FX_QUEUE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (final IllegalStateException stopped) {
            // JavaFX stopped: nothing left to run
        } catch (final InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}

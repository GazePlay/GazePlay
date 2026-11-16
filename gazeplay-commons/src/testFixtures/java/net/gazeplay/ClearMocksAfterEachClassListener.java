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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Frees the mocks of Mockito some time after the end of the test class that created them, in each JVM of tests of the
 * modules using these fixtures. A mock keeps every call made on it with its arguments: the games given to the mocks of
 * a test (nodes, so the scene and its images) stayed in memory until the end of the JVM. Measured on 16/11/2026
 * (REVUE_DES_JEUX.md, "Mémoire des tests"): the tests of gazeplay-games went over 4 GB without it.
 * <p>
 * Not at once: a game left running by a test (a "Lost!" pause, a countdown) may fire a little after the end of its
 * class, and a freed mock then throws a DisabledMockException in the next class (seen on the CI on 16/11/2026, a timing
 * not seen on a faster PC). The mocks of a class are freed at the end of a later class, once {@link #DELAY_MILLIS} have
 * passed since the end of theirs. Mocks are followed on the thread of the tests and on the JavaFX thread (deep stubs
 * are often created there), with a {@link MockCreationListener}, whose listeners belong to a thread in Mockito; a mock
 * created on another thread is not freed (as before). Found by JUnit through
 * META-INF/services/org.junit.platform.launcher.TestExecutionListener.
 */
public class ClearMocksAfterEachClassListener implements TestExecutionListener {

    /**
     * Longer than the pauses of the games (2 or 3 s).
     */
    static final long DELAY_MILLIS = 10_000;

    private record Batch(List<Object> mocks, long endedAt) {
    }

    private final Object lock = new Object();

    private List<Object> current = new ArrayList<>();

    private final Deque<Batch> waiting = new ArrayDeque<>();

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
        final long now = System.currentTimeMillis();
        synchronized (lock) {
            waiting.addLast(new Batch(current, now));
            current = new ArrayList<>();
            while (!waiting.isEmpty() && now - waiting.peekFirst().endedAt() >= DELAY_MILLIS) {
                for (final Object mock : waiting.removeFirst().mocks()) {
                    Mockito.framework().clearInlineMock(mock);
                }
            }
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
}

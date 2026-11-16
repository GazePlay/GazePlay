package net.gazeplay;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.mockito.Mockito;

/**
 * Frees the mocks of Mockito at the end of each test class, in each JVM of tests of the modules using these fixtures.
 * A mock keeps every call made on it with its arguments: the games given to the mocks of a test (nodes, so the scene
 * and its images) stayed in memory until the end of the JVM. Measured on 16/11/2026 (REVUE_DES_JEUX.md, "Mémoire des
 * tests"): the tests of gazeplay-games went over 4 GB without it, 594 MB at most with it. A game left running by a
 * test (a timer firing after its class) now fails with a DisabledMockException: close it at the end of the test
 * ({@code dispose()}). No test class may share its mocks with another one, nor use {@code @Nested} classes. Found by
 * JUnit through META-INF/services/org.junit.platform.launcher.TestExecutionListener.
 */
public class ClearMocksAfterEachClassListener implements TestExecutionListener {

    @Override
    public void executionFinished(final TestIdentifier testIdentifier, final TestExecutionResult result) {
        if (testIdentifier.getSource().filter(ClassSource.class::isInstance).isPresent()) {
            Mockito.framework().clearInlineMocks();
        }
    }
}

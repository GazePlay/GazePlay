package net.gazeplay.commons.threads;

import lombok.extern.slf4j.Slf4j;

/**
 * Logs the exceptions that end a thread, which would otherwise only be printed on the console.
 */
@Slf4j
public class LoggingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        log.error("uncaughtException in Thread {} : {} : {}", t.getName(), e.getClass().getName(), e.getMessage(), e);
    }
}

package net.gazeplay.commons.threads;

import lombok.Setter;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class GroupingThreadFactory implements ThreadFactory {

    private final ThreadGroup threadGroup;

    private final AtomicInteger threadIndex = new AtomicInteger(0);

    /**
     * by default, the created threads should not be daemons.
     */
    @Setter
    private boolean daemon = false;

    @Setter
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new LoggingUncaughtExceptionHandler();

    public GroupingThreadFactory(final String groupName) {
        super();
        threadGroup = new ThreadGroup(groupName);
    }

    @Override
    public Thread newThread(final Runnable r) {
        final Thread thread = new Thread(threadGroup, r);
        thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        thread.setDaemon(daemon);
        thread.setName("Thread-" + threadIndex.incrementAndGet());
        return thread;
    }

}

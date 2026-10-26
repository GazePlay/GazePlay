package net.gazeplay.commons.threads;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Thread factory naming each thread after a prefix and the class of its task, to find them in the logs and thread
 * dumps.
 */
@Slf4j
public class CustomThreadFactory implements ThreadFactory {

    private final String namePrefix;

    private final ThreadFactory delegate;

    public CustomThreadFactory(final String namePrefix) {
        this(namePrefix, Executors.defaultThreadFactory());
    }

    public CustomThreadFactory(final String namePrefix, final ThreadFactory delegate) {
        this.namePrefix = namePrefix;
        this.delegate = delegate;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        final Thread result = delegate.newThread(runnable);

        final Class<? extends Runnable> runnableClass = runnable.getClass();

        final String nameBuilder = namePrefix +
            "-" +
            runnableClass.getSimpleName() +
            "-" +
            // original name
            result.getName();
        result.setName(nameBuilder);

        return result;
    }

}

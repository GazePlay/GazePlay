package net.gazeplay.commons.threads;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class CustomThreadFactory implements ThreadFactory {

    private final String namePrefix;

    private final ThreadFactory delegate;

    public CustomThreadFactory(String namePrefix) {
        this(namePrefix, Executors.defaultThreadFactory());
    }

    public CustomThreadFactory(String namePrefix, ThreadFactory delegate) {
        this.namePrefix = namePrefix;
        this.delegate = delegate;
    }

    public Thread newThread(Runnable runnable) {
        Thread result = delegate.newThread(runnable);

        Class<? extends Runnable> runnableClass = runnable.getClass();

        String nameBuilder = namePrefix +
            "-" +
            runnableClass.getSimpleName() +
            "-" +
            // original name
            result.getName();
        result.setName(nameBuilder);

        return result;
    }

}

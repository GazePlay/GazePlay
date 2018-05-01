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

        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(namePrefix);
        nameBuilder.append("-");
        nameBuilder.append(runnableClass.getSimpleName());
        nameBuilder.append("-");
        // original name
        nameBuilder.append(result.getName());
        result.setName(nameBuilder.toString());

        return result;
    }

}

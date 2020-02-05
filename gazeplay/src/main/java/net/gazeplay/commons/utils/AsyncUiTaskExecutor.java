package net.gazeplay.commons.utils;

import lombok.Getter;
import net.gazeplay.commons.threads.CustomThreadFactory;
import net.gazeplay.commons.threads.GroupingThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AsyncUiTaskExecutor {

    @Getter
    private static final AsyncUiTaskExecutor instance = new AsyncUiTaskExecutor();

    @Getter
    private final ExecutorService executorService = new ThreadPoolExecutor(0, 4, 1, TimeUnit.MINUTES,
        new LinkedBlockingQueue<>(), new CustomThreadFactory("AsyncUi", new GroupingThreadFactory("AsyncUi")));

}

package io.github.jerryzhongj.calabash_brothers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ThreadPool {
    public static final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(Settings.SCHEDULED_POOL_SIZE);
    public static final ExecutorService nonScheduled = Executors.newCachedThreadPool();
}

package io.github.jerryzhongj.calabash_brothers.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

class ThreadPool {
    static final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(Settings.SCHEDULED_POOL_SIZE);
    static final ExecutorService nonScheduled = Executors.newCachedThreadPool();
}

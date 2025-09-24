package ru.hzerr.utils;

import jakarta.annotation.Nonnull;

import java.util.concurrent.ThreadFactory;

public class DaemonThreadFactory implements ThreadFactory {

    private final String threadName;

    public DaemonThreadFactory(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public Thread newThread(@Nonnull Runnable action) {
        Thread t = new Thread(action);
        t.setDaemon(true);
        t.setName(threadName);
        return t;
    }

    public static DaemonThreadFactory newFactory(String newThreadName) {
        return new DaemonThreadFactory(newThreadName);
    }
}

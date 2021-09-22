package org.minos.core.context;

import java.util.concurrent.Callable;

public final class ContextUtils {

    public static Runnable wrap(Runnable task) {
        return new ContextRunnable(task);
    }

    public static <T> Callable<T> wrap(Callable<T> task) {
        return new ContextCallable<>(task);
    }
}

package org.minos.core.context;

import java.util.Map;
import java.util.concurrent.Callable;

public class ContextCallable<T> implements Callable<T> {

    private Callable<T> task;

    private Map<String, Object> context;

    public ContextCallable(Callable<T> task) {
        this.task = task;
        this.context = ThreadLocalContextHolder.holder.get();
    }

    @Override
    public T call() throws Exception {
        ThreadLocalContextHolder.holder.set(context);

        return task.call();
    }
}

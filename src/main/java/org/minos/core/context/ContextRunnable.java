package org.minos.core.context;

import java.util.Map;

/**
 * ContextRunnable
 */
public class ContextRunnable implements Runnable {

    private Map<String, Object> context;

    private Runnable task;

    public ContextRunnable(Runnable task) {
        this.task = task;
        this.context = ThreadLocalContextHolder.holder.get();
    }

    @Override
    public void run() {
        ThreadLocalContextHolder.holder.set(context);

        task.run();
    }
}
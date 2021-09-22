package org.minos.discover.client.listener;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * DiscoveryCacheEventPublisher
 *
 * @date 2019-12-21 08:29
 */
public class DiscoveryCacheEventPublisher {
    private final Set<DiscoveryCacheListener> listeners;

    public DiscoveryCacheEventPublisher() {
        this.listeners = Sets.newHashSet();
    }

    public synchronized void registerListener(DiscoveryCacheListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public synchronized void deregisterListener(DiscoveryCacheListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void fireEvent(DiscoveryCacheEvent event) {
        listeners.forEach(listener -> listener.onEvent(event));
    }
}

package org.minos.discover.client.eureka;

import com.netflix.discovery.shared.resolver.EurekaEndpoint;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Eureka server选择器
 * 在ServerList中随机选择一个Prefer Server，尽量会选择该Server去连接，保证Server在异常恢复后的整体负载较为均衡
 */
public class StickyEndpointSelector<T extends EurekaEndpoint>  implements EndPointSelector<T> {
    private static final int SELECT_NOT_PREFER_SERVER_THRESHOLD = 5;

    /**
     * server resolver
     */
    private final List<T> eurekaEndpointList;

    private int selectIndex;

    private final T preferServer;

    private final AtomicInteger notPreferSelectCount = new AtomicInteger(0);

    public StickyEndpointSelector(List<T> endpoints) {
        this.eurekaEndpointList = endpoints;

        //随机化
        Collections.shuffle(this.eurekaEndpointList);

        this.selectIndex = 0;
        this.preferServer = eurekaEndpointList.get(this.selectIndex);
    }

    @Override
    public List<T> endpoints() {
        return eurekaEndpointList;
    }

    private T selectNext() {
        return this.eurekaEndpointList.get((++selectIndex) % this.eurekaEndpointList.size());
    }

    private T selectCurrent() {
        return this.eurekaEndpointList.get(selectIndex % this.eurekaEndpointList.size());
    }

    @Override
    public T select() {
        return this.select(false);
    }

    @Override
    public T select(boolean reselect) {
        T select;
        if (reselect) {
            select = selectNext();
        } else {
            select = selectCurrent();
        }

        //尽量保证选择preferServer
        if (this.preferServer == select) {
            this.notPreferSelectCount.set(0);
            this.selectIndex = 0;
            return select;
        }

        if (this.notPreferSelectCount.get() >= SELECT_NOT_PREFER_SERVER_THRESHOLD) {
            this.notPreferSelectCount.set(0);
            this.selectIndex = 0;
            return this.preferServer;
        }
        this.notPreferSelectCount.addAndGet(1);

        return select;
    }

    protected int getSelectIndex() {
        return selectIndex;
    }

    protected EurekaEndpoint getPreferServer() {
        return preferServer;
    }

    protected AtomicInteger getNotPreferSelectCount() {
        return notPreferSelectCount;
    }
}

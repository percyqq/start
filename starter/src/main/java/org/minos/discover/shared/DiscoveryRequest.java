package org.minos.discover.shared;

/**
 * 对客户端Long Polling请求的封装
 * <p>
 * 包含挂起的连接，和客户端请求的epoch。epoch字段暂时没有使用，后续可以用来做监控
 *
 * @date 2019/10/14
 */
public class DiscoveryRequest {

    private Long epoch;

    private Boolean enableDelta;

    private DiscoveryClientInfo client;

    public Long getEpoch() {
        return epoch;
    }

    public void setEpoch(Long epoch) {
        this.epoch = epoch;
    }

    public Boolean isEnableDelta() {
        return enableDelta;
    }

    public void setEnableDelta(Boolean enableDelta) {
        this.enableDelta = enableDelta;
    }

    public DiscoveryClientInfo getClient() {
        return client;
    }

    public void setClient(DiscoveryClientInfo client) {
        this.client = client;
    }
}

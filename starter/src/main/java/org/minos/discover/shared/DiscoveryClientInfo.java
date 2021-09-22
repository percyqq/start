package org.minos.discover.shared;

/**
 * DiscoveryClientInfo
 *
 * @date 2020/5/7
 */
public class DiscoveryClientInfo {

    private String id;

    private String ip;

    private String version;

    private Long requestTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Long requestTime) {
        this.requestTime = requestTime;
    }
}

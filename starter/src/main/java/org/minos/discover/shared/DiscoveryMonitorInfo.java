package org.minos.discover.shared;

import java.util.List;

/**
 * DiscoveryMonitorInfo
 *
 * @date 2019/10/23
 */
public class DiscoveryMonitorInfo {

    private Long epoch;

    private List<DiscoveryRequest> requests;

    public Long getEpoch() {
        return epoch;
    }

    public void setEpoch(Long epoch) {
        this.epoch = epoch;
    }

    public List<DiscoveryRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<DiscoveryRequest> requests) {
        this.requests = requests;
    }
}

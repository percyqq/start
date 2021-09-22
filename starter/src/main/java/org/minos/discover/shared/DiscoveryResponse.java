package org.minos.discover.shared;

import java.util.List;

/**
 * DiscoveryResponse
 *
 * @date 2019/10/14
 */
public class DiscoveryResponse {
    private long epoch;

    private List<DiscoveryApplication> applications;

    public long getEpoch() {
        return epoch;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }

    public List<DiscoveryApplication> getApplications() {
        return applications;
    }

    public void setApplications(List<DiscoveryApplication> applications) {
        this.applications = applications;
    }
}

package org.minos.discover.shared;

import java.util.List;

/**
 * DiscoveryApplication
 *
 * @date 2019/10/16
 */
public class DiscoveryApplication {

    private String name;

    private List<DiscoveryInstance> instances;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DiscoveryInstance> getInstances() {
        return instances;
    }

    public void setInstances(List<DiscoveryInstance> instances) {
        this.instances = instances;
    }
}

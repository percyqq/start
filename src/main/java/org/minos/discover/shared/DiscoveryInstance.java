package org.minos.discover.shared;

import java.util.Map;

/**
 * DiscoveryInstance
 *
 * @date 2019/10/16
 */
public class DiscoveryInstance {

    private String instanceId;

    private String hostName;

    private String app;

    private String ipAddr;

    private String status;

    private String overriddenStatus;

    private Port port;

    private Port securePort;

    private int countryId;

    private LeaseInfo leaseInfo;

    private Map<String, String> metadata;

    private String homePageUrl;

    private String statusPageUrl;

    private String healthCheckUrl;

    private String vipAddress;

    private String secureVipAddress;

    private boolean coordinatingDiscoveryServer;

    private long lastUpdatedTimestamp;

    private long lastDirtyTimestamp;

    private String actionType;

    private DataCenterInfo dataCenterInfo;

    public static class Port {

        private boolean enabled;

        private int port;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class DataCenterInfo {

        private String className;

        private String name;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class LeaseInfo {

        private int renewalIntervalInSecs;

        private int durationInSecs;

        private long registrationTimestamp;

        private long lastRenewalTimestamp;

        private long evictionTimestamp;

        private long serviceUpTimestamp;

        public int getRenewalIntervalInSecs() {
            return renewalIntervalInSecs;
        }

        public void setRenewalIntervalInSecs(int renewalIntervalInSecs) {
            this.renewalIntervalInSecs = renewalIntervalInSecs;
        }

        public int getDurationInSecs() {
            return durationInSecs;
        }

        public void setDurationInSecs(int durationInSecs) {
            this.durationInSecs = durationInSecs;
        }

        public long getRegistrationTimestamp() {
            return registrationTimestamp;
        }

        public void setRegistrationTimestamp(long registrationTimestamp) {
            this.registrationTimestamp = registrationTimestamp;
        }

        public long getLastRenewalTimestamp() {
            return lastRenewalTimestamp;
        }

        public void setLastRenewalTimestamp(long lastRenewalTimestamp) {
            this.lastRenewalTimestamp = lastRenewalTimestamp;
        }

        public long getEvictionTimestamp() {
            return evictionTimestamp;
        }

        public void setEvictionTimestamp(long evictionTimestamp) {
            this.evictionTimestamp = evictionTimestamp;
        }

        public long getServiceUpTimestamp() {
            return serviceUpTimestamp;
        }

        public void setServiceUpTimestamp(long serviceUpTimestamp) {
            this.serviceUpTimestamp = serviceUpTimestamp;
        }
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOverriddenStatus() {
        return overriddenStatus;
    }

    public void setOverriddenStatus(String overriddenStatus) {
        this.overriddenStatus = overriddenStatus;
    }

    public Port getPort() {
        return port;
    }

    public void setPort(Port port) {
        this.port = port;
    }

    public Port getSecurePort() {
        return securePort;
    }

    public void setSecurePort(Port securePort) {
        this.securePort = securePort;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public LeaseInfo getLeaseInfo() {
        return leaseInfo;
    }

    public void setLeaseInfo(LeaseInfo leaseInfo) {
        this.leaseInfo = leaseInfo;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getHomePageUrl() {
        return homePageUrl;
    }

    public void setHomePageUrl(String homePageUrl) {
        this.homePageUrl = homePageUrl;
    }

    public String getStatusPageUrl() {
        return statusPageUrl;
    }

    public void setStatusPageUrl(String statusPageUrl) {
        this.statusPageUrl = statusPageUrl;
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }

    public String getVipAddress() {
        return vipAddress;
    }

    public void setVipAddress(String vipAddress) {
        this.vipAddress = vipAddress;
    }

    public String getSecureVipAddress() {
        return secureVipAddress;
    }

    public void setSecureVipAddress(String secureVipAddress) {
        this.secureVipAddress = secureVipAddress;
    }

    public boolean isCoordinatingDiscoveryServer() {
        return coordinatingDiscoveryServer;
    }

    public void setCoordinatingDiscoveryServer(boolean coordinatingDiscoveryServer) {
        this.coordinatingDiscoveryServer = coordinatingDiscoveryServer;
    }

    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public long getLastDirtyTimestamp() {
        return lastDirtyTimestamp;
    }

    public void setLastDirtyTimestamp(long lastDirtyTimestamp) {
        this.lastDirtyTimestamp = lastDirtyTimestamp;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public DataCenterInfo getDataCenterInfo() {
        return dataCenterInfo;
    }

    public void setDataCenterInfo(DataCenterInfo dataCenterInfo) {
        this.dataCenterInfo = dataCenterInfo;
    }
}

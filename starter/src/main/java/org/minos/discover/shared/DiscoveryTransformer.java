package org.minos.discover.shared;

import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.LeaseInfo;
import com.netflix.appinfo.MyDataCenterInfo;
import com.netflix.discovery.converters.jackson.DataCenterTypeInfoResolver;
import com.netflix.discovery.shared.Application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * DiscoveryTransformer
 *
 * @date 2019/10/16
 */
public final class DiscoveryTransformer {

    public static List<Application> toEureka(List<DiscoveryApplication> applicationList) {
        List<Application> result = new LinkedList<>();
        for (DiscoveryApplication application : applicationList) {
            result.add(toEureka(application));
        }

        return result;
    }

    public static Application toEureka(DiscoveryApplication application) {
        Application result = new Application();
        result.setName(application.getName());

        List<DiscoveryInstance> instanceList = new ArrayList<>(application.getInstances());
        for (DiscoveryInstance instance : instanceList) {
            result.addInstance(toEurekaInstance(instance));
        }

        return result;
    }

    public static DiscoveryApplication fromEureka(Application eurekaApp) {
        DiscoveryApplication result = new DiscoveryApplication();
        result.setName(eurekaApp.getName());

        List<InstanceInfo> eurekaInstanceList = new ArrayList<>(eurekaApp.getInstances());
        result.setInstances(new ArrayList<>(eurekaInstanceList.size()));

        for (InstanceInfo eurekaInstance : eurekaInstanceList) {
            result.getInstances().add(fromEurekaInstance(eurekaInstance));
        }

        return result;
    }

    public static List<DiscoveryApplication> fromEureka(List<Application> eurekaAppList) {
        List<Application> eurekaApplicationList = new ArrayList<>(eurekaAppList);

        List<DiscoveryApplication> result = new ArrayList<>(eurekaApplicationList.size());
        for (Application eurekaApp : eurekaApplicationList) {
            result.add(fromEureka(eurekaApp));
        }

        return result;
    }

    private static DiscoveryInstance fromEurekaInstance(InstanceInfo eurekaInstance) {
        DiscoveryInstance result = new DiscoveryInstance();
        result.setActionType(eurekaInstance.getActionType() == null ? "" : eurekaInstance.getActionType().name());
        result.setApp(eurekaInstance.getAppName());
        result.setCoordinatingDiscoveryServer(eurekaInstance.isCoordinatingDiscoveryServer());
        result.setCountryId(eurekaInstance.getCountryId());

        DiscoveryInstance.DataCenterInfo dataCenterInfo = new DiscoveryInstance.DataCenterInfo();
        dataCenterInfo.setClassName(DataCenterTypeInfoResolver.MY_DATA_CENTER_INFO_TYPE_MARKER);
        dataCenterInfo.setName("MyOwn");
        result.setDataCenterInfo(dataCenterInfo);

        result.setHealthCheckUrl(eurekaInstance.getHealthCheckUrl());
        result.setHomePageUrl(eurekaInstance.getHomePageUrl());
        result.setHostName(eurekaInstance.getHostName());
        result.setInstanceId(eurekaInstance.getInstanceId());
        result.setIpAddr(eurekaInstance.getIPAddr());
        result.setLastDirtyTimestamp(eurekaInstance.getLastDirtyTimestamp());
        result.setLastUpdatedTimestamp(eurekaInstance.getLastUpdatedTimestamp());

        DiscoveryInstance.LeaseInfo lease = new DiscoveryInstance.LeaseInfo();
        LeaseInfo eurekaLease = eurekaInstance.getLeaseInfo();
        if (eurekaLease != null) {
            lease.setDurationInSecs(eurekaLease.getDurationInSecs());
            lease.setLastRenewalTimestamp(eurekaLease.getRenewalTimestamp());
            lease.setDurationInSecs(eurekaLease.getDurationInSecs());
            lease.setRegistrationTimestamp(eurekaLease.getRegistrationTimestamp());
            lease.setRenewalIntervalInSecs(eurekaLease.getRenewalIntervalInSecs());
            lease.setServiceUpTimestamp(eurekaLease.getServiceUpTimestamp());
            result.setLeaseInfo(lease);
        }

        result.setMetadata(new HashMap<>(eurekaInstance.getMetadata()));
        result.setOverriddenStatus(eurekaInstance.getOverriddenStatus().name());

        DiscoveryInstance.Port port = new DiscoveryInstance.Port();
        port.setEnabled(eurekaInstance.isPortEnabled(InstanceInfo.PortType.UNSECURE));
        port.setPort(eurekaInstance.getPort());
        result.setPort(port);

        DiscoveryInstance.Port securePort = new DiscoveryInstance.Port();
        securePort.setEnabled(eurekaInstance.isPortEnabled(InstanceInfo.PortType.SECURE));
        securePort.setPort(eurekaInstance.getSecurePort());
        result.setSecurePort(securePort);

        result.setSecureVipAddress(eurekaInstance.getSecureVipAddress());
        result.setStatus(eurekaInstance.getStatus().name());
        result.setStatusPageUrl(eurekaInstance.getStatusPageUrl());
        result.setVipAddress(eurekaInstance.getVIPAddress());

        return result;
    }

    private static InstanceInfo toEurekaInstance(DiscoveryInstance instance) {
        InstanceInfo.Builder builder = InstanceInfo.Builder.newBuilder();
        builder.setHostName(instance.getHostName());
        builder.setActionType(InstanceInfo.ActionType.valueOf(instance.getActionType()));
        builder.setAppName(instance.getApp());
        builder.setIsCoordinatingDiscoveryServer(instance.isCoordinatingDiscoveryServer());
        builder.setCountryId(instance.getCountryId());
        builder.setHealthCheckUrls(null, instance.getHealthCheckUrl(), null);
        builder.setHomePageUrl(null, instance.getHomePageUrl());
        builder.setInstanceId(instance.getInstanceId());
        builder.setIPAddr(instance.getIpAddr());
        builder.setLastDirtyTimestamp(instance.getLastDirtyTimestamp());
        builder.setLastUpdatedTimestamp(instance.getLastUpdatedTimestamp());

        DiscoveryInstance.LeaseInfo lease = instance.getLeaseInfo();
        if (lease != null) {
            LeaseInfo.Builder leaseBuilder = LeaseInfo.Builder.newBuilder();
            leaseBuilder.setDurationInSecs(lease.getDurationInSecs());
            leaseBuilder.setRenewalTimestamp(lease.getLastRenewalTimestamp());
            leaseBuilder.setDurationInSecs(lease.getDurationInSecs());
            leaseBuilder.setRegistrationTimestamp(lease.getRegistrationTimestamp());
            leaseBuilder.setRenewalIntervalInSecs(lease.getRenewalIntervalInSecs());
            leaseBuilder.setServiceUpTimestamp(lease.getServiceUpTimestamp());
            builder.setLeaseInfo(leaseBuilder.build());
        }

        builder.setMetadata(instance.getMetadata());
        builder.setOverriddenStatus(InstanceInfo.InstanceStatus.valueOf(instance.getOverriddenStatus()));

        builder.enablePort(InstanceInfo.PortType.UNSECURE, instance.getPort().isEnabled());
        builder.enablePort(InstanceInfo.PortType.SECURE, instance.getSecurePort().isEnabled());
        builder.setPort(instance.getPort().getPort());
        builder.setSecurePort(instance.getSecurePort().getPort());

        builder.setSecureVIPAddress(instance.getSecureVipAddress());
        builder.setStatus(InstanceInfo.InstanceStatus.toEnum(instance.getStatus()));
        builder.setStatusPageUrl(null, instance.getStatusPageUrl());
        builder.setVIPAddress(instance.getVipAddress());

        DataCenterInfo dataCenterInfo = new MyDataCenterInfo(DataCenterInfo.Name.MyOwn);
        builder.setDataCenterInfo(dataCenterInfo);

        return builder.build();
    }
}

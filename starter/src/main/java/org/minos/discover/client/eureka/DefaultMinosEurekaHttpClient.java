package org.minos.discover.client.eureka;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.resolver.EurekaEndpoint;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.minos.discover.client.common.VersionUtils;
import org.minos.discover.client.MinosServiceInstance;
import org.minos.discover.client.common.Constants;
import org.minos.discover.client.common.TimeUtils;
import org.minos.discover.client.http.HttpClient;
import org.minos.discover.client.http.HttpRequest;
import org.minos.discover.client.http.HttpResponse;
import org.minos.discover.shared.DiscoveryInstance;
import org.minos.discover.shared.DiscoveryMonitorInfo;
import org.minos.discover.shared.DiscoveryResponse;
import org.springframework.cloud.netflix.eureka.http.RestTemplateEurekaHttpClient;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.minos.discover.client.common.LoggerUtils.CLIENT_LOGGER;
import static org.minos.discover.client.eureka.ServerLongPollingResponse.*;

/**
 * @date 2020/8/7 08:56
 */
public class DefaultMinosEurekaHttpClient extends RestTemplateEurekaHttpClient implements MinosEurekaHttpClient {
    private static final String FULL_EPOCH_URI = "/discovery/epoch";
    private static final String MONITOR_URI = "/discovery/monitor";
    private final HttpClient http;
    private final EurekaEndpoint endpoint;
    private final MinosServiceInstance instance;
    private final int timeout;

    public DefaultMinosEurekaHttpClient(RestTemplate restTemplate, MinosServiceInstance instance, EurekaEndpoint endpoint, int timeout) {
        super(restTemplate, endpoint.getServiceUrl());
        this.endpoint = endpoint;
        this.instance = instance;
        this.timeout = timeout;
        this.http = new HttpClient(timeout);
    }

    public DefaultMinosEurekaHttpClient(RestTemplate restTemplate, EurekaEndpoint endpoint, int timeout) {
        this(restTemplate, null, endpoint, timeout);
    }

    @Override
    public ServerLongPollingResponse epoch(long epoch, boolean delta) {
        try {
            Map<String, String> headers = buildLongPollingHeaders();
            if (delta) {
                headers.put(Constants.REQUEST_DELTA_HEADER, "true");
            }

            Map<String, String> params = new HashMap<>(2);
            params.put("timeout", String.valueOf(TimeUtils.secToMilli(timeout)));
            params.put("epoch", String.valueOf(epoch));

            HttpRequest request = new HttpRequest(buildRequestUrl(endpoint, FULL_EPOCH_URI), "GET", params, headers);

            HttpResponse<DiscoveryResponse> response = this.http.execute(request, DiscoveryResponse.class);

            if (response.code == org.apache.http.HttpStatus.SC_OK) {
                String isDeltaResponseStr = response.getHeader(Constants.RESPONSE_DELTA_HEADER);
                return okRsp(endpoint, epoch, "true".equals(isDeltaResponseStr), response.data);
            }

            if (response.code == org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE) {
                //server超时返回的是503，说明没有epoch更新
                return okRsp(endpoint, epoch, false, null);
            }

            if (response.code == HttpStatus.SC_NOT_FOUND) {
                return notSupportRsp(endpoint, epoch);
            }

            return errRsp(endpoint, epoch);
        } catch (Exception e) {
            CLIENT_LOGGER.error("failed to request new epoch data", e);
            return errRsp(endpoint, epoch);
        }
    }

    private Map<String, String> buildLongPollingHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(org.apache.http.HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        headers.put(org.apache.http.HttpHeaders.ACCEPT_ENCODING, "gzip,deflate,sdch");
        headers.put(org.apache.http.HttpHeaders.CONNECTION, "Keep-Alive");

        if (this.instance != null) {
            headers.put(Constants.CLIENT_APP_HEADER, this.instance.serviceId());
            headers.put(Constants.CLIENT_ID_HEADER, this.instance.instanceId());
            headers.put(Constants.CLIENT_ID_NEW_HEADER, this.instance.instanceId());
        }
        headers.put(Constants.CLIENT_VERSION_HEADER, VersionUtils.VERSION);
        headers.put(Constants.CLIENT_VERSION_NEW_HEADER, VersionUtils.VERSION);
        return headers;
    }

    @Override
    public List<Application> getApplications() {
        return epoch(0, false).getApplications();
    }

    @Override
    public DiscoveryMonitorInfo monitor() {
        try {
            HttpRequest request = new HttpRequest(buildRequestUrl(endpoint, MONITOR_URI), "GET", null, null);

            HttpResponse<DiscoveryMonitorInfo> response = this.http.execute(request, DiscoveryMonitorInfo.class);

            if (response.code == org.apache.http.HttpStatus.SC_OK) {
                return response.data;
            } else {
                CLIENT_LOGGER.warn("failed to request monitor {}", response.code);
            }
            return null;
        } catch (Exception e) {
            CLIENT_LOGGER.warn("failed to request monitor", e);
            return null;
        }
    }

    @Override
    public DiscoveryInstance setStatus(String ip, InstanceInfo.InstanceStatus status) {
        try {
            Map<String, String> params = new HashMap<>(2);
            params.put("ip", ip);
            params.put("status", status.toString());

            HttpRequest request = new HttpRequest(buildRequestUrl(endpoint, MONITOR_URI), "PUT", params, null);

            HttpResponse<DiscoveryInstance> response = this.http.execute(request, DiscoveryInstance.class);

            if (response.code == org.apache.http.HttpStatus.SC_OK) {
                return response.data;
            } else {
                CLIENT_LOGGER.warn("failed to request monitor {}", response.code);
            }
            return null;
        } catch (Exception e) {
            CLIENT_LOGGER.warn("failed to request monitor", e);
            return null;
        }
    }


    private String buildRequestUrl(final EurekaEndpoint endpoint, String path) throws URISyntaxException {
        return new URIBuilder().setScheme(endpoint.isSecure() ? "https" : "http")
                .setHost(endpoint.getNetworkAddress())
                .setPort(endpoint.getPort())
                .setPath(path)
                .build().toString();
    }
}
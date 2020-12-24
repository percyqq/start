package org.minos.discover.client.eureka;

import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.resolver.EurekaEndpoint;
import org.minos.discover.shared.DiscoveryResponse;
import org.minos.discover.shared.DiscoveryTransformer;

import java.util.List;

import static org.minos.discover.client.eureka.ServerLongPollingResponse.Code.*;

/**
 * 服务器获取app更新数据返回结果封装
 *
 */
public class ServerLongPollingResponse {
    private final Code code;
    private final EurekaEndpoint server;
    private final long reqEpoch;
    private final long rcvEpoch;
    private final boolean delta;
    private final List<Application> applications;

    public ServerLongPollingResponse(Code code, EurekaEndpoint server, long reqEpoch, long rcvEpoch,
            List<Application> applications) {
        this.code = code;
        this.server = server;
        this.reqEpoch = reqEpoch;
        this.rcvEpoch = rcvEpoch;
        this.applications = applications;
        this.delta = false;
    }

    private ServerLongPollingResponse(Code code, EurekaEndpoint server, long reqEpoch, long rcvEpoch, boolean delta,
            List<Application> applications) {
        this.code = code;
        this.server = server;
        this.reqEpoch = reqEpoch;
        this.rcvEpoch = rcvEpoch;
        this.applications = applications;
        this.delta = delta;
    }

    public static ServerLongPollingResponse notSupportRsp(EurekaEndpoint server, long reqEpoch) {
        return new ServerLongPollingResponse(NOT_SUPPORT, server, reqEpoch, -1, null);
    }

    public static ServerLongPollingResponse errRsp(EurekaEndpoint server, long reqEpoch) {
        return new ServerLongPollingResponse(ERROR, server, reqEpoch, -1, null);
    }

    public static ServerLongPollingResponse okRsp(EurekaEndpoint server, long reqEpoch, boolean delta,
            DiscoveryResponse rsp) {
        if (rsp == null) {
            return new ServerLongPollingResponse(OK, server, reqEpoch, -1, delta, null);
        }

        if (reqEpoch == rsp.getEpoch()) {
            return new ServerLongPollingResponse(OK, server, reqEpoch, rsp.getEpoch(), delta, null);
        } else {
            return new ServerLongPollingResponse(OK, server, reqEpoch, rsp.getEpoch(), delta,
                    DiscoveryTransformer.toEureka(rsp.getApplications()));
        }
    }

    public static enum Code {
        OK,
        ERROR,
        NOT_SUPPORT,
    }

    public Code getCode() {
        return code;
    }

    public EurekaEndpoint getServer() {
        return server;
    }

    public long getReqEpoch() {
        return reqEpoch;
    }

    public long getRcvEpoch() {
        return rcvEpoch;
    }

    public boolean isDelta() {
        return delta;
    }

    public List<Application> getApplications() {
        return applications;
    }
}

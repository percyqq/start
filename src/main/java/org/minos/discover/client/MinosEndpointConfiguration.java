package org.minos.discover.client;

import com.netflix.discovery.EurekaClient;
import org.apache.http.entity.ContentType;
import org.minos.discover.client.common.JacksonUtils;
import org.minos.discover.client.loadbalancer.RibbonContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.minos.discover.client.common.LoggerUtils.CLIENT_LOGGER;

/**
 * 通用Web接口配置
 *
 * @date 2019/11/11
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureAfter({
        EurekaClientAutoConfiguration.class,
        MinosEurekaClientConfiguration.class,
})
@ConditionalOnBean({ServiceRegistry.class, Registration.class})
@Order(Ordered.LOWEST_PRECEDENCE)
public class MinosEndpointConfiguration {

    @Autowired(required = false)
    private ServiceRegistry serviceRegistry;
    @Autowired(required = false)
    private Registration registration;
    @Autowired(required = false)
    private EurekaClient eurekaClient;
    @Autowired(required = false)
    private SpringClientFactory ribbonContexts;


    @Bean
    public ServletRegistrationBean actuatorAppInstanceServlet() {
        return new ServletRegistrationBean(new DiscoveryInstanceServlet(), "/actuator/discovery/instance");
    }

    @Bean
    public ServletRegistrationBean actuatorStatusServlet() {
        return new ServletRegistrationBean(new ServerStatusServlet(), "/actuator/service/status");
    }

    @Bean
    public ServletRegistrationBean ribbonServlet() {
        return new ServletRegistrationBean(new RibbonServlet(), "/actuator/ribbon");
    }


    class ServerStatusServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            try {
                String status = req.getParameter("status");
                serviceRegistry.setStatus(registration, status);
                CLIENT_LOGGER.info("set client status {}", status);
                resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
                resp.getWriter().write("{\"code\":\"OK\"}");
                resp.getWriter().flush();
            } catch (Exception e) {
                CLIENT_LOGGER.warn("set client status failed", e);
                resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
                resp.getWriter().write("{\"code\":\"ERROR\"}");
                resp.getWriter().flush();
            }
        }
    }

    class DiscoveryInstanceServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            try {
                resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
                if (eurekaClient == null) {
                    resp.getWriter().write("{}");
                } else {
                    resp.getWriter().write(JacksonUtils.mapper.writeValueAsString(eurekaClient.getApplications().getRegisteredApplications()));
                }
                resp.getWriter().flush();

            } catch (Exception e) {
                CLIENT_LOGGER.info("/actuator/discovery/instance fail", e);
                resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
                resp.getWriter().write("{\"code\":\"ERROR\"}");
                resp.getWriter().flush();
            }
        }
    }

    class RibbonServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            try {
                resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
                if (ribbonContexts == null) {
                    resp.getWriter().write("{}");
                } else {
                    resp.getWriter().write(JacksonUtils.mapper.writeValueAsString(RibbonContextUtils.ribbonClientInfo(ribbonContexts)));
                }
                resp.getWriter().flush();

            } catch (Exception e) {
                CLIENT_LOGGER.info("/actuator/ribbon fail", e);
                resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
                resp.getWriter().write("{\"code\":\"ERROR\"}");
                resp.getWriter().flush();
            }
        }
    }
}

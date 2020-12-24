package org.minos.discover.client.http;

import org.apache.http.Header;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.minos.discover.client.common.JacksonUtils;
import org.minos.discover.client.common.TimeUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import static org.minos.discover.client.common.LoggerUtils.CLIENT_LOGGER;
import static org.minos.discover.client.common.LoggerUtils.HTTP_LOGGER;

/**
 * @date 2020/7/1 11:23
 */
public class HttpClient {
    private final static int DEFAULT_CONN_TIMEOUT_MS = 3000;
    private final CloseableHttpClient httpClient;

    public HttpClient(int timeout) {
        //加1秒，比真实超时多一点，避免时间冲突
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(DEFAULT_CONN_TIMEOUT_MS)
                .setSocketTimeout(TimeUtils.secToMilli(timeout + 1))
                .build();

        this.httpClient =
                HttpClientBuilder.create().setDefaultRequestConfig(defaultRequestConfig).build();
    }

    public <T> HttpResponse<T> execute(HttpRequest request, Class<T> clazz) {
        HttpUriRequest httpClientRequest = buildHttpClientRequest(request);
        HTTP_LOGGER.debug("http request: url: {}, header: {}", httpClientRequest.getURI().toString(), request.getHeaders());

        if (request.getHeaders() != null) {
            request.getHeaders().forEach(httpClientRequest::addHeader);
        }

        try (CloseableHttpResponse httpResponse = httpClient.execute(httpClientRequest)) {
            int statusCode = httpResponse.getStatusLine().getStatusCode();

            HttpResponse<T> response;
            if (HttpURLConnection.HTTP_OK == statusCode || HttpURLConnection.HTTP_NOT_MODIFIED == statusCode) {
                response = new HttpResponse<>(statusCode, JacksonUtils.read(httpResponse.getEntity().getContent(), clazz));
            } else {
                response = new HttpResponse<>(statusCode, null);
            }

            Map<String, String> headers = new HashMap<>();
            for (Header header : httpResponse.getAllHeaders()) {
                headers.put(header.getName(), header.getValue());
            }
            response.headers = headers;

            return response;
        }  catch (SocketTimeoutException e) {
            CLIENT_LOGGER.debug("request timeout");
            return new HttpResponse<>(HttpURLConnection.HTTP_UNAVAILABLE, null);
        } catch (ParseException | IOException e) {
            CLIENT_LOGGER.error("request failed ", e);
            return new HttpResponse<>(HttpURLConnection.HTTP_INTERNAL_ERROR, null);
        }
    }

    private HttpUriRequest buildHttpClientRequest(HttpRequest request) {
        switch (request.getMethod()) {
            case "GET":
                return new HttpGet(request.buildUrl());
            case "POST":
                return new HttpPost(request.buildUrl());
            case "PUT":
                return new HttpPut(request.buildUrl());
            default:
                throw new NotSupportMethodException(request.getMethod());
        }
    }
}

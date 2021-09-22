package org.minos.discover.client.http;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @date 2020/7/1 13:27
 */
public class HttpRequest {
    private String url;
    private String method = "GET";
    private Map<String, String> params;
    private Map<String, String> headers;

    public HttpRequest(String url) {
        this.url = url;
    }

    public HttpRequest(String url, String method, Map<String, String> params, Map<String, String> headers) {
        this.url = url;
        this.method = method;
        this.params = params;
        this.headers = headers;
    }

    public void addParam(String key, String value) {
        if (value == null || value.length() == 0) {
            return;
        }

        if (params == null) {
            params = new HashMap<>();
        }

        params.put(key, value);
    }

    public void addHeader(String key, String value) {
        if (value == null || value.length() == 0) {
            return;
        }

        if (headers == null) {
            headers = new HashMap<>();
        }

        headers.put(key, value);
    }

    public String buildUrl() {
        StringBuilder sb = new StringBuilder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (StringUtils.isEmpty(entry.getValue())) {
                    continue;
                }

                sb.append(entry.getKey()).append("=");
                sb.append(entry.getValue());
                sb.append("&");
            }
        }

        if (sb.length() > 0) {
            sb = sb.deleteCharAt(sb.length() - 1);
        }
        return this.url + "?" + sb.toString();
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getMethod() {
        return method;
    }
}

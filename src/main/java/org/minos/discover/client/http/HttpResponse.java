package org.minos.discover.client.http;

import java.util.Map;

/**
 * @date 2020/7/1 13:27
 */
public class HttpResponse<T> {
    public int code;
    public T data;
    public Map<String, String> headers;

    public HttpResponse(int code, T data) {
        this.code = code;
        this.data = data;
    }


    public String getHeader(String key) {
        if (headers == null) {
            return null;
        }
        return headers.get(key);
    }
}

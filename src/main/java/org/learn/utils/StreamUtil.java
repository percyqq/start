package org.learn.utils;

import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;

/**
 * @create: 2020-07-24 11:36
 */
public class StreamUtil {
    public static void close(CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

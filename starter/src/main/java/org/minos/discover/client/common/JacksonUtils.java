package org.minos.discover.client.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * @date 2020/7/1 14:09
 */
public class JacksonUtils {
    public static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static <T> T read(InputStream stream, Class<T> clazz) throws IOException {
        return mapper.readValue(stream, clazz);
    }
}
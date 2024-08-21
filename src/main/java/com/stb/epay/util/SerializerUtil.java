package com.stb.epay.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.stb.epay.objects.Constants.LogFormat.ERROR_EXCEPTION_FORMAT;

@Slf4j
public class SerializerUtil {
    private SerializerUtil() {

    }

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.registerModules(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static <T> T deserialize(String contentType, String obj, Class<T> clazz) {
        try {
            return mapper.readValue(obj, clazz);

        } catch (Exception e) {
            log.error(ERROR_EXCEPTION_FORMAT, e.toString());
            return null;
        }
    }

    public static <T> T deserialize(String obj, Class<T> clazz) {
        return deserialize(null, obj, clazz);
    }

    public static <T> String serialize(String contentType, T obj) {
        try {
            return mapper.writeValueAsString(obj);

        } catch (Exception e) {
            log.error(ERROR_EXCEPTION_FORMAT, e.toString());
            return null;
        }
    }

    public static <T> String serialize(T obj) {
        return serialize(null, obj);
    }

    public static class StringTrimModule extends SimpleModule {

        public StringTrimModule() {
            addDeserializer(String.class, new StdDeserializer<String>(String.class) {
                @Override
                public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                    return jsonParser != null && jsonParser.getValueAsString() != null ? jsonParser.getValueAsString().trim() : "";
                }
            });
        }

    }
}

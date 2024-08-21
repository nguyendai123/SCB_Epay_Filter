package com.stb.epay.caches;

import com.stb.epay.config.RedisFactory;
import com.stb.epay.objects.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

import static com.stb.epay.lib.common.CommonUtil.trim;
import static com.stb.epay.lib.crypto.AESUtils.*;

@Slf4j
@Data
public abstract class BaseCaches<T> {

    private final String DPK_KEY = "DPK_KEY_12345678";
    private final Class<T> clazz;
    private String prefixKey = Constants.PREFIX_DEFAULT_CACHE;
    private int duration = Integer.MAX_VALUE;
    private ObjectMapper objectMapper;
    private final Map<String, String> localCache = new HashMap<>();
    private boolean useRedis = false;
    private boolean isEncrypt = false;
    private RedisFactory redisFactory;


    @SuppressWarnings("unchecked")
    protected BaseCaches() {
        this.clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected BaseCaches(Class<T> clazz) {
        this.clazz = clazz;
    }


    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Autowired
    public final void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RedisFactory getRedisFactory() {
        return redisFactory;
    }

    @Autowired
    public final void setRedisFactory(RedisFactory redisFactory) {
        this.redisFactory = redisFactory;
    }

    @SuppressWarnings("unchecked")
    public T get(String key) {
        key = getKey(key);
        if (!StringUtils.hasText(key)) {
            return null;
        }
        return !useRedis ? (T) getObject(localCache.get(key), key) :
                (T) getObject(redisFactory.redisCommands().get(key), key);

    }


    public T add(String key, T obj) {
        return add(key, obj, duration);
    }

    public T add(String key, T obj, long durationValue) {
        key = getKey(key);
        try {
            String value = this.clazz == String.class ? (String) obj : objectMapper.writeValueAsString(obj);
            if (!useRedis) {
                localCache.put(key, value);
            } else {
                redisFactory.redisCommands().setex(key, durationValue,
                        isEncrypt ? encrypt(value, DPK_KEY, key) : value);
            }
            return obj;
        } catch (Exception ex) {
            log.error(">>> ", ex);
            return null;
        }
    }

    public boolean remove(String key) {
        key = getKey(key);
        try {
            if (!useRedis) {
                localCache.remove(key);
            } else {
                redisFactory.redisCommands().del(key);
            }
            return true;

        } catch (Exception ex) {
            log.error(">>> ", ex);
        }
        return false;
    }


    private Object getObject(String data, String key) {
        try {
            if (useRedis && isEncrypt) {
                data = decrypt(data, DPK_KEY, key);
            }

            if (clazz == String.class) {
                return data;
            }
            return data != null ? objectMapper.readValue(data, clazz) : null;
        } catch (Exception ex) {
            log.error(">>> ", ex);
            return null;
        }
    }

    private String getKey(String key) {
        key = trim(key);
        try {
            if (!useRedis) return prefixKey + key;
            return prefixKey + (isEncrypt ? getKeyCache(key) : key);
        } catch (Exception ex) {
            log.error(">>> ", ex);
            return null;
        }
    }
}

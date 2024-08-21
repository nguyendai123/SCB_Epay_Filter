package com.stb.epay.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.support.ConnectionPoolSupport;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@Slf4j
public class RedisFactory {

    @Value("${cache.redis.username:}")
    private String username;
    @Value("${cache.redis.password:}")
    private String password;
    @Value("${cache.redis.host:redis}")
    private String host;
    @Value("${cache.redis.port:6379}")
    private int port;

    @Value("${cache.redis.minIdle:0}")
    private int minIdle;

    @Value("${cache.redis.maxIdle:8}")
    private int maxIdle;

    @Value("${cache.redis.maxTotal:8}")
    private int maxTotal;

    private RedisClient redisClient;

    private GenericObjectPool<StatefulRedisConnection<String, String>> pool;

    @PostConstruct
    private void init() {
        RedisURI.Builder client = RedisURI
                .Builder
                .redis(host, port);
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            client.withAuthentication(username, password.toCharArray());
        }
        redisClient = RedisClient.create(client.build());
        settingConnectionPool(redisClient);
    }

    @PreDestroy
    public void preDestruy() {
        pool.close();
        redisClient.shutdown();
    }

    private void settingConnectionPool(RedisClient redisClient) {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMaxTotal(maxTotal);

        pool = ConnectionPoolSupport.createGenericObjectPool(
                redisClient::connect, poolConfig
        );
    }

    public RedisCommands<String, String> redisCommands() {
        return getRedisConnection().sync();
    }


    private StatefulRedisConnection<String, String> getRedisConnection() {
        StatefulRedisConnection<String, String> redisCommands = null;
        try {
            redisCommands = pool.borrowObject();
            if (redisCommands == null) {
                throw new RuntimeException("Pool redis error");
            }
            return redisCommands;

        } catch (Exception ex) {
            throw new RuntimeException("Pool redis error");
        } finally {
            if (redisCommands != null) {
                pool.returnObject(redisCommands);
            }
        }
    }
}

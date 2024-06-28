package com.ewallet.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Slf4j
public class BeansConfig {

    @Value("${async.executor.corePoolSize:3}")
    private int corePoolSize;

    @Value("${async.executor.maxPoolSize:500}")
    private int maxPoolSize;

    @Value("${async.executor.queueCapacity:2000}")
    private int queueCapacity;

    @Value("${async.executor.threadNamePrefix:'AsyncThread-'}")
    private String threadNamePrefix;

    @Value("${async.executorLog.corePoolSize:3}")
    private int corePoolSizeLog;

    @Value("${async.executorLog.maxPoolSize:500}")
    private int maxPoolSizeLog;

    @Value("${async.executorLog.queueCapacity:2000}")
    private int queueCapacityLog;

    @Value("${async.executorLog.threadNamePrefix:'AsyncThreadLog-'}")
    private String threadNamePrefixLog;


    @Bean(name = "customObjectMapper")
    public ObjectMapper getObjectMapper() {
        var mapper = new ObjectMapper()
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.registerModules(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        log.info("asyncExecutor: corePoolSize-{} : maxPoolSize-{} : queueCapacity-{} : threadNamePrefix: {}",
                corePoolSize, maxPoolSize, queueCapacity, threadNamePrefix);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }

    @Bean(name = "asyncLogExecutor")
    public ThreadPoolTaskExecutor asyncLogExecutor() {
        log.info("asyncLogExecutor: corePoolSizeLog-{} : maxPoolSizeLog-{} : queueCapacityLog-{} : threadNamePrefixLog: {}",
                corePoolSizeLog, maxPoolSizeLog, queueCapacityLog, threadNamePrefixLog);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSizeLog);
        executor.setMaxPoolSize(maxPoolSizeLog);
        executor.setQueueCapacity(queueCapacityLog);
        executor.setThreadNamePrefix(threadNamePrefixLog);
        executor.initialize();
        return executor;
    }

    @Bean(name = "customPrinter")
    public JsonFormat.Printer customPrinter() {
        return JsonFormat.printer().omittingInsignificantWhitespace();
    }

}

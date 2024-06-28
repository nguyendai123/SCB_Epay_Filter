package com.ewallet;

import com.ewallet.config.RedisFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class FilterConfigApplication implements CommandLineRunner {

    private final RedisFactory redisFactory;

//    public static void main(String[] args) {
//        SpringApplication.run(FilterConfigApplication.class, args);
//    }

    @Override
    public void run(String... args) throws Exception {
        redisFactory.redisCommands().setex("test", 10000, "ok");
        log.info("data redis test : {}",redisFactory.redisCommands().get("test"));    }
}

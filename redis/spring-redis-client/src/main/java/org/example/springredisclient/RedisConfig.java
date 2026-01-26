package org.example.springredisclient;

import io.lettuce.core.ReadFrom;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    public LettuceConnectionFactory lettuceConnectionFactory;


    public LettuceConnectionFactory gracefulShutdown(){

        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                .shutdownQuietPeriod(Duration.ZERO)  // Shutdown 요청 대기 시간
                .shutdownTimeout(Duration.ofMillis(100))      // Shutdown 최대 대기 시간
                .build();

        RedisClusterConfiguration clusterRedisConfiguration = new RedisClusterConfiguration();


        return new LettuceConnectionFactory(clusterRedisConfiguration, clientConfiguration)
    }

    public LettuceConnectionFactory readForm(){

        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .build();

        RedisClusterConfiguration clusterRedisConfiguration = new RedisClusterConfiguration();


        return new LettuceConnectionFactory(clusterRedisConfiguration, clientConfiguration)
    }
}

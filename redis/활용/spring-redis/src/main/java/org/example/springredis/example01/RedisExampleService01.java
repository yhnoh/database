package org.example.springredis.example01;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@RequiredArgsConstructor
public class RedisExampleService01 {

    private final StringRedisTemplate stringRedisTemplate;

    public void set(String key, String value) {
        BoundValueOperations<String, String> operations = stringRedisTemplate.boundValueOps(key);
        operations.set(value);
    }

    public String get(String key) {
        BoundValueOperations<String, String> operations = stringRedisTemplate.boundValueOps(key);
        return operations.get();
    }

}

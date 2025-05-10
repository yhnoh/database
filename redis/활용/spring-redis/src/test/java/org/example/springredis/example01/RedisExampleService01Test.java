package org.example.springredis.example01;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisExampleService01Test {

    @Autowired
    private RedisExampleService01 redisExampleService01;

    @Test
    public void setGetTest() {

        redisExampleService01.set("hello", "world");

        assertThat(redisExampleService01.get("hello")).isEqualTo("world");
    }

}
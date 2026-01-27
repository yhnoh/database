package org.example.springredisclient;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public class RedisSerializerTest {


    public RedisTemplate<String, String> createStringRedisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    public RedisTemplate<String, Object> createJsonRedisTemplate(LettuceConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, Object.class));
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, Object.class));
        return redisTemplate;
    }


    @Test
    void stringSerializeTest() {

        StringRedisSerializer serializer = new StringRedisSerializer();
        User user1 = new User("user1", 10);

        byte[] serialize = serializer.serialize(user1.toString());

        System.out.println("serialize = " + new String(serialize));
    }

    @Test
    void jsonSerializeTest() {
        ObjectMapper objectMapper = new ObjectMapper();
        Jackson2JsonRedisSerializer<User> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, User.class);

        User user1 = new User("user1", 10);

        byte[] serialize = serializer.serialize(user1);
        System.out.println("serialize = " + new String(serialize));
    }

    @Test
    void jsonSerializeClassCastExceptionTest() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 다양한 타입을 처리하기 위하여 Object.class 사용
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        User user1 = new User("user1", 10);

        byte[] serialize = serializer.serialize(user1);
        // 역직렬화 시 ClassCastException 발생
        User deserialize = (User) serializer.deserialize(serialize);
        System.out.println("deserialize = " + deserialize);
    }

    @Test
    void genericJsonSerializeTest() {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        serializer.configure(objectMapper -> {
            // 추가 설정 가능
        });

        User user1 = new User("user1", 10);

        byte[] serialize = serializer.serialize(user1);

        System.out.println("serialize = " + new String(serialize));
    }

    @Test
    void genericJsonSerializeTypeTest() {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        UserJsonType userJsonType = new UserJsonType("user1", 10);

        byte[] serialize = serializer.serialize(userJsonType);

        System.out.println("serialize = " + new String(serialize));

        UserJsonType deserialize = serializer.deserialize(serialize, UserJsonType.class);
        Assertions.assertEquals(UserJsonType.class, deserialize.getClass());
    }


    @Getter
    static class User {

        private String name;
        private int age;

        public User() {
        }

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    /**
     * @JsonTypeInfo와 @JsonSubTypes를 사용하여 @type 필드로 변경 및 값을 user로 지정
     */
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = UserJsonType.class, name = "user")
    })
    @Getter
    public static class UserJsonType {
        private String name;
        private int age;

        public UserJsonType() {
        }

        public UserJsonType(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

}

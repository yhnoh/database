




## Redis Client Graceful Shutdown
- Redis Client를 사용하는 애플리케이션을 종료할 때, Redis Client의 연결을 정상적으로 종료하는 것이 중요하다.
  - 만약 애플리케이션 종료 시점에 Redis Client의 연결이 강제로 종료되면, Redis 서버와의 연결이 비정상적으로 종료되거나, 요청한 데이터의 손실이 일어날 수 있다.
- Spring Data Data 에서 제공하는 LettuceConnectionFactory 는 애플리케이션 종료 시점에 Redis Client의 연결을 정상적으로 종료할 수 있도록 지원한다.
  - `LettuceConnectionFactory`는 Spring Bean의 `destroy` 메서드에서 해당 설정을 사용하여 연결을 종료한다. 즉 Spring Context가 Bean을 소멸시키는 시점에 해당 설정이 적용된다.
- Spring 에서는 많은 부분에서 Graceful Shutdown 을 지원하여, 빈이 소멸되기전에 작업을 최대한 마무리할 수 있도록 돕는다. 때문에 Redis Client의 연결 종료 오류가 발생하지 않을 확률이 높다.
  - 하지만 관련 오류가 발생하였을때, LettuceClientConfiguration 에서 Graceful Shutdown 설정을 조정하여 문제를 해결할 수 있다는 것을 알아두는 것이 좋다.

### LettuceClientConfiguration 에서 Graceful Shutdown 설정하기
```java
LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
        .shutdownQuietPeriod(Duration.ZERO)  // Shutdown 요청 대기 시간, 기본값: 0ms
        .shutdownTimeout(Duration.ofMillis(100))      // Shutdown 최대 대기 시간, 기본값: 100ms
        .build();
```
- `shutdownQuietPeriod`: 요청 대기 시간 설정
  - 해당 시간 동안 새로운 요청이 들어오지 않으면, 더이상 요청을 받지 않고 연결을 종료한다. 만약 새로운 요청이 들어오면, 다시 대기 시간을 초기화한다.
- `shutdownTimeout`: 최대 대기 시간 설정
  - 해당 시간 동안 연결 종료가 완료되지 않으면, 강제로 연결을 종료한다.



## 복제 지연(Replication Lag)으로 인한 데이터 불일치 문제 해결하기
- Redis Master-Slave 구조에서는 Master 노드에서 데이터가 변경되면 해당 변경 사항이 Slave 노드로 복제된다.
- Redis가 Master-Slave 구조를 지원하는 이유는 주로 읽기 성능 향상과 데이터 가용성 확보를 위함이다. 특히 읽기 작업이 많은 애플리케이션에서는 Slave 노드에서 읽기 작업을 수행함으로써 Master 노드의 부하를 줄일 수 있다.
- 하지만 Redis의 복제 방식은 비동기적으로 이루어지기 때문에, Slave 노드가 Master 노드의 최신 데이터를 즉시 반영하지 못하는 복제 지연이 발생할 수 있다.
- 때문에 데이터의 일관성이 중요한 애플리케이션에서 읽기 작업을 Slave 노드에서 수행할 경우, 복제 지연으로 인하여 잘못된 데이터를 읽어올 위험이 있다.
- 때문에 각 상황별로 적절한 읽기 노드 선택 전략을 수립하는 것이 중요하며, `LettuceConnectionFactory`는 이를 위하여 `ReadFrom` 설정을 제공한다.
  - 만약 데이터 일관성이 매우 중요한 애플리케이션이라면, 모든 읽기 작업을 Master 노드에서 수행하도록 설정하는 것이 좋다.
  - 반면에 읽기 성능이 더 중요하고, 약간의 데이터 불일치가 허용되는 경우에는 Slave 노드에서 읽기 작업을 수행하도록 설정할 수 있다.

### LettuceClientConfiguration에서 ReadFrom 설정하기
```java
LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
        .readFrom(ReadFrom.REPLICA_PREFERRED)  // 읽기 노드 선택 전략 설정
        .build();
```

- REPLICA_PREFERRED: 가능하면 Slave 노드에서 읽기 작업을 수행하고, Slave 노드가 사용 불가능한 경우에만 Master 노드에서 읽기 작업을 수행한다.
- ANY_PREFERRED: 가능하면 어떤 노드에서든 읽기 작업을 수행하고, Slave 노드가 사용이 불가하면 Master 노드에서 읽기 작업을 수행한다.
- UPSTREAM/MASTER: 항상 Master 노드에서 읽기 작업을 수행한다.
- 이외에도 다양한 읽기 노드 선택 전략이 존재한다. 각 전략의 특징과 사용 사례에 대해서는 아래 링크를 참고하자.

> https://redisgate.kr/redis/clients/springboot_readfrom.php
> https://github.com/redis/lettuce/wiki/ReadFrom-Settings


## Serialization
- Redis는 기본적으로 문자열 형태로 데이터를 저장한다.
    - 실제로는 바이트 코드로 저장되지만, 사람이 읽을 수 있는 형태로 표현하면 문자열이다. 바이트 코드는 다양한 데이터 타입을 표현할 수 있지만, ***일반적으로 Redis Client 라이브러리는 문자열로 인코딩/디코딩***한다.
- 따라서 객체를 다루는 애플리케이션에서는 객체를 그대로 Redis에 저장할 수 없고, 객체를 Redis가 이해할 수 있는 형식으로 변환해야 한다.
- 객체를 문자열로 변환하는 과정에서 가장 널리 사용되는 방법은 JSON 직렬화이다. JSON 직렬화는 객체를 JSON 문자열로 변환하여 저장하는 방식으로, 다양한 프로그래밍 언어에서 지원된다.
- Spring Data Redis에서는 다양한 직렬화/역직렬화 방식을 제공하며, `RedisSerializer` 인터페이스를 구현한 여러 클래스를 이용하여 직렬화/역직렬화를 수행하게 되며, `RedisTemplate`에서 이를 설정하여 사용할 수 있다. 

### 주요 RedisSerializer 종류

#### StringRedisSerializer
- 문자열을 그대로 저장한다. 주로 키(key)와 값(value)이 모두 문자열인 경우에 사용된다.
- 객체를 저장할때는 `toString()` 메서드를 호출하여 문자열로 변환한 후 직렬화한다. 때문에 객체의 필드 값을 직렬화하는 것이 아니라, 객체의 메모리 주소를 나타내는 문자열이 저장된다.

```java
StringRedisSerializer serializer = new StringRedisSerializer();
User userJsonType = new User("userJsonType", 10);

byte[] serialize = serializer.serialize(userJsonType.toString());

// serialize = org.example.springredisclient.RedisSerializerTest$User@389c4eb1
System.out.println("serialize = " + new String(serialize));
```

#### Jackson2JsonRedisSerializer
- Jackson 라이브러리를 사용하여 JSON 직렬화한다. 객체 생성시, 클래스 타입 정보를 셋팅해주어야 하며 지정된 클래스 타입으로만 직렬화/역직렬화가 가능하다.

```java
ObjectMapper objectMapper = new ObjectMapper();
Jackson2JsonRedisSerializer<User> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, User.class);

User userJsonType = new User("userJsonType", 10);

byte[] serialize = serializer.serialize(userJsonType);
// serialize = {"name":"userJsonType","age":10}
System.out.println("serialize = " + new String(serialize));
```

- 하지만 정해진 클래스 타입으로만 직렬화/역직렬화가 가능하기 때문에, 다양한 클래스 타입의 객체를 저장해야 하는 경우에는 적합하지 않다.
- 예를 들어서 Spring 에서는 RedisTemplate을 빈으로 등록하고 이를 다양한 곳에서 주입받아 사용하는데, 이때 여러 종류의 객체를 저장해야 한다면 Jackson2JsonRedisSerializer는 적합하지 않다.

```java
ObjectMapper objectMapper = new ObjectMapper();
// 다양한 타입을 처리하기 위하여 Object.class 사용
Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

User userJsonType = new User("userJsonType", 10);

byte[] serialize = serializer.serialize(userJsonType);
// 역직렬화 시 ClassCastException 발생
User deserialize = (User) serializer.deserialize(serialize);
```

위 코드에서 다양한 타입을 처리하기 위해서 Object.class를 사용하였지만, 역직렬화 시점에 ClassCastException이 발생한다. <br/>
이는 ObjectMapper가 역직렬화 시점에 클래스 타입 정보를 알 수 없기 때문에 Json 문자열을 LinkedHashMap으로 변환했기 때문이다. <br/> 
이로 인하여 User 타입으로 캐스팅할 수 없게 된다.

#### GenericJackson2JsonRedisSerializer

Jackson 라이브러리를 사용하여 JSON 직렬화한다. 직렬화시, 클래스 타입 정보를 함께 저장하여, 다양한 클래스 타입의 객체를 직렬화/역직렬화할 수 있다.

```java
// 내부적으로 ObjectMapper에서 DefaultTyping 설정을 사용
GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
serializer.configure(objectMapper -> {
    // 추가 설정 가능
});

User user = new User("user1", 10);

byte[] serialize = serializer.serialize(user);

// serialize = {"@class":"org.example.springredisclient.RedisSerializerTest$User","name":"user1","age":10}
System.out.println("serialize = " + new String(serialize));
```

역직렬화 시점에 `@class` 필드에 클래스 타입 정보를 함께 저장하기 때문에, 다양한 클래스 타입의 객체를 직렬화/역직렬화할 수 있다. <br/>
따라서 Spring 에서 RedisTemplate을 빈으로 등록하고, 이를 다양한 곳에서 주입받아 사용하는 경우에 적합하다. <br/>
하지만 몇가지 단점이 존재한다.
1. 리팩토링을 통해서 패키지 이동이나 클래스 이름 변경이 발생할 경우, 역직렬화가 실패할 수 있다. 
   - 예를 들어 `CacheManager`에서 `GenericJackson2JsonRedisSerializer`를 사용할 경우, 캐싱 대상이 되는 클래스 타입이나 패키지가 변경되면 역직렬화가 실패할 수 있다.
   - 이를 보완하기 위해서는 별도의 타입 매핑 전략을 수립하거나, 클래스 이름 변경 시점에 데이터 마이그레이션 작업이 필요하다.
2. 데이터의 크기가 커질 수 있다.
    - 클래스 타입 정보를 함께 저장하기 때문에, 직렬화된 데이터의 크기가 커질 수 있다. 이는 네트워크 전송 비용과 저장 공간에 영향을 미칠 수 있다.
3. Java 이외의 프로그램에서는 `@class` 필드를 해석할 수 없다.
    - 만약 Redis에 저장된 데이터를 Java 이외의 프로그램에서 읽어야 하는 경우, `@class` 필드를 해석할 수 없기 때문에 불필요한 정보가 포함된 JSON 데이터를 처리해야 한다.

만약 캐시 대상이 되는 클래스의 패키지 이동이나 클래스 이름 변경이 발생할 경우에는 어떻게 대처하는 것이 좋을까?
1. **타입 별칭 사용** <br/>
직렬화 시점에 클래스 타입 정보를 별칭(alias)으로 매핑하여 저장하는 방법이다. @JsonTypeInfo 어노테이션과 @JsonSubTypes 어노테이션을 사용하여, 클래스 타입 정보를 별칭으로 매핑할 수 있다. <br/>
참고로 위 어노테이션은 Json의 다형성(Polymorphism)을 지원하기 위한 어노테이션이다.
```java
    
GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

UserJsonType userJsonType = new UserJsonType("user1", 10);

byte[] serialize = serializer.serialize(userJsonType);
// serialize = {"@type":"user","name":"user1","age":10}
System.out.println("serialize = " + new String(serialize));

UserJsonType deserialize = serializer.deserialize(serialize, UserJsonType.class);
Assertions.assertEquals(UserJsonType.class, deserialize.getClass());

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
```
위 코드에서 어노테이션을 사용하여 `@type` 필드로 변경하고, 값을 `user`로 지정하였다. 이로 인해서 클래스의 이름이 변경되거나, 패키지가 이동하더라도, `@type` 필드와 값이 동일하다면 역직렬화가 정상적으로 수행된다. <br/>
이를 활용하여 운영 환경에서도 별칭이 변경되지 않는한 자유롭게 리팩토링이 가능하다. 또한 MSA 환경에서 다른 서비스 간에 동일한 별칭을 사용하여 데이터를 공유할 수 도 있다. <br/>
하지만 별칭이 중복되지 않도록 주의해야 하고, 쉽게 변경되지 않아야 하며, 별칭 관리에 대한 정책이 필요하다. 또한 MSA 환경에서 별칭을 공유하게 될경우 서비스 간의 강한 결합이 발생할 수 있다. <br/>

2. **데이터 마이그레이션** <br/>




> [망나니개발자 > 스프링이 제공하는 레디스 직렬화/역직렬화(Redis Serializer/Deserializer)의 종류와 한계](https://mangkyu.tistory.com/402)


## Redisson

Redisson은 Redis와 Valkey를 지원하는 Java 기반의 오픈소스 클라이언트 라이브러리이다. <br/>
Lettuce, Jedis와 같은 Redis 클라이언트 라이브러리와 유사항 기능을 제공하지만, Redis에서 기본적으로 제공해주지 않는 추가 기능들을 제공한다. <br/>

- 자바 컬렉션을 통해서 구현한 분산 컬렉션 구현체 제공 (예: RMap, RSet, RList 등..)
- 자바 동기화 도구를 통해서 구현한 분산 동기화 구현체 제공 (예: RLock, RReadWriteLock 등...)
- 자바 고수준 스레드 API를 통해 구현한 분산 ExecutorService 제공 (예: RExecutorService,  RScheduledExecutorService 등...)
- Redisson Pro를 통한 고급 기능 제공 (예: Data partitioning, Queues, Reliable Pub/Sub 등..)

자바와 동일한 인터페이스 및 스프링 통합 환경을 제공하며, 2개 이상의 분산 환경에서 ***Redis를 공유 자원으로 활용하여 다양한 분산 시스템을 구축할 수 있도록 돕는다.*** <br/>

### Distributed Lock
Redisson은 자바 Lock 인터페이스를 구현한 RLock 클래스를 제공하여, 분산 환경에서의 락(lock) 기능을 지원한다. <br/>
보통 애플리케이션 서버의 경우 고가용성을 위하여 단일 서버가 아닌 여러대의 서버로 구성되어 있는 경우가 많다. <br/>
이러한 환경에서 Java의 동기화 기법만으로는 원격 데이터베이스에 존재하는 공유 자원에 대한 동기화 문제를 해결할 수 없다. <br/>
Distributed Lock은 여러 애플리케이션 서버에서 동시에 접근하는 공유 자원에 대한 동기화 문제를 해결해주며, 자바와 동일한 인터페이스를 제공해준다는 큰 장점이 있다. <br/>

```java
// 스레드 100개가 동시에 Counter 증가 시도
int iter = 100;
Counter counter = new Counter();
Counter lockCounter = new Counter();

ArrayList<Thread> threads = new ArrayList<>();
ArrayList<Thread> lockThreads = new ArrayList<>();

for (int i = 0; i < iter; i++) {
    // Lock 없이 카운터 증가
    Thread thread = new Thread(() -> {
        try {
            Thread.sleep(100);
            counter.increment();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    });
    threads.add(thread);

    // Distributed Lock을 사용한 카운터 증가
    Thread lockThread = new Thread(() -> {

        try {
            RLock lock = redissonClient.getLock("lock");
            // Uses pub/sub channel to notify other threads across all Redisson instances waiting to acquire a lock.
            boolean isAcquired = lock.tryLock(100000, 1000, TimeUnit.MILLISECONDS);
            if (isAcquired) {
                try {
                    Thread.sleep(100);
                    lockCounter.increment();
                } finally {
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        lockThreads.add(new Thread(() -> {

        }));
    });
    lockThreads.add(lockThread);

}

for (int i = 0; i < iter; i++) {
    threads.get(i).start();
    lockThreads.get(i).start();
}

for (int i = 0; i < iter; i++) {
    threads.get(i).join();
    lockThreads.get(i).join();
}

// "count = 93"
System.out.println("count = " + counter.getCount());
// "lock count = 100"
System.out.println("lock count = " + lockCounter.getCount());
```

해당 코드는 100개의 스레드가 동시에 Counter 객체의 increment 메서드를 호출하여 카운터를 증가시키는 예제이다. <br/>
첫번째 카운터는 Distributed Lock을 사용하지 않고, 두번째 카운터는 Distributed Lock을 사용하여 카운터를 증가시킨다. <br/>
실행 결과를 보면 Distributed Lock을 사용하지 않은 카운터는 100이 아닌 93이 출력되는 반면, Distributed Lock을 사용한 카운터는 100이 출력되는 것을 확인할 수 있다. <br/>
이는 Distributed Lock을 사용하지 않은 경우, 여러 스레드가 동시에 공유 자원에 접근하여 동시성 이슈가 발생했기 때문이다. <br/>
반면에 Distributed Lock을 사용한 경우, 여러 스레드가 동시에 공유 자원에 접근하지 못하도록 막아주었기 때문에, 동시성 이슈가 발생하지 않았다. <br/>

#### Lock
- RLock은 Redis의 Pub/Sub 기능을 활용하여, 여러 애플리케이션 서버에서 동시에 접근하는 공유 자원에 대한 동기화 문제를 해결해준다.


#### ReadWriteLock


#### MuliLock


> [Redisson](https://redisson.pro/docs/overview/)
## Data Type

#### PUB/SUB



### 모니터링 및 관리




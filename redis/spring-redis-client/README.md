




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


### Serialization
- Redis는 기본적으로 문자열 형태로 데이터를 저장한다.
    - 실제로는 바이트 코드로 저장되지만, 사람이 읽을 수 있는 형태로 표현하면 문자열이다. 바이트 코드는 다양한 데이터 타입을 표현할 수 있지만, ***일반적으로 Redis Client 라이브러리는 문자열로 인코딩/디코딩***한다.
- 따라서 객체를 다루는 애플리케이션에서는 객체를 그대로 Redis에 저장할 수 없다.
-
- 따라서 객체를 Redis에 저장하려면 Redis가 이해할 수 있는 형식으로 직렬화(Serialization)해야 한다.

> [망나니개발자 > 스프링이 제공하는 레디스 직렬화/역직렬화(Redis Serializer/Deserializer)의 종류와 한계](https://mangkyu.tistory.com/402)


### Redisson


### 사용할만한 자료구조

#### PUB/SUB



### 모니터링 및 관리




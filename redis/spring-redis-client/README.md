

### Serialization
- Redis는 기본적으로 문자열 형태로 데이터를 저장한다.
  - 실제로는 바이트 코드로 저장되지만, 사람이 읽을 수 있는 형태로 표현하면 문자열이다. 바이트 코드는 다양한 데이터 타입을 표현할 수 있지만, ***일반적으로 Redis Client 라이브러리는 문자열로 인코딩/디코딩***한다.
- 따라서 객체를 다루는 애플리케이션에서는 객체를 그대로 Redis에 저장할 수 없다.
- 
- 따라서 객체를 Redis에 저장하려면 Redis가 이해할 수 있는 형식으로 직렬화(Serialization)해야 한다.

> [망나니개발자 > 스프링이 제공하는 레디스 직렬화/역직렬화(Redis Serializer/Deserializer)의 종류와 한계](https://mangkyu.tistory.com/402)

### 복제 지연(Replication Lag)으로 인한 데이터 불일치 문제 해결하기
- Redis Master-Slave 구조에서는 Master 노드에서 데이터가 변경되면 해당 변경 사항이 Slave 노드로 복제된다.
- 이러한 복제 방식은 비동기적으로 이루어지기 때문에, Slave 노드가 Master 노드의 최신 데이터를 즉시 반영하지 못하는 복제 지연이 발생할 수 있다.
- 때문에 데이터의 일관성이 중요한 애플리케이션에서 읽기 작업을 Slave 노드에서 수행할 경우, 복제 지연으로 인하여 잘못된 데이터를 읽어올 위험이 있다.
- 때문에 각 상황별로 적절한 읽기 노드 선택 전략을 수립하는 것이 중요하다.



> https://redisgate.kr/redis/clients/springboot_readfrom.php
> https://github.com/redis/lettuce/wiki/ReadFrom-Settings


### Redisson


### 사용할만한 자료구조

### 모니터링 및 관리




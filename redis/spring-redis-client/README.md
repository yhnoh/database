

### Serialization
- 


### 복제 지연(Replication Lag)으로 인한 데이터 불일치 문제 해결하기
- Redis Master-Slave 구조에서는 Master 노드에서 데이터가 변경되면 해당 변경 사항이 Slave 노드로 복제된다.
- 이러한 복제 방식은 비동기적으로 이루어지기 때문에, Slave 노드가 Master 노드의 최신 데이터를 즉시 반영하지 못하는 복제 지연이 발생할 수 있다.
- 때문에 데이터의 일관성이 중요한 애플리케이션에서 읽기 작업을 Slave 노드에서 수행할 경우, 



> https://redisgate.kr/redis/clients/springboot_readfrom.php
> https://github.com/redis/lettuce/wiki/ReadFrom-Settings


### Redisson


### 사용할만한 자료구조

### 모니터링 및 관리




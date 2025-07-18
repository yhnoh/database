## Redis Replication
- Redis는 마스터-슬레이브 구조의 복제를 지원한다.
- 복제본이 필요한 이유는 다음과 같다.
  - 마스터 노드가 다운되었을때 복제본을 마스터로 승격시켜서 서비스 연속성을 유지하기 위함
  - 읽기 작업을 복제본에서 처리하여 마스터 노드의 부하를 줄이기 위함
  - 백업을 복제본에서 수행하도록 하여 마스터 노드의 부하를 줄이기 위함
- Redis의 마스터 노드는 하나만 존재하면, 여러 개의 복제본을 가질수 있다. 마스터 노드만이 데이터 쓰기/읽기 작업이 가능하며 복제본은 데이터 읽기 작업만 가능하다.

## Redis Replication 특징

- Replication ID와 Offset
  
  - 모든 Redis 노드는 복제 ID를 가지고 있으며, 복제 ID는 오프셋과 쌍으로 구성되어 있다.
  - 커맨드를 수행할 때마다 오프셋이 증가하며, 

- 부분 재동기화

- Secondary 복제 ID
  - redis는 Primary와 Secondary 복제 ID를 가지고 있다.
  - 복제 ID를 두개 가지고 있는 이유는 마스터 노드가 중지되어 복제 노드를 마스터 노드로 승격시킬때, 데이터 동기화 작업을 최소화하기 위해서이다.
  - 마스터 노드와 2개의 복제 노드가 있을때
    - 마스터 노드가 어떠한 이유 때문에 애플리케이션 종료됨
    - 복제 노드가 마스터 노드로 승격됨
    - 이때 복제 노드가 마스터 노드로 승격되면, 기존 마스터 노드의 복제 ID는 Secondary 복제 ID로 변경된다.
  - 
- 백로그
  - 
### 복제 매커니즘
  - `REPLICAOF <host port | NO ONE>` 명령어를 통해서 복제 진행
  - 마스터 노드가 RDB 스냅샷을 생성하여 복제본에 전달하거나 소켓 통신을 통해서 RDB 데이터를 전달
  - 스냅샷 생성이 진행되는 동안 마스터 노드의 변경 작업은 마스터 노드의 Replication Buffer에 저장
  - RDB 스냅샷 생성이 완료되면 파일을 복제본 노드에 전달한 이후 복사 진행
  - 이후 Replication Buffer에 존재하는 데이터를 복제 노드에 전달
- 7버전 이전에는 마스터 노드가 RDB 스냅샷을 생성하여 복제본에 전달하는 방식을 사용하였는데 마스터 노드와 복제 노드 둘다 RDB 스냅샷을 생성해야하기 때문에 디스크 I/O 처리량에 따라 속도가 느려질 수 있다.
  - `repl-diskless-sync no`
- 7버전 이후에는 위와 같은 방식을 해결하기 위하여 소켓 통신을 통해서 복제 노드에게 데이터를 전달하고 복제본만 RDB 스냅샷을 생성하도록 변경되었다.
  - `repl-diskless-sync이 yes`
```sh
### 마스터 노드 로그
1:M 24 Mar 2025 13:19:12.278 * Replica 172.19.0.4:6379 asks for synchronization
1:M 24 Mar 2025 13:19:12.278 * Full resync requested by replica 172.19.0.4:6379
1:M 24 Mar 2025 13:19:12.278 * Replication backlog created, my new replication IDs are 'cd299983a215427129e00aed85b6592260d3c30d' and '0000000000000000000000000000000000000000'
1:M 24 Mar 2025 13:19:12.278 * Delay next BGSAVE for diskless SYNC
1:M 24 Mar 2025 13:19:12.278 * Replica 172.19.0.3:6379 asks for synchronization
1:M 24 Mar 2025 13:19:12.278 * Full resync requested by replica 172.19.0.3:6379
1:M 24 Mar 2025 13:19:12.278 * Delay next BGSAVE for diskless SYNC
1:M 24 Mar 2025 13:19:17.313 * Starting BGSAVE for SYNC with target: replicas sockets
1:M 24 Mar 2025 13:19:17.316 * Background RDB transfer started by pid 21
21:C 24 Mar 2025 13:19:17.319 * Fork CoW for RDB: current 0 MB, peak 0 MB, average 0 MB
1:M 24 Mar 2025 13:19:17.320 * Diskless rdb transfer, done reading from pipe, 2 replicas still up.
1:M 24 Mar 2025 13:19:17.323 * Background RDB transfer terminated with success
1:M 24 Mar 2025 13:19:17.323 * Streamed RDB transfer with replica 172.19.0.4:6379 succeeded (socket). Waiting for REPLCONF ACK from replica to enable streaming
1:M 24 Mar 2025 13:19:17.323 * Synchronization with replica 172.19.0.4:6379 succeeded
1:M 24 Mar 2025 13:19:17.323 * Streamed RDB transfer with replica 172.19.0.3:6379 succeeded (socket). Waiting for REPLCONF ACK from replica to enable streaming
1:M 24 Mar 2025 13:19:17.323 * Synchronization with replica 172.19.0.3:6379 succeeded

### 복제 노드
1:S 24 Mar 2025 13:19:12.276 * Connecting to MASTER redis-master:6379
1:S 24 Mar 2025 13:19:12.277 * MASTER <-> REPLICA sync started
1:S 24 Mar 2025 13:19:12.277 * Non blocking connect for SYNC fired the event.
1:S 24 Mar 2025 13:19:12.277 * Master replied to PING, replication can continue...
1:S 24 Mar 2025 13:19:12.278 * Partial resynchronization not possible (no cached master)
1:S 24 Mar 2025 13:19:17.317 * Full resync from master: cd299983a215427129e00aed85b6592260d3c30d:0
1:S 24 Mar 2025 13:19:17.319 * MASTER <-> REPLICA sync: receiving streamed RDB from master with EOF to disk
1:S 24 Mar 2025 13:19:17.319 * MASTER <-> REPLICA sync: Flushing old data
1:S 24 Mar 2025 13:19:17.319 * MASTER <-> REPLICA sync: Loading DB in memory
1:S 24 Mar 2025 13:19:17.323 * Loading RDB produced by version 7.2.4
1:S 24 Mar 2025 13:19:17.323 * RDB age 0 seconds
1:S 24 Mar 2025 13:19:17.323 * RDB memory usage when created 0.93 Mb
1:S 24 Mar 2025 13:19:17.323 * Done loading RDB, keys loaded: 0, keys expired: 0.
1:S 24 Mar 2025 13:19:17.323 * MASTER <-> REPLICA sync: Finished with success
```

### 데이터 복제 특징
- 마스터 노드가 복제 노드에게 데이터 전달시 비동기로 데이터 전달
- 마스터 노드의 데이터가 정상적으로 복제되고 있는지 확인 하기 위한 복제 ID와 오프셋을 가지고 있음
  - 마스터 노드와 복제 노드의 오프셋이 동일해야지 두 노드가 일치하는 상태
- 백로그
- Secandary 복제 ID
- 복제 노드는 기본적으로 읽기 전용
  - replica-read-only 설정을 해제하게 되면 쓰기 작업 가능
  - 이후 마스터 노드와 복제 노드의 재동기화가 이루어지게 되면 기존 복제 노드의 쓰기 작업은 사라진다.

### 복제시 유의할 사항
- 기본적으로 마스터-슬레이브의 복제 구조에서 백업 기능을 활용하는 것이 좋다,.


## Redis Replication 만으로는 고가용성 확보가 불가
```sh
## 마스터 노드 다운
docker exec -it redis-master redis-cli shutdown
## 복제 중지
docker exec -it redis-slave-1 redis-cli REPLICAOF NO ONE
## redis-slave-1 마스터 노드로 승격
docker exec -it redis-slave-2 redis-cli REPLICAOF redis-slave-1 6379
```

- 수동 장애 조치(Failover) 
  - Redis Replication만 이용할 경우 하나의 복제본을 마스터로 승격 시키는 작업을 수동으로 진행해야한다.
  - 수동으로 장애조치를 진행하는 동안 서비스의 연속성을 보장할 수 없기 때문에 Redis Replication만으로는 고가용성을 확보할 수 없다.
  - 때문에 고가용성 솔루션을 도입하여 자동으로 장애 조치를 진행할 수 있도록 해야한다.
- 데이터의 일관성 문제
  - Redis Replication은 비동기 방식으로 마스터 노드가 복제 노드에게 데이터를 전달하기 때문에, 마스터 노드가 복제 노드에게 데이터를 전달하기 전에 마스터 노드가 다운되면, 복제 노드에는 최신 데이터가 반영되지 않을 수 있다.
  - 때문에 데이터 손실을 최소화할 수 있는 방법이 필요하다.
- 클라이언트 연결 문제
  - 수동으로 장애 조치를 했다고 해서 클라이언트가 자동으로 새로운 마스터 노드에게 연결되지 않는다.
  - 때문에 클라이언트가 새로운 마스터 노드에게 연결할 수 있도록 수정 및 배포를 해야하는데 이 과정에서 서비스의 연속성을 보장할 수 없다.
  - 때문에 복제 노드를 마스터 노드로 승격되더라도 클라이언트의 연결을 자동으로 처리할 수 있는 방법이 필요하다.


> [Redis Docs > Replication](https://redis.io/docs/latest/operate/oss_and_stack/management/replication/)
> https://redis.io/docs/latest/commands/replicaof/
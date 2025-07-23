## Redis Replication
- Redis는 마스터 노드와 복제 노드를 구성하여 데이터를 복제하는 Replication 기능을 제공한다.
- 기본적으로 고가용성을 확보하기 위해서는 복제본이 필요하며, 복제본을 통해서 얻을수 있는 이점은 다음과 같다.
  - 마스터 노드가 다운되었을때 ***복제본을 마스터로 승격시켜서 서비스 연속성을 유지***하기 위함
  - ***읽기 작업을 복제본에서 처리하여 마스터 노드의 부하를 줄이기 위함***
  - 복제 노드는 ***마스터 노드의 데이터를 백업하는 역할을 수행하여 데이터 손실을 최소화***하기 위함
- Redis Replication 구조에서는 마스터 노드는 하나만 존재하면 여러 개의 복제본을 가질수 있으며, 마스터 노드만이 데이터 쓰기/읽기 작업이 가능하며 복제 노드는 데이터 읽기 작업만 가능하다.

### Redis Replication 복제 방식
- 레디스는 `REPLICAOF <host port | NO ONE>` 명령어를 통해서 Replication을 설정할 수 있다.
  - `REPLICAOF <host port>`: 해당 호스트와 포트로 복제본을 설정
  - `REPLICAOF NO ONE`: 복제본 설정 해제 (마스터 노드로 승격)
- `REPLICAOF` 명령어를 통해서 복제본을 설정하면, 마스터 노드와 복제 노드간의 동기화가 시작된다.
  - 마스터 노드가 RDB 스냅샷을 생성하여 복제본에 전달하거나 소켓 통신을 통해서 RDB 데이터를 전달 
    - `repl-diskless-sync no`: 마스터 노드가 RDB 스냅샷을 생성하여 복제본에 전달하는 방식
    - `repl-diskless-sync yes`: 마스터 노드가 소켓 통신을 통해서 복제본에게 데이터를 전달하는 방식
  - 위 작업이 진행되는 동안 마스터 노드에 수행되는 모든 데이터 변경 작업을 Replication Buffer에 저장
  - RDB 파일이 생성 완료되면 복제본 노드에 저장되어 있는 모든 데이터를 삭제한뒤 RDB 파일 내용을 로드
    - `repl-diskless-load disabled`: RDB 파일을 디스크에 저장한 후 로드
  - RDB 파일 로드가 완료된 이후 Replication Buffer에 저장되어 있는 데이터를 복제 노드에 전달이후 복제 연결 진행
  - `redis-diskless-sync-delay` 설정
    - `repl-diskless-sync yes` 로 설정하게 될경우 마스터 노드가 RDB 파일을 저장하지 않기 때문에 하나의 복제 연결이 시작된 경우에는 다른 복제 연결을 수행할 수 없다. (파일 vs 스트림)
    - 때문에 `redis-diskless-sync-delay` 설정을 통해서 복제 연결을 지연 시켜서 다수의 복제 연결을 처리할 수 있도록 할 수 있다.
    - 보통 Redis Replication 구조의 초기 셋팅시 여러개의 복제 노드를 한번에 설정하기 때문에 `redis-diskless-sync-delay` 설정을 통해서 복제 연결을 지연시켜서 다수의 복제 노드를 처리할 수 있도록 하는것이 좋다.  
> 7버전 이전에는 마스터 노드가 RDB 스냅샷을 생성하여 복제본에 전달하는 방식을 사용하였는데 마스터 노드와 복제 노드 둘다 RDB 스냅샷을 생성해야하기 때문에 디스크 I/O 처리량에 따라 속도가 느려질 수 있다.
  - `repl-diskless-sync no`
```sh
### 마스터 노드 로그
1:C 22 Jul 2025 12:56:07.143 * oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
1:C 22 Jul 2025 12:56:07.143 * Redis version=7.2.4, bits=64, commit=00000000, modified=0, pid=1, just started
1:C 22 Jul 2025 12:56:07.143 # Warning: no config file specified, using the default config. In order to specify a config file use redis-server /path/to/redis.conf
1:M 22 Jul 2025 12:56:07.143 * monotonic clock: POSIX clock_gettime
1:M 22 Jul 2025 12:56:07.143 * Running mode=standalone, port=6379.
1:M 22 Jul 2025 12:56:07.144 * Server initialized
1:M 22 Jul 2025 12:56:07.144 * Ready to accept connections tcp
1:M 22 Jul 2025 12:56:07.319 * Replica 172.20.0.4:6379 asks for synchronization
1:M 22 Jul 2025 12:56:07.319 * Full resync requested by replica 172.20.0.4:6379
1:M 22 Jul 2025 12:56:07.319 * Replication backlog created, my new replication IDs are '0af57d3a0a7c9dacd27a433fa28a38a200286e27' and '0000000000000000000000000000000000000000'
1:M 22 Jul 2025 12:56:07.319 * Delay next BGSAVE for diskless SYNC
1:M 22 Jul 2025 12:56:07.323 * Replica 172.20.0.3:6379 asks for synchronization
1:M 22 Jul 2025 12:56:07.323 * Full resync requested by replica 172.20.0.3:6379
1:M 22 Jul 2025 12:56:07.323 * Delay next BGSAVE for diskless SYNC
1:M 22 Jul 2025 12:56:12.328 * Starting BGSAVE for SYNC with target: replicas sockets
1:M 22 Jul 2025 12:56:12.330 * Background RDB transfer started by pid 21
21:C 22 Jul 2025 12:56:12.332 * Fork CoW for RDB: current 0 MB, peak 0 MB, average 0 MB
1:M 22 Jul 2025 12:56:12.332 * Diskless rdb transfer, done reading from pipe, 2 replicas still up.
1:M 22 Jul 2025 12:56:12.337 * Background RDB transfer terminated with success
1:M 22 Jul 2025 12:56:12.337 * Streamed RDB transfer with replica 172.20.0.4:6379 succeeded (socket). Waiting for REPLCONF ACK from replica to enable streaming
1:M 22 Jul 2025 12:56:12.337 * Synchronization with replica 172.20.0.4:6379 succeeded
1:M 22 Jul 2025 12:56:12.337 * Streamed RDB transfer with replica 172.20.0.3:6379 succeeded (socket). Waiting for REPLCONF ACK from replica to enable streaming
1:M 22 Jul 2025 12:56:12.337 * Synchronization with replica 172.20.0.3:6379 succeeded

### 복제 노드 (172.20.0.3)
1:C 22 Jul 2025 12:56:07.317 * oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
1:C 22 Jul 2025 12:56:07.317 * Redis version=7.2.4, bits=64, commit=00000000, modified=0, pid=1, just started
1:C 22 Jul 2025 12:56:07.317 * Configuration loaded
1:S 22 Jul 2025 12:56:07.317 * monotonic clock: POSIX clock_gettime
1:S 22 Jul 2025 12:56:07.318 * Running mode=standalone, port=6379.
1:S 22 Jul 2025 12:56:07.318 * Server initialized
1:S 22 Jul 2025 12:56:07.318 * Ready to accept connections tcp
1:S 22 Jul 2025 12:56:07.321 * Connecting to MASTER redis-master:6379
1:S 22 Jul 2025 12:56:07.322 * MASTER <-> REPLICA sync started
1:S 22 Jul 2025 12:56:07.322 * Non blocking connect for SYNC fired the event.
1:S 22 Jul 2025 12:56:07.322 * Master replied to PING, replication can continue...
1:S 22 Jul 2025 12:56:07.322 * Partial resynchronization not possible (no cached master)
1:S 22 Jul 2025 12:56:12.328 * Full resync from master: 0af57d3a0a7c9dacd27a433fa28a38a200286e27:0
1:S 22 Jul 2025 12:56:12.332 * MASTER <-> REPLICA sync: receiving streamed RDB from master with EOF to disk
1:S 22 Jul 2025 12:56:12.333 * MASTER <-> REPLICA sync: Flushing old data
1:S 22 Jul 2025 12:56:12.333 * MASTER <-> REPLICA sync: Loading DB in memory
1:S 22 Jul 2025 12:56:12.336 * Loading RDB produced by version 7.2.4
1:S 22 Jul 2025 12:56:12.337 * RDB age 0 seconds
1:S 22 Jul 2025 12:56:12.337 * RDB memory usage when created 0.93 Mb
1:S 22 Jul 2025 12:56:12.337 * Done loading RDB, keys loaded: 0, keys expired: 0.
1:S 22 Jul 2025 12:56:12.337 * MASTER <-> REPLICA sync: Finished with success
```


## Redis Replication 특징

### 복제 노드에게 비동기식 데이터 전달
- Redis의 마스터 노드는 복제 노드에게 ***데이터 전달시 비동기 방식으로 데이터를 전달***한다.
  - ***복제 노드가 명령을 처리하기 전까지 마스터 노드가 대기하지 않아도 되기 때문에***, 복제 구조를 사용하더라도 ***낮은 지연 시간과 높은 처리량을 제공***할 수 있다.
- 비동기 방식 데이터 처리로 인하여 복제 노드는 매 초마다 마스터 노드에게 정상 연결 및 데이터가 어디까지 동기화가 되어 있는지에 대한 PING을 보낸다.
  - PING 명령어를 통해서 마스터 노드는 복제 노드가 정상적으로 연결되어 있는지 확인할 수 있고, 복제 노드가 어디까지 데이터를 동기화했는지 확인할 수 있다.
- 비동기식 데이터 전달로 인하여 마스터 노드가 복제 노드에게 데이터를 전달하기 전에 마스터 노드가 다운되면, 복제 노드에는 최신 데이터가 반영되지 않을 수 있기 때문에 데이터 손실이 발생할 수 있다.

#### 복제 구조에서 서버 설정을 통하여 조건 충족시에만 쓰기 작업 허용
- 복제 구조에서 마스터 노드의 쓰기 작업시 비동기 방식으로 이루어지기 때문에, 복제 노드가 장애가 생기거나 복제 지연이 발생하더라도 마스터 노드가 쓰기 작업을 계속 진행할 수 있다.
- 만약 복제 구조의 ***강력한 데이터의 일관성을 보장하거나 복제 지연의 최소화가 필요하다면, 설정을 통하여 클라이언트의 쓰기 요청을 제한***할 수 있다.
  - `min-replicas-to-write`: 마스터 노드가 쓰기 작업을 진행하기 위해서 최소한의 복제 노드 수를 설정 (default: 0)
    - 예를 들어 `min-replicas-to-write 1`로 설정하면, 최소한 하나의 복제 노드가 정상적으로 연결되어 있어야만 마스터 노드가 쓰기 작업을 진행할 수 있다.
  - `min-replicas-max-lag`: 마스터 노드가 쓰기 작업을 진행하기 위해서 복제 노드의 최대 지연 시간을 설정 (default: 10)
    - 예를 들어 `min-replicas-max-lag 5`로 설정하면, 현재 복제 노드의 지연 시간이 5초 이하인 경우에만 마스터 노드가 쓰기 작업을 진행할 수 있다.
```sh
## Redis 마스터-복제 노드 시작
docker compose up -d

## min-replicas-to-write를 2로 설정
docker exec -it redis-master redis-cli CONFIG SET min-replicas-to-write 2

## 복제 노드 하나 다운
docker compose down redis-slave-1

## 마스터 노드에 쓰기 작업 요청 및 요청 실패 확인
docker exec -it redis-master redis-cli SET key1 value1
(error) NOREPLICAS Not enough good replicas to write.
```

> [Redis Docs > Allow Writes Only with N Attached Replicas](https://redis.io/docs/latest/operate/oss_and_stack/management/replication/#allow-writes-only-with-n-attached-replicas)

#### 복제 구조에서 클라이언트 명령어를 통하여 데이터 동기화 상태 확인
- Redis는 `WAIT <numreplicas> <timeout>` 명령어를 통해서 ***현재 클라이언트가 실행한 명령어가 복제 노드까지 전파되었는지를 확인***할 수 있다.
  - `numreplicas`: 지정한 수만큼 복제 노드가 명령어를 처리할때 까지 대기
  - `timeout`: 지정한 밀리초 동안 대기, 만약 지정한 시간 이내에 복제 노드가 명령어를 처리하였다면 대기하지 않고 응답
  - `return`: `WAIT` 명령어를 요청한 이후 복제 노드가 명령어를 처리한 수를 반환
- `WAIT` 명령어를 사용하면, 클라이언트는 마스터 노드에게 실행한 명령어가 복제 노드에게 얼마만큼 전파되었는지를 확인할 수 있으며, 이를 통해서 추가 로직을 구현할 수 있다.
  > 참고로 해당 기능이 가능한 이유는 마스터 노드와 복제 노드의 Offset의 비교를 통하여 해당 클라이언트의 명령어가 복제 노드에게 전파되었는지를 확인할 수 있기 때문이다.
> [Redis Docs > WAIT](https://redis.io/docs/latest/commands/wait/)



### Replication ID와 Offset
- Redis는 ***Replication ID와 Offset을 통해서 마스터 노드와 복제 노드간의 동기화 상태를 확인하거나 유지***한다.
  - Replication ID를 통해서 마스터 노드와 복제 노드가 동일한 복제 셋임을 알 수 있고, Offset을 통해서 마스터 노드와 어디까지 데이터 동기화가 이루어졌는지 알 수 있다.
  - Replication ID와 Offset이 동일한 경우에는 마스터 노드와 복제 노드가 동일한 상태라는 것을 알 수 있고, 만약 Replication ID가 동일하더라도 Offset이 다르다면 마스터 노드와 복제 노드가 동기화되지 않은 상태라는 것을 알 수 있다.
- 아래는 마스터 노드와 복제 노드의 Replication ID와 Offset을 확인을 통한 동기화 상태 확인 예시이다.
```sh
## 마스터 노드에서 복제 상태 확인
INFO REPLICATION

role:master
connected_slaves:2
## 복제 노드 정보 및 Offset을 통한 동기화 상태 확인
slave0:ip=172.20.0.4,port=6379,state=online,offset=56,lag=0
slave1:ip=172.20.0.3,port=6379,state=online,offset=56,lag=1
master_failover_state:no-failover
## Replication ID와 Offset 정보 확인
master_replid:c3fd032d6651602daef865f17743149a643f08dc
master_replid2:0000000000000000000000000000000000000000
master_repl_offset:56

```
```sh
## 복제 노드에서 복제 상태 확인
INFO REPLICATION

role:slave
master_host:redis-master
master_port:6379
## 복제 노드가 마스터 노드로 부터 읽어들인 오프셋 정보
slave_read_repl_offset:434
## 복제 노드가 마스터 노드로 부터 성공적으로 동기화된 오프셋 정보
slave_repl_offset:434
## 마스터 노드의 Replication ID와 Offset 정보 확인
master_replid:c3fd032d6651602daef865f17743149a643f08dc
master_replid2:0000000000000000000000000000000000000000
master_repl_offset:434

```
- 참고로 복제 노드의 `master_repl_offset - slave_repl_offset > 0`을 비교하여 복제 지연에 대한 정보도 유추해볼 수 있다.

### 부분 재동기화
- Redis는 복제 노드가 네트워크 이슈나 타임아웃이 발생하여 마스터 노드와 연결이 끊어진 이후 다시 연결했을 때, 마스터 노드와 복제 노드가 동일한 Replication ID를 가지고 있다면, ***부분 재동기화(Partial Resynchronization)***을 진행한다.
  - 만약 일시적인 연결 끊김이 발생했을때 전체 재동기화를 진행하게 되면, 마스터 노드와 복제 노드간의 데이터 동기화 작업이 오래 걸리게 되며 이로 인해서 서비스에 영향을 줄 수 있다.

#### 부분 재동기화를 위한 백로그
- Redis는 부분 재동기화를 위해서 ***백로그(Backlog)***를 사용한다. 
  - 백로그는 마스터 노드가 ***복제 노드에게 전달한 명령어를 저장하는 임시 버퍼로, Offset 및 명령어를 저장***한다.
  - 복제 노드가 마스터 노드와 연결이 끊어진 이후 다시 연결되었을 때, 백로그에 저장된 명령어를 통해서 부분 재동기화를 진행할 수 있다.
- 백로그는 `repl-backlog-size` 설정을 통해서 크기를 조정할 수 있으며, `repl-backlog-ttl` 설정을 통해서 해당 시간 경과시 백로그를 삭제할 수 있다.
- 백로그를 통한 부분 재동기화 과정
  - 복제 노드 연결 끊기 및 재연결 시도
  - `PSYNC (Partial SYNC)` 명령어를 통해서 마스터 노드에게 부분 재동기화를 요청
  - `Replication ID` 일치 여부 확인 및 `Offset`이 백로그 범위 내에 유효한지 확인
  - 유효하다면 부분 재동기화 진행 이후 정상적으로 연결 상태 유지
- 만약 `Replication ID`가 일치하지 않거나 `Offset`이 백로그 범위 밖에 있는 경우에는 전체 재동기화를 진행한다.

### Secondary ID
- Redis의 Replication ID는 레디스 인스턴스가 시작되거나, 복제 노드가 마스터 노드로 승격될 때 새롭게 생성되며, Redis는 두개의 Replication ID인 Main ID와 Secondary ID를 가지고 있다.
- Redis가 Replication ID를 두개 가지고 있는 이유는 ***마스터 노드가 중지되어 복제 노드를 마스터 노드로 승격시킬때, 데이터 동기화 작업을 최소화하기 위해서이다.***
  > Replication ID가 동일하다는 의미는 마스터 노드와 복제 노드가 같은 복제 셋임을 의미한다.

#### Secondary ID가 없는 경우
- Secondary ID가 없는 경우에 복제 노드가 마스터 노드로 승격되는 경우를 가정해보자.
```text
a는 마스터 노드, b와 c는 복제 노드라고 가정
a 마스터 노드가 다운됨으로 인하여 b 복제 노드를 마스터 노드로 승격
b를 복제 노드로 승격시키면서 새로운 Replication ID를 생성
c가 b의 복제 노드가 될때 Replication ID를 확인
동일한 Replication ID를 가지고 있지 않기 때문에 전체 재동기화 진행
```
- 위 과정 처럼 Replication ID가 동일하지 않기 때문에 전체 재동기화를 진행할 수 밖에 없고, 이로 인하여 데이터 동기화 작업이 오래 걸리며 서비스에 영향을 줄 수 있다. 

#### Secondary ID가 있는 경우
- Secondary ID가 있는 경우에 복제 노드가 마스터 노드로 승격되는 경우를 가정해보자.
```text
a는 마스터 노드, b와 c는 복제 노드라고 가정
a 마스터 노드가 다운됨으로 인하여 b 복제 노드를 마스터 노드로 승격
b를 복제 노드로 승격시키면서 새로운 Replication ID를 생성 및 이전 마스터 노드의 Replication ID를 Secondary ID로 변경
c가 b의 복제 노드가 될때 기존 Replication ID를 Secondary ID로 변경 및 b의 Replication ID를 Main ID로 변경
c와 b의 Secondary ID가 동일하기 때문에 부분 재동기화 진행
```
- 만약 b 노드가 백로그를 가지고 있는 상황이라면 부분 재동기화를 통해서 복제 상태를 빠르게 구축할 수 있으며, 이로 인하여 데이터 동기화 작업이 최소화되어 서비스에 영향을 줄 수 있는 시간을 최소화할 수 있다.

> [Redis Docs > Replication ID explained](https://redis.io/docs/latest/operate/oss_and_stack/management/replication/#replication-id-explained)




### 백업을 사용하지 않을 때 마스터 노드 재기동시 발생할 수 있는 위험성
- 복제 구조에서 백업을 사용하지 않은 경우, 마스터 노드가 다운되어 다시 재시작 했을때 기존 복제 노드의 데이터까지 삭제될 수 있다.
```sh
## Redis 마스터-복제 노드 시작
docker compose up -d

## 마스터 노드 데이터 쓰기
docker exec -it redis-master redis-cli SET key1 value1
OK

## 복제 노드 데이터 읽기
docker exec -it redis-slave-1 redis-cli GET key1
"value1"
docker exec -it redis-slave-2 redis-cli GET key1
"value1"

## 마스터 노드 컨테이너 종료
docker compose down redis-master

## 마스터 노드 재시작
docker compose up -d redis-master

## 마스터 노드 및 복제 노드 데이터 확인
docker exec -it redis-master redis-cli GET key1
(nil)
docker exec -it redis-slave-1 redis-cli GET key1
(nil)
docker exec -it redis-slave-2 redis-cli GET key1
(nil)
```
- 하지만 고가용성이 확보된 시스템의 경우 다운된 마스터 노드를 재시작 하는 경우보다 복제 노드를 마스터 노드로 승격 시키는 경우가 많기 때문에 위와 같은 위험성은 낮다.
- 그럼에도 불구하고 백업을 사용하지 않은 경우 최악의 상황이 벌어졌을때 복구가 불가능하기 때문에 백업을 사용하는 것이 좋다.
  - 운영 실수나 재해로 인하여 데이터를 삭제한 경우
  - 특정 시점의 데이터를 복구하여 분석해야하는 경우


## Redis Replication 만으로는 고가용성 확보가 불가
- Redis Replication은 고가용성을 확보하기 위한 기본적인 기능이지만, 해당 기능만으로는 고가용성을 확보하기 어렵다.
- 만약 Redis Replication만을 사용하여 고가용성을 확보하려고 한다면 다음과 같은 문제점이 발생할 수 있다.
```sh
## 마스터 노드 다운
docker exec -it redis-master redis-cli shutdown
## 복제 중지
docker exec -it redis-slave-1 redis-cli REPLICAOF NO ONE
## redis-slave-1 마스터 노드로 승격
docker exec -it redis-slave-2 redis-cli REPLICAOF redis-slave-1 6379
```

- ***수동 장애 조치(Failover)***
  - Redis Replication만 이용할 경우 ***하나의 복제본을 마스터로 승격 시키는 작업을 수동으로 진행***해야한다.
  - ***수동으로 장애조치를 진행하는 동안 서비스의 연속성을 보장할 수 없기 때문에*** Redis Replication만으로는 고가용성을 확보할 수 없다.
  - 때문에 ***고가용성 솔루션을 도입하여 자동으로 장애 조치를 진행할 수 있도록 해야한다.***
- ***클라이언트 연결 문제***
  - 수동으로 장애 조치를 했다고 해서 ***클라이언트가 자동으로 새로운 마스터 노드에게 연결되지 않는다.***
  - 때문에 클라이언트가 새로운 마스터 노드에게 연결할 수 있도록 수정 및 배포를 해야하는데 이 과정에서 서비스의 연속성을 보장할 수 없다.
  - 때문에 복제 노드를 마스터 노드로 승격되더라도 클라이언트의 연결을 자동으로 처리할 수 있는 방법이 필요하다.
- ***데이터의 일관성 문제***
  - Redis Replication은 비동기 방식으로 마스터 노드가 복제 노드에게 데이터를 전달하기 때문에, 마스터 노드가 복제 노드에게 데이터를 전달하기 전에 마스터 노드가 다운되면, 복제 노드에는 최신 데이터가 반영되지 않을 수 있다.
  - 때문에 ***데이터 손실을 최소화할 수 있는 방법이 필요***하다.


## References
> [Redis Docs > Replication](https://redis.io/docs/latest/operate/oss_and_stack/management/replication/) <br/>
> [Redis Docs > REPLICAOF](https://redis.io/docs/latest/commands/replicaof/)

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


> https://redis.io/docs/latest/commands/replicaof/
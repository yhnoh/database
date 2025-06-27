## 레디스 백업의 필요성
- 레디스는 메모리 기반의 데이터베이스로써, 레디스 인스턴스가 장애로 인하여 재실행하게 되면 저장된 데이터가 모두 사라진다.
  - 캐시: 캐시 데이터가 사라지며 원본 데이터베이스의 부하가 발생할 수 있다.
  - 데이터베이스: 데이터 손실이 발생하여 복구가 불가능하다.

- 

> https://redis.io/docs/latest/operate/rs/databases/configure/database-persistence/
## 레디스 영속성

### RDB (Snapshot)
- 일정 시점에 메모리에 저장된 저ㄴ체 데이터를 바이너리 파일로 저장하는 방식
- 
- 전체 데이터가 저장된 시점에 

```sh
save <seconds> <changes>
stop-writes-on-bgsave-error yes
rdbcompression yes
rdbchecksum yes
dbfilename dump.rdb

```

### AOF (Append Only File)
- 레디스 인스턴스가 처리한 모든 쓰기 작업에 대한



- 하나의 인스턴스에 RDB 및 AOF 옵션을 동시에 사용하는 것이 가능하다.
- 헤디스 인스턴스의 실행 도중에 데이터 파일을 읽어들일 수 없다.

### AOF (Append Only File)

- 원하는 시점으로 복구가 가능하다.
### RDB (Redis Database)

- 시점 단위의 데이터 백업이 가능하게 때문에 특정 시점으로 복구가 가능하다.


> [케시 데이터 영구 저장하는 방법(RDB / AOF)](https://inpa.tistory.com/entry/REDIS-%F0%9F%93%9A-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EC%98%81%EA%B5%AC-%EC%A0%80%EC%9E%A5%ED%95%98%EB%8A%94-%EB%B0%A9%EB%B2%95-%EB%8D%B0%EC%9D%B4%ED%84%B0%EC%9D%98-%EC%98%81%EC%86%8D%EC%84%B1)
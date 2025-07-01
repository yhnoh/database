## 레디스 백업의 필요성
- 레디스는 메모리 기반의 데이터베이스로써, 레디스 인스턴스가 장애로 인하여 재실행하게 되면 저장된 데이터가 모두 사라진다.
  - 레디스를 캐시로 활용: 장애 발생시 캐시 데이터가 사라지며 원본 데이터베이스의 부하가 발생할 수 있다.
  - 레디스를 영구저장소로 활용: 장애 발생시 데이터 손실이 발생하여 복구가 불가능하다.

### RDB와 AOP
- 레디스는 RDB와 AOF 두 가지 방식으로 데이터를 백업하여 영속성을 보장할 수 있다.
  - RDB: 레디스 인스턴스의 ***메모리 데이터를 특정 시점에 스냅샷으로 저장하는 방식***
  - AOF: 레디스 인스턴스가 처리한 ***모든 쓰기 작업에 대한 로그를 기록하는 방식***

#### RDB와 AOP 복원
- 레디스는 인스턴스 실행되고 잇는 시점에는 RDB와 AOF 파일의 데이터를 읽어올 수 없으며, ***레디스 서버가 재시작할때 파일의 데이터를 로드***할 수 있다.
  - RDB 파일이 존재하는 경우 RDB 파일을 읽어들여서 데이터를 복원한다.
  - AOF 파일이 존재하는 경우 AOF 파일을 읽어들여서 데이터를 복원한다.
  - RDB와 AOF 파일이 모두 존재하는 경우 AOF 파일을 읽어들여서 데이터를 복원한다.

#### RDB와 AOF 저장시 저장되는 내용

```sh
SET key1 value1
SET key2 value2
DEL key2
```

- 위 명령어를 실행한 후 RDB와 AOF 파일에 저장되는 내용은 다음과 같다.
- RDB 파일에는 저장되는 시점에 메모리 데이터가 그대로 저장한다.
  - key2는 삭제되었기 때문에 RDB 파일에는 key1만 저장된다. 
- AOF 파일에는 실행된 모든 쓰기 작업이 저장된다.
  - AOF 파일에는 위 명령어가 순차적으로 저장된다.

#### RDB와 AOF의 장단점
- RDB는 특정 시점의 데이터를 저장하기 때문에 특정 시점에 대한 데이터 복구가 가능하다.
- 




### RDB와 AOF의 차이점

#### 


> [Redis Docs > Persistence](https://redis.io/docs/latest/operate/oss_and_stack/management/persistence/)
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
### 특정 조건에 따른 RDB 스냅샷 생성

```sh

## CONFIG GET 명령어를 통한 RDB 설정 확인
127.0.0.1:6379> CONFIG GET save
1) "save"
2) "3600 1 300 100 60 10000"

## CONFIG SET 명령어를 통한 RDB 설정 변경
127.0.0.1:6379> CONFIG SET save ""
OK

127.0.0.1:6379> CONFIG GET save
1) "save"
2) ""

```

### 수동으로 RDB 파일 생성
- SAVE, BGSAVE 명령어를 통해서 수동으로 RDB 파일을 생성할 수 있다.
- SAVE: 동기적으로 RDB 파일을 생성
- BGSAVE: 비동기적으로 RDB 파일을 생성


### AOF (Append Only File)
- 레디스 인스턴스가 처리한 모든 쓰기 작업에 대한 로그를 기록하는 방식이다.



- 하나의 인스턴스에 RDB 및 AOF 옵션을 동시에 사용하는 것이 가능하다.
- 헤디스 인스턴스의 실행 도중에 데이터 파일을 읽어들일 수 없다.

### AOF (Append Only File)

- 원하는 시점으로 복구가 가능하다.
### RDB (Redis Database)

- 시점 단위의 데이터 백업이 가능하게 때문에 특정 시점으로 복구가 가능하다.


> [케시 데이터 영구 저장하는 방법(RDB / AOF)](https://inpa.tistory.com/entry/REDIS-%F0%9F%93%9A-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EC%98%81%EA%B5%AC-%EC%A0%80%EC%9E%A5%ED%95%98%EB%8A%94-%EB%B0%A9%EB%B2%95-%EB%8D%B0%EC%9D%B4%ED%84%B0%EC%9D%98-%EC%98%81%EC%86%8D%EC%84%B1)
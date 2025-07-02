## 레디스 영속성
- 레디스는 메모리 기반의 데이터베이스로써, 레디스 인스턴스가 장애로 인하여 재실행하게 되면 저장된 데이터가 모두 사라진다.
  - 레디스를 캐시로 활용: 장애 발생시 캐시 데이터가 사라지며 원본 데이터베이스의 부하가 발생할 수 있다.
  - 레디스를 영구저장소로 활용: 장애 발생시 데이터 손실이 발생하여 복구가 불가능하다.

### RDB와 AOF
- 레디스는 RDB와 AOF 두 가지 방식으로 데이터를 백업하여 영속성을 보장할 수 있다.
  - RDB: 레디스 인스턴스의 ***메모리 데이터를 특정 시점에 스냅샷으로 저장하는 방식***
  - AOF: 레디스 인스턴스가 처리한 ***모든 쓰기 작업에 대한 로그를 기록하는 방식***

#### RDB와 AOF 복원
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

## RDB
- RDB는 레디스 인스턴스의 ***메모리 데이터를 특정 시점에 스냅샷으로 저장하는 방식***이다.
- 사용자가 지정한 특정 시점 단위로 데이터가 저장되기 때문에, ***저장된 시점 이후에 장애가 발생하면 해당 데이터는 복구할 수 없는 단점이 있다.***
- RDB 파일은 레디스 설정을 통해서 특정 조건에 따라서 자동으로 생성하거나, 명령어를 통해서 수동으로 생성할 수 있다. 

### 레디스 설정을 통한 특정 조건에 따라서 자동으로 생성

```sh
## 3600초 동안 1개 이상의 키 변경
## 300초 동안 100개 이상의 키 변경
## 60초 동안 10000개 이상의 키 변경
save 3600 1 300 100 60 10000
## RDB 파일명
dbfilename "dump.rdb"
## RDB 파일 저장 경로
dir "/data" 
```
- 레디스 설정 파일에서 `save` 옵션을 통해서 특정 조건에 따라서 RDB 파일을 자동으로 생성할 수 있다.
  - 위 설정은 다음과 같은 조건에 따라서 RDB 파일을 자동으로 생성한다.
  - 3600초 동안 1개 이상의 키 변경
  - 300초 동안 100개 이상의 키 변경
  - 60초 동안 10000개 이상의 키 변경
- 만약 RDB 파일을 저장하고 싶지 않다면 `save` 옵션을 빈 문자열로 설정하면 된다.

### 레디스 명령어를 통한 수동 생성
- 레디스 `BGSAVE`와 `SAVE` 명령어를 통해서 RDB 파일을 수동으로 생성할 수 있다.

#### BGSAVE
- `BGSAVE` 명령어 수행시 자식 프로세스를 생성하여 비동기적으로 RDB 파일을 생성한다.
  - 때문에 RDB 파일 생성 중에도 클라이언트의 요청을 처리할 수 있다.
- 백그라운드에서 RDB 파일을 생성하기 때문에 실제 저장 완료 시점을 응답하지 않는다.
  - `redis-cli INFO persistence` 명령어를 통해서 `rdb_last_save_time`와 `rdb_last_bgsave_status` 출력값을 통해서 RDB 파일 생성 상태를 확인할 수 있다.
  - `LASTSAVE` 명령어를 통해서 마지막으로 RDB 파일이 저장된 시점을 확인할 수 있다.
- `BGSAVE` 명령어 실행시 만약 데이터 백업이 수행중이라면 명령어가 취소된다.
  - `BGSAVE SCHEDULE` 옵션을 통해서 백업 작업을 예약한뒤, 이미 진행중이던 백업이 완료되면 예약된 백업 작업이 수행된다.

#### SAVE
- `SAVE` 명령어 수행시 현재 프로세스에서 동기적으로 RDB 파일을 생성한다.
  - 때문에 RDB 파일 생성 중에는 클라이언트의 요청을 처리할 수 없다.
- 대규모 데이터를 저장하게 될때 `SAVE` 명령어를 사용하게되면 클라이언트 요청을 처리할 수 없기 때문에 사용을 권장하지 않는다.

## AOF (Append Only File)
- AOF는 레디스 인스턴스가 처리한 ***모든 쓰기 작업에 대한 로그를 기록하는 방식***이다.
- AOF는 메모리에 영향을 끼치는 모든 쓰기 작업을 기록할 수 있으며 

### AOF Disk Flush
- AOF 방식은 모든 쓰기 작업에 대하여 로그를 기록하기 때문에 RDB 방식보다 데이터 손실이 적다.
  - AOF 방식은 모든 쓰기 작업을 기록하기 때문에, 장애가 발생하더라도 마지막으로 기록된 쓰기 작업까지 복구할 수 있다.
- 모든 쓰기 작업이 발생할때마다 디스크에 데이터를 기록한다면 안정성은 높아지지만 성능이 저하된다. 반대로 디스크 쓰기 작업의 빈도를 줄이면 성능은 향상되지만 데이터 손실이 발생할 수 있다.
- 레디스 `appendfsync` 설정을 통해서 쓰기 작업 발생시 AOF 파일에 얼마나 자주 데이터를 기록할지를 설정할 수 있다.

#### AOF Disk Flush 과정
- 클리아언트 요청으로 인하여 쓰기 작업이 발생하였다고 해서 레디스가 디스크에 직접 접근하여 기록하는 것은 아니다.
- 레디스에서 쓰기 작업이 발생하면 커널 영역의 OS 버퍼에 임시로 저장된다.
- OS 버퍼에 저장된 데이터는 `fsync` 시스템 콜을 통해서 디스크에 기록된다.
  - 일반적인 리눅스 운영체제에서는 30초마다 OS 버퍼에 저장된 데이터를 디스크에 기록한다.

#### appendfsync 설정
- `appendfsync always`
  - 쓰기 작업 발생시마다 `fsync` 시스템 콜을 호출하여 AOF 파일에 데이터를 기록한다.
  - 안정성은 높지만 성능이 저하된다.
- `appendfsync everysec` (default)
  - 쓰기 작업이 발생하면 OS 버퍼에 데이터를 저장하고, 백그라운드에서 1초마다 `fsync` 시스템 콜을 호출하여 AOF 파일에 데이터를 기록한다.
  - 약간의 데이터 유실은 발생할 수 있지만 성능이 향상된다.
- `appendfsync no`
  - 레디스는 OS 버퍼에 데이터를 저장하기만 하고, `fsync` 시스템 콜을 통한 디스크 기록은 운영체제에 맡긴다.
  - 일반적인 리눅스 운영체제에서는 30초마다 OS 버퍼에 저장된 데이터를 디스크에 기록하며, 안정성은 가장 낮지만 성능은 가장 높다.
- AOF 파일에서 데이터 유실은 `appendfsync` 설정에 따라 달라지면 최대 30초간의 데이터 쓰기 작업에 대한 유실이 발생할 수 있다.
 
 > [](https://redisgate.kr/redis/configuration/param_appendfsync.php)

### AOF Rewrite
- AOF 파일은 모든 쓰기 작업을 기록하기 때문에 시간이 지남에 따라서 AOF 파일의 크기가 커진다.
- ***AOF 파일의 크기가 커지게 되면 AOF 파일을 작성하는 시간 및 AOF 파일을 읽어드려 복구하는 시간이 길어지게된다.***
  - AOF 파일 I/O 작업의 비효율성 증대
- 때문에 현재 레디스 메모리의 상태를 기반으로 AOF 파일을 다시 작성하여 AOF 파일의 크기를 줄이는 작업을 수행하며 ***이를 재구성 또는 Rewrite라고 한다.***

#### AOF Rewrite의 필요성 예시
- 예를 들어서 데이터를 1씩 증가하는 작업을 100번 수행한다다고 가정했을때, AOF 파일에는 `INCR key1` 명령어가 100번 기록될 것며 AOF 파일의 크기가 점점 커지게된다.  
```sh
### appendonly.aof.[시퀀스].incr.aof 파일 내용
$4
INCR
$4
key1
*2
$4
INCR
$4
key1
....
```
- 이런 경우 `INCR key1 100` 또는 `SET key1 100` 명령어로 AOF 파일을 재구성하여 AOF 파일의 크기를 줄일 수 있다.

#### Redis 7.0 이상 기준의 AOF Rewrite 과정 (Multi Part AOF Mechanism)

- 레디스 7.0 이후에는 `base`와 `incr` 파일 및ㅊ `manifest` 파일로 구성된 Multi Part AOF Mechanism을 사용한다.
  - `base` 파일: ***레디스의 현재 상태를 기반으로 생성된 AOF 파일, 명령어가 재구성된 파일***
  - `incr` 파일: `base` 파일이 생성된 이후에 발생한 ***쓰기 작업을 기록하는 AOF 파일***
  - `manifest` 파일: ***`base`와 `incr` 파일의 메타데이터***

1. ***AOF Rewrite 시작***
    - 레디스가 `fork()` 시스템 콜을 통해서 자식 프로세스를 생성한다.
2. ***base 파일 생성***
   - `fork()` 시점에 레디스 데이터의 상태를 기반으로 명령어를 압축하여 base 파일에 저장한다.
3. ***incr 파일 생성 및 기록***
   - base 파일이 생성되고 저장되는 동안 `fork()`된 시점 이후에 발생하는 쓰기 작업을 기록한다.
4. ***AOF Rewirte 완료***
   - base 파일의 재구성이 완료되면 임시 매니페스트 파일을 생성한 뒤, 변경된 버전으로 파일 내용을 업데이트한다.
   - 임시 매니패스트 파일을 기반으로 매니패스트 파일을 원자적으로 업데이트한다.
   - 이후 이전 AOF 파일을 삭제한다.

> 만약 AOF Rewrite 과정이 실패하더라도, ***이전 AOF 파일이 존재하기 때문에 데이터 손실은 발생하지 않는다.***

#### AOF Rewirte 자동 수행 및 수동 수행
- 레디스 설정 파일에서 `auto-aof-rewrite-percentage`와 `auto-aof-rewrite-min-size` 설정을 통해서 ***AOF Rewrite를 자동으로 수행***할 수 있다.
  - `auto-aof-rewrite-percentage`: AOF 파일의 크기가 이전 AOF 파일 크기의 몇 퍼센트 이상 커졌을 때 AOF Rewrite를 수행할지를 설정한다.
  - `auto-aof-rewrite-min-size`: AOF 파일의 크기가 최소 몇 바이트 이상일 때 AOF Rewrite를 수행할지를 설정한다.
    - `auto-aof-rewrite-min-size` 옵션이 필요한 경우는 이전 AOF 파일의 크기가 너무 작을때 `auto-aof-rewrite-percentage` 옵션으로 인하여 AOF Rewrite가 자주 발생하는 경우가 발생할 수 있다.
  - 예를 들어서 `auto-aof-rewrite-percentage`를 100으로 설정하고, `auto-aof-rewrite-min-size`를 64MB로 설정하면 AOF 파일의 크기가 64MB 이상이고, 이전 AOF 파일의 크기의 100% 이상 커졌을 때 AOF Rewrite가 수행된다.
- `BGREWRITEAOF` 명령어를 통해서 AOF Rewrite를 수동으로 수행할 수 있다.


> [Redis Docs > Persistence > Log rewriting](https://redis.io/docs/latest/operate/oss_and_stack/management/persistence/) <br/>
> [Redis Docs > Persistence > Append-only file](https://redis.io/docs/latest/operate/oss_and_stack/management/persistence/) <br/>
<br/>


## 레디스 백업시 주의 사항
- 레디스 백업시 자식 프로세스르 생성하여 비동기적으로 백업을 진행할때 Copy-on-Write(COW) 방식으로 백업을 진행한다.
  - RDB 자동 백업 및 `BGSAVE` 명령어 사용, AOF Rewrite 수행
  > Copy-on-Write: 부모 프로세스와 자식 프로세스가 동일한 메모리 페이지를 공유하고 있으며, ***부모 프로세스가 메모리 페이지를 수정하게 되면 자식 프로세스는 해당 페이지를 복사하여 사용***하게 된다.
- Copy-on-Write 방식을 통해서 모든 데이터를 자식 프로세스가 복사하지 않기 때문에 메모리 사용량이 적고, 자식 프로세스가 백업을 진행하는 동안 부모 프로세스는 계속해서 클라이언트 요청을 처리할 수 있다.
- 하지만 최악의 경우에 기존의 레디스 메모리 사용량의 100%에 가까운 상태에서 백업을 진행하게 되면, Out Of Memory(OOM) 오류가 발생하여 서버가 다운될 수 있다.
- 때문에 레디스 `maxmemory` 설정을 통해서 서버의 실제 메모리 용량보다 여유를 가지고 설정하는 것이 안정적으로 서버를 운영할 수 있는 방안이다.



> [Redis 7.2 Configuration](https://raw.githubusercontent.com/redis/redis/7.2/redis.conf) <br/>
> [Redis Docs > Persistence](https://redis.io/docs/latest/operate/oss_and_stack/management/persistence/) <br/>
> [Redisgate > Params RDB](https://redisgate.kr/redis/configuration/param_save.php)
> [Redisgate > Params AOF](https://redisgate.kr/redis/configuration/param_appendonly.php)
> [케시 데이터 영구 저장하는 방법(RDB / AOF)](https://inpa.tistory.com/entry/REDIS-%F0%9F%93%9A-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EC%98%81%EA%B5%AC-%EC%A0%80%EC%9E%A5%ED%95%98%EB%8A%94-%EB%B0%A9%EB%B2%95-%EB%8D%B0%EC%9D%B4%ED%84%B0%EC%9D%98-%EC%98%81%EC%86%8D%EC%84%B1)
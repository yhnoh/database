

### String
- Key와 Value 형태의 문자열을 저장할 수 있는 데이터 구조이다.
- 최대 512MB 데이터를 저장할 수 있으며 문자열이 binery-safe하게 처리되기 때문에 이미지와 오디오와 같은 다양한 데이터를 저장하는 것이 가능하다.

> binary-safe
> 길이 명시: Binary-safe한 시스템이나 함수는 일반적으로 문자열의 길이를 명시적으로 저장하거나 전달합니다. 따라서 널 바이트를 만나더라도 문자열의 실제 길이를 알 수 있어 그 이후의 데이터도 정확하게 처리할 수 있습니다.
> 바이트 단위 처리: 문자열을 단순히 문자의 나열이 아닌 바이트의 연속으로 취급합니다. 따라서 어떤 바이트 값이라도 특별한 의미를 부여하지 않고 데이터의 일부로 간주합니다.
> 인코딩 독립성: 특정 문자 인코딩에 의존하지 않고 바이트 스트림으로 처리하므로 다양한 유형의 데이터를 안전하게 다룰 수 있습니다.

#### 데이터 저장 명령어
- SET
  - `SET key value [NX | XX] [GET] [EX seconds | PX milliseconds |
  EXAT unix-time-seconds | PXAT unix-time-milliseconds | KEEPTTL]`
  - 문자열 데이터를 저장할 수 있으며 키가 이미 존재하는경우 덮어씌운다.
  - 옵션
    - NX: 키가 존재하지 않는 경우에 데이터를 저장
    - XX: 키가 존재하는 경우에 데이터를 저장
- INCR
  - `INCR key`
  - 저장된 데이터를 1씩 증가시킬 수 있으며, 증가된 데이터를 반환한다.
- INCRBY
  - `INCRBY key increment`
  - 입력한 값만큼 데이터를 증가시킬 수 있으며, 증가된 데이터를 반환한다.
- DECR
  - `DECR key`
  - 저장된 데이터를 1씩 감소시킬 수 있으며, 감소된 데이터를 반환한다.
- DECRBY
  - `DECRBY key decrement`
  - 입력한 값만큼 데이터를 감소시킬 수 있으며, 감소된 데이터를 반환한다.
- MSET
  - `MSET key value [key value ...]`
  - 여러 키와 해당 하는 값을 동시에 저장한다. 
#### 데이터 읽기 명령어
- GET
  - `GET key`
  - 지정한 키의 값을 가져온다.
- MGET
  - `MGET key [key ...]`
  - 지정한 모든 키의 값을 반환한다.

> [Redis Docs > String](https://redis.io/docs/latest/develop/data-types/#strings)


### List
- List는 순서를 가지는 문자열의 목록이며 연결 리스트의 자료구조 특성을 가진다.
  - 연결 리스트의 자료구조 특성을 가지기 때문에 특정 인덱스를 통해서 데이터를 조회할때도 첫번째부터 순차적으로 접근하여 해당 인덱스의 데이터를 조회한다.
- 최대 42억 여개의 데이터를 저장할 수 있으며, 특정 커맨드를 이용하여 Stack과 Queue처럼 활용할 수 있다. 
- List 데이터 타입을 가진 키의 사이즈가 0이 되면 레디스는 해당 키를 자동으로 삭제한다.
  - 이는 다른 컬렉션 타입인 Set, Hash, Sorted Set에도 해당된다.

#### 데이터 저장 명령어
- LPUSH
  - `LPUSH key element [element ...]`
  - 지정한 데이터 목록을 왼쪽에 추가한다.
- RPUSH
  - `RPUSH key element [element ...]`
  - 지정한 데이터 목록을 오른쪽에 추가한다.
- LPOP
  - `LPOP key [count]`
  - 왼쪽에서 데이터를 반환함과 동시에 List에서 데이터를 삭제한다.
- RPOP
  - `RPOP key [count]`
  - 오른쪽에서 데이터를 반환함과 동시에 List에서 데이터를 삭제한다.
- LINSERT
  - `LINSERT key <BEFORE | AFTER> pivot element`
  - 특정 인덱스를 기준으로 앞이나 뒤에 데이터를 추가할 수 있다.
- LTRIM
  - `LTRIM key start stop`
  - 시작과 끝의 인덱스를 입력받아 지정되지 않은 범위의 데이터를 삭제한다.

#### Blocking 명령어
- BLPOP, BRPOP
  - `BLPOP key [key ...] timeout`
  - `BRPOP key [key ...] timeout`
  - 데이터를 반환함과 동시에 List에서 데이터를 삭제한다.
  - 리스트에 데이터가 존재한다면 데이터를 즉시 반환하지만 리스트에 데이터가 존재하지 않는다면 지정한 timeout 동안 대기를 한다.
  - 대기하는 동안 리스트에 데이터가 추가된다면 해당 데이터를 반환하지만, 만약 지정한 대기시간 동안 데이터가 추가되지 않는다면 nil을 반환한다.
  - 요청한 클라리언트는 대기를 하게되만 다른 레디스 클라이언트들은 레디스에 접근 및 명령어 수행이 가능하다.

#### 데이터 읽기 명령어
- LRANGE
  - `LRANGE key start stop`
  - 시작과 끝의 인덱스를 입력받아 해당하는 데이터를 반환한다.
- LINDEX
  - `LINDEX key index`
  - 입력받은 인덱스의 데이터를 해당하는 반환한다.
- LLEN
  - `LLEN key`
  - 리스트의 길이를 반환한다.

> [Redis Docs > List](https://redis.io/docs/latest/develop/data-types/#lists)


### Hash
- Hash는 field-value 구조를 가진 집합구조이다.
  - 필드와 값 모두 문자열 데이터로 저장되며, 관계형 데이터베이스나 객체와 같은 표현하기 적합하다.
- 최대 42억 여개의 field와 value를 저장할 수 있으며, 실제로는 레디스 노드의 메모리만큼의 제한을 따른다.
#### 데이터 저장 명령어
- HSET
  - `HSET key field value [field value ...]`
  - field-value로 구성된 데이터를 하나이상 저장할 수 있다.
  - 동일 field가 입력되면 데이터는 override 된다.
- HINCRBY
  - `HINCRBY key field increment`
  - 지정한 정수나 field의 값을 증가시킨다. 
- HGETDEL
  - `HGETDEL key FIELDS numfields field [field ...]`
  - Redis 8.0.0 버전 이후부터는 지정된 필드의 값을 가져온이후 삭제할 수 있다.


#### Field 만료시간 명령어
- HEXPIRE, HEXPIREAT, HEXPIRETIME, HPERSIST
  - Redis 7.4.0 버전 이후부터는 각 field에 TTL을 적용할 수 있으며, 만료 시간이 지나면 지정한 필드가 삭제된다.
  - TTL을 지정한 field들의 만료일을 제거하여 영구적으로 데이터를 보관하는 `HPERSIST`라는 명령어도 존재한다.
#### 데이터 읽기 명령어
- HGET
  - `HGET key field`
  - 입력된 field의 value를 반환한다.
- MHGET
  - `HMGET key field [field ...]`
  - 입력된 field들의 value를 반환하며, 입력한 field 순으로 value를 반환한다. 만약 field가 존재하지 않는다면 nil을 반환한다. 
- HGETALL
  - `HGETALL key`
  - 키에 해당하는 모든 field와 value를 반환한다.
- HKEYS
  - `HKEYS key`
  - - 키에 해당하는 모든 field를 반환한다.

> [Redis Docs > Hash](https://redis.io/docs/latest/develop/data-types/#hashes)



### Set
- Set 데이터 타입은 중복해서 데이터를 저장할 수 없으며 정렬되지 않은 데이터 모음이다.
- 최대 42억 여개의 데이터를 저장할 수 있으며, 특정 커맨드를 이용하여 두개 이상의 Set의 교집합, 합집합, 차집합등의 관계를 표현할 수 있다.

#### 데이터 저장 명령어
- SADD
  - `SADD key member [member ...]`
  - 하나 이상의 데이터를 저장할 수 있는 명령어다.
- SREM
  - `SREM key member [member ...]`
  - 하나 이상의 데이터를 삭제할 수 있는 명령어다.

#### 집합 관계를 이용할 수 있는 명령어

- 두개이상의 SET을 이용하여 집합 관계를 표현할 수 있는 명령어를 제공한다.
- SUNION
  - `SUNION key [key ...]`
  - 두개 이상의 SET 자료구조의 합집합을 구하여 데이터를 반환한다.
- SINTER
  - `SINTER key [key ...]`
  - 두개 이상의 SET 자료구조의 교집합을 구하여 데이터를 반환한다.
- SDIFF
  - `SDIFF key [key ...]`
  - 두개 이상의 SET 자료구조의 차집합을 구하여 데이터를 반환한다.

#### 데이터 읽기 명령어
- SISMEMBER
  - `SISMEMBER key member`
  - SET 데이터타입 내부에 해당하는 데이터가 존재하면 1을 없으면 0을 반환한다.
- SMEMBERS
  - `SMEMBERS key`
  - 전체 데이터를 읽을 수 있는 명령어
- SPOP
  - `SPOP key [count]`
  - SET에서 랜덤으로 데이터를 뽑아내어 데이터를 반환함과 동시에 삭제하는 명령어
- SCARD
  - `SCARD key`
  - SET 데이터타입의 사이즈를 반환한다.



> [Redis Docs > Set](https://redis.io/docs/latest/develop/data-types/#sets)


### Sorted Set
- Sorted Set 자료구조는 스코어와 값에 따라 정렬되는 데이터 모음이다.
- 스코어는 중복될수 있지만 값은 중복될 수 없다.
- 스코어는 실수 및 정수 데이터를 사용할 수 있다.

#### 데이터 쓰기 명령어
- ZADD
  - 

#### 데이터 읽기 명령어
- ZRANGE
  - 인덱스 및 스코어를 기반으로 데이터 조회
    ```sh
    ZRANGE score 0 3 ## 0 ~ 3 인덱스 데이터 조회
    ZRANGE score 1 1 ## 첫번째 인덱스 데이터 조회

    ZRANGE score 100 150 BYSCORE ## 스코어 기반 데이터 조회
    ```
> [](https://redis.io/docs/latest/develop/data-types/#sorted-sets)


### Bitmap
- 비트맵은 String 데이터타입에서 Bit 연산을 수행할 수 있도록 확장한 형태이다.
  - 때문에 String 데이터 타입도 비트맵 명령어를 수행할 수 있다.
  - 또한 두개의 비트맵을이나 String을 이용하여 비트 연산도 가능하다.
- String 자료구조를 사용하기 때문에 최대 512MB 값을 저장할 수 있는 제한사항(2의 32승, 약 40억 비트)이 존재한다.
- 비트맵의 가장 큰 장점은 특정 사용 방식에 따라서 저장공간을 줄일 수 있는 장점이 존재한다.
  - 하나의 쿠폰에 대하여 회원별 사용여부와 같은 플래그를 이용한 서비스 개발을 할때 유용하게 사용할 수 있다.

#### 데이터 쓰기 명령어
- SETBIT
  - `SETBIT key offset value`
  - 해당하는 위치에 0과 1을 저장할 수 있다.

#### 데이터 읽기 명령어
- GETBIT
  - `GETBIT key offset`
  - 해당하는 위치의 값을 읽을 수 있다.

> [Redis Docs > Bitmap](https://redis.io/docs/latest/develop/data-types/#bitmaps)












### Stream
- Stream은 데이터를 저장하고 해당 데이터를 읽어들일 수 있는 자료구조로써 Redis를 메시징 서비스로 활용할 수 있도록하는 자료구조이다.
  - 로그를 쌓는것과 같이 데이터의 업데이트하지 않고, 계속해서 추가하는 방식으로 저장(append-only)된다.
  - Stream은 데이터를 저장한 순서대로 정렬되어 있으며 해당 순서대로 데이터를 읽어들일 수 있다.
    - 카프카는 토픽 내부에 파티션이란 개념이 존재하기 때문에 파티션내의 순서는 보장할 수 있지만, 토픽내의 순서는 보장할 수 없다.
- Stream에서는 데이터를 추가하게되면 `<millisecondsTime>-<sequenceNumber>`라는 유니크한 ID를 가지게 되면 해당 ID는 직접 정의해서 데이터를 저장할 수 있다.
  - 해당 ID를 통해서 데이터를 읽거나 삭제가 가능하다.

#### 소비자 그룹
- 같은 데이터를 여러 소비자가 나눠서 
- Stream에서 소비자 그룹 내의 한 소비자는 다른 소비자가 읽지 않는 데이터만 읽어간다.
- 소비자 그룹을 이용하여 작업을 수행할 때마다 그룹 내에서 소비자를 고유하게 식별할 수 있는 이름을 지정해야한다.
- 하나의 Stream에서 서로 다른 소비자 그룹을 생성할 수 있다.
  - 서로 다른 소비자 그룹내에서는 동일한 데이터를 읽을 수 있다.
- 하나의 소비자 그룹에서 2개 이상의 stream을 리스닝할 수 있다.

### 메시징 재처리 기능
- 레디스 Stream 에서는 소비자 그룹에 속한 소비자가 메시지를 읽어가면 각 소비자별로 읽어간 메시지에 대한 리스트를 새로 생성하며 last_delivered_id 값을 업데이트한다.
- 소비자가 데이터를 가져가는 순간 소비자별로 보류 리스트를 만든다. 어떤 소비자가 어떤 데이터를 가져갔는지 인지할 수 있다.
- 데이터를 소비한 서버가 작업을 정상적으로 수행했다는 뜻의 ACK를 보내면 Stream은 보류 리스트에서 id를 삭제한다.
- 만약 소비자 서버가 메시지를 소비한뒤 작업을 수행하는 도중 장애가 발생하면 다른 소비자가 해당 서버의 보류리스트에 남아 있는 메시지가 있는지 확인한다.
- 때문에 소비자 서버가 데이터 처리를 완료한 이후에는 XACK를 주기적으로 전송하는 작업이 필요한다.
- 메시지 보증 전략
- at most once
- at least once 
- exactly once

- 메시지 재할당
- XCLAIM 커맨드를 이용하여 메시지의 소유권을 다른 소비자에게 할당할 수 있다.
  - 소비자가 메시지를 소비한 이후 데이터를 처리하던중 장애가 발생했을 때, 다른 소비자가 대신 처리할 수 있다.
  - 최소 대기 시간을 지정해야한다. 보류 상태로 머무른 시간이 최소 대기 시간을 초과한 경우에만 소유권을 변경할 수 있다.
- XAUTOCLAIM
  - 더 이상 대기 중인 보류 메시지가 없을 경우 0-0을 반환
- Stream 내에 각 메시지는 counter라는 값을 가지고 있다. XREADGROUP을 이용해 소비자에게 할당하거나 XCLAIM 커맨드릴 이용해 재할당할경우 1씩 증가한다.
- counter 값이 계속 증가함에도 메시지 처리를 못할 경우 다른 stream으로 보내 관리자가 추후에 처리할 수 있도록 할 수 있다.
- XINFO 명령어를 사용하여 stream의 상태를 확인할 수 있다.
#### 데이터 쓰기 명령어
- XADD
  - `XADD key [NOMKSTREAM] [<MAXLEN | MINID> [= | ~] threshold [LIMIT count]] <* | id> field value [field value ...]`
  - 
#### 데이터 읽기 명령어
- XREAD
  - `XREAD [COUNT count] [BLOCK milliseconds] STREAMS key [key ...] id [id ...]`
  - 하나이상의 스트림 데이터를 읽고 반환할 수 있는 명령어다.
  - BLOCK을 통한 대기
    - BLOCK 옵션은 메시지를 읽고 반환할 수 있을 때까지 대기할 수 있는 옵션이다.
    - `BLOCK 0`이라고 선언한다면 Stream에 데이터가 존재하지 않으면 읽을 수 있는 데이터가 존재할 때까지 대기하게 된다.
    - `BLOCK 1000`이라고 선언한다면 Stream에 데이터가 존재하지 않으면 1초동안 대기한 이후 null을 반환한다.
  - id를 통한 특정 데이터 반환
    - id를 이용하여 특정 데이터를 반환하거나 입력한 id 값보다 큰 id를 가진 데이터를 반환할 수 있다.
      - 0 또는 0-0을 입력하게 되면 stream에 존재하는 모든 데이터를 읽을 수 있다.
    - $이라는 특수 id를 입력하면 XREAD를 실행한 이후부터의 데이터를 읽을 수 있다.
  - COUNT를 통한 특정 데이터 반환
- XRANGE
  - `XRANGE key start end [COUNT count]`
  - id의 범위를 지정하여 데이터를 읽고 반환할 수 있는 명령어이다.
  - XREAD와의 가장 큰 차이는 XREAD에는 BLOCK 옵션이 있기 때문에 데이터가 추가될때까지 대기할 수 있지만, XRANGE는 명령어를 입력한 시점에 즉시 반환된다는 차이가 있다.
- XREVRANGE
  - `XREVRANGE key end start [COUNT count]`
  - id의 범위를 지정하여 데이터를 읽고 반환할 수 있는 명령어이다.
  - `XRANGE`의 역순으로 데이터를 읽고 반환할때 사용한다.


- XLEN
  - 스트림의 길이를 반환한다.
  - 

> https://redis.io/docs/latest/develop/data-types/#streams


#### PUB/SUB
- 레디스는 PUB/SUB 기능을 재공하며, 특정 레디스 노드에서 채널을 통해서 메시지를 발행하면 해당 채널을 구독하고 있는 레디스 노드가 메시지를 읽을 수 있다.
- 최소한의 메시지 기능만 전달기능만 제공하기 때문에, 정상적으로 모든 구독자에게 메시지가 전달되었는지는 확인할 수 없다. 구독자 또한 메시지를 어떤 발행자에 의해서 전달되는지에 대한 메타데이터 정보를 알 수 없다.
- 때문에 정합성이 중요한 데이터를 전달하기에는 적합하지 않을 수 있으며, 정합성이 중요하지 않고 오류를 고려할 필요가 없는 작업(로깅, 통계)에 사용하는 것이 좋다.

#### 발행자 명령어
- PUBLISH
  - `PUBLISH channel message`
  - 지정된 채널에 메시지를 보낸다.

#### 구독자 명령어
- SUBSCRIBE
  - `SUBSCRIBE channel [channel ...]`
  - 지정된 채널을 구독하여 메시지를 가져온다.


> [](https://redis.io/docs/latest/develop/interact/pubsub/)
### Hyperloglog



### Geospatial
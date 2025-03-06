

### String

- 최대 512MB 데이터를 저장할 수 있다.,
- Key와 Value 형태의 문자열을 저장할 수 있는 데이터 구조이다.
  - 문자열이 binary-safe하게 처리되기 때문에 JPEG 이미지와 같은 바이트값, HTTP 응답값 등 다양한 데이터를 저장하는 것도 가능하다.

#### 데이터 저장 명령어

#### 데이터 읽기 명령어

```sh
127.0.0.1:6379> SET hello world
OK
127.0.0.1:6379> GET hello
"world"
127.0.0.1:6379> SET hello wolrd2
OK
127.0.0.1:6379> GET hello
"wolrd2"
## NX: 키가 존재하지 않을 때만 값을 저장
127.0.0.1:6379> SET hello world3 NX
(nil)
127.0.0.1:6379> GET hello
"wolrd2"
## XX: 키가 존재하는 경우에만 값을 저장
127.0.0.1:6379> SET hello2 world XX
(nil)
127.0.0.1:6379> GET hello2
(nil)
```



- 단일 커맨드에서는 원자적이기 때문에 동시에 10번의 증가 값이 들어와도 결과는 10을 가지게 된다.
  - 
- 읽고 증가하고 저장하는 일련의 과정을 모두 원자적으로 발생
```sh
127.0.0.1:6379> INCR counter
(integer) 1
127.0.0.1:6379> INCR counter
(integer) 2
127.0.0.1:6379> INCRBY counter 50
(integer) 52
```


- MGET과 MSET을 이용하여 한번에 여러 키의 데이터를 저장 및 읽을 수 있다.
```sh
127.0.0.1:6379> MSET a 10 b 20 c 30
OK
127.0.0.1:6379> MGET a b
1) "10"
2) "20"
127.0.0.1:6379> MGET a b cc
1) "10"
2) "20"
3) (nil)
127.0.0.1:6379> MGET a b c
1) "10"
2) "20"
3) "30"
127.0.0.1:6379> 
```

> [Redis > String](https://redis.io/docs/latest/develop/data-types/#strings)


### List

```sh
127.0.0.1:6379> LPUSH collections A
(integer) 1
127.0.0.1:6379> RPUSH collections B
(integer) 2
127.0.0.1:6379> LRANGE collections 0 -1
1) "A"
2) "B"
127.0.0.1:6379> LPUSH collections A
(integer) 3
127.0.0.1:6379> LRANGE collections 0 -1
1) "A"
2) "A"
3) "B"
127.0.0.1:6379> LRANGE collections 0 1
1) "A"
2) "A"

127.0.0.1:6379> LPOP collections
"A"
127.0.0.1:6379> RPOP collections
"B"
127.0.0.1:6379> LANGE collections 0 -1
(error) ERR unknown command 'LANGE', with args beginning with: 'collections' '0' '-1' 
127.0.0.1:6379> LRANGE collections 0 -1
1) "A"
```

LPUSH 와 LTRIM을 이용하여 고정된 크기의 대기열을 만들 수 있다.
LINDEX
- 
LTRIM
- 인덱스 범위만큼의 데이터를 남겨두고 나머지는 버린다.
LSET
- 원하는 인덱스에 데이터를 덮어쓴다. 만약 리스트에 해당하는 인덱스가 없으면 `ERR index out of range`에러가 발생하게 된다.
- 
> [Redis > List](https://redis.io/docs/latest/develop/data-types/lists/)


#### 데이터 저장 명령어

#### 데이터 읽기 명령어


### Hash
- Hash는 필드-값을 가진 집합구조이다.

> https://redis.io/docs/latest/develop/data-types/#hashes

#### 데이터 저장 명령어

#### 데이터 읽기 명령어


### Set
- Set 자료구조는 중복해서 데이터를 저장할 수 없으며 정렬되지 않은 데이터 모음이다.
- 두개 이상의 Set을 이용하여 교집합, 합집합, 차집합등의 관계를 표현할 수 있다.

#### 데이터 저장 명령어
- SADD
  - 하나이상의 데이터를 저장할 수 있는 명령어
- SREM
  - 데이터를 삭제할 수 있는 명령어
#### 데이터 읽기 명령어
- SMEMBERS
  - 전체 데이터를 읽을 수 있는 명령어
- SPOP
  - SET 자료구조 내부에서 랜덤으로 데이터를 뽑아내어 데이터를 반환함과 동시에 삭제하는 명령어
- SUNION
  - 두개 이상의 SET 자료구조의 합집합을 읽을 수 있는 명령어
- SINTER
  - 두개 이상의 SET 자료구조의 교집합을 읽을 수 있는 명령어
- SDIFF
  - 두개 이상의 SET 자료구조의 차집합을 읽을 수 있는 명령어

> [](https://redis.io/docs/latest/develop/data-types/sets/)


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
- 비트맵은 String 자료구조에서 Bit 연산을 수행할 수 있도록 확장한 형태이다.
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
- BITCOUNT
  - BITCOUNT key [start end [BYTE | BIT]]
  - 1로 저장된 비트의 개수를 카운팅할 수 있다.

> [](https://redis.io/docs/latest/develop/data-types/#bitmaps)

### Stream
- 레디스를 메시지 브로커로서 사용할 수 있게 하는 자료구조이다.
  - 메시지 처리를 못하는 상황이 오더라도 어딘가에 해당 메시지를 저장한 이후 후처리를 할 수 있도록 하는 것이 메시지 브로커의 역할이다.
- 실시간 이벤트 혹인 로그성 데이터를 저장할 수 있다.

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
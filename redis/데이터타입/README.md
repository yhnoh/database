

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


### Redis 트랜잭션
- 트랜잭션이란 트랜잭션 시작과 종료 지점 사ㅇ에 일어는 모든 작업을 반영하거나 롤백할 수 있는 하나의 작업 단위이다.
- Redis는 `MULTI, EXEC, DISCARD, WATCH`라는 네가지 명령어를 통해서 이루어진다.

### 트랜잭션 사용법
- Redis 트랜잭션의 사용 방법은 간단하다.
1. multi 명령어를 사용하면 트랜잭션이 시작된다.
2. 트랜잭션 내부에서 명령어를 수행할 때마다 대기열(Queue)에 명령어를 쌓는다.
3. exec 명령어를 사용해서 대기열에서 대기하고 있는 명령어를 수행하게 되며 트랜잭션이 종료된다.
4. discard 명령어를 사용하게 된다면 대가열에 존재하는 명령어를 수행하지 않는다. 마치 롤백과 같은 기능을 수행한다.

```sh
127.0.0.1:6379> multi
OK

127.0.0.1:6379(TX)> incr foo
QUEUED

127.0.0.1:6379(TX)> incr foo
QUEUED

127.0.0.1:6379(TX)> exec
1) (integer) 1
2) (integer) 2
```

> https://redis.io/docs/latest/commands/multi/ <br/>
> https://redis.io/docs/latest/commands/exec/ <br/>

### 2. 트랜잭션을 사용하는 도중 에러가 발생할 때 Redis가 가지는 특징
- 일반적인 RDBMS에서는 트랜잭션을 사용하는 도중 에러가 발생할 경우 실행한 모든 작업을 롤백시킨다.
- 하지만 redis는 트랜잭션을 시작하고 중간에 에러가 발생하는 경우 트랜잭션의 일부 작업이 반영되거나 전부 반영되지 않는 경우가 있다.

#### 2.1. 에러 발생으로 인한 전체 작업을 수행하지 않는 경우
- 트랜잭션을 시작한 이후 작성된 명령어가 문법적인 오류(syntactically wrong)를 발생 시키거나, 레디스 서버 자체의 문제로 인해서 트랜잭션의 전체 작업이 반영되지 않는다.
  - 문법적 오류 : 하나의 인자만 받을 수 있는 명령어에 여러 인자를 넘겨주거나 인자를 넘겨주지 않은 경우, 명령어에 오타가 잇을 경우...
  - 레디스 서버 문제: 메모리 부족 현상... 

```sh
## 트랜잭션 시작
127.0.0.1:6379> multi
OK
## 작업 1
127.0.0.1:6379(TX)> set a b
QUEUED
## 작업 2, 문법 오류 발생
127.0.0.1:6379(TX)> keys
(error) ERR wrong number of arguments for 'keys' command
## exec 수행
127.0.0.1:6379(TX)> exec
(error) EXECABORT Transaction discarded because of previous errors.

## 문법 오류로 인한 작업 반영 X
127.0.0.1:6379> get a
(nil)
```

#### 2.2. 에러가 발생하였지만 일부 작업이 반영된 경우
- 트랜잭션을 시작한 이후 키에 대하여 잘못된 명령어를 수행하는 경우 트랜잭션의 일부 작업이 반영될 수 있다.
  - Strings 데이터 타입을 가진 키에 list 데이터 타입과 관련된 명령어를 수행하는 경우
- 일반적으로는 중간에 명령어가 실패했을 경우 수행한 명령어가 전부 수행되지 않기를 기대하지만, redis는 위와 같은 경우 일부 작업이 반영될 수 있기 때문에 신경을 쓰고 개발하는 것이 좋다.

```sh
## 트랜잭션 시작
127.0.0.1:6379> multi
OK
## 작업 1, Strings 데이터 타입을 가진 키 저장
127.0.0.1:6379(TX)> set a a
QUEUED
## 작업 2, Strings 데이터 타입을 가진 키에 List 데이터 타입과 관련된 명령어를 수행
127.0.0.1:6379(TX)> lpop a
QUEUED
## 작업 3, Strings 데이터 타입을 가진 키 저장 
127.0.0.1:6379(TX)> set b b
QUEUED
127.0.0.1:6379(TX)> exec
1) OK
2) (error) WRONGTYPE Operation against a key holding the wrong kind of value
3) OK

## 에러가 발생하였지만, set 명령어가 정상적으로 동작한 것을 확인 가능
127.0.0.1:6379> get a
"a"
127.0.0.1:6379> get b
"b"
```

> [redis > transaction](https://redis.io/docs/latest/develop/interact/transactions/)


### 3. CAS(check-and-set)을 사용한 동시 업데이트 방지를 위한 낙관적 락
- WATCH 명령어를 사용하면 키의 값이 변경되는 것을 감지할 수 있다.
- WATCH 명령어를 수행한 이후 EXEC 명령어 이전에 키의 값이 변경되었음을 감지하였을 때, 수행중인 트랜잭션이 중단되며 EXEC는 Null 값을 반환하여 트랜잭션이 실패했음을 알리게 된다.
- EXEC, DISCARD 명령어를 통해서 WATCH 명령어를 해제할 수 도 있지만, UNWATCH 명령어를 통해서 WATCH를 통해 감시되는 키에 대하여 해제할 수 도 있다.

```sh
## connection 1
127.0.0.1:6379> watch test
OK
127.0.0.1:6379> multi
OK
127.0.0.1:6379(TX)> set test test1
QUEUED

## connection 2
127.0.0.1:6379> set test test2
OK

## connection 1
127.0.0.1:6379(TX)> exec
(nil)
127.0.0.1:6379> get test
"test2"
```
- 위에서 확인할 수 있듯이 connection1에서 WATCH 명령어를 통해서 키의 값이 변경되는 것을 감지할 수 있도록 한 이후, EXEC가 수행되기 이전에 connection2에서 값을 변경하는 것을 확인할 수 있다.
- 이후 connection1에서 값을 변경하게 될경우 null을 응답받았다는 것을 확인할 수 있다. 이는 곳 트랜잭션의 실패를 의미한다.
- 만약 connection1이 실패한 이후에 해당 트랜잭션이 정상적으로 수행되기를 원한다면 성공할 때 까지 작업을 반복하는 것이 좋다.


> [](https://sabarada.tistory.com/177) <br/>
> [](https://velog.io/@cmsskkk/redis-transaction-spring-and-lua-pipeline) <br/>
> [redis > transaction](https://redis.io/docs/latest/develop/interact/transactions/)

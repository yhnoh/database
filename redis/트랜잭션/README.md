## 트랜잭션(Transaction)이란?
- 트랜잭션은 ***하나이상의 연산을 하나의 작업 단위로 만들는 것을 의미***한다.
- 여러 연산들을 하나의 작업 단위로 만들기 때문에 모든 연산이 성공적으로 수행되거나, 하나라도 실패하게 될경우 이전상태로 롤백되어 ***작업 단위의 데이터 일관성을 보장***할 수 있다.

## 락(Lock)이란?
- 여러 클라이언트가 동시에 동일한 데이터에 접근하여 연산을 수행하고자할때, 여러 클라이언트가 ***동시에 동일한 데이터를 연산하지 못하도록하기 위한 동시성 제어(Concurrency Control) 메커니즘***이다. 
- 락을 통한 동시성 제어는 크게 두가지 기준으로 나눌 수 있다.
  - 여러 클라이언트가 동시에 데이터를 연산할때 어디까지 허용할 것인가?
  - 여러 클라이언트가 동시에 데이터를 연산할때 어떻게 처리할 것인가?

### 데이터 접근 권한에 따른 공유 락(Shared Lock, Read Lock)과 베타 락(Exclusive Lock, Write Lock)

- 공유 락은 동시 읽기는 허용하지만 쓰기 작업을 제한한다.
  - 공유 락이 걸려있는 데이터는 공유 락을 허용하지만 베타 락은 허용하지 않는다.
- 베타 락은 동시 읽기/쓰기 작업을 제한한다. 
  - 베타 락이 걸려있는 데이터는 공유 락 베타 락 둘다 허용하지 않는다.

### 동시성 충돌을 처리하는 방식에 따른 비관적 락(Pessimistic Lock)과 낙관적 락(Optimistic Lock) 

- 비관적 락은 데이터가 동시에 접근하여 충돌할 가능성이 높다고 가정하여, ***작업 시작시 미리 락을 걸어 다른 클라이언트가 접근하지 못하도록 한다.***
  - 데이터 일관성 보장 및 개발 용이성이 좋지만, 락을 통한 작업 선점으로 인한 성능 저하 및 데드락이 발생할 가능성이 높다.
- 낙관적 락은 데이터가 동시에 접근하여 충돌할 가능성이 낮다고 가정하여, ***작업 시작시 락을 걸지 않고 작업 완료 시점에 다른 클라이언트가 동일 데이터를 변경했는지 확인***한다. 만약 다른 클라이언트에 의한 데이터 변경이 확인되면 작업이 실패한다.
  - 락을 미리 걸지 않기 때문에 높은 성능을 기대할 수 있으며 데드락 발생 가능성이 없지만, 충돌 시 재시도 로직을 구현해야하는 개발 복잡성이 증가한다.



## Redis의 트랜잭션
- Redis는 트랜잭션이 시작된 이후 Redis 명령어들을 큐에 적재한 이후, 큐에 적재된 명령어를 수행하거나 큐에 적재된 명령어를 취소한다.
- 이로 인해서 Redis의 트랜잭션은 RDBMS에서 제공하는 트랜잭션과 조금 다른 특성이 있다.
- 원자성
  - 일반적인 RDBMS에서는 트랜잭션 내부에서 에러가 발생할 경우 실행한 모든 작업을 롤백시킨다.
  - 하지만 Redis는 트랜잭션을 시작하고 중간에 에러가 발생하는 경우 트랜잭션의 일부 작업이 반영되거나 전부 반영되지 않는 경우가 있다.
- 격리 수준과 락 
  - RDBMS는 트랜잭션 내에서의 데이터 보장을 위한 다양한 격리 수준(Isolation Level) 및 락을 통해서 동시성 이슈를 해결할 수 있는 다양한 메커니즘을 제공한다.
  - Redis는 트랜잭션 시작 시점에 읽은 데이터가 트랜잭션 완료 시점에도 유효한지는 보장할 수 없다. 즉 다른 클라이언트로 인하여 변경된 값을 읽어올 수 있다. 이를 해소하기 위하여 Redis는 낙관적 락을 제공하며 읽은 데이터의 변경 점이 존재한다면 해당 트랜잭션은 실패한다.


### Redis 트랜잭션 사용해보기
- Redis는 `MULTI, EXEC, DISCARD`라는 세가지 명령어를 통해서 트랜잭션을 지원한다.
  - MULTI: 트랜잭션 시작
  - EXEC: 트랜잭션 내부에서 큐에 적재된 명령어를 수행
  - DISCARD: 트랜잭션 내부에서 큐에 적재된 명령어를 취소
```sh
## EXEC 명령어로 큐에 적재된 명령어 수행
127.0.0.1:6379> MULTI
OK
127.0.0.1:6379(TX)> INCR foo
QUEUED
127.0.0.1:6379(TX)> INCR foo
QUEUED
127.0.0.1:6379(TX)> GET foo
QUEUED
127.0.0.1:6379(TX)> EXEC
1) (integer) 1
2) (integer) 2
3) "2"

## DISCARD 명령어로 큐에 적재된 명령어 취소
127.0.0.1:6379> MULTI
OK
127.0.0.1:6379(TX)> INCR foo
QUEUED
127.0.0.1:6379(TX)> INCR foo
QUEUED
127.0.0.1:6379(TX)> GET foo
QUEUED
127.0.0.1:6379(TX)> DISCARD
OK

```
1. `MULTI` 명령어를 사용하면 트랜잭션이 시작된다.
2. 트랜잭션 내부에서 명령어를 수행할 때마다 대기열(Queue)에 명령어를 적재한다.
3. `EXEC` 명령어를 사용해서 대기열에서 대기하고 있는 명령어를 수행하게 되며 트랜잭션이 종료된다.
4. `DISCARD` 명령어를 사용하게 되면 대가열에 존재하는 명령어를 취소한다.

### Redis 트랜잭션내에서 에러 발생한다면?
- Redis는 트랜잭션을 시작하고 중간에 에러가 발생하는 경우 트랜잭션의 일부 작업이 반영되거나 전부 반영되지 않는 경우가 있다고 했다.
- 어떤 경우에 반영되고 어떤 경우에 반영이되지 않을까?

#### 트랜잭션 내부에서 에러 발생으로 인하여 작업이 반영되지 않는 경우
- EXEC 명령어를 수행하기 전에 발생하는 에러의 경우 트랜잭션은 취소된다. 트랜잭션이 취소되기 때문에 EXEC 명령어를 수행하더라도 DISCARD 되면서 대기열에 존재하는 명령어는 취소된다.
- EXEC 명령어를 수행하기 전에 발생하는 에러는 문법적 오류(Syntactically Wrong)나 레디스 서버 문제로 발생할 수 있다.
  - 문법적 오류 : 하나의 인자만 받을 수 있는 명령어에 여러 인자를 넘겨주거나 인자를 넘겨주지 않은 경우, 명령어에 오타가 있을 경우...
  - 레디스 서버 문제: 메모리 부족 현상...
```sh
## 트랜잭션 시작
127.0.0.1:6379> MULTI
OK
## 작업 1
127.0.0.1:6379(TX)> SET a b
QUEUED
## 작업 2, 문법 오류 발생
127.0.0.1:6379(TX)> KEYS
(error) ERR wrong number of arguments for 'keys' command
## exec 수행
127.0.0.1:6379(TX)> EXEC
(error) EXECABORT Transaction discarded because of previous errors.

## 문법 오류로 인한 작업 반영 X
127.0.0.1:6379> GET a
(nil)
```
#### 트랜잭션 내부에서 에러 발생으로 인하여 일부 작업이 반영되는 경우
- EXEC 명령어를 수행한 후에 발생하는 에러의 경우 발생한 에러를 제외한 나머지 작업은 반영된다.
  - 트랜잭션을 시작한 이후 키에 대하여 잘못된 명령어를 수행하는 경우 트랜잭션의 일부 작업이 반영될 수 있다.

```sh
## 트랜잭션 시작
127.0.0.1:6379> MULTI
OK
127.0.0.1:6379(TX)> SET a a
QUEUED
## Strings 데이터 타입을 가진 키에 List 데이터 타입과 관련된 명령어를 수행
127.0.0.1:6379(TX)> LPOP a
QUEUED
127.0.0.1:6379(TX)> SET b b
QUEUED
127.0.0.1:6379(TX)> EXEC
1) OK
2) (error) WRONGTYPE Operation against a key holding the wrong kind of value
3) OK

## 에러가 발생하였지만 에러를 제외한 나머지 명령어 정상 수행 확인
127.0.0.1:6379> GET a
"a"
127.0.0.1:6379> GET b
"b"
```
- 큐에 적재된 내부 명령어 중에 일부 오류가 발생하여도 나머지 명령어들은 정상적으로 수행된것을 확인할 수 있다.
  > It's important to note that even when a command fails, all the other commands in the queue are processed – Redis will not stop the processing of commands.
- 큐에 적재된 명령어를 취소하는 것은 가능하지만 롤백을 제공해주지 않는다.
  > is does not support rollbacks of transactions since supporting rollbacks would have a significant impact on the simplicity and performance of Redis.


### Redis에서 제공하는 동시성을 제어하기 위한 락
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

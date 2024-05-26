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

### 트랜잭션을 사용하는 도중 에러가 발생하는 경우
- 트랜잭션을 수행하는 도중 발생할 수 있는 에러의 가지수는 exec 명령어를 수행하기 전/후이다.
- exec 명령어를 수행하기 전에 명령어가 잘못되었거나 서버의 메모리를 초과해서 사용하는 경우 에러가 발생할 수 있다.
- 키에 대해서 잘못된 값을 작성하게 될 경우 exec 명령어를 호출된 이후에 에러가 발생할 수 있다.
- redis에서 트랜잭션의 조금 특이한 점은 exec 명령어가 호출된 이후 에러가 발생하는 경우이다.
  - 중간의 명령어가 실패하여도 대기열에 존재하는 모든 명령어가 동작한다.
  - 트랜잭션 중간에 특정 키에대해서 잘못된 명령어를 사용하더라도 그 이외의 명령어는 정상으로 수행되는 것을 확인할 수 있다.
  - 일반적으로는 중간에 명령어가 실패했을 경우, 이를 감지하여 롤백 처리가 정상적으로 이루어지기를 바라지만 redis에서는 이러한 방식들을 애플리케이션에서 해결해아한다.

```sh
## 트랜잭션 시작
127.0.0.1:6379> multi
OK

127.0.0.1:6379(TX)> set foo foo
QUEUED

## 문자열 키에 대하여 잘못된 명령어 사용
127.0.0.1:6379(TX)> lpop foo
QUEUED

127.0.0.1:6379(TX)> set foo bar
QUEUED

## 트랜잭션 종료
127.0.0.1:6379(TX)> exec
1) OK
2) (error) WRONGTYPE Operation against a key holding the wrong kind of value
3) OK

## set 명령어가 정상적으로 수행되었다는 것을 확인 가능
127.0.0.1:6379> get foo
"bar"
```



### 락
WATCHed 키는 변경 사항을 감지하기 위해 모니터링됩니다. EXEC 명령 이전에 하나 이상의 감시된 키가 수정되면 전체 트랜잭션이 중단되고 EXEC는 Null 응답을 반환하여 트랜잭션이 실패했음을 알립니다.

> [[redis] 트랜잭션(Transaction) - 이론편](https://sabarada.tistory.com/177)
> [redis > transaction](https://redis.io/docs/latest/develop/interact/transactions/)

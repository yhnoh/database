
### 1. 트랜잭션 격리 수준

---

- 트랜잭션 격리 수준이란 트랜잭션이 동시에 처리될 때, ***특정 트랜잭션이 다른 트랜잭션에서 변경하거나 조회하는 데이터를 볼 수 있게 허용할지 말지를 결정***하는 것이다.
- 격리 수준에 따라서 4가지 정도로 분류가 된다. 순서가 밑에 있을 수록 격리 수준이 높다.
  - READ UNCOMMITED
  - READ COMMITTED
  - REPEATABLE READ
  - SERIALIZABLE
  - 격리 수준이 높다는 것은 ....

### 2. READ UNCOMMITED

---

- READ UNCOMMITED라는 의미에서 알수 있듯이, ***커밋 되지 않은 데이터를 다른 곳에서 읽어 올 수 있다는 의미***이다.
  - 커밋 되지 않은 데이터라는 의미는 해당 트랜잭션이 완료되지 않았다는 의미로 해석할 수 있다.
  - 즉 A라는 트랜잭션이 완료되지 않았음에도 불구하고, 다른 트랜잭션에서 A가 작업중인 데이터를 조회할 수 있다.
- 위와 같은 행위를 Dirty Read라고 하며 READ UNCIMMITED는 Dirty Read가 허용되어 있는 격리 수준이다.
- READ UNCOMMITED 트랜잭션 격리 수준의 경우에는 데이터 정합성의 문제가 발생할 확률이 높기 때문에 잘 사용되지 않는다.
  - 예를 들어 회원가입 트랜잭션이 종료되지 않았음에도 불구하고, 회원이 조회될 수 있다.
> ***왠만한 경우가 아니고서야 READ COMMITTED 이상의 격리수준을 사용하기를 권장한다.***

#### 2.1. Dirty Read 체험해보기

1. Dirty Read 확인을 위한 테이블 생성
    ```sql
    create table read_uncommited
    (
        name varchar(20) not null
            primary key
    );
    ```
2. 트랜잭션 A에서 table에 insert 작업 이후 10초 동안 sleep한 이후 트랜잭션 종료
    ```sql
    set session autocommit = false;
    start transaction;

    insert into read_uncommited values ('name1');

    -- 다른 트랜잭션에서 해당 데이터를 읽을 수 있는지 확인하기 위하여 sleep문 작성
    do sleep(10);

    commit;
    ```
3. 트랜잭션B는 트랜잭션A가 커밋되기 전에 데이터를 읽을 수 있는지 확인
    ```sql
    -- READ UNCOMMITTED 격릭 수준 트랜잭션 시작
    set session autocommit = false;
    set session transaction isolation level READ UNCOMMITTED;
    start transaction ;

    select * from read_uncommited where name = 'name1';

    commit;
    ```

### 3. READ COMMITED

---

- READ COMMITED 격리 수준에서는 ***하나의 트랜잭션에서 데이터를 변경했어도 commit이 완료된 데이터만 다른 트랜잭션에서 조회할 수 있다.***
  - 즉, 더티 리드가 발생하지 않는 트랜잭션 격리 수준이다.
- 하지만 READ COMMITED 격리 수준에서도 Non-Repeatable Read 데이터 부정합성 문제가 발생한다.
  - ***Non-Repeatable Read는 하나의 트랜잭션에서 동일한 결과값을 보장하지 않는다.***
  - 예를 들어 보자면 이러한 문제가 발생한다.
    1. A트랜잭션에서 select 문을 실행하였다.
    2. A트랜잭션에서 결과 값을 얻을 수 없었다.
    3. B트랜잭션에서 insert문을 통해서 데이터를 삽입을 하고 커밋되었다.
    4. A트랜잭션에서 동일한 테이블에서 select 문을 실행하였다.
    5. A트랜잭션에서 결과 값을 얻을 수 있었다.
    6. A,B 모두 트랜잭션이 종료되었다.
  - 위 예제에서 A트랜잭션은 동일 테이블을 두번 조회하였는데 첫 번째 select 문은 데이터가 존재하지 않았고, 두 번째 select문은 데이터가 존재하였다.
  - 하나의 트랜잭션 내에서 똑같은 select쿼리를 실행시켰을 때, 항상 동일한 결과값을 가져와야 한다는 REPEATABLE READ 정합성에 어긋난다.
- Non-Repeatable Read 문제가 발생하면 ***하나의 트랜잭션에서 동일 데이터를 여러번 읽고 변경하는 작업에서 문제가 발생할 수 있다.***
  - 동일 데이터를 수정 및 변경하는데 해당 트랜잭션의 시간이 길 수록 Non-Repeatable Read가 발생할 가능성이 크다.
  - 또한 해당 뮨제가 발생하였을 때 어떠한 문제가 일어났는지 파악하기가 힘들 수 있다.
  - 요청에 대한 응답이 빨라야하는 웹프로그래밍에서는 크게 문제될게 없을 수 있지만, 응답 지연이 길어지거나 많은 요청이 몰릴 수록 문제가 발생할 수 도 있다.

#### 3.1. Non-Repeatable Read 체험해보기

1. Non-Repeatable Read 확인을 위한 테이블 생성
    ```sql
    create table read_commited(
        name varchar(20) not null primary key
    );
    ```
2. 트랜잭션 A에서 Non-Repeatable Read 확인해보기
    ```sql
    set session autocommit = false;
    set session transaction isolation level READ COMMITTED;
    start transaction;

    -- 동일 트랜잭션인데 데이터가 존재하지 않음
    select * from read_commited where name = 'name1';

    -- Non-Repeatable Read 문제를 확인하기 위하여 sleep
    do sleep(10);

    -- 동일 트랜잭션인데 데이터가 존재함
    select * from read_commited where name = 'name1';
 
    commit;
    ```
    - 첫번째 select문의 경우에는 데이터가 존재하지 않았다가 다른 트랜잭션에서 데이터를 삽입했을 때 두번째 select문에서 데이터가 존재하는 것을 확인하기 위하여 sleep문을 활용한다.
3. 트랜잭션B는 트랜잭션A의 두번째 select문이 실행되기 전에 데이터 삽입하기
    ```sql
    set session autocommit = false;
    start transaction;

    insert into read_commited values ('name1');

    commit;
    ```





### 4. REPEATABLE READ

---

- REPEATABLE READ 격리 수준은 ***동일 트랜잭션 내에서 동일한 결과를 보여줄 수 있는 트랜잭션 격리 수준***이다.
  - 동시성 제어 방식인 MVCC를 이용하기 때문에 Non-Repeatable Read가 발생하지 않는다.
  - 언두 영역에 백업된 이전 데이터를 저장하고, 이를 이용하여 동일 트랜잭션에서 동일한 결과를 보여준다.
    - 언두 영역이 단순히 트랜잭션의 데이터 정합성을 위해서만 사용되지는 않는다.
      - 예를 들어 롤백의 경우 레코드를 다시 원복 시키기 위하여 언두 영역에서 이전 데이터를 가져온다.
    - 때문에 REPEATABLE READ 이상의 격리수준에서만 언두 영역을 활용하는 것은 아니다.

#### 4.1. Repeatable Read 격리 수준에서 Non-Repeatable Read가 발생하지 않는지 확인해보자

1. Non-Repeatable Read가 발생하지 않는 확인하기 위하여 테이블 생성 및 데이터 삽입
    ```sql
    create table repeatable_read (
        id int not null auto_increment primary key,
        name varchar(20) not null
    );

    insert into repeatable_read(name) values ('name1');
    ```

2. 트랜잭션 A에서 Non-Repeatable Read가 발생하지 않는지 확인해보기
    ```sql
    set session autocommit = false;
    set session transaction isolation level REPEATABLE READ;
    start transaction ;

    -- 수정되기전 데이터 select
    select * from repeatable_read;

    -- Non-Repeatable Read 문제가 안일어나는지 확인하기 위하여 sleep
    do sleep(10);

    -- 언두 영역에서 조회하였기 때문에 데이터가 수정되었어도 동일한 결과 select
    select * from repeatable_read;

    commit;
    ```
3. 트랜잭션B는 트랜잭션A의 두번째 select문이 실행되기 전에 데이터 수정하기
    ```sql
    set session autocommit = false;
    start transaction;

    update repeatable_read set name = 'name2' where name = 'name1';

    commit;
    ```

#### 4.2. Repeatable Read 격리 수준에서 어떻게 Non-Repeatable Read가 발생하지 않는 걸까? 

![](./img/repeatable_read.png.png)


- ***Repeatable Read는 트랜잭션 번호를 조회하여 먼저 실행된 트랜잭션의 데이터만 조회***한다.
- ***테이블 레코드에 먼저 실행된 트랜잭션 데이터가 존재하지 않는다면 이전 트랜잭션 번호를 가진 언두 로그에서 데이터를 조회***한다.


#### 4.3. Repeatable Read는 데이터 부정합이 발생하지 않는가?

- Repeatable Read가 MVCC를 이용한다고 하여도 데이터 부정합이 일어날 수 있다.
- Repeatable Read가 일어나지 않는 격리 수준에서 동일한 결과를 보여주지 않는 Phantom Read가 발생한다.
  - 주로 데이터를 추가하거나 삭제할때 발생한다.
- 주로 쓰기나 읽기 잠금을 통해서 레코드를 읽을 때 해당 문제가 발생한다.
> MySQL의 경우 갭 락 덕분에 Phantom Read 현상이 잘 발생하지 않는다.
 
#### 4.4. MySQL에서 Phantom Read 현상 확인해보기

1. 데이터 삭제
```sql
-- 기존 데이터 삭제
delete from repeatable_read;
```

1. 트랜잭션 A에서 Phantom Read 데이터 부정합을 위하여 두번째 조회시 for update문 추가
```sql
set session autocommit = false;
set session transaction isolation level REPEATABLE READ;
start transaction ;

-- 데이터 조회 안됨
select * from repeatable_read where name = 'name1';

--  Phantom Read 데이터 부정합 발생 확인을 위하여 sleep
do sleep(10);

-- select... for update 쓰기 잠금으로 인한, Phantom Read 데이터 부정합 발생
select * from repeatable_read where name = 'name1' for update;

commit;
```
  - select... for update 쿼리는 select하는 레코드에 쓰기 잠금을 걸어야하 하는데, 언두 레코드에는 잠금을 걸 수 없다.
  - 때문에 언두 영역으 변경 전 데이터를 가져오는 것이 아니라 현재 레코드의 값을 가져오게 되는 것이다.

3. 트랜잭션B는 트랜잭션A의 두번째 select문이 실행되기 전에 데이터 삽입하기

```sql
set session autocommit = false;
start transaction;

insert into repeatable_read(name) value ('name1');

commit;
```

### 5. SERIALIZABLE

---

- 가장 단순한 격리 수준이면서 동시에 가장 엄격한 격리 수준이다.
  - 그만큼 ***동시 처리 성능도 다른 트랜잭션 격리 수준보다 떨어진다.***
- Serializable 트랜잭션 격리 수준에서 동시 처리 성능이 떨어지는 이유는 InnoDB 테이블에서도 ***읽기 작업도 공유 잠금(읽기 잠금)을 획득해야만 하기 때문이다.***
  - 이는 하나의 트랜잭션이 읽고 쓰는 작업을 진행하는 동안, 다른 트랜잭션에서는 절대 접근할 수 없다는 것이다.
  


> Real MySql 8.0 개발자와 DBA를 위한 MySQL 실전 가이드, 백은비,이성욱, P176-183 <br/>
> https://en.wikipedia.org/wiki/Isolation_(database_systems) <br/>
> https://mangkyu.tistory.com/299 <br/>
> https://mangkyu.tistory.com/300 <br/>
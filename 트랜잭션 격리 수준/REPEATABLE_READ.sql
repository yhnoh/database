-- 테이블 생성
create table member (
    id int not null primary key,
    member_id varchar(20) not null,
    member_name varchar(20) not null
);


-- ############### 세션 A ###############

set session autocommit = false;
set session transaction isolation level REPEATABLE READ;
start transaction;

-- 트랜잭션 격리 수준 확인
SELECT @@tx_isolation;

-- 데이터가 존재하지 않음
select * from member;

-- Non-Repeatable Read 문제가 안일어나는지 확인하기 위하여 sleep
do sleep(10);

-- 데이터가 삽입되었어도 동일한 결과를 얻기 위해서 언두 로그에서 조회
select * from member;

commit;



-- ############### 세션 B ###############


set session autocommit = false;
start transaction;

SELECT @@tx_isolation;

insert into member values (1, 'memberId1', 'memberName1');

commit;


-- ############### Phantom Read 확인하기 ###############



-- ############### 세션 A ###############

set session autocommit = false;
set session transaction isolation level REPEATABLE READ;
start transaction;

-- 트랜잭션 격리 수준 확인
SELECT @@tx_isolation;

-- 데이터가 존재하지 않음
select * from member;

-- Phantom Read 문제가 일어나는지 확인하기 위하여 sleep
do sleep(10);

-- Phantom Read로 인해서 데이터 존재
select * from member where id >= 0 for update;

commit;


-- ############### 세션 B ###############

set session autocommit = false;
start transaction;

SELECT @@tx_isolation;

insert into member values (1, 'memberId1', 'memberName1');

commit;




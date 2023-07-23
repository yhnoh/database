-- 테이블 생성
create table member (
    id int not null primary key,
    member_id varchar(20) not null,
    member_name varchar(20) not null
);

-- ############### 세션 A ###############
set session autocommit = false;
set session transaction isolation level READ COMMITTED;
start transaction;

-- 트랜잭션 격리 수준 확인
SELECT @@tx_isolation;

-- 동일 트랜잭션인데 데이터가 존재하지 않음
select * from member;

-- Non-Repeatable Read 문제를 확인하기 위하여 sleep
do sleep(10);

-- 동일 트랜잭션인데 데이터가 존재함
select * from member;

commit;

-- ############### 세션 B ###############
set session autocommit = false;
start transaction;

SELECT @@tx_isolation;

insert into member values (1, 'memberId1', 'memberName1');

commit;



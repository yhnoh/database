
-- 테이블 생성
create table member (
    id int not null primary key,
    member_id varchar(20) not null,
    member_name varchar(20) not null
);

-- ############### 세션 A ###############
set session autocommit = false;
start transaction;

-- 트랜잭션 격리 수준 확인, 디폴트는 REPEATABLE READ
SELECT @@tx_isolation;

insert into member values (1, 'memberId1', 'memberName1');

-- 세션B에서 해당 데이터를 읽을 수 있는지 확인하기 위하여 sleep문 작성
do sleep(10);

commit;


-- ############### 세션 B ###############
set session autocommit = false;
set session transaction isolation level READ UNCOMMITTED;
start transaction;

-- 트랜잭션 격리 수준 확인
SELECT @@tx_isolation;

select * from member;

commit;

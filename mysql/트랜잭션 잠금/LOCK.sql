-- 테이블 생성
create table member (
    id int not null primary key,
    member_id varchar(20) not null,
    member_name varchar(20) not null
);

-- member 데이터 삽입
insert into member values (1, 'memberId1', 'memberName1');

-- ###################### 사용자 A ##########################

set session autocommit = false;
start transaction;

SELECT @@tx_isolation;

update member
set member_id = 'memberId2'
where id = 1;


do sleep(30);
commit;


-- ###################### 사용자 B ##########################

set session autocommit = false;
start transaction;

SELECT @@tx_isolation;

update member
set member_id = 'memberId3'
where id = 1;


do sleep(30);
commit;


-- ###################### 사용자 C ##########################

set session autocommit = false;
start transaction;

SELECT @@tx_isolation;

update member
set member_id = 'memberId4'
where id = 1;


do sleep(30);
commit;


-- process list 확인
show processlist;


-- 실행중인 프로세스 id 값을 통해서 종료
kill [process_id]

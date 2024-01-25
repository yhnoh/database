### 1. 해시 조인

- MySQL 에서는 8.0.18버전 미만에서는 Hash Join을 지원하지 않고, NL 조인의 기능을 개선한 BKA(Batched Key Access), BNL(Block Nested Loop) 방식을 활용하였지만 8.0.18버전 이상부터는 Hash Join을 지원하게 되었다.
    > **By default, MySQL (8.0.18 and later) employs hash joins whenever possible**
- Sort Merge Join의 경우 양쪽 테이블을 정렬는 것의 부담과 두 테이블이 정렬이 될때까지 조인을 수행하지 않지만, Hash Join은 이러한 부담 없이 수행된다.
- 

#### 1.1. 해시 조인 처리 단계

- 해시 조인은 두 단계를 통해서 진행된다
  - Build 단계: 작은 쪽 테이블(Build Input)을 읽어 해시 테이블(해시 맵)을 생성한다. 테이블의 크키는 행의 수가 아닌 바이트로 측정된다.
  - Probe 단계: 큰 쪽 테이블을(Probe Input) 읽어 해시 테이블을 탐색하면서 조인한다.

```sql
-- 10000건
create table t1 (c1 int);
-- 20000건
create table t2 (c1 int);

-- explain analyze 
explain analyze
select * from t1 join t2 on t1.c1 = t2.c1;

-- result
-> Inner hash join (t2.c2 = t1.c1)  (cost=19.2e+6 rows=19.2e+6) (actual time=11.5..21.8 rows=10000 loops=1)
    -> Table scan on t2  (cost=0.0222 rows=19237) (actual time=0.0148..5.38 rows=20000 loops=1)
    -> Hash
        -> Table scan on t1  (cost=1003 rows=9980) (actual time=1.23..5.87 rows=10000 loops=1)
```
1. Build 단계
   - 상대적으로 작은 테이블인 t1을 해시 테이블로 만들며, 조인 조건으로 사용된 컬럼은 해시 테이블의 키로 사용된다. 해시 테이블 저장하기 위해서 로컬 메모리 영역을 활용하게 되며 만약 로컬 메모리 영역을 초과할 경우에는 임시 디스크를 활용하게 된다.
2. Probe 단계
   - 상대적으로 큰 테이블인 t2 테이블을 통해서 해시테이블로 구성된 t1의 데이터를 조회하게 된다. 해시 테이블의 경우는 키를 통해 조회할 경우 평균 O(1)의 시간복잡도로 데이터를 조회할 수 있다. 때문에 조인을 위하여 일치하는 행을 찾을 때 거의 일정한 속도로 데이터를 조회하여 조인을 수행할 수 있다.


> 친절한 SQL 튜닝, 조시형, 부분범위 처리 활용 P282-298 <br/>
> https://hoing.io/archives/14457 <br/>
> 

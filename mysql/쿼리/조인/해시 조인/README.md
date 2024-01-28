### 1. 해시 조인

- Sort Merge 조인으로 대용량 데이터리를 처리하게 되는 경우 NL 조인에 비해서 좀 더 빠른 성능을 기대할 수 있다. 하지만 Sort Merge 조인의 경우 양쪽 테이블을 정렬는 것의 부담과 두 테이블이 정렬이 될때까지 조인을 수행하지 않는다.
- Hash 조인의 경우 이러한 Sort Merge 조인이 가지는 부담 없이 조인을 수행 할 수 있다. 하지만 대용량 데이터를 조인하는 과정을 모두 해시 조인으로 수행할 수 는 없기 때문에 해시 조인의 기본 매커니즘에 대한 이해도가 필요하다.

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


### 2. MySQL에서의 해시 조인

#### 2.1. MySQL 버전별 지원하는 해시 조인

- MySQL 에서는 8.0.18버전 미만에서는 Hash Join을 지원하지 않고, NL 조인의 기능을 개선한 BKA(Batched Key Access), BNL(Block Nested Loop) 방식을 활용하였지만 8.0.18버전 이상부터는 Hash Join을 지원하게 되었다. 하지만 MySQL 8.0.20 버전이 나오기 전까지는 동등 조인 조건에 대해서만 해시 조인을 지원하였고, 그 이외의 조인의 경우에는 해시조인을 사용할 수없어 성능이 떯어지는 BNL 조인이 사용되었다.
    > ***By default, MySQL (8.0.18 and later) employs hash joins whenever possible*** <br/>
    > ***Prior to MySQL 8.0.20, a hash join could not be used if any pair of joined tables did not have at least one equi-join condition, and the slower block nested loop algorithm was employed.***
- MySQL 8.0.20 이상부터는 동등 조인 조건이외에도 다양한 경우에도 해시 조인 처리가 가능해 아래의 예제를 통해서 해시 조인이 사용한 경우를 확인해볼 수 있다.
    ```sql
    -- inner join 동등 조건을 사용하지 않은 경우
    select * from t1 join t2 on t1.c1 <= t2.c1;
    -- 서브쿼리를 이용한 조인 (세미 조인, 안티 조인)
    select * from t1 where t1.c1 in (select t2.c1 from t2);
    select * from t1 where t1.c1 not in (select t2.c1 from t2);
    -- outer join
    select * from t1 left join t2 on t1.c1 = t2.c1;
    select * from t1 right join t2 on t1.c1 = t2.c1;
    ```

#### 2.2. MySQL 해시 조인과 관련된 시스템 변수
- 해시 조인이 사용될 경우 메모리 사용량은 join_buffer_size 시스템 변수에 의해 사용량이 결정된다. 해시 조인은 join_buffer_size를 초과하는 메모리를 사용할 수 없고 만약 메모리 사용량이 초과된다면 임시 디스크를 활용하여 해시 조인을 처리하게 된다. 
- join_buffer_size를 초과하여 임시 디스크를 활용하게 되는 경우에 open_files_limit에 설정된 것 보다 더 많은 파일을 생성하게 되면 조인이 실패할 수 있다.
- 때문에 대용량 데이터를 해시 조인 처리시 join_buffer_size, open_files_limit 시스템 변수를 늘려갈 필요성이 있을 수 있다.

### 3. 결론

- 대용량 데이터를 조인하게 될 경우 해시 조인을 활용하면 NL 조인이나 소트 머지 조인보다 빠른 성능을 기대할 수 있다는 것을 알 수 있다.
- 하지만 쿼리에 대한 요청 빈도가 높고, 응답 시간이 빨라야하는 상황(OLTP 환경)에서도 해시 조인을 활용해야하는지 한번 점검을 해봐야한다.
- 해시 조인은 기본적으로 독립적인 메모리 공간을 활용하며 메모리 사용량을 높이고, 해시 테이블과 조인을 수행하는 단계에서 CPU 사용량이 높아질 수 밖에 없는 구조이다. 뿐만 아니라 대용량 데이터를 해시 조인 처리하는 것이 아무리 빨라도 한계가 명확하다.
- 요청 빈도가 높다는 의미는 서버가 버티지 못할 가능성이 있다는 의미이고, 대용량 데이터를 처리하는게 아무리 빠르다고 해시 조인만을 이용한 응답 처리는 한계가 분명히 발생할 수 밖에 없다.
- 때문에 모든 요청과 응답을 RDBMS 하나만으로 처리할려는 노력 보다는 다른 저장소를 도입하는 것도 좋은 방법이 될 수 있다.

> 친절한 SQL 튜닝, 조시형, 부분범위 처리 활용 P282-298 <br/>
> https://dev.mysql.com/doc/refman/8.0/en/hash-joins.html <br/>
> https://hoing.io/archives/14457 <br/>


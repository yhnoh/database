### 1.서브쿼리 최적화
- 실무에서 쿼리르 작성하다보면 서브쿼리를 통하여 질의하는 경우가 존재하다. 이때 옵티마이저는 사용자가 작성한 SQL문을 최적화하여 가장 빠른 결과 집합을 응답할 수 있도록 쿼리 변환을 진행한다. 하지만 옵티마이저가 모든 경우의 SQL문에 대해서 최적화를 진행해 주지 않는다.
- 서브쿼리를 잘못 사용하게 될 경우 느린 쿼리의 원인이 될 수 있기 때문에 옵티마이저가 어떤 경우에 서브쿼리를 최적화해주는지 알고 있는 것이 중요하다.
- 기본적으로 서브쿼리를 작성하게 되면 대부분의 경우 옵티마이저는 조인을 통해서 쿼리 최적화를 시도하려고 한다.
    ```sql
    -- 사용자가 작성한 쿼리
    select * from t1 where t1.c1 in (select t2.c1 from t2);

    -- 옵티마이저가 변환한 쿼리
    select t1.* from t1 inner join t2 on t1.c1 = t2.c1;
    ```
- 쿼리 최적화를 하는 이유는 서브쿼리를 통해서 나온 결과물은 인덱스를 사용하지 못할 수 있고, 잘못하면 하나의 레코드당 한번씩 쿼리를 수행해야 할 수도 있기 때문이다.

#### 1.1. 서브쿼리 사용하는 위치에 따른 분류
- 서브쿼리를 DBMS마다 다르게 분류를 하는데 사용하는 위치에 따라서 세 가지의 서브쿼리로 분류할 수 있다.

1. 스칼라 서브쿼리 (Scalar Subquery)
- SELECT문에 있는 서브쿼리 절로 하나의 레코드당 하나의 값을 반환하는 서브쿼리이다.
2. 중첩된 서브쿼리 (Nested Subquery)
- WHERE 절에 사용되는 서브쿼리를 말한다.
3. 인라인 뷰 (Inline View)
- FROM 절에 사용하는 서브쿼리를 말한다.


### 2. MySQL에서의 서브쿼리 최적화

#### 2.1. MySQL 스칼라 서브쿼리(Scalar Subquery) 최적화
- MySQL에서는 스칼라 서브쿼리의 최적화를 지원하지 않는다. 때문에 스칼라 서브쿼리 대신에 Outer Join을 활용하여 문제를 해결하는 것이 좋은 방법이다. 안그러면 하나의 레코드당 서브쿼리를 한번씩 실행하게 되며 이는 느린 쿼리의 원인이 될 수 있다.
- 아래의 쿼리를 통해서 스칼라 서브쿼리가 실제로 최적화를 진행하지 않는지 조인으로 변경 시 어떻게 문제가 해결 되는지 확인해보자.
```sql
-- 1. 스칼라 서브쿼리 사용
select (select t2.c1 from t2 where t2.c1 = t1.c1) from t1;

-- explain analyze
-> Filter: (0 <> t1.c1)  (cost=1003 rows=8982) (actual time=1.77..12.1 rows=10000 loops=1)
    -> Table scan on t1  (cost=1003 rows=9980) (actual time=1.76..9.55 rows=10000 loops=1)
-> Select #2 (subquery in projection; dependent)
    -- t1의 레코드 하나당 t2의 테이블을 조회한 것을 확인할 수 있다. 
    -> Filter: (t2.c1 = t1.c1)  (cost=223 rows=1984) (actual time=1.18..4.69 rows=1 loops=10000)
        -> Table scan on t2  (cost=223 rows=19836) (actual time=0.00214..3.97 rows=20000 loops=10000)

-- 2. 스칼라 서브쿼리를 조인문으로 변경
select t2.c1 from t1 left join t2 on t1.c1 = t2.c1;

-- explain analyze
-- 해시 조인 사용
-> Left hash join (t2.c1 = t1.c1)  (cost=19.8e+6 rows=198e+6) (actual time=8.78..15.5 rows=10000 loops=1)
    -> Table scan on t1  (cost=1003 rows=9980) (actual time=0.012..2.46 rows=10000 loops=1)
    -> Hash
        -> Table scan on t2  (cost=0.202 rows=19836) (actual time=0.0364..4.57 rows=20000 loops=1)
```
- 스칼라 서브쿼리를 사용했을 때 레코드 값을 하나 가져올 때마다 서브쿼리를 실행하게 되지만, 스칼라 서브쿼리를 조인문으로 변경했을 경우 해시 조인을 활용하는 것을 확인할 수 있다. 단순히 서브쿼리를 조인으로 변경했을 뿐인데 성능상 어마어마한 이점을 안겨준다.

#### 2.2. MySQL 중첩된 서브쿼리(Nested Subquery) 최적화 
- MySQL에서는 중첩된 서브쿼리의 최적화를 지원한다. 하지만 모든 경우에 대해서 최적화를 진행하지는 않기 때문에 어느 경우에 서브쿼리의 최적화를 진행하는지에 대해서 알아두면 좋다. 만약 중첩된 서브쿼리를 지원한다고 알고만 있다면 옵티마이저가 최적화하지 못하는 쿼리를 작성하게 될 수 있다.


> https://dev.mysql.com/doc/refman/8.0/en/subquery-optimization.html
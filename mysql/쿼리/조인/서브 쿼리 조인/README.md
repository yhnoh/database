### 1.서브쿼리 최적화
---

- 실무에서 쿼리를 작성하다보면 서브쿼리를 사용하는 경우가 많다. 이때 옵티마이저는 사용자가 작성한 SQL문을 최적화하여 가장 빠른 결과 집합을 응답할 수 있도록 쿼리 변환을 진행한다. 하지만 옵티마이저가 모든 경우의 SQL문에 대해서 최적화를 진행해 주지 않는다.
- 서브쿼리를 잘못 사용하게 될 경우 느린 쿼리의 원인이 될 수 있기 때문에 옵티마이저가 어떤 경우에 서브쿼리를 최적화해주는지 알고 있는 것이 중요하다.
- 기본적으로 서브쿼리를 작성하게 되면 대부분의 경우 옵티마이저는 조인을 통해서 쿼리 최적화를 시도하려고 한다.
    ```sql
    -- 사용자가 작성한 쿼리
    select (select t2.c1 from t2 where t2.c1 = t1.c1) from t1;

    -- 옵티마이저가 변환한 쿼리
    select t2.c1 from t1 left join t2 on t1.c1 = t2.c1;
    ```
- 쿼리 최적화를 하는 이유는 서브쿼리를 통해서 나온 결과물은 인덱스를 사용하지 못할 수 있고, 잘못하면 하나의 레코드당 한번씩 쿼리를 수행해야 할 수도 있기 때문이다.

<br/><br/>

#### 1.1. 서브쿼리를 사용하는 위치에 따른 분류
- 서브쿼리 최적화를 알아보기 전에 서브쿼리를 사용하는 위치에 따라서 옵티마이저는 서브쿼리 최적화를 다르게 할 수 있다.
- 때문에 서브쿼리를 사용하는 위치에 따른 분류를 먼저 확인해 본 이후에 실제 서브쿼리가 어떻게 최적화 되는지 한번 알아보자.
  - 참고로 서브쿼리는 DBMS마다 다르게 분류 하는데 MySQL에서 서브쿼리를 어떻게 최적화하는지 알기 위해서 임의로 서브쿼리를 사용하는 위치에 따른 분류를 하였다.
- 서브쿼리는 위치에 따라서 세 가지의 서브쿼리로 분류할 수 있다.

1. 스칼라 서브쿼리 (Scalar Subquery)
   - SELECT문에 있는 서브쿼리 절로 하나의 레코드당 하나의 값을 반환하는 서브쿼리이다.
2. 중첩된 서브쿼리 (Nested Subquery)
   - WHERE 절에 사용되는 서브쿼리를 말한다.
3. 인라인 뷰 (Inline View)
   - FROM 절에 사용하는 서브쿼리를 말한다.


### 2. MySQL에서의 서브쿼리 최적화
---

#### 2.1. MySQL 스칼라 서브쿼리(Scalar Subquery) 최적화
- MySQL에서는 스칼라 서브쿼리의 최적화를 지원하지 않는다. 때문에 스칼라 서브쿼리 대신에 Outer Join을 활용하여 문제를 해결하는 것이 좋은 방법이다.
- 안그러면 하나의 레코드당 서브쿼리를 한번씩 실행하게 되며 이는 느린 쿼리의 원인이 될 수 있다.
- 아래의 쿼리를 통해서 스칼라 서브쿼리가 실제로 최적화를 진행하지 않는지 조인으로 변경 시 어떻게 문제가 해결 되는지 확인해보자.
    ```sql
    -- 1. 스칼라 서브쿼리 사용, 쿼리 실행을 통한 응답 시간: 2s 517m
    select (select t2.c1 from t2 where t2.c1 = t1.c1) from t1;

    -- explain analyze
    -> Filter: (0 <> t1.c1)  (cost=1003 rows=8982) (actual time=1.77..12.1 rows=10000 loops=1)
        -> Table scan on t1  (cost=1003 rows=9980) (actual time=1.76..9.55 rows=10000 loops=1)
    -> Select #2 (subquery in projection; dependent)
        -- t1의 레코드 하나당 t2의 테이블을 조회한 것을 확인할 수 있다. 
        -> Filter: (t2.c1 = t1.c1)  (cost=223 rows=1984) (actual time=1.18..4.69 rows=1 loops=10000)
            -> Table scan on t2  (cost=223 rows=19836) (actual time=0.00214..3.97 rows=20000 loops=10000)

    -- 2. 스칼라 서브쿼리를 조인문으로 변경, 쿼리 실행을 통한 응답 시간: 74ms
    select t2.c1 from t1 left join t2 on t1.c1 = t2.c1;

    -- explain analyze
    -> Left hash join (t2.c1 = t1.c1)  (cost=19.8e+6 rows=198e+6) (actual time=8.78..15.5 rows=10000 loops=1)
        -> Table scan on t1  (cost=1003 rows=9980) (actual time=0.012..2.46 rows=10000 loops=1)
        -> Hash
            -> Table scan on t2  (cost=0.202 rows=19836) (actual time=0.0364..4.57 rows=20000 loops=1)
    ```
    - 스칼라 서브쿼리에서 조인으로 변경하면서 응답 시간이 `2s 517ms -> 74ms`인 것을 확인할 수 있다.
    - explain을 분석해보면 스칼라 서브쿼리를 사용했을 때 레코드 값을 하나 가져올 때마다 서브쿼리를 실행하게 되지만, 스칼라 서브쿼리를 조인문으로 변경했을 경우 해시 조인을을 이용한 것을 확인할 수 있다.
    - 때문에 스칼라 서브쿼리를 이용하여 쿼리를 작성하기 보다는 조인을 이용하여 쿼리를 작성하는 것이 성능상 이점을 가져올 수 있다.

<br/><br/>

#### 2.2. MySQL 중첩된 서브쿼리(Nested Subquery) 최적화
- MySQL 5.6 이후부터 중첩된 서브쿼리의 최적화를 지원한다. 하지만 모든 경우에 대해서 최적화를 진행하지는 않기 때문에 어느 경우에 서브쿼리의 최적화를 진행하는지에 대해서 알아두면 좋다. 만약 중첩된 서브쿼리를 지원한다고 알고만 있다면 옵티마이저가 최적화하지 못하는 쿼리를 작성하게 될 수 있다.
- MySQL은 IN, EXISTS문 실행 시 옵티마이저는 세미 조인을 통한 쿼리 최적화를 진행한다.
  > 세미조인이란 서브 쿼리를 사용해서 서브 쿼리에 존재하는 데이터만 메인 쿼리에 추출하는 조인을 의미한다. 조인을 사용하기 때문에 일치하는 행을 찾게 되면 멈추고 다음 행을 찾을 수 있다는 장점이 있다.
  > MySQL 8.0.17 이후 부터는 NOT IN, NOT EXISTS 실행 시 옵티마이저는 안티 조인을 통한 쿼리 최적화를 진행한다.

<br/><br/>

#### 2.2.1. 옵티마이저가 세미 조인이 아닌 다른 조인으로 최적화를 한다면 어떻게 될까?
- 만약 옵티마이저가 IN, EXISTS문에 세미 조인을 활용하지 않고, 다른 조인문을 통한 최적화를 진행하게되면 어떻게 되는지 한번 알아보자.
    ```sql
    -- 사용자가 실행한 쿼리
    SELECT class_num, class_name
    FROM class
    WHERE class_num IN (SELECT class_num FROM roster)

    -- 옵티마이저가 최적화한 쿼리
    SELECT class.class_num, class.class_name
    FROM class INNER JOIN roster
    WHERE class.class_num = roster.class_num;
    ```
    - 위 쿼리에서 사용자가 실행한 쿼리를 옵티마이저가 조인을 통해서 쿼리 최적화를 진행하고 있다. 하지만 여기서 문제가 하나 있는데 IN절을 통한 결과를 조인으로 변경하게 되면 메인 쿼리의 결과가 중복되서 나올 수 있다는 문제가 있다. 
    - 때문에 이러한 중복을 없애기 위하여 조인이 완료된 이후 `distinct, group by`절을 통한 중복을 제거해야한다. 중복을 제거하기 위해서는 조인문이 완료된 이후에 중복을 제거하는 것은 조금 비효율 적일 수 있다. 가장 좋은 것은 이미 중복이 서브쿼리를 조인으로 변경하는 것이 가장 효율적이라는 것을 알 수 있다.

<br/><br/>

#### 2.2.2. 옵티마이저가 IN, EXISTS 문에 대한 세미 조인 최적화를 실행하지 않는다면 어떻게 될까?
- 아래 쿼리는 옵티마이저 세미조인을 비활성화 이후 실행한 쿼리다.
    ```sql
    -- 옵티마이저 세미조인 비활성화
    set session optimizer_switch = 'materialization=off,semijoin=off,loosescan=off,firstmatch=off';

    -- 쿼리 실행을 통한 응답 시간: 36s
    select t2.c1 from t2 where t2.c1 in (select t1.c1 from t1); 

    -- explain analyze
    -> Filter: <in_optimizer>(t2.c1,<exists>(select #2))  (cost=2008 rows=19836) (actual time=0.062..34771 rows=10000 loops=1)
        -> Table scan on t2  (cost=2008 rows=19836) (actual time=0.0335..6.13 rows=20000 loops=1)
        -> Select #2 (subquery in condition; dependent)
            -> Limit: 1 row(s)  (cost=105 rows=1) (actual time=1.74..1.74 rows=0.5 loops=20000)
                -> Filter: (<cache>(t2.c1) = t1.c1)  (cost=105 rows=998) (actual time=1.74..1.74 rows=0.5 loops=20000)
                    -> Table scan on t1  (cost=105 rows=9980) (actual time=884e-6..1.46 rows=7500 loops=20000)

    ```
    - 해당 쿼리의 결과를 응답받기까지 36s가 걸렸다. 조인을 수행하지 않았기 때문에 하나의 행을 찾기까지 계속해서 테이블 풀 스캔이 일어났을 것이다. 해당 쿼리를 세미 조인으로 변경하게 되면 어마어마한 속도차이가 발생하게 된다.
- 아래 쿼리는 옵티마이저 세미조인을 활성화 이후 실행한 쿼리다.
    ```sql
    -- 옵티마이저 세미조인 활성화
    set session optimizer_switch = 'materialization=on,semijoin=on,loosescan=on,firstmatch=on';

    -- 쿼리 실행을 통한 응답 시간: 84ms
    select t2.c1 from t2 where t2.c1 in (select t1.c1 from t1);

    -- explain analyze
    -> Inner hash join (t2.c1 = `<subquery2>`.c1)  (cost=19.8e+6 rows=19.8e+6) (actual time=19.6..37.4 rows=10000 loops=1)
        -> Table scan on t2  (cost=227 rows=19836) (actual time=0.0795..7.68 rows=20000 loops=1)
        -> Hash
            -> Table scan on <subquery2>  (cost=2001..2128 rows=9980) (actual time=7.46..8.33 rows=10000 loops=1)
                -> Materialize with deduplication  (cost=2001..2001 rows=9980) (actual time=7.46..7.46 rows=10000 loops=1)
                    -> Filter: (t1.c1 is not null)  (cost=1003 rows=9980) (actual time=0.447..4.53 rows=10000 loops=1)
                        -> Table scan on t1  (cost=1003 rows=9980) (actual time=0.44..3.85 rows=10000 loops=1)

    ```
    - 똑같은 쿼리이지만 세미조인을 활성화 시킨것만으로도 응답 시간이 36s -> 84ms 속도가 개선된것을 확인할 수 있다.


#### 2.2.3. 서브쿼리 최적화를 지원하지 않는 경우

- 몇몇 경웨 따라서 서브 쿼리 최적화를 지원하지 않는다고 MySQL 공식문서에는 나와있기는 한데, 실제로 사용을 해보니 안그런 경우들이 있다.
  - UNION, HAVING, 집계함수, LIMIT, STRAIGHT_JOIN 등의 경우 최적화를 지원하지 않는다라고 명시되어 있다. 
    > https://dev.mysql.com/doc/refman/8.0/en/semijoins.html
  - 하지만 실제 쿼리를 수행하면 해당 경우인데 빠른 결과가 나오는 경우가 있는데, 이는 옵티마이저가 서브쿼리를 옵티마이적 임시테이블로 만들어 해당 임시테이블을 참조하여 최적화를 수행하기 때문이다.
    > https://dev.mysql.com/doc/refman/8.0/en/subquery-materialization.html
    ```sql
    -- 임시테이블 활용하지 못함
    select t2.c1 from t2 where t2.c1 in (select t1.c1 from t1 union all select t2.c1 from t2);

    -- 임시테이블 활용
    select t2.c1 from t2 where t2.c1 in (select t1.c1 from t1 group by t1.c1);
    select t2.c1 from t2 where t2.c1 in (select avg(1) from t1 group by t1.c1);
    select t2.c1 from t2 where t2.c1 in (select t1.c1 from t1 group by t1.c1 having t1.c1 > 100);
    ```

<br/><br/>

#### 2.3. MySQL 인라인 뷰(Inline View) 최적화

- MySQL은 인라인 뷰의 최적화를 지원한다.
  - 인라인 뷰로 사용된 쿼리 블록을 병합하여 처리
  - 인라인 뷰로 사용된 쿼리 결과를 임시테이블을 사용하여 처리

<br/><br/>

#### 2.3.1 인라인 뷰로 사용된 쿼리 블록을 병합하여 처리
- 인라인 뷰로 사용된 쿼리 블록을 병합하여 처리하는 과정을 아래의 쿼리를 통해서 한번 알아보자.
    ```sql
    -- 사용자가 사용한 쿼리
    select * from t1 join (select t2.c1 from t2) AS derived_t2 on t1.c1=derived_t2.c1 where t1.c1 > 0;

    -- 사용자가 사용한 쿼리 explain analyze
    -> Inner hash join (t2.c1 = t1.c1)  (cost=6.6e+6 rows=6.6e+6) (actual time=9.93..23.1 rows=10000 loops=1)
        -> Table scan on t2  (cost=0.0674 rows=19836) (actual time=0.0153..6.78 rows=20000 loops=1)
        -> Hash
            -> Filter: (t1.c1 > 0)  (cost=1003 rows=3326) (actual time=0.0751..5.15 rows=10000 loops=1)
                -> Table scan on t1  (cost=1003 rows=9980) (actual time=0.0729..4.23 rows=10000 loops=1)

    -- 옵티망저가 변환해준 쿼리
    select t1.*, t2.c1 from t1 join t2 on t1.c1=t2.c1 where t1.c1 > 0;

    -- 사용자가 사용한 쿼리 explain analyze
    -> Inner hash join (t2.c1 = t1.c1)  (cost=6.6e+6 rows=6.6e+6) (actual time=9.23..24.2 rows=10000 loops=1)
        -> Table scan on t2  (cost=0.0674 rows=19836) (actual time=0.0212..8.86 rows=20000 loops=1)
        -> Hash
            -> Filter: (t1.c1 > 0)  (cost=1003 rows=3326) (actual time=0.102..4.1 rows=10000 loops=1)
                -> Table scan on t1  (cost=1003 rows=9980) (actual time=0.0982..3.28 rows=10000 loops=1)

    ```
    - 사용자가 사용한 쿼리와 옵티마이저가 변환해준 쿼리를 분석했을 때 차이가 없다는 것을 `explain analyze` 키워드를 통해서 알수 있다.

<br/><br/>

#### 2.3.2 인라인 뷰로 사용된 쿼리 결과를 임시테이블을 사용하여 처리

- 인라인 뷰로 사용된 쿼리가 임시 테이블로 사용되는 경우는 다음과 같다.
  - 집계함수 사용
  - DISTINCT
  - GROUP BY
  - HAVING
  - LIMIT
  - UNION or UNION ALL
  - 등등...
  > https://dev.mysql.com/doc/refman/8.0/en/derived-table-optimization.html
- MySQL에서는 인라인 뷰 사용 시 임시테이블을 만드는 것뿐만 아니라 특정 상황에서는 인덱스도 만들어서 쿼리 성능을 향상시키는 경우가 있다.
    - 조인 컬럼 조건이 동등 조건일 때 임시테이블을 만들 때 인덱스도 같이 생성한다. 인덱스가 생성 된다면 NL 조인을 활용하여 쿼리를 실행하게 된다.
    - 만약 인덱스 조인컬럼이 동등 조건으로 되어있다고 하더라도 옵티마이저가 쿼리 실행 통계를 통해서 인덱스 생성의 비용이 더 크다고 판단을 하면 인덱스를 생성하지 않는다.
    ```sql
    -- 조인 컬럼 동등 조건일 경우
    select * from t1 join (select * from t2 where t2.c1 group by c1) t2 on t1.c1 = t2.c1;

    -- 조인 컬럼 동등 조건일 경우 explain analyze
    -> Nested loop inner join  (cost=17.8e+6 rows=178e+6) (actual time=36.3..52 rows=10000 loops=1)
    -> Filter: (t1.c1 is not null)  (cost=1003 rows=9980) (actual time=0.0511..4.71 rows=10000 loops=1)
        -> Table scan on t1  (cost=1003 rows=9980) (actual time=0.05..3.92 rows=10000 loops=1)
    -> Covering index lookup on t2 using <auto_key0> (c1=t1.c1)  (cost=5804..5806 rows=10) (actual time=0.00431..0.00451 rows=1 loops=10000)
        -> Materialize  (cost=5804..5804 rows=17852) (actual time=36.2..36.2 rows=20000 loops=1)
            -> Table scan on <temporary>  (cost=3793..4019 rows=17852) (actual time=19.5..22.2 rows=20000 loops=1)
                -> Temporary table with deduplication  (cost=3793..3793 rows=17852) (actual time=19.5..19.5 rows=20000 loops=1)
                    -> Filter: (0 <> t2.c1)  (cost=2008 rows=17852) (actual time=0.0115..13.9 rows=20000 loops=1)
                        -> Table scan on t2  (cost=2008 rows=19836) (actual time=0.01..11.8 rows=20000 loops=1)

    -- 조인 컬럼 동등 조건이 아닐 경우
    select * from t1 join (select * from t2 where t2.c1 group by c1) t2 on t1.c1 > t2.c1;

    -- 조인 컬럼 동등 조건이 아닐 경우 explain analyze
    -> Filter: (t1.c1 > t2.c1)  (cost=17.8e+6 rows=59.4e+6) (actual time=26.4..15913 rows=50e+6 loops=1)
        -> Inner hash join (no condition)  (cost=17.8e+6 rows=59.4e+6) (actual time=26.4..6680 rows=200e+6 loops=1)
            -> Table scan on t2  (cost=5804..6030 rows=17852) (actual time=20.2..25.3 rows=20000 loops=1)
                -> Materialize  (cost=5804..5804 rows=17852) (actual time=20.2..20.2 rows=20000 loops=1)
                    -> Table scan on <temporary>  (cost=3793..4019 rows=17852) (actual time=15.9..18.4 rows=20000 loops=1)
                        -> Temporary table with deduplication  (cost=3793..3793 rows=17852) (actual time=15.9..15.9 rows=20000 loops=1)
                            -> Filter: (0 <> t2.c1)  (cost=2008 rows=17852) (actual time=0.041..10 rows=20000 loops=1)
                                -> Table scan on t2  (cost=2008 rows=19836) (actual time=0.0267..7.99 rows=20000 loops=1)
            -> Hash
                -> Table scan on t1  (cost=1003 rows=9980) (actual time=0.0523..5.06 rows=10000 loops=1)
    ```

<br/><br/>

> https://dev.mysql.com/doc/refman/8.0/en/subquery-optimization.html <br/>
> https://hoing.io/archives/73895
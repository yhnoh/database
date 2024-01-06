## 1. GROUP BY 처리

---

- GROUP BY는 집계를 처리하기 위하여 자주 사용되는 경우가 많다. 
    > 나의 경우에는 마케팅 자료나 IR자료를 만들기 위해서 주로 사용했다. 실제 쿼리에서도 활용할 수는 있지만 왠만하면 사용하지 않으려 했다.
- GROUP BY 절의 가장 일반적인 처리 방법은 테이블을 스캔하고, 임시 테이블을 생성하여 일치하는 행을 임시테이블에 삽입한 이후, 집계함수를 적용하거나 정렬을 수행한다.
- 하지만 특정 경우에는 MySQL에서 더 나은 성능을 발휘하기 위하여 인덱스에 접근으로 결과를 보여줄 수 있으며 임시테이블의 생성을 방지할 수 있다.

### 1.1. 인덱스를 이용한 GROUP BY 처리
---

- 위에서 인덱스에 접근으로만 결과를 보여주어 임시테이블의 생성을 방지한다고 하였다. 실제로 인덱스 컬럼을 통해서 정렬을 수행했을 경우 테이블의 물리적 순서와 다르게 인덱스 정렬 순으로 데이터가 나오는 것을 확인할 수 있다.

#### 1.1. 인덱스 컬럼을 GROUP BY 절에 사용하면 어떻게 정렬될 것인가? 
- 예제를 통해서 확인해보자. salaries 테이블은 primary key (emp_no, from_date) 순으로 물리적으로 정렬되어 있다.
```sql
-- salary 인덱스 컬럼를 통해서 GROUP BY 사용
select salary,min(emp_no), min(from_date) from salaries group by salary;

-- 결과 값
+------+-----------+--------------+
|salary|min(emp_no)|min(from_date)|
+------+-----------+--------------+
|38623 |253406     |2002-02-20    |
|38735 |49239      |1996-09-17    |
|38786 |281546     |1996-11-13    |
|38812 |15830      |2001-03-12    |
|38836 |64198      |1989-10-20    |
|38849 |475254     |1993-06-04    |
|38850 |50419      |1996-09-22    |
|38851 |34707      |1990-10-03    |
|38859 |49239      |1995-09-18    |
|38864 |274049     |1996-09-01    |
            .
            .
            .
            .
            .
            .
            .
```
- select 절에 emp_no, from_date를 포함시킴으로 해당 결과값이 어떤식으로 정렬되어 있는지 확인하기 위하여 나타낸 컬럼이다.
- 실제 결과를 통해서 확인해보면 테이블의 물리적 순서가 아닌 salary 컬럼의 값의 순서로 정렬된 결과 값이 나온 것을 확인할 수 있다.
- 이 예저를 통해서 확인할 수 있는 것은 salary 인덱스만 조회를 하고 실제로 물리적 디스크에는 접근하지 않았을 확률이 높다.
  - emp_no, from_date는 salray 인덱스의 리프 노드 값에 포함되어 있으므로 실제로 인덱스를 통한 접근으로만 결과값을 도출해낼 수 있다.
- 때문에 GROUP BY가 인덱스를 통해서 처리되는 쿼리는 이미 정렬된 인덱스를 읽는 것이므로 쿼리 실행 시점에 추가적인 정렬 작업이나 내부 임시테이블이 필요하지 않다.

#### 1.2. 인덱스를 이용한 GROUP BY 처리의 조건

- 인덱스 접근만을 통해서 GROUP BY를 사용하는 기본적인 전제 조건은 ***GROUP BY 절에 명시된 컬럼이 인덱스 컬럼의 순서와 같아야 한다.***
- 다중 컬럼 인덱스의 경우에는 GROUP BY절에 인덱스 후행 컬럼을 생략한다고 하더라도 인덱스를 활용할 수 있다.
- 만약 GROUP BY절에 인덱스 컬럼이 명시되어 있지 않거나 인덱스 순서가 맞지 않을 경우에는 인덱스를 이용한 GROUP BY 절을 활용할 수없다.
```sql
-- primary key (emp_no, from_date, title)

-- 인덱스를 이용한 GROUP BY절 사용 가능
explain select emp_no from titles group by emp_no;
explain select emp_no, from_date from titles group by emp_no, from_date;
explain select emp_no, from_date, title from titles group by emp_no, from_date, title;

-- 인덱스를 이용한 GROUP BY절 서용 불가능
explain select from_date, title, from_date from titles group by from_date, title, from_date; -- 인덱스 컬럼 순서가 올바르지 않음
explain select from_date, title from titles group by from_date, title; -- 인덱스 선행 컬럼 존재 하지 않음
```

### 2. Tight Index Scan
---

- 앞에서 인덱스를 이용한 GROUP BY절을 사용하기 위해서는 GROUP BY 절에 명시된 컬럼이 인덱스 컬럼의 순서와 같아야 한다고 했다.
- 하지만 GROUP BY절에 인덱스 컬럼이 존재하지 않더라도 WHERE 절에 인덱스 컬럼을 통한 등치 조건으로 사용한다면 인덱스를 이용한 GROUP BY 처리를 할 수 잇다.

#### 2.1. Tight Index Scan을 이용한 GROUP BY 처리

- 예제를 통해서 확인해보자. titles 테이블은 primary key (emp_no, from_date, title)의 인덱스를 가지고 있다.

```sql
-- 선행 컬럼을 등치 조건으로 사용

explain select emp_no, from_date, title from titles where emp_no = 499981 group by from_date, title;
+--+-----------+------+----------+----+-----------------+-------+-------+-----+----+--------+-----------+
|id|select_type|table |partitions|type|possible_keys    |key    |key_len|ref  |rows|filtered|Extra      |
+--+-----------+------+----------+----+-----------------+-------+-------+-----+----+--------+-----------+
|1 |SIMPLE     |titles|null      |ref |PRIMARY,ix_todate|PRIMARY|4      |const|2   |100     |Using index|
+--+-----------+------+----------+----+-----------------+-------+-------+-----+----+--------+-----------+

-- 후행 컬럼을 등치 조건으로 사용

explain select emp_no, from_date, title from titles where from_date = '1985-02-10' group by emp_no, title;
+--+-----------+------+----------+-----+-----------------+-------+-------+----+------+--------+------------------------+
|id|select_type|table |partitions|type |possible_keys    |key    |key_len|ref |rows  |filtered|Extra                   |
+--+-----------+------+----------+-----+-----------------+-------+-------+----+------+--------+------------------------+
|1 |SIMPLE     |titles|null      |index|PRIMARY,ix_todate|PRIMARY|209    |null|414562|10      |Using where; Using index|
+--+-----------+------+----------+-----+-----------------+-------+-------+----+------+--------+------------------------+

explain select emp_no, from_date, title from titles where title = 'Senior Engineer' group by emp_no, from_date;
+--+-----------+------+----------+-----+-----------------+-------+-------+----+------+--------+------------------------+
|id|select_type|table |partitions|type |possible_keys    |key    |key_len|ref |rows  |filtered|Extra                   |
+--+-----------+------+----------+-----+-----------------+-------+-------+----+------+--------+------------------------+
|1 |SIMPLE     |titles|null      |index|PRIMARY,ix_todate|PRIMARY|209    |null|414562|10      |Using where; Using index|
+--+-----------+------+----------+-----+-----------------+-------+-------+----+------+--------+------------------------+

```

- 선행 컬럼을 등치 조건으로 사용했을 경우에는 레인지 인덱스 스캔이 활용된 것을 알 수 있고, 후행 컬럼을 등치 조건으로 사용했을 경우에는 풀 인덱스 스캔을 사용한 이후 MySQL엔진에서 필터링 작업이 들어간 것을 확인할 수 있다.
- 왜 이렇게 되는지 현재는 잘 모르겠다.

### 3. 루스 인덱스 스캔을 이용한 GROUP BY 처리
---

- 루스 인덱스 스캔 방식은 인덱스의 레코드를 건너뛰면서 필요한 부분만 읽어서 가져오는 스캔방식이며, GROUP BY를 처리할 때 임시테이블이 아닌 인덱스를 이용한 GROUP BY 처리가 가능한 스캔 방식이다.
- 예시를 하나 들어보겠다.

```sql
-- primary key (emp_no, from_date)
explain select emp_no, min(from_date) from salaries where from_date between '1985-01-01' and '1985-12-31' group by emp_no;

+--+-----------+--------+----------+-----+-----------------+-------+-------+----+------+--------+-------------------------------------+
|id|select_type|table   |partitions|type |possible_keys    |key    |key_len|ref |rows  |filtered|Extra                                |
+--+-----------+--------+----------+-----+-----------------+-------+-------+----+------+--------+-------------------------------------+
|1 |SIMPLE     |salaries|null      |range|PRIMARY,ix_salary|PRIMARY|7      |null|274543|100     |Using where; Using index for group-by|
+--+-----------+--------+----------+-----+-----------------+-------+-------+----+------+--------+-------------------------------------+
```
- 인덱스가 (emp_no, from_date)순으로 로 생성되어 있으며 해당 쿼리는 실제로 인덱스 레인지 스캔 방식으로 이용할 수 없는 쿼리이다.
- 하지만 쿼리 실행 계획은 인덱스 레인지 스캔을 활용하였으며, Extra 컬럼의 메시지를 보면 GROUP BY 처리까지 인덱스를 사용했다는 것을 확인할 수 있다.

#### 3.1. 루스 인덱스 스캔을 이용한 GROUP BY 처리된 순서

1. 인덱스는 emp_no asc, from_date asc 순으로 정렬되어 있을 것이다.
2. 때문에 emp_no 별로 첫번째 레코드의 from_date만 읽고 다음 emp_no의 첫번째 레코드만 읽으면 된다.. 즉 emp_no에 해당 하는 전체 from_date를 스캔할 필요성이 없다.
3. 인덱스 리프 노드를 스캔하면서 불필요한 부분은 그냥 건너뛰고 필요한 부분만 읽어서 가져오슨 루스 인덱스 스캔 방시을 사용한다.



### 4. 임시 테이블을 사용하는 GROUP BY

- GROUP BY의 기준 컬럼이 인덱스를 전혀 사용하지 못할 때는 임시테이블을 사용하여 처리하게 된다.

```sql
-- to_date는 인덱스가 걸려 있지 않다.
explain select to_date, sum(salary) from salaries group by to_date;

+--+-----------+--------+----------+----+-------------+----+-------+----+-------+--------+---------------+
|id|select_type|table   |partitions|type|possible_keys|key |key_len|ref |rows   |filtered|Extra          |
+--+-----------+--------+----------+----+-------------+----+-------+----+-------+--------+---------------+
|1 |SIMPLE     |salaries|null      |ALL |null         |null|null   |null|2579708|100     |Using temporary|
+--+-----------+--------+----------+----+-------------+----+-------+----+-------+--------+---------------+
```
- `Extra` 컬럼의 값이 `Using temporary`인 것을 통해서 임시 테이블을 활용했다는 것을 알 수 있다.
- MySQL 8.0에서는 GROUP BY가 필요한 경우 내부적으로 GROUP BY 절의 컬럼들로 구성된 유니크 인덱스를 가진 임시테이블을 생성한다.
    ```sql
    CREATE TEMPORARY TABLE [table_name] (
        to_date date,
        'sum(salary)' int,
        UNIQUE INDEX ui_to_date (to_date) 
    );
    ```
- 임시 테이블을 만들어서 건별로 데이터를 INSERT 또는 UPDATE 작업을 수행하여, 중복 제거 작업 및 집합 함수 연산을 수행한다.
- 만약 GROUP BY 와 ORDER BY가 같이 사용되는 경우 Filesot를 이용한 정렬을 수행하게 되며 Extra 컬럼에서 Using filesort가 표시되는 것을 확인할 수 있다.


> https://dev.mysql.com/doc/refman/8.0/en/group-by-optimization.html <br/>
> Real MySql 8.0 개발자와 DBA를 위한 MySQL 실전 가이드, 백은비,이성욱, P305-309
### 1. GROUP BY 처리

#### 1.1. 루스 인덱스 스캔을 이용한 GROUP BY

- 루스 인덱스 스캔 방식은 인덱스의 레코드를 건너뛰면서 필요한 부분만 읽어서 가져오는 스캔방식이다.
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
1. 인덱스는 emp_no asc, from_date asc 순으로 정렬되어 있을 것이다.
2. 때문에 emp_no 별로 첫번째 레코드의 from_date만 읽고 다음 emp_no의 첫번째 레코드만 읽으면 된다.. 즉 emp_no에 해당 하는 전체 from_date를 스캔할 필요성이 없다.
3. 인덱스 리프 노드를 스캔하면서 불필요한 부분은 그냥 건너뛰고 필요한 부분만 읽어서 가져오슨 루스 인덱스 스캔 방시을 사용한다.

- 실행계획을 통해서 type 컬럼을 확인해보면 인덱스 레인지 스캔을 사용하였고, Extra 컬럼의 메시지를 보면 GROUP BY 처리가 되었다는 것을 확인할 수 있다.


#### 1.1. 임시 테이블을 사용하는 GROUP BY

- GROUP BY의 기준 컬럼이 인덱스를 전혀 사용하지 못할 때는 임시테이블을 사용하여 처리하게 된다.

```sql
-- to_date는 인덱스가 걸려 있지 않다.
explain select to_date from salaries group by to_date;

+--+-----------+--------+----------+----+-------------+----+-------+----+-------+--------+---------------+
|id|select_type|table   |partitions|type|possible_keys|key |key_len|ref |rows   |filtered|Extra          |
+--+-----------+--------+----------+----+-------------+----+-------+----+-------+--------+---------------+
|1 |SIMPLE     |salaries|null      |ALL |null         |null|null   |null|2579708|100     |Using temporary|
+--+-----------+--------+----------+----+-------------+----+-------+----+-------+--------+---------------+
```
- `Extra` 컬럼의 값이 `Using temporary`인 것을 통해서 임시 테이블을 활용했다는 것을 알 수 있다.
- MySQL 8.0에서는 GROUP BY가 필요한 경우 내부적으로 GROUP BY 절의 컬럼들로 구성된 유니크 인덱스를 가진 임시테이블을 만들어서 중복 제거와 집합 연산을 수행한다.
```sql
CREATE TEMPORARY TABLE [table_name] (
    to_date date not null
    UNIQUE INDEX ui_to_date (to_date) 
);
```

### 2. DISTINCT 처리

- 단순히 SELECT되는 레코드 중에서 유니크한 레코드만 가져오고자 하면 SELECT DISTINCT 형태의 쿼리 문장을 사용한다.

#### 2.1. 집합 함수와 함께 사용된 DISTINCT

- 집합 함수 내에서 사용된 DISTINCT는 그 집합 함수의 인자로 전달된 컬럼값이 유니크한 것들을 가져오단다.
- 
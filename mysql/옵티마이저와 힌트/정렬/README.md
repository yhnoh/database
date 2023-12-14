### 정렬을 처리하는 방법

### 1. 인덱스를 이용한 정렬
- 인덱스를 이용한 정렬의 경우 인덱스의 값이 정렬되어 있기 때문에, 인덱스를 순서대로 읽기만 하면 되며 MySQL 엔진에서 별도의 정렬을 위한 추가 작업을 수행하지 않아도 된다.

#### 1.1. 어떤 인덱스를 사용해야 인덱스를 이용한 정렬이 될까?

- 인덱스를 이용한 정렬의 경우 인덱스의 값이 정렬되어 있기 때문에 따로 MySQL엔진에서 정렬을 수행하지 않아도 된다고 말했다. 그렇다면 어떤 인덱스가 인덱스를 이용한 정렬을 수행할까?
- 그렇다면 모든 인덱스에 해당되는 이야기일까? 실제로 쿼리를 수행해보며 어떤 인덱스가 정렬을 수행하는지 확인해보자. 
```SQL
-- 클러스터링 인덱스를 통한 정렬
explain select * from employees where emp_no between 100000 and 100100 order by emp_no asc;

+--+-----------+---------+----------+-----+------------------------+-------+-------+----+----+--------+-----------+
|id|select_type|table    |partitions|type |possible_keys           |key    |key_len|ref |rows|filtered|Extra      |
+--+-----------+---------+----------+-----+------------------------+-------+-------+----+----+--------+-----------+
|1 |SIMPLE     |employees|null      |range|PRIMARY,ix_emp_no_gender|PRIMARY|4      |null|101 |100     |Using where|
+--+-----------+---------+----------+-----+------------------------+-------+-------+----+----+--------+-----------+

-- 세컨더리 인덱스를 통한 정렬
explain select * from employees where emp_no between 100000 and 100100 order by hire_date asc;

+--+-----------+---------+----------+-----+------------------------+-------+-------+----+----+--------+---------------------------+
|id|select_type|table    |partitions|type |possible_keys           |key    |key_len|ref |rows|filtered|Extra                      |
+--+-----------+---------+----------+-----+------------------------+-------+-------+----+----+--------+---------------------------+
|1 |SIMPLE     |employees|null      |range|PRIMARY,ix_emp_no_gender|PRIMARY|4      |null|101 |100     |Using where; Using filesort|
+--+-----------+---------+----------+-----+------------------------+-------+-------+----+----+--------+---------------------------+
```
- 클러스터링 인덱스를 통해서 정렬 했을 경우 Extra컬럼의 값에 Using filesort 값이 없지만, 세컨더리 인덱스를 통한 정렬을 수행할 경우 Using filesort 값이 있는 것을 확인할 수 있다.
- 클러스터링 인덱스의 경우 물리적으로 정렬이 되어 있지만, 세컨더리 인덱스의 경우 물리적으로 정렬되어 있지는 않기 때문에 위와 같은 결과가 나온 것이 아닐까라는 생각이 든다.

#### 1.2. 인덱스를 이용한 정렬을 수행하기 위한 조건
1. order by에 명시된 컬럼의 순서대로 생성된 클러스터링 인덱스가 필요하다.
- 위에서 설명했듯이 클러스터링 인덱스만 정렬이 가능하다는 것을 확인했다.
2. 조인을 사용할 경우 order by에 명시된 컬럼(클러스터링 인덱스)이 드라이빙 테이블에 속해야한다.

```SQL
explain select * from employees e left join salaries s on e.emp_no = s.emp_no where e.emp_no between 100000 and 100100 order by e.emp_no;
+--+-----------+-----+----------+-----+------------------------+-------+-------+------------------+----+--------+-----------+
|id|select_type|table|partitions|type |possible_keys           |key    |key_len|ref               |rows|filtered|Extra      |
+--+-----------+-----+----------+-----+------------------------+-------+-------+------------------+----+--------+-----------+
|1 |SIMPLE     |e    |null      |range|PRIMARY,ix_emp_no_gender|PRIMARY|4      |null              |101 |100     |Using where|
|1 |SIMPLE     |s    |null      |ref  |PRIMARY                 |PRIMARY|4      |employees.e.emp_no|9   |100     |null       |
+--+-----------+-----+----------+-----+------------------------+-------+-------+------------------+----+--------+-----------+

explain select * from employees e left join salaries s on e.emp_no = s.emp_no where e.emp_no between 100000 and 100100 order by e.emp_no, from_date;
+--+-----------+-----+----------+-----+------------------------+-------+-------+------------------+----+--------+--------------------------------------------+
|id|select_type|table|partitions|type |possible_keys           |key    |key_len|ref               |rows|filtered|Extra                                       |
+--+-----------+-----+----------+-----+------------------------+-------+-------+------------------+----+--------+--------------------------------------------+
|1 |SIMPLE     |e    |null      |range|PRIMARY,ix_emp_no_gender|PRIMARY|4      |null              |101 |100     |Using where; Using temporary; Using filesort|
|1 |SIMPLE     |s    |null      |ref  |PRIMARY                 |PRIMARY|4      |employees.e.emp_no|9   |100     |null                                        |
+--+-----------+-----+----------+-----+------------------------+-------+-------+------------------+----+--------+--------------------------------------------+
```
- 위 쿼리에서 확인해본바와 같이 조인 사용시 드리븐 테이블에서의 컬럼을 통해서 정렬을 수행할 경우 Extra 컬럼의 값이 Using temporary; Using filesort인 것을 확인할 수 있다.

### 2. Filesort 이용한 정렬
- 인덱스를 이용한 정렬을 사용할 수 없는 경우, Filesort를 이용한 정렬을 수행한다.
- 실행 계획을 통해서 Extra 컬럼의 값이 Using filesort일 경우 Filesort 정렬이 수행되었다는 것을 확인할 수 있다.
- Filesort를 이용한 정렬 작업은 sort_buffer_size 시스템 변수에 명시된 크기에 따라 메모리나 임시 디스크를 활용하여 정렬을 수행한다.
  - sort_buffer_size 미만의 정렬을 수행할 경우 메모리 내에서 정렬을 수행하게 된다.
  - sort_buffer_size를 초과할 경우 메모리와 임시 디스크를 활용하며, 정렬해야 할 레코드를 여러 조각으로 나누어 임시 디스크에 저장하고, 각 임시 디스크별로 저장된 내용을 다시 병합해 주는 작업이 필요하다. 이러한 병합 작업을 멀티 머지라고 한다. 임시 디스크를 활용한 정렬을 수행할 경우 디스크 I/O를 유발하며 쿼리 수행 시간에 영향을 미칠 수 있다.
  - 때문에 최대한 sort_buffer_size 이내에 정렬이 수행될 수 있도록 조건절이나 페이징 처리를 활용하여 정렬해야하는 레코드 수를 줄여주는 것이 좋다.

#### 2.2. sort_buffer_size
- sort_buffer_size는 세션 메모리 영역에 해당하며 여러 커넥션을 통해서 정렬 작업을 하게 되면 사용하는 메모리 공간이 커짐을 의미한다.
  - 때문에 동시에 많은 정렬을 수행하게 될경우 메모리 부족 현상을 겪을 수 있다. 
- Filesort를 이용한 정렬을 수행할 때 단순히 정렬이 느리다는 이유로 sort_buffer_size 시스템 변수의 크기를 늘리게 될경우 MySQL 서버 자체가 문제가 될 수 있으므로 주의해야한다.

> https://dev.mysql.com/doc/refman/8.2/en/order-by-optimization.html

### 3. 임시테이블을 이용한 정렬

- 하나의 테이블로부터 나온 결과 값들을 정렬하는 경우라면 임시 테이블이 필요하지 않을 수 있지만, 테이블을 조인해서 나온 결과 값들을 정렬하는 경우라면 임시 테이블이 필요할 수 있다.
  - 임시테이블을 이용한 정렬을 수행하는 경우 Extra컬럼에서 `Using temporary; Using filesort` 값이 나오는 것을 확인할 수 있다.

#### 3.1. 언제 임시테이블을 이용한 정렬을 수행할까?
- 조인 활용시 임시 테이블을 이용한 정렬을 수행하지 않는 경우는 드라이빙 테이블의 클러스터링 인덱스를 통해서 정렬하는 경우를 제외하고는 임시 테이블을 이용한 정렬을 수행한다.
```sql
-- 드라이빙 테이블의 클러스터링 인덱스를 이용한 정렬 수행
explain select * from employees e left join salaries s on e.emp_no = s.emp_no where e.emp_no between 100000 and 100100 order by e.emp_no;

+--+-----------+-----+----------+-----+------------------------+-------+-------+------------------+----+--------+-----------+
|id|select_type|table|partitions|type |possible_keys           |key    |key_len|ref               |rows|filtered|Extra      |
+--+-----------+-----+----------+-----+------------------------+-------+-------+------------------+----+--------+-----------+
|1 |SIMPLE     |e    |null      |range|PRIMARY,ix_emp_no_gender|PRIMARY|4      |null              |101 |100     |Using where|
|1 |SIMPLE     |s    |null      |ref  |PRIMARY                 |PRIMARY|4      |employees.e.emp_no|9   |100     |null       |
+--+-----------+-----+----------+-----+------------------------+-------+-------+------------------+----+--------+-----------+

-- 드리븐 테이블의 클러스터링 인덱스를 이용한 정렬 수행 
explain select * from employees e left join salaries s on e.emp_no = s.emp_no where e.emp_no between 100000 and 100100 order by s.emp_no, s.from_date;

+--+-----------+-----+----------+-----+------------------------+-------+-------+------------------+----+--------+--------------------------------------------+
|id|select_type|table|partitions|type |possible_keys           |key    |key_len|ref               |rows|filtered|Extra                                       |
+--+-----------+-----+----------+-----+------------------------+-------+-------+------------------+----+--------+--------------------------------------------+
|1 |SIMPLE     |e    |null      |range|PRIMARY,ix_emp_no_gender|PRIMARY|4      |null              |101 |100     |Using where; Using temporary; Using filesort|
|1 |SIMPLE     |s    |null      |ref  |PRIMARY                 |PRIMARY|4      |employees.e.emp_no|9   |100     |null                                        |
+--+-----------+-----+----------+-----+------------------------+-------+-------+------------------+----+--------+--------------------------------------------+
```
- 임시 테이블을 활용하는 이유는 



### 스트리밍 방식

- 

### 버퍼링 방식
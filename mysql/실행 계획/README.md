### id 컬럼

- id 컬럼은 SELECT 단위 쿼리별로 부여되는 식별자 값이다.


### 2. select_type 컬럼
- select_type 컬럼을 통해서 SEELCT 쿼리가 어떤 유형의 쿼리인지를 알 수 있다.

#### 2.1. SIMPLE
- ***UNNION이나 서브쿼리를 사용하지 않은 단순한 SELECT 쿼리***인 경우 select_type은 SIMPLE로 표시된다.
- 아무리 복잡하더라도 실행 계획에서 select_type이 SIMPLE인 단위 쿼리는 하나만 존재하며, 일반적으로 제일 바깥 SELECT 쿼리의 select_type이 SIMPLE로 표시된다.

#### 2.2. PRIMARY
- UNION이나 서브쿼리를 가지는 SELECT 쿼리의 실행계획에서 가장 바깥쪽에 있는 단위인 경우 select_type은 PRIMARY로 표시된다.

```sql
explain
select employees.emp_no,
       (select dept_emp.dept_no from dept_emp where dept_emp.emp_no = employees.emp_no)
from employees;

+--+------------------+---------+
|id|select_type       |table    |
+--+------------------+---------+
|1 |PRIMARY           |employees|
|2 |DEPENDENT SUBQUERY|dept_emp |
+--+------------------+---------+
```

#### 2.3. SUBQUERY

- select_type의 SUBQUERY는 FROM절 이외에서 사용되는 서브쿼리를 의미한다.

#### 2.4 DEPENDENT SUBQUERY

- 서브쿼리가 바깥쪽 SEELCT 쿼리에서 정의된 컬럼을 사용하는 경우, select_type이 DEPENDENT SUBQUERY라고 표시된다.

```sql
explain
select employees.emp_no,
       (select dept_emp.dept_no from dept_emp where dept_emp.emp_no = employees.emp_no)
from employees;

```

- 서브쿼리 결과가 외부의 SELECT 쿼리의 컬럼에 의존적이기 때문에 `DEPENDENT(의존하는)`라는 키워드가 붙는다.
- DEPENDENT 키워드가 붙은 쿼리의 경우 외부의 쿼리가 먼저 실행되고 난뒤, 해당 쿼리가 실행되어야 하므로 일반적인 쿼리보다는 처리 속도가 늦을 수 있다.

#### 2.5. DERIVED (파생된)
- SELECT 쿼리의 실행 결과로 메모리나 디스크에 임시테이블을 생성하는 경우, select_type에 DERIVED가 표시된다.
    ```sql
    explain
    select *
    from
        (select dept_no, emp_no
        from dept_emp
        group by dept_no, emp_no) de -- group by절 때문에 임시 테이블 생성
    left join
        employees e
    on de.emp_no = e.emp_no;
    +--+-----------+----------+
    |id|select_type|table     |
    +--+-----------+----------+
    |1 |PRIMARY    |<derived2>|  -- 파생 테이블 생성
    |1 |PRIMARY    |e         |
    |2 |DERIVED    |dept_emp  |
    +--+-----------+----------+
    ```
- 서브쿼리가 FROM절에서 사용되는 경우, select_type이 DERIVED인 실행계획을 사용할 것 같지만, 옵티마이저가 외부 쿼리와 통합하는 형태의 최적화를 수행하기도 한다.
    ```sql
    explain
    select *
    from
        (select dept_no, emp_no from dept_emp where emp_no) de
    left join
        employees e
    on de.emp_no = e.emp_no;
    +--+-----------+--------+
    |id|select_type|table   |
    +--+-----------+--------+
    |1 |SIMPLE     |dept_emp|
    |1 |SIMPLE     |e       |
    +--+-----------+--------+
    ```
    - 마치 조인문 처럼 처리함
- 옵티마이저가 많은 부분을 최적화 해주지만 모든 부분을 임시테이블 생성을 하지 않고 처리해주지는 않는다.
  - 때문에 select_type컬럼에서 DERIVED형태의 실행 계획은 조인으로 해결할 수 있게 쿼리를 개선해주는 것이 성능상 유리하게 가져갈 수 있다.
    - 불필요한 서브쿼리는 조인으로 쿼리를 재작성해서 처리 
  - 옵티마이이저가 처리할 수 있는 것은 한계각 있으므로 최적화된 쿼리를 작성하는 것이 중요하다.

#### DEPENEDENT DERIVED

#### UNCACHEABLE SUBQUERY

#### MATERIALIZED
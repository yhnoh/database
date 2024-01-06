### NL Join (Nested Loop Join)

- NL 조인은 드라이빙 테이블에서 하나의 행을 읽고 드라이빙 테이블에서 읽은 행을 통해서 조인 대상이 되는 드리븐 테이블의 행을 찾는 일련의 과정을 반복하며 결과 집합을 찾는 조인 방식이다.
- 한번에 하나씩 행을 읽어가며 조인을 수행하기 때문에 드리븐 테이블에 동일한 데이터를 여러번 읽을 수 있는 가능성이 있다.

```sql
select *
from employees e join salaries s
on e.emp_no and s.emp_no;

```

```java
for (e in employees) {
    for (s in salaries) {
        if(e.emp_no == s.emp_no) {
            // 해당 행이 조인을 만족
            break;
        }
    }
}
```


> https://dev.mysql.com/doc/refman/8.2/en/nested-loop-joins.html
> 친절한 SQL 튜닝, 조시형, P255-273


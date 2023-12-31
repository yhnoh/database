### 1. 클러스터링 인덱스

---

- 클러스터링 인덱스는 ***인덱스의 정렬 순서에 따라서 테이블 레코드의 정렬 순서가 정해지는 인덱스를 의미***한다.
  - 클러스터링 인덱스는 테이블의 데이터를 인덱스의 키 값에 따라 물리적으로 정렬된다.
  - MySQL에서 클러스터링 인덱스는 InnoDB 스토리지 엔진에만 지원하며, 테이블의 프라이머리 키에만 적용되는 내용이다.
- 프라이머리 키 쓰기
  - 프라이머리 키의 값에 따라서 레코드의 저장위치가 결정되기 때문에 일반적인 인덱스의 쓰기 작업보다는 성능이 느리다.
    - 일반적인 인덱스 컬럼의 변경 작업은 B-Tree 인덱스의 저장 위치만 변경할 수 있으며, 체인지 버퍼의 도움을 받아 인덱스 변경의 지연처리도 가능하다.
    - 하지만 프라이머리 키 값의 변경은 중복 체크 때문에 체인지 버퍼의 도움을 받을 수 없으며, B-Tree인덱스의 저장 위치 변경 및 레코드의 저장 위치도 변경해야한다.
      - 해당 작업을 진행하면서 읽기 및 쓰기 잠금이 일어난다.
    - 때문에 프라이머리 키 값은 최대한 변경될 일이 없으며, 유일성을 가진 컬럼으로 지정하는 것이 좋다.
- 프라이머리 키 읽기
  - 일반적인 인덱스에 비해서 쓰기 작업이 느린 대신 읽기 작업에서 좀 더 좋은 성능을 발휘한다.
  - 특히 범위 검색에서 일반적인 인덱스에 비해서 더 좋은 성능을 기대할 수 있다.
    - 범위 검색에서 좋은 성능을 기대할 수 있는 이유는 인덱스의 인덱스의 키 값에 따라 물리적으로 정렬되기 때문이다.
      - 디스크에서 레코드를 읽기위해서는 랜덤 I/O가 발생하는데, 일반적인 인덱스의 경우 해당 인덱스 값의 정렬에 따라서 물리적으로 정렬되어 있지 않기 때문에 연속적으로 블록을 읽을 가능성이 낮다.
      - 하지만 클러스터링 인덱스의 경우 한 번의 디스크 회전으로 여러 개의 블록을 읽을 수 있으며, 디스크 헤더의 이동이 최소화하여 I/O 비용이 절감되어 쿼리의 실행 속도가 향상될 수 있습니다.
      - [순차(Sequential) I/O와 랜덤(Random) I/O](https://velog.io/@ddangle/%EC%88%9C%EC%B0%A8Sequential-IO%EC%99%80-%EB%9E%9C%EB%8D%A4Random-IO) 

#### 1.1. 클러스터링 테이블 사용시 주의할 사항
1. 프라이머리 키를 무조건 생성해주는 것을 권장한다.
   - InnoDB 테이블에서 프라이머리 키를 정의하지 않으면 InnoDB 스토리지 엔진이 내부적으로 일련번호 컬럼을 추가하게 되지만, 자동으로 생성된 값으로 사용자가 접근할 수 없다.
   - 이 말은 InnoDB 테이블에서 사용자가 프라이키를 정의하지 않더라도 프라이머리 키를 자동 정의하게 된다는 의미이다.
   - 때문에 프라이머리 키를 걸만한 컬럼이 없을 경우 AUTO_INCREMENT 값이라도 프라이머리 키로 설정하는 것이 좋다.
   - 프라이머리 키는 대부분 검색에서 상당히 빈번하게 사용되며, 클러스터링되지 않는 테이블에 비해 매우 빠르게 최리될 수 있다. (특히 범위로 많은 레코드르 검색하는 경우)
   - 복제 성능을 보장하기위해서라도 프라이머리 키를 꼭 생성하는 것이 좋다.
2. 프라이머리 키를 생성할 때 컬럼의 우선순위를 생각해보자.
   - 프라이머리 키를 생성할 때는 해당 테이블을 대표적으로 표현할 수 있는 컬럼으로 생성하는 것을 먼저 고려하는 것이 좋다.
     - 대표적으로 표현할 수 있는 컬럼일 경우 검색의 빈도가 높기 때문에 해당 컬럼으로 생성하는 것이 좋다.
     - 때문에 테이블 생성 시 무작정 AUTO_INCREMENT 컬럼을 추가하지말고 대표적으로 표현할 수 있는 값이 있는지 먼저 확인해보자.
   - 만약 프라이머리 키 값의 크기가 너무 크거나 대표적으로 표현할만한 컬럼이 없을 경우 AUTO_INCREMENT 컬럼을 추가하고, 이를 프라이머리 키로 설정하자.

### 2. 유니크 인덱스

---

- 유니크 인덱스는 테이블이나 인덱스에 값이 중복될 수 없는 인덱스를 의미한다.
  - 제약 조건을 가진 인덱스이며 MySQL에서는 인덱스 없이 유니크 제약만 설정할 방법이 없다.
- 유니크 인덱스 읽기
  - 일반적인 인덱스와 유니크 인덱스의 레코드 읽기 작업의 성능 차이는 거의 없다.
  - 레코드 읽기의 성능을 구분하는 것은 디스크 I/O이며, 일반적인 인덱스의 경우에는 중복 값을 허용하기 때문에 더 많은 레코드를 읽어 들여야하기 때문에 느린 것이지 인덱스 자체의 특성 때문에 느린 것은 아니다.
- 유니크 인덱스 쓰기
  - 유니크 인덱스의 쓰기 작업은 일반적인 인덱스 쓰기 작업보다 느리게 작동된다.
    - 유니크 인덱스의 경우 중복 값을 체크하기 위해서 일반적인 인덱스에 비해 읽기 및 쓰기 작업에 대해서 잠금을 걸어야하기 때문이다.
    - 또한 일반적인 인덱스는 인덱스 키의 변경에 대한 많은 자원 소모를 해결하기 위해서 체인지 버퍼를 이용하는데, 유니크 인덱스의 경우에는 중복 값 체크 때문에 인덱스 변경 작업에 대한 지연처리를 할 수 없다.
  - 일반적인 인덱스에 비해서 중복 체크 제약조건을 추가할 수 있는 장점이 있지만, 인덱스 쓰기에 대한 성능 이슈가 발생할 수 있기 때문에 꼭 필요한 경우가 아니라면 생성하지 않는 것이 좋다.
    - 여기서 꼭 필요한 경우랑 중복 체크 제약조건이 필요한 경우를 의미한다.

> Real MySql 8.0 개발자와 DBA를 위한 MySQL 실전 가이드, 백은비,이성욱, P270-279 <br/>
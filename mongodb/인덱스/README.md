###


- 100만건 데이터를 member 컬렉션에 삽입
```sql
for (i=0; i < 1000000; i++){
    db.member.insertOne({'username': 'username' + i, 'password': 'password' + i, 'age': i });
}

db.member.find({username : 'username1'}).explain('executionStats');

```
- explain 커서 보조자 메서드는 CURD 작업의 실행 정보를 제공한다.
- 인덱스를 사용하지 않을 경우, 쿼리 실행결과를 확인해보자

```json
[
  {
    //...
    "executionStats": {
      "executionSuccess": true,
      "nReturned": 1,
      "executionTimeMillis": 214,
      "totalKeysExamined": 0,
      "totalDocsExamined": 999999,
      "executionStages": {
        "stage": "COLLSCAN",
        "filter": {
          "username": {
            "$eq": "username1"
          }
        },
        "nReturned": 1,
        "executionTimeMillisEstimate": 2,
        "works": 1000000,
        "advanced": 1,
        "needTime": 999998,
        "needYield": 0,
        "saveState": 1000,
        "restoreState": 1000,
        "isEOF": 1,
        "direction": "forward",
        "docsExamined": 999999
      }
    }
    //...
  }
]
```

- 해당 결과를 통해서 
- nReturned: 쿼리를 통해 반환된 도큐먼트 개수
- totalKeysExamined: 쿼리를 실행하며 살펴본 인덱스 개수
- totalDocsExamined: 쿼리를 실행하며 살펴본 도큐먼트 개수
- executionTimeMillis: 쿼리 실행에 따라 걸린 시간


```sql
db.member.createIndex({'username': 1});

db.member.find({username : 'username1'}).explain('executionStats');
```

```json
[
  {
    //...
    "executionStats": {
      "executionSuccess": true,
      "nReturned": 1,
      "executionTimeMillis": 4,
      "totalKeysExamined": 1,
      "totalDocsExamined": 1,
      "executionStages": {
        "stage": "FETCH",
        "nReturned": 1,
        "executionTimeMillisEstimate": 0,
        "works": 2,
        "advanced": 1,
        "needTime": 0,
        "needYield": 0,
        "saveState": 0,
        "restoreState": 0,
        "isEOF": 1,
        "docsExamined": 1,
        "alreadyHasObj": 0,
        "inputStage": {
          "stage": "IXSCAN",
          "nReturned": 1,
          "executionTimeMillisEstimate": 0,
          "works": 2,
          "advanced": 1,
          "needTime": 0,
          "needYield": 0,
          "saveState": 0,
          "restoreState": 0,
          "isEOF": 1,
          "keyPattern": {
            "username": 1
          },
          "indexName": "username_1",
          "isMultiKey": false,
          "multiKeyPaths": {
            "username": []
          },
          "isUnique": false,
          "isSparse": false,
          "isPartial": false,
          "indexVersion": 2,
          "direction": "forward",
          "indexBounds": {
            "username": ["[\"username1\", \"username1\"]"]
          },
          "keysExamined": 1,
          "seeks": 1,
          "dupsTested": 0,
          "dupsDropped": 0
        }
      }
    }
    //...
  }
]
```

- 인덱스 생성을 통해서 nReturned, totalDocsExamined,, executionTimeMillis이 더 나은 수치를 보여준다.
- 인덱싱된 필드를 변경하는 쓰기 작업은 더 오래 걸린다.
  - 데이터가 변경될 때 도큐먼트뿐만 아니라 인덱스를 갱신해야 하기 때문이다.

- 실무에서는 하나이상의 키를 기반으로 인덱스를 작성하는 경우가 많다.
- 인덱스는 모든 값을 정렬된 순서로 보관하므로 인덱스 키로 도큐먼트를 정렬하는 작업이 훨씬 빨라진다.

- 예를 들어 username 인덱스는 다음과 같은 쿼리에서는 도움이 되지 않는다. 
```sql
db.member.find().sort({age:1, username:1}).explain('executionStats');
```
- 명령에서 age를 정렬한후 username을 정렬하기 때문에 username 으로만 정렬되어 있는 인덱스는 별로 도움이 되지 않는다.
- 때문에 age와 username 필드를 인덱스를 걸어서 복합인덱스로 걸어두는 것이좋다.
  - 중요한 것은 인덱스의 순서이다.
  - username과 age 순으로 인덱스를 걸 경우에는 username 순으로 정렬된 뒤, age순으로 정렬된 인덱스가 나오니 위의 쿼리를 통해 인덱스를 효율적으로 활용할 수 없다. 

> https://www.mongodb.com/docs/manual/reference/method/db.collection.explain/

- 컬렉션 스캔
- 컬렉션 내에 있는 모든 데이터를 읽는 스캔 방식이다.


### 몽고DB가 인덱스를 선택하는 방법

- 사용자가 쿼리를 실행하면 몽고DB는 해당 쿼리 모양(query shape)을 확인한다.
  - 검색할 필드와 정렬 여부 등 추가 정보와 관련이 있다.
- 여러 인덱스 중 몇몇 인덱스를 쿼리 후보로 식별하고, 각 인덱스 후보의 쿼리 플랜(query plan)을 만들어 병렬 스레드에서 쿼리를 실행한다.
  - 어떤 스레드에서 가장 빨리 결과를 반환하는지 확인하기 위한 작업이다.
- 이후 동일한 쿼리 모양이 나올 경우 경쟁에 이긴 쿼리 플랜을 사용하가 위해서 캐시에 저장된다.
  - 컬렉션 또는 인덱스가 변경되거나, mongod 프로세스를 다시 시작할 때는 캐시가 제거된다.
  - 쿼리 플랜 캐시는 명시적으로도 삭제가 가능하다.


### 인덱스 설계시 고려해야할 점

- 커서 hint 메서드를 사용하면 모양이나 이름을 지정함으로써 사용할 인덱스를 지정할 수 있다.
- 모든 데이터 셋에 해당되지는 않지만, 일반적으로 동등 필터를 사용할 필드가 다중값 필터를 사용할 필드보다 앞에 오도록 복합 인덱스를 설계하는 것이 좋다.
- 인덱스를 사용할 경우 totalKeysExamined와 nReturned의 개수가 유사할 수록 인덱스를 효율적으로 활용했다고 이야기할 수 있다.


#### 복합 인덱스를 설계시 고려해야할 점
- 복합 인덱스를 설계할 때는 인덱스를 사용할 공통 쿼리 패턴의 동등 필터, 다중값 필터, 정렬 구성 요소를 처리하는 방법을 알아야한다.
- 복합 인덱스를 구성할 때 고려해볼만한 점
  - 동등 필터에 대한 필드를 선행 인덱스로 두자
  - 정렬에 사용되는 필드를 다중값 필드 앞에 표시하자.
  - 다중값 필터에 대한 필드는 마지막에 표시하자.


### 인덱스 정렬 방향 선택하기

- 인덱스 항목은 오름차순과 내림차순으로 정렬할 수 있다.
- 복합 인덱스의 경우 인덱스 정렬 방향
  - 복합 인덱스의 경우 선행 필드에 대해서는 정렬이 상관이 없을 수 있지만, 이후 필드에 대해서는 쿼리를 사용하는 방식에 따라 정렬을 최적하해야한다.
  - `db.member.createIndex({age:1, username:1});`와 - `db.member.createIndex({age:-1, username:1});`은 역방향 인덱스로 거의 비슷한 성능을 제공한다.
  - `db.member.createIndex({age:1, username:1});`와 - `db.member.createIndex({age:1, username:-1});`은 역방향 인덱스는 쿼리 검색 결과에 따라 다른 성능을 제공한다.
- 단일 인덱스의 경우 인덱스 정렬 방향
  - 단일 인덱스의 경우 필드의 정렬 방향이 크게 영향이이 없다.
  - 때문에 하나의 필드에 정렬 방향이 서로 다른 두개의 인덱스를 설정할 필요는 없다.
  - `db.member.createIndex({'username': 1});`와 `db.member.createIndex({'username': -1});`은 역방향 인덱스로 거의 비슷한 성능을 제공한다.
    - B-Tree 인덱스와 동일하다.
  - 때문에 하나의 필드에 정렬 방향이 서로 다른 두개의 인덱스를 설정할 필요는 없다.
  
#### 커버드 쿼리 사용하기

- 인덱스는 항상 도큐먼트를 찾는 데 사용되고, 실제 도큐먼트를 가져오기 위하여 I/O를 통해 저장소에 접근하게 된다.
- 하지만 쿼리가 단지 인덱스에 포함된 필드를 찾는 중이라면 저장소 I/O를 통해서 도큐먼트를 가져올 필요가 없다.
  - 이를 커버드 쿼리라고 한다.
  - 불필요한 필드를 제공하지 않고, 이를 통해서 불필요한 I/O를 줄일 수 있으며 성능 향상에 큰 도움이 된다.

#### 암시적 인덱스
- 복합 인덱스가 N 개의 필드를 가진다면, 앞 부분의 필드들은 공짜 인덱스를 가진다.
- 예를 들어 `{a: 1, b: 1, c: 1}`의 인덱스를 가지면 `{a: 1}, {a: 1, b: 1}` 인덱스와 동일한 인덱스 효과를 가진다.
- 앞부분만 공짜 인덱스가 되므로 `{b: 1}`이나 `{a: 1, c: 1}`과 같은 인덱스를 사용하느 쿼리는 최적화 되지 않는다.
 
 ### $연산자의 인덱스 사용법

 #### 비효율적인 연산자
 - $ne 쿼리는 인덱스를 사용하긴 하지만 잘 활요하지는 못한다.
 - $ne 로 지정된 항목을 제외한 모든 인덱스 항목을 살펴봐야 하므로 기본적으로 전체 인덱스를 살펴봐야 한다.
   - $ne 로 지정된 항목이 컬렉션의 대부분을 차지고할 때는 효율적이지만, 그렇지 않다면 거의 컬렉션 전체를 확인해야 한다.


#### 범위
- 다중 필드로 인덱스를 설계할 때는 완전 일치가 사용될 필드를 첫 번째에, 범위가 사용될 필드를 마지막에 놓는 것이 좋다.
- 이는 쿼리가 첫 번째 인덱스 키와 정확히 일치하는 값을 찾은 후 두번째 인덱스 범위 안에서 검색하게 해준다.

#### OR 쿼리
- 일반적으로 몽고 DB는 쿼리당 하나의 인덱스만 사용할 수 있다.
- {x : 1}, {y : 1}로 { x: 123, y: 456}으로 쿼리를 실행하면 몽고 DB는 생성한 인덱스 두 개 중 하나만 사용한다.
- 유일한 예외는 $or이다.
  - $or절은 두개의 쿼리를 수행하고 결과를 합치므로 하나씩 인덱스를 사용할 수 있다.
- 일반적으로 두번 쿼리해서 결과를 병합하면 한 번 쿼리할 때보다 훨씬 비효율적이므로 가능하면 $or보다는 $in절을 사용해보자.

### 객체 및 배열 인덱싱
- 몽고 DB는 도큐먼트 내부의 내장 필드와 배열에 인덱스를 생성하도록 허용한다.
- 또한 내장 객체와 배열 필드는 복합 인덱스에서 최상위 필드와 결합될 수 있다.

#### 서브 도큐먼트 인덱싱
- 서브 도큐먼트 전체를 인덱싱하면, 서브 도큐먼트 전체에 쿼리할 때만 도움이 된다.
- 

#### 배열 인덱싱하기
- 배열에도 인덱싱이 가능하다.

### explain

- isMultiKey: 다중키 인덱스 사용 여부
- nReturned: 쿼리에 의해 반환된 도큐먼트 개수
- totalDocsExamined: 
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


### MongoDB
- 몽고 DB는 **분산 확장**을 위하여 관계형 데이터베이스가 아닌 도큐먼트 지향 데이터베이스이다.
  - 도큐먼트는 여러 형태의 자료구조를 허용함으로써 **복잡한 계층 관계를 하나의 레코드로 표현**할 수 있다.
  - 도큐먼트의 키와 값을 미리 정의하지 않기 ***고정된 스키마가 없다***. 때문에 쉽게 필드를 추가하거나 삭제할 수 있다. 모델링이 정형화되어 있지 않은 데이터베이스를 사용중일 때는 쉽게 필드를 추가하거나 제거하 수 있다.
- 성능 확장 보다는 분산확장
  - 도큐먼트를 여러 서버에 더 쉽게 분산하고, 자동으로 재분배하고 해당 데이터를 라우터를 이용하여 서빙하기 때문에 클러스트 내 데이터 양과 부하를 조절할 수 있다.
- 다양한 기능 제공
  - 일반적인 인덱스이외의 계층 구조에 따른 인덱스 및 공간 정보, 전문 인덱싱 기능 제공
- 집계
  - 데이터 처리 파이프라인 개념인 집계 프레임워크 제공
  - 
- 컬렉션 유형
- 파일 스토리지


#### Document

#### Collection

#### Database
- admin: 인증과 권한 부여 역할을 한다.
- local: 단일 서버에 대한 데이터를 저장한다. 복제 셋에서 local은 복제 프로세스에 사용된 데이터를 저장, 인스턴스별 각기 다른 local 데이터베이스가 구성
- config: 샤딩된 몽고DB 클러스터는 config 데이터베이스를 사용해 각 샤드 정보를 저장한다.

#### MongoDB Shell
- 몽고 DB 인스터스와 상호작용하는 자바스크립트 셸 제공, 때문에 표준 자바스크립트 라이브러리의 모든 기능을 활용 가능, 독자적인 MongoDB 문법 제공, 기본적으로 CRUD 기능 제공 및
mongod 명령어를 이용하여 몽고DB 실행파일 실행

help를 입력하여 셸에 내장된 도움말을 볼 수 있다.

.mongorc.js
- 자주 로드되는 스크립트를 .mongorc.js 파일에 넣어 사용랄 수 있다.
- dropDatabase나 deleteIndexs 같은 함수가 아무것도 수행하지 않게 재정의할 수 있다.
- 프롬프트를 설정하기 좋음, 예를 들어 사용자가 쿼리를 입력하게 되었을 때 실패한 오류나 성공한 커맨드를 알려줄 수 잇다.
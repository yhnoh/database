### 가용성



## 센티널

- 센티널은 각 레디스 노드들을 모니터링하며, ***마스터 노드 장애시 복제 노드를 마스터 노드로 자동 승격시켜주어(자동 페일오버) 고가용성을 확보***할 수 있다.
  - 만약 고가용성이 확보되지 않는다면 복제 노드의 읽기 전용 상태를 해제한 이후 레디스 클라이언트가 해당 복제 노드의 엔드포인트를 변경해야한다. 이 시간동안 서비스 장애를 겪을 수 있다.
  - 또한 레디스를 캐시로 사용하고 있는 애플리케이션의 경우 장애 발생시 원본 데이터 소스를 직접 읽어들이는 상황이 발생하며 더 큰 장애로 번질 수 있는 위험성이 존재한다.

### 센티널이 제공하는 기능
1. 모니터링: 마스터와 복제 노드의 상태를 실시간으로 확인한다.
2. 자동 페일오버(Auto Failover): 마스터 비정상 상태를 감지해 정상 상태의 복제본 중 하나를 마스터 노드로 승격시킨다.
3. 인스턴스 구성 정보 안내: 센티널은 클라이언트에게 현재 마스터 정보를 알려준다. 때문에 페일오버가 발생하여 마스터 노드가 변경되더라도 클라이언트가 레디스 엔드포인트를 변경할 필요가 없다.

### 자동 페일오버를 하기위한 구성
- 센티널은 레디스와 다른 별도의 프로그램으로써 센티널 프로그램 자체의 고가용성을 확보기하기 위하여 최소 3대 이상일때 정상작동한다.
- 센티널은 자동 페일오버를 하기위해서 과반수 선출 개념을 사용하기 때문에 3대 이상의 홀수로 구성하는 것이 좋다.
  - 과반수 선출을 하기위하여 쿼럼(quorum)이라는 개념을 사용하여 센티널 인스턴스가 3개이고 쿼럼을 2로 설정하면 최소 2개 이상의 센티널 인스턴스가 마스터 비정상 상태에 동의한다면 페일오버 프로세스를 진행한다.

## 센티널 자동 페일오버 과정


### 센티널 명령어

```sh
## 센티널 실행 명령어
redis-sentinel /path/to/sentinel.conf
redis-server /path/to/sentinel.conf --sentinel

SENTINEL HELP

## 마스터,복제,센티널 노드 상태 확인
SENTINEL MASTER mymaster
SENTINEL REPLICAS mymaster
SENTINEL SENTINELS mymaster

## 센티널이 쿼럼을 충족시켜 
SENTINEL CKQUORUM mymaster

## 센티널 수동 페일오버
SENTINEL FAILOVER mymaster

## 센티널 구성정보 변경
SENTINEL MONITOR 
SENTINEL REMOVE
SENTINEL SET <master-name> <option> <value>

SETTINEL RESET <master-name>

```


> [Redis > High availability with Sentinel](https://redis.io/docs/latest/operate/oss_and_stack/management/sentinel/)
## Redis Cluster
- Redis Replication 만을 이용하여 고가용성을 확보하기에는 몇가지 문제가 있었다.
  - 수동 장애 조치: 하나의 복제본을 마스터로 승격 시키는 작업을 수동으로 진행행야한다.
  - 클라이언트 연결 문제: 수동 장애 조치 이후 클라이언트는 새로운 마스터 노드로 연결을 다시 시도해야 한다.
  - 스케일업의 한계: Redis Replication은 하드웨어의 사양을 높이는 방식으로 저장 공간을 확보할 수 있으며 이는 한계지점이 존재한다.
- Redis Cluster는 Redis Replication 만으로는 해결할 수 없는 문제를 해결하여 고가용성을 확보하고 서비스의 연속성을 보장할 수 있는 기능을 제공한다.
  - 자동 장애 조치 (Auto Failover)
  - 스케일 아웃 가능


### Redis Cluster Create
```sh
redis-cli --cluster create [host1:port1 host2:port2 ... hostN:portN] --cluster-replicas 1 --cluster-yes
```



### 레디스 클러스터 동작방식
- 레디스 클러스터의 모든 노드는 어디에 키가 저장되어있는지 알고 있다.
- 클라이언트가 아무 노드에 데이터를 읽거나 쓰려고 할때 키가 할당된 마스터 노드로 리다이렉션 한다.
- 클라이언트에서는 클러스터 내에서 특정 키가 어떤 마스터에 저장되어 있는지에 대한 정보를 캐싱하여 키를 찾아오는 시간을 단축시킬 수 있다.


- 클러스터에 속한 각 노드는 서로를 모니터링하고 마스터 노드에 장애가 발생하면 복제본 노드를 자동 페일오버 시킨다.
- 해시 슬롯에 저장 
- client는 아무 Redis Master엑

### 레디스 클러스터 특징
#### 데이터 쓰기 및 읽기
- 해시슬롯
- 복수개의 키를 읽기 위한 해시태그
#### 자동 재구성
- 자동 페일오버
  - 클러스터 구조에서 
- 자동 복제본 마이그레이션

### Redis Cluster Architecture
단일 장애점(Single point of failure)이 없는 토폴로지: 메쉬(Mesh)   ---»   Redis Cluster
- Redis는 서로가 서로의 노드의 상태를 확인하는 작업을 수행하며 만약 하나의 노드가 장애가 일어나더라도 Slave노드를 Master로 승격시킨다던지, 
- 일부 노드가 다운되어도 다른 노드에 영향을 주지 않지만, 과반수 이상의 노드가 다운되면 레디스 클러스터는 멈추게 된다.
- 클라이언트는 어느 노드든지 접속해서 클러스터 구성 정보(슬롯 할당 정보)를 가져와서 보유하며, 입력되는 키(key)에 따라 해당 노드에 접속해서 처리한다.

> [redis > redis-enterprise-cluster-architecture](https://redis.io/redis-enterprise/technology/redis-enterprise-cluster-architecture/)

### Redis Cluster Sharding
- 하나의 DBMS에서 대량의 데이터를 처리하기 위하여 물리적인 저장공간을 분리하는 방식을 파티셔닝이(Partitioning)라 하고, 여러 DBMS에 데이터를 분리하는 방식을 샤딩(Sharding)이라고 한다. 
  - 파티셔닝은 
  - 샤딩을 이용하여 데이터를 분할하기 위해서는 샤드한 수만큼 DBMS의 수가 필요하다.
- Cluster를 구축하여 데이터를 저장하는 이유는 분산환경엥서 
- Redis Cluster 환경이 구축되어 있을 때 만약 키에 대한 데이터를 저장한다고 하면 자동으로 Redis가 어느 노드에 저장할지를 지정한다.
  - CRC16을 이용하여 사용자가 직접 데이터를 분배할 필요 없이 자동으로 데이터를 균등하게 분배한다.
  - 만약 사용자가 직접 파티셔닝 전략을 구상하여 데이터를 분배하고자 한다면, 이는 사용자에게 데이터 분배의 책임이 있기 때문에 직접 구현해야 한다.
  - 파티셔닝 전략에는 해시 파티셔닝, 범위 파티셔닝
- Hash slots은 16384까지 각각의 마스터 노드들이 관리를 하게 되며, 노드가 추가될 때마다 Reshading을 통해서 데이터를 균등하게 분배해야한다.
- 만약 동일한 노드에 key를 저장하고 싶다면, 

- 1000대의 노드까지 확장할 수 있도록 설계되었습니다.
- 노드 추가, 삭제 시 레디스 클러스터 전체를 중지할 필요 없고, 키 이동 시에만 해당 키에 대해서만 잠시 멈출 수 있습니다.

> [Redis > Key distribution model](https://redis.io/docs/latest/operate/oss_and_stack/reference/cluster-spec/#Key%20distribution%20model) <br/>
> [redisgate > cluster_introduction](http://www.redisgate.com/redis/cluster/cluster_introduction.php)
> https://sup2is.github.io/2020/07/22/redis-cluster.html
- Cluster 구조에서는 Key들이 하나의 노드에만 할당되어 있지 않고 여러 노드에 분산되어 있다. 때문에 Multiple Keys를 인자로 가지고 있는 명령어를 수행할 수 없다.
  - 하지만 Hashtag를 사용하여 같은 슬롯에 저장하는 것이 가능하다.
    - ` mset {user1}.1 a {user1}.2 b {user1}.3 c`

- Redis Cluster의 경우 
- multiple keys는 같은 해시 슬롯에 배치되지만, 


### Shading
- 


> [Redis Docs > Cluster](https://redis.io/docs/latest/operate/oss_and_stack/management/scaling/)
> https://meetup.nhncloud.com/posts/226
> https://cla9.tistory.com/102
> https://www.youtube.com/watch?v=mPB2CZiAkKM
> https://redis.io/docs/latest/operate/oss_and_stack/reference/cluster-spec/#key-distribution-model
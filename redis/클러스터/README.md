## Redis Cluster
- Redis Replication 만을 이용하여 고가용성을 확보하기에는 몇가지 문제가 있었다.
  - 수동 장애 조치: 하나의 복제본을 마스터로 승격 시키는 작업을 수동으로 진행행야한다.
  - 클라이언트 연결 문제: 수동 장애 조치 이후 클라이언트는 새로운 마스터 노드로 연결을 다시 시도해야 한다.
  - 스케일업의 한계: Redis Replication은 하드웨어의 사양을 높이는 방식으로 저장 공간을 확보할 수 있으며 이는 한계지점이 존재한다.
- Redis Cluster는 Redis Replication 만으로는 해결할 수 없는 문제를 해결하여 고가용성을 확보하고 서비스의 연속성을 보장할 수 있는 기능을 제공한다.
  - 스케일 아웃 가능
  - 자동 장애 조치 (Auto Failover)


### Redis Cluster 생성
- Redis Cluster 생성을 위해서는 설정 파일에서 클러스터 모드를 활성화하고, `create` 명령어를 사용하여 클러스터를 생성해야 한다.

#### Redis Cluster 생성을 위한 설정 파일 구성
- Redis Cluste를 활성화 하기 위해서는 `redis.conf` 파일에서 ***`cluster-enabled yes` 설정을 추가***해야 한다.
- 모든 Redis Cluster 노드는 ***`nodes.conf` 파일에 클러스터 구성 정보를 저장***하며, Redis Cluster 노드가 시작할때 생성하고 필요할때 마다 업데이트 된다.
  - nodes.conf 파일은 Redis Cluster에 구성된 각 노드의 상태를 확인할 수 있는 정보를 포함하고 있다.
```sh
## redis.conf 설정
cluster-enabled yes
cluster-config-file nodes.conf
```

#### Redis Cluster 생성을 위한 명령어
- 설정 파일에서 클러스터 모드를 활성화한 이후, `create` 명령어를 사용하여 Redis Cluster를 생성할 수 있다.
  - `redis-cli --cluster create [host1:port1 host2:port2 ... hostN:portN] --cluster-replicas 1 --cluster-yes`
  - `--cluster-replicas 1`: 각 마스터 노드에 대해 하나의 복제 노드를 생성
  - `--cluster-yes` 클러스터 생성 시 사용자에게 확인을 묻지 않고 자동으로 클러스터를 생성
- Redis Cluster 생성 시 모든 노드들은 자체적인 노드 ID를 가지고 있으며, Cluster에 구성된 모든 노드들이 다른 노드의 ID를 가지고 있다.

```sh
## 클러스터 생성 및 마스터-슬레이브 노드 설정
redis-cli --cluster create [host1:port1 host2:port2 ... hostN:portN] --cluster-replicas 1 --cluster-yes

## Redis Cluster 노드별 해시 슬롯 할당 및 마스터-슬레이브 설정 확인
Master[0] -> Slots 0 - 5460
Master[1] -> Slots 5461 - 10922
Master[2] -> Slots 10923 - 16383
Adding replica redis-node-5:6383 to redis-node-1:6379
Adding replica redis-node-6:6384 to redis-node-2:6380
Adding replica redis-node-4:6382 to redis-node-3:6381
M: da3c676c80bb8934ec7ae40e3405e132c5c686d1 redis-node-1:6379
   slots:[0-5460] (5461 slots) master ## 마스터 노드 해시 슬롯 할당
M: 448d727302eabb0096f53b34d00d226ae379d767 redis-node-2:6380
   slots:[5461-10922] (5462 slots) master
M: 6446ebc91c4c5c1c67aaf7fbcfca3e0245f0f5d2 redis-node-3:6381
   slots:[10923-16383] (5461 slots) master
S: 4eabb23a5e9fd248d9a2b06353ebe840b1dd3d0f redis-node-4:6382
   replicates 6446ebc91c4c5c1c67aaf7fbcfca3e0245f0f5d2 ## 복제 노드 해시 슬롯을 할당하지 않음
S: 01ebb88c6dcc386e92262a57b41923e36323299a redis-node-5:6383
   replicates da3c676c80bb8934ec7ae40e3405e132c5c686d1
S: 4c3b37d68c07b5d543d68e3fa7a7216f62442f02 redis-node-6:6384
   replicates 448d727302eabb0096f53b34d00d226ae379d767
>>> Nodes configuration updated
>>> Assign a different config epoch to each node
>>> Sending CLUSTER MEET messages to join the cluster
Waiting for the cluster to join
..
## Performing Cluster Check
>>> Performing Cluster Check (using node redis-node-1:6379)
M: da3c676c80bb8934ec7ae40e3405e132c5c686d1 redis-node-1:6379
   slots:[0-5460] (5461 slots) master
   1 additional replica(s)
S: 4c3b37d68c07b5d543d68e3fa7a7216f62442f02 172.20.0.3:6384
   slots: (0 slots) slave
   replicates 448d727302eabb0096f53b34d00d226ae379d767
S: 4eabb23a5e9fd248d9a2b06353ebe840b1dd3d0f 172.20.0.6:6382
   slots: (0 slots) slave
   replicates 6446ebc91c4c5c1c67aaf7fbcfca3e0245f0f5d2
M: 448d727302eabb0096f53b34d00d226ae379d767 172.20.0.4:6380
   slots:[5461-10922] (5462 slots) master
   1 additional replica(s)
M: 6446ebc91c4c5c1c67aaf7fbcfca3e0245f0f5d2 172.20.0.2:6381
   slots:[10923-16383] (5461 slots) master
   1 additional replica(s)
S: 01ebb88c6dcc386e92262a57b41923e36323299a 172.20.0.5:6383
   slots: (0 slots) slave
   replicates da3c676c80bb8934ec7ae40e3405e132c5c686d1
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.
```

- [클러스터 생성 스크립트](./cluster-create.sh)

### Redis Cluster Node 상태 확인
- `cluster nodes` 명령어를 사용하여 Redis Cluster에 구성된 노드들의 상태를 확인할 수 있다.
  - `redis-cli cluster nodes`
- 해당 정보는 `nodes.conf` 파일에 저장된 내용을 기반으로 하며, 각 노드의 ID, IP 주소 및 포트, 마스터/슬레이브 상태, 해시 슬롯 할당 정보 등을 포함하고 있다.

```sh
cluster nodes
<id> <ip:port@cport[,hostname]> <flags> <master> <ping-sent> <pong-recv> <config-epoch> <link-state> <slot> <slot> ... <slot>

id: 노드 ID
ip:port@cport: 노드의 IP 주소와 포트, 클러스터 버스 포트
flags: 노드의 상태 (master, slave, myself 등)
master: 슬레이브 노드인 경우 마스터 노드의 ID
ping-sent: 마지막 PING 메시지를 보낸 시간, 보류 중인 PING이 없다면 0 있다면 마지막 PING 메시지를 보낸 시간
pong-recv: 마지막 PONG 메시지를 받은 시간
config-epoch: 노드의 구성 에폭 (구성 변경 시 증가)
link-state: 노드의 연결 상태 (connected/disconnected)
slot: 노드가 관리하는 해시 슬롯
```

- [클러스터 상태 확인 스크립트](./cluster-nodes.sh) <br/>
- [Redis Docs > Cluster Nodes](https://redis.io/docs/latest/commands/cluster-nodes/)

### Redis Cluster 클라이언트 연결
- Redis Cluster는 여러 개의 노드로 구성되어 있으며 키의 이름에 따라서 어느 노드에 저장될지를 결정하게 된다.
- 일반적인 클라이언트 연결 방식은 단일 노드에 연결하여 데이터를 읽거나 쓰는 방식이자만, Redis Cluster 구조에서는 데이터가 여러 개의 노드로 분산되어 있기 때문에 사용하기 쉽지 않다는 단점이 있다.
- 만약 일반적인 클라이언트를 사용하여 데이터를 저장하거나 읽으려고 할때, 다른 노드에서 관리하는 해시 슬롯에 해당하는 키를 요청할 경우 에러가 발생한다. 
```sh
## 클러스터 모드를 지원하지 않는 클라이언트로 연결
redis-cli
## 명령어 수행
127.0.0.1:6379> SET key1 value1
(error) MOVED 9189 172.20.0.4:6380
```
- key1이라는 키를 입력하려고 했지만, 해당 키는 9189번 해시 슬롯으로 할당되어 있고, 이 슬롯은 172.20.0.4:6380 노드가 관리하고 있기 때문에 해당 노드로 리다이렉션 하라는 에러가 발생한다.

#### Redis Cluster 모드를 이용한 클라이언트 연결
- 클러스터 모드를 지원하는 클라이언트를 사용하면, 올바른 노드로 데이터를 저장하거나 읽을 수 있도록 자동으로 리다이렉션한다.
- 많은 클라이언트 라이브러리들이 클러스터 모드를 지원하며, `redis-cli`는 `-c` 옵션을 사용하여 클러스터 모드로 연결할 수 있다. 
```sh
## 클러스터 모드를 지원하는 클라이언트 연결
redis-cli -c
## 명령어 수행
127.0.0.1:6379> SET key1 value1
## 172.20.0.4:6380 노드로 리다이렉션된 이후 데이터 저장 수행
-> Redirected to slot [9189] located at 172.20.0.4:6380
OK
## 실제 데이터가 저장된 노드로 리다이렉션 됨
172.20.0.4:6380> 
```
- 클러스터 모드로 연결하여 `SET key1 value1` 명령어 실행시, 클라이언트는 자동으로 172.20.0.4:6380 노드로 연결하여 명령어 요청을 재전송하여 처리한것을 확인할 수 있다.

```
SET key1 value1 명령어 수행
(error) MOVED 발생, 이때 해시슬롯을 관리하는 노드 정보 제공
클라이언트는 해당 해시 슬롯을 관리하는 노드로 연결 수행
SET key1 value1
```
- 많은 클라이언트 라이브러리들은 리다이렉션 정보를 캐싱하여, 다음번에 동일한 키를 요청할때는 리다이렉션을 수행하지 않고 해당 노드로 직접 연결하여 데이터를 읽거나 쓰기 작업을 수행하도록 최적화되어 있다.
- 때문에 만약 클러스터이 구조가 변경되어 해시슬롯에 대한 정보가 변경되었을때 해당 클라이언트가 캐싱된 정보를 업데이트하는지를 확인할 필요가 있다.

### Redis Cluster 자동 페일오버
- Redis Replication 구조에서는 마스터 노드에 장애가 발생하면, 수동으로 복제 노드를 마스터로 승격시켜야한다.
  - 이로 인해서 수동으로 장애 조치를 수행하는 동안 서비스의 연속성을 보장할 수 없으며 고가용성의 확보가 어렵다는 단점이 있다.
- Redis Cluster는 자동 페일오버 기능을 제공하며, 마스터 노드에 장애가 발생할 경우 복제 노드를 자동으로 마스터로 승격시켜 서비스의 연속성을 보장할 수 있다.

#### 자동 페일오버 테스트
```sh
## 클러스터 노드 정보 확인
redis-cli cluster nodes
## 마스터: 172.20.0.7:6379, 복제: 172.20.0.5:6383 
fe24c1fc0c63e675f9c468b5600fad0032008e2e 172.20.0.7:6379@16379 myself,master - 0 1754813378000 1 connected 0-5460
c2c6fbbf4244c0aeab639a3646671e7f3faa270d 172.20.0.5:6383@16383 slave fe24c1fc0c63e675f9c468b5600fad0032008e2e 0 1754813378756 1 connected
3455c073a449e5996197e0cf4210ebecd8652e14 172.20.0.6:6384@16384 slave 769ef5cdf11d0a29e6f0b757cb3aa3ad6acbdd0a 0 1754813377512 2 connected
bcc2518b18b1abae38693b56dc579d8d03a3271b 172.20.0.2:6381@16381 master - 0 1754813378241 3 connected 10923-16383
769ef5cdf11d0a29e6f0b757cb3aa3ad6acbdd0a 172.20.0.4:6380@16380 master - 0 1754813378000 2 connected 5461-10922
2898e7e949dd3386ed0165428c5ee17a6979fb8a 172.20.0.3:6382@16382 slave bcc2518b18b1abae38693b56dc579d8d03a3271b 0 1754813378000 3 connected

## 172.20.0.7:6379 마스터 노드 shutdown
redis-cli shutdown

## 172.20.0.5:6383 복제 노드가 마스터 노드로 승격되었는지 확인
redis-cli -p 6383 cluster nodes
2898e7e949dd3386ed0165428c5ee17a6979fb8a 172.20.0.3:6382@16382 slave bcc2518b18b1abae38693b56dc579d8d03a3271b 0 1754813868527 3 connected
fe24c1fc0c63e675f9c468b5600fad0032008e2e 172.20.0.7:6379@16379 master,fail - 1754813654708 1754813652107 1 connected
769ef5cdf11d0a29e6f0b757cb3aa3ad6acbdd0a 172.20.0.4:6380@16380 master - 0 1754813868841 2 connected 5461-10922
bcc2518b18b1abae38693b56dc579d8d03a3271b 172.20.0.2:6381@16381 master - 0 1754813868000 3 connected 10923-16383
3455c073a449e5996197e0cf4210ebecd8652e14 172.20.0.6:6384@16384 slave 769ef5cdf11d0a29e6f0b757cb3aa3ad6acbdd0a 0 1754813867803 2 connected
c2c6fbbf4244c0aeab639a3646671e7f3faa270d 172.20.0.5:6383@16383 myself,master - 0 1754813868000 7 connected 0-5460
```
- 클러스터 노드 정보를 통해서 172.20.0.7:6379 마스터 노드를 shutdown 시킨 이후, 172.20.0.5:6383 복제 노드가 마스터 노드로 자동으로 승격되었는지 확인할 수 있다.

#### Redis Cluster에서 자동 페일오버가 가능한 이유
- Redis Cluster가 자동 페일오버가 가능한 이유는 각 노드가 서로를 모니터링하고 있으며, 해당 모니터링을 통해서 마스터 노드의 장애를 감지할 수 있기 때문이다. (Full Connected Mesh Topology)
- Redis Cluster에 구성된 각 노드는 서로를 모니터링하며 노드의 상태를 확인하기 위해서 클러스터 버스라는 독립적인 통신을 사용한다.
  - 일반적으로 레디스 노드가 사용하는 포트의 10000을 더한 값으로 자동 설정되며, 주의할점은 Redis Cluster로 구성된 노드간에 클러스터 버스 포트가 접근 가능해야한다. 만약 클러스터 버스 포트에 대한 접근이 불가하다면 Redsi Cluster가 정상저적으로 동작하지 않을 수 있다.
- Redis Cluster의 각 노드들은 클러스터 버스를 통해서 서로에게 PING 메시지를 보내고, 응답(PONG)을 받지 못하면 해당 노드가 장애가 발생했다고 판단한다.

> [Redis Docs > The cluster bus](https://redis.io/docs/latest/operate/oss_and_stack/reference/cluster-spec/#the-cluster-bus)

### Redis Cluster Sharding
- Redis 마스터 노드가 가지고 있는 해시슬롯중 일부를 다른 마스터로 이동하는 것이 가능하며 이를 리샤딩이라한다.
- 

docker exec -it redis-node-1 redis-cli --cluster reshard 127.0.0.1:7000
docker exec -it redis-node-1 redis-cli --cluster reshard redis-node-1:6379 --cluster-from 45127b3c8dc28d7ed3af02f6f2e7300a71496897 --cluster-to 4105d7b13f62b63ff24a7349983b581cd9aa0d41 --cluster-slots 100 --cluster-yes

redis-cli --cluster reshard <host>:<port> --cluster-from <node-id> --cluster-to <node-id> --cluster-slots <number of slots> --cluster-yes


4105d7b13f62b63ff24a7349983b581cd9aa0d41 172.20.0.5:6380@16380 master - 0 1754913848538 2 connected 5461-10922
1d7a44ee88789a5391d6631541b1050d8bbaa77d 172.20.0.3:6381@16381 master - 0 1754913849260 3 connected 10923-16383
45127b3c8dc28d7ed3af02f6f2e7300a71496897 172.20.0.7:6379@16379 myself,master - 0 1754913849000 1 connected 0-5460

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

> [Redis Docs > Scale with Redis Cluster](https://redis.io/docs/latest/operate/oss_and_stack/management/scaling/) <br/>
> [Redis Docs > Redis cluster specification](https://redis.io/docs/latest/operate/oss_and_stack/reference/cluster-spec/) <br/>

> https://meetup.nhncloud.com/posts/226
> https://cla9.tistory.com/102
> https://www.youtube.com/watch?v=mPB2CZiAkKM
> https://redis.io/docs/latest/operate/oss_and_stack/reference/cluster-spec/#key-distribution-model
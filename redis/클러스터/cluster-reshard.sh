#!/bin/bash

./cluster-create.sh



## 리샤딩 전 클러스터 노드 상태 확인을 위한 스크립트
BEFORE_RESHARD=$(docker exec -it redis-node-1 redis-cli cluster nodes | grep "master")

## 클러스터 리샤딩
echo "클러스터 리샤딩 시작"

MASTER_ID1="$(docker exec -it redis-node-1 redis-cli cluster nodes | grep "master" | head -n 1 | awk '{print $1}')"
MASTER_ID2="$(docker exec -it redis-node-1 redis-cli cluster nodes | grep "master" | tail -n +2 | head -n 1 | awk '{print $1}')"

docker exec -it redis-node-1 redis-cli --cluster reshard redis-node-1:6379 \
    --cluster-from $MASTER_ID1 \
    --cluster-to $MASTER_ID2 \
    --cluster-slots 100 \
    --cluster-yes

if [ $? -ne 0 ]; then
    echo "클러스터 리샤딩 실패"
    exit 1
fi

sleep 2

echo "클러스터 리샤딩 완료: --cluster-from $MASTER_ID1 --cluster-to $MASTER_ID2 --cluster-slots 100"

echo "리샤딩 전 클러스터 노드 상태 확인"
echo "${BEFORE_RESHARD}"
## 리샤딩 후 클러스터 노드 상태 확인
echo "리샤딩 후 클러스터 노드 상태 확인"
docker exec -it redis-node-1 redis-cli cluster nodes | grep "master"
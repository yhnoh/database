#!/bin/bash

./cluster-create.sh


## 노드 추가 전 클러스터 노드 상태 확인
echo "마스터 노드 추가 전 클러스터 노드 상태 확인"
docker exec -it redis-node-1 redis-cli cluster nodes

## 클러스터 마스터 노드 추가
echo "클러스터에 마스터 노드 추가"
docker exec -it redis-node-1 redis-cli --cluster add-node redis-node-add-master:6385 redis-node-1:6379 \
  --cluster-yes

MASTER_ID=$(docker exec -it redis-node-1 redis-cli cluster nodes | grep "master" | grep "6385" | awk '{print $1}')

sleep 2

## 클러스터 슬레이브 노드 추가
echo "클러스터에 슬레이브 노드 추가"
docker exec -it redis-node-1 redis-cli --cluster add-node redis-node-add-slave:6386 redis-node-1:6379 \
  --cluster-master-id $MASTER_ID \
  --cluster-yes

sleep 2

## 노드 추가 후 클러스터 노드 상태 확인
echo "슬레이브 노드 추가 후 클러스터 노드 상태 확인"
docker exec -it redis-node-1 redis-cli cluster nodes
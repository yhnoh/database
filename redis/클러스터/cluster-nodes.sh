#!/bin/bash

docker compose up -d

## 클러스터 생성
docker exec -it redis-node-1 redis-cli --cluster create redis-node-1:6379 \
  redis-node-2:6380 \
  redis-node-3:6381 \
  redis-node-4:6382 \
  redis-node-5:6383 \
  redis-node-6:6384 \
  --cluster-replicas 1 --cluster-yes

## 클러스터 노드 상태 확인
docker exec -it redis-node-1 redis-cli cluster nodes
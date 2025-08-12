#!/bin/bash

docker compose down

docker compose up -d

echo "클러스터 생성"

docker exec -it redis-node-1 redis-cli --cluster create redis-node-1:6379 \
  redis-node-2:6380 \
  redis-node-3:6381 \
  redis-node-4:6382 \
  redis-node-5:6383 \
  redis-node-6:6384 \
  --cluster-replicas 1 --cluster-yes


NODES=(redis-node-1:6379 redis-node-2:6380 redis-node-3:6381 redis-node-4:6382 redis-node-5:6383 redis-node-6:6384)

for NODE in "${NODES[@]}"
do

  CONTAINER_NAME=$(echo ${NODE} | awk -F ':' '{print $1}')
  PORT=$(echo ${NODE} | awk -F ':' '{print $2}')
  ## 클러스터 상태를 가지고 오기 위한 스크립트
  CLUSTER_STATE=$(docker exec -it ${CONTAINER_NAME} redis-cli -p ${PORT} cluster info | grep cluster_state | awk -F ':' '{print $2}' | tr -d "\r")
  echo "${CONTAINER_NAME} 클러스터 상태: ${CLUSTER_STATE}"

  while [ "${CLUSTER_STATE}" != "ok" ]; 
  do
    echo "클러스터 구성 대기..."
    sleep 1
    CLUSTER_STATE=$(docker exec -it ${CONTAINER_NAME} redis-cli -p ${PORT} cluster info | grep cluster_state | awk -F ':' '{print $2}' | tr -d "\r")
  done
  
done



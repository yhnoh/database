#!/bin/bash

./cluster-create.sh

## 클러스터 노드 상태 확인
docker exec -it redis-node-1 redis-cli cluster nodes
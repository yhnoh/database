NETWORK_NAME=mongodb_sharding_network

docker network create --driver bridge ${NETWORK_NAME}

docker inspect ${NETWORK_NAME}

docker compose -f ./config/docker-compose.yml up -d

docker compose -f ./shard/docker-compose.yml up -d

docker compose -f ./mongos/docker-compose.yml up -d

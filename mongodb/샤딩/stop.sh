NETWORK_NAME=mongodb_sharding_network

docker compose -f ./mongos/docker-compose.yml down

docker compose -f ./shard/docker-compose.yml down

docker compose -f ./config/docker-compose.yml down

docker network rm ${NETWORK_NAME}

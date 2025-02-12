docker exec config_mongodb1 mongosh /script/init.js
docker exec shard_mongodb1 mongosh /script/init.js
docker exec mongos_mongodb mongosh /script/init.js

docker exec shard_mongodb1 mongosh /script/data.js

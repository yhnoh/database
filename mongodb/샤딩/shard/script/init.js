rs.initiate({
    _id: "shardRS",
    members: [
        {_id: 0, host: "shard_mongodb1"},
        {_id: 1, host: "shard_mongodb2"},
        {_id: 2, host: "shard_mongodb3"}
    ]
});

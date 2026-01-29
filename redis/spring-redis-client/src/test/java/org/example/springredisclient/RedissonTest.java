package org.example.springredisclient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class RedissonTest {

    private RedissonClient redissonClient;
    @BeforeEach
    void setUp() {

        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379");
//                .setDatabase(0)
//                .setConnectionPoolSize(64)
//                .setConnectionMinimumIdleSize(10)
//                .setTimeout(3000)
//                .setDnsMonitoringInterval(5000)

//        Config.fromYAML();
        redissonClient = Redisson.create(config);
    }

    @AfterEach
    void tearDown() {
        redissonClient.shutdown();
    }

    @Test
    void redissonClientTest() {
        RBucket<String> bucket = redissonClient.getBucket("redisson");
        bucket.set("Hello, Redisson!");
        String value = bucket.get();
        System.out.println("value = " + value);
    }

    static class Counter {
        private int count = 0;

        public void increment() {
            count++;
        }

        public int getCount() {
            return count;
        }
    }

    @Test
    void lockTest() throws InterruptedException, ExecutionException {

        // 스레드 100개가 동시에 Counter 증가 시도
        int iter = 100;
        Counter counter = new Counter();
        Counter lockCounter = new Counter();

        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<Thread> lockThreads = new ArrayList<>();

        for (int i = 0; i < iter; i++) {
            // Lock 없이 카운터 증가
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(100);
                    counter.increment();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            });
            threads.add(thread);

            // Distributed Lock을 사용한 카운터 증가
            Thread lockThread = new Thread(() -> {

                try {
                    RLock lock = redissonClient.getLock("lock");
                    // Uses pub/sub channel to notify other threads across all Redisson instances waiting to acquire a lock.
                    boolean isAcquired = lock.tryLock(100000, 1000, TimeUnit.MILLISECONDS);
                    if (isAcquired) {
                        try {
                            Thread.sleep(100);
                            lockCounter.increment();
                        } finally {
                            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                                lock.unlock();
                            }
                        }
                    }

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                lockThreads.add(new Thread(() -> {

                }));
            });
            lockThreads.add(lockThread);

        }

        for (int i = 0; i < iter; i++) {
            threads.get(i).start();
            lockThreads.get(i).start();
        }

        for (int i = 0; i < iter; i++) {
            threads.get(i).join();
            lockThreads.get(i).join();
        }

        // "count = 93"
        System.out.println("count = " + counter.getCount());
        // "lock count = 100"
        System.out.println("lock count = " + lockCounter.getCount());
    }

    void increment(Counter counter) {
        counter.increment();
    }


}

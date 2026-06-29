package com.bms.concurrency;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import java.util.concurrent.TimeUnit;

public class RedisLockManager implements DistributedLockManager {
    private final RedissonClient redissonClient;

    public RedisLockManager(RedissonClient redissonClient) {
        // For composite Key -> A1, A2 and A2, A3 => Reddisson Multi Lock can solve this easily with Out of the Box support by Redis
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean acquireLock(String lockKey, long waitTimeSeconds, long leaseTimeSeconds) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // Try to acquire lock. Wait up to waitTimeSeconds, hold for leaseTimeSeconds
            // leaseTimeSeconds -1 for watchdog to keep extending the thread TTL by 30 seconds with ping in each 10 seconds
            return lock.tryLock(waitTimeSeconds, leaseTimeSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void releaseLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        // Only unlock if this specific thread owns it, preventing accidental unlocks
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}

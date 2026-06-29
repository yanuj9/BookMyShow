package com.bms.concurrency;

public interface DistributedLockManager {
    boolean acquireLock(String lockKey, long waitTimeSeconds, long leaseTimeSeconds);
    void releaseLock(String lockKey);
}

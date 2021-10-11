package me.jellysquid.mods.lithium.common.util.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A ReadWriteLock for which the read lock doesn't do anything and the write lock is a standard ReentrantLock.
 */
public class NullReadBasicWriteLock implements ReadWriteLock {
    private final NullLock readLock = new NullLock();
    private final ReentrantLock writeLock = new ReentrantLock();

    @Override
    public Lock readLock() {
        return this.readLock;
    }

    @Override
    public Lock writeLock() {
        return this.writeLock;
    }
}

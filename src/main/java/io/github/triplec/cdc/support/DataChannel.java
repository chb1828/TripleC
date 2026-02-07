package io.github.triplec.cdc.support;

import jakarta.annotation.Nullable;

import javax.annotation.concurrent.ThreadSafe;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 21.
 */
@ThreadSafe
public final class DataChannel<T> {
    private final Queue<T> channel = new LinkedList<>();
    private final Queue<Throwable> errorChannel = new LinkedList<>();
    private final int maxChannelSize;
    private volatile boolean close;

    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public DataChannel(int maxChannelSize) {
        this.maxChannelSize = maxChannelSize;
    }

    public void error(Throwable throwable) {
        lock.lock();
        try {
            errorChannel.add(throwable);
        } finally {
            lock.unlock();
        }
    }

    public boolean hasError() {
        return !errorChannel.isEmpty();
    }

    public Throwable pollError() {
        return errorChannel.poll();
    }

    public void add(T data) {
        lock.lock();
        try {
            while (maxChannelSize == channel.size()) {
                try {
                    notFull.await();
                } catch (InterruptedException ignore) {
                }
            }
            channel.add(data);
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    public T poll() {
        lock.lock();
        try {
            while (channel.isEmpty()) {
                if (close) {
                    return null;
                }
                try {
                    notEmpty.await();
                } catch (InterruptedException ignore) {
                }
            }
            T data = channel.poll();
            notFull.signalAll();
            return data;
        } finally {
            lock.unlock();
        }
    }

    public void close() {
        if (close) {
            return;
        }

        this.close = true;
        lock.lock();
        try {
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
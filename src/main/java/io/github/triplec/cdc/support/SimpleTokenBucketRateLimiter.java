package io.github.triplec.cdc.support;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@sk.com
 * @since 2025. 4. 18.
 */
@ThreadSafe
public class SimpleTokenBucketRateLimiter {
    private final AtomicReference<Long> tokenRefillCheckPoint = new AtomicReference<>(System.currentTimeMillis());

    private final AtomicLong tokenBucket;
    private final long tokenThreshold;
    private final long periodMillis;

    public SimpleTokenBucketRateLimiter(AtomicLong tokenBucket, long tokenThreshold, long periodMillis) {
        this.tokenBucket = tokenBucket;
        this.tokenThreshold = tokenThreshold;
        this.periodMillis = periodMillis;
    }

    public static SimpleTokenBucketRateLimiter from(long tokenThreshold,
                                             Duration refillPeriod) {

        AtomicLong tokenBucket = new AtomicLong();
        return new SimpleTokenBucketRateLimiter(tokenBucket, tokenThreshold, refillPeriod.toMillis());
    }

    private void refillIfRequired() {
        long currentTimeMillis = System.currentTimeMillis();
        boolean isPossibleRefill = (((currentTimeMillis - tokenRefillCheckPoint.get()) / periodMillis) * tokenThreshold) > 0;
        if (isPossibleRefill) {
            tokenBucket.set(tokenThreshold);
            tokenRefillCheckPoint.set(currentTimeMillis);
        }
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void acquire() {
        long awakeTimeMillis = 25L;
        final Object lock = new Object();

        while (true) {
            if (tryAcquire()) {
                return;
            }

            synchronized (lock) {
                try {
                    lock.wait(awakeTimeMillis);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    public boolean tryAcquire() {
        refillIfRequired();
        return tokenBucket.getAndDecrement() > 0;
    }
}

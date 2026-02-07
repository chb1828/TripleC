package io.github.triplec.cdc.support;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 설명:
 *
 * @author 최현범(Jayce) / hb.choi@dreamus.io
 * @since 2025. 4. 21.
 */
public class AutoFlushBuffer<T> extends TimerTask {
    private final int limit;
    private final Consumer<Collection<T>> consumer;

    public AutoFlushBuffer(int limit, Duration rate, Consumer<Collection<T>> consumer) {
        this.limit = limit;
        this.consumer = consumer;
        new Timer().scheduleAtFixedRate(AutoFlushBuffer.this, rate.toMillis(), rate.toMillis());
    }

    private final AtomicBoolean doFlush = new AtomicBoolean(false);

    private final Collection<T> buffer = new ArrayList<>();

    public void add(T t) {
        buffer.add(t);
        if (buffer.size() >= limit) {
            flush();
        }
    }

    public void flush() {
        boolean swapped = doFlush.compareAndSet(false, true);
        if (!swapped) {
            synchronized (buffer) {
                try {
                    buffer.wait();
                } catch (InterruptedException ignore) {
                }
                return;
            }
        }

        Collection<T> copied = new ArrayList<>(buffer);
        buffer.clear();
        doFlush.set(false);
        synchronized (buffer) {
            buffer.notifyAll();
        }

        try {
            if (copied.isEmpty()) {
                return;
            }
            consumer.accept(copied);
        } catch (Exception e) {
            // 적절한 예외 처리
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        flush();
    }
}
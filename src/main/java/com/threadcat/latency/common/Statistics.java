package com.threadcat.latency.common;

/**
 * @author threadcat
 */
public class Statistics {
    private long startMs;
    private long stopMs;
    private long maxNs;
    private long totalNs;
    private long counter;

    public void start() {
        reset();
    }

    public void stop() {
        this.stopMs = System.currentTimeMillis();
    }

    public void reset() {
        startMs = System.currentTimeMillis();
        stopMs = 0;
        maxNs = 0;
        totalNs = 0;
        counter = 0;
    }

    public void update(long timeA, long timeB) {
        long delta = timeB - timeA;
        if (delta > maxNs) {
            maxNs = delta;
        }
        totalNs += delta;
        counter++;
    }

    // Returns micros
    public double max() {
        return 1e-3 * maxNs;
    }

    // Returns micros
    public double avg() {
        return 1e-3 * totalNs / counter;
    }

    // Returns seconds
    public double elapsed() {
        return 1e-3 * (stopMs - startMs);
    }
}

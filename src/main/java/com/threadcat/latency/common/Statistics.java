package com.threadcat.latency.common;

/**
 * Accumulates total, updates maximum, calculates average.
 *
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

    /**
     * Maximum time distance, microseconds.
     */
    public double max() {
        return 1e-3 * maxNs;
    }

    /**
     * Average time distance, microseconds.
     */
    public double avg() {
        return 1e-3 * totalNs / counter;
    }

    /**
     * Number of updates
     */
    public long counter() {
        return counter;
    }

    /**
     * Accumulated time frames, seconds.
     */
    public double total() {
        return 1e-9 * totalNs;
    }

    /**
     * Time elapsed, seconds.
     */
    public double elapsed() {
        return 1e-3 * (stopMs - startMs);
    }
}

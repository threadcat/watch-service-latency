package com.threadcat.latency.common;

/**
 * Accumulates total, updates maximum, calculates average.
 *
 * @author threadcat
 */
public class Statistics {
    private long start;
    private long stop;
    private long max;
    private long total;
    private long counter;

    public void start(long start) {
        reset(start);
    }

    public void stop(long stop) {
        this.stop = stop;
    }

    public void reset(long start) {
        this.start = start;
        stop = 0;
        max = 0;
        total = 0;
        counter = 0;
    }

    public void update(long timeA, long timeB) {
        long delta = timeB - timeA;
        if (delta > max) {
            max = delta;
        }
        total += delta;
        counter++;
    }

    /**
     * Maximum time distance.
     */
    public long max() {
        return max;
    }

    /**
     * Average time distance.
     */
    public double avg() {
        return (double) total / counter;
    }

    /**
     * Number of updates
     */
    public long counter() {
        return counter;
    }

    /**
     * Accumulated time frames.
     */
    public long total() {
        return total;
    }

    /**
     * Time elapsed.
     */
    public long elapsed() {
        return stop - start;
    }
}

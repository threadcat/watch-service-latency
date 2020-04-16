package com.threadcat.latency.common;

/**
 * Adjusts time from another JVM.
 *
 * @author threadcat
 */
public class ClockOffset {
    private long offset;
    private boolean adjusted;

    public long getOffset() {
        return offset;
    }

    public boolean isAdjusted() {
        return adjusted;
    }

    public long adjust(long timeA, long timeB, long timeC) {
        long fitted = timeB + offset;
        if (fitted > timeC) {
            offset += timeC - fitted;
            fitted = timeB + offset;
            adjusted = true;
        } else if (fitted < timeA) {
            offset += timeA - fitted;
            fitted = timeB + offset;
            adjusted = true;
        } else {
            adjusted = false;
        }
        return fitted;
    }

    @Override
    public String toString() {
        return String.format("offset=%s adjusted=%s", offset, adjusted);
    }
}

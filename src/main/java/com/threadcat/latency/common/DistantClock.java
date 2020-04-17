package com.threadcat.latency.common;

/**
 * Adjusts time from another JVM.
 *
 * @author threadcat
 */
public class DistantClock {
    private long offset;
    private boolean offsetUpdated;

    public DistantClock() {
        reset();
    }

    public long getOffset() {
        return offset;
    }

    public boolean isOffsetUpdated() {
        return offsetUpdated;
    }

    public void reset() {
        offset = 0;
        offsetUpdated = false;
    }

    /**
     * Refines offset between local time (A,C) and server time (B). Calculates effective server time.
     *
     * @param timeA ping request sent
     * @param timeB server timestamp
     * @param timeC ping response received
     * @return server time fitted into local time frame (A,C)
     */
    public long adjust(long timeA, long timeB, long timeC) {
        // Time granularity is one of possible causes of time variation e.g. shorter ping and bigger offset
        offsetUpdated = false;
        long fitted = timeB + offset;
        if (fitted > timeC) {
            adjustToMiddle(timeA, timeB, timeC);
            fitted = timeB + offset;
            if (fitted > timeC) {
                offset += timeC - fitted;
                fitted = timeC;
            }
            offsetUpdated = true;
        } else if (fitted < timeA) {
            adjustToMiddle(timeA, timeB, timeC);
            fitted = timeB + offset;
            if (fitted < timeA) {
                offset += timeA - fitted;
                fitted = timeA;
            }
            offsetUpdated = true;
        }
        return fitted;
    }

    private void adjustToMiddle(long timeA, long timeB, long timeC) {
        long gap = (timeA + timeC) / 2 - timeB;
        offset = offset == 0 ? gap : (gap + offset) / 2;
    }

    @Override
    public String toString() {
        return String.format("offset=%s updated=%s", offset, offsetUpdated);
    }
}

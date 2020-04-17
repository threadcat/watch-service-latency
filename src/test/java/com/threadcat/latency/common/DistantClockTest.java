package com.threadcat.latency.common;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DistantClockTest {

    @Test
    void testAdjust() {
        DistantClock clockOffset = new DistantClock();
        // Format: [ timeA, timeB, timeC, expectation ]
        long[][] samples = {
                // gap increasing
                {1_000_000, 1_000_120, 1_000_018, 1000_009}, // offset = 18 / 2 - 120 = -111
                {1_000_000, 1_000_120, 1_000_012, 1000_009}, // offset = -111
                {1_000_000, 1_000_124, 1_000_012, 1000_010}, // offset = (-118 -111) / 2 = -114
                {1_000_000, 1_000_134, 1_000_012, 1000_012}, // offset = (-128 -114) / 2 = -121 and -1
                // gap decreasing
                {1_000_000, 1_000_120, 1_000_012, 1000_002}, // offset = (-114 -122) / 2 = -118
                {1_000_000, 1_000_110, 1_000_012, 1000_000}, // offset = (-104 -118) / 2 = -111 and +1
                {1_000_000, 1_000_116, 1_000_012, 1000_006}, // offset = -110
        };
        for (long[] sample : samples) {
            long timeA = sample[0];
            long timeB = sample[1];
            long timeC = sample[2];
            long actual = clockOffset.adjust(timeA, timeB, timeC);
            long expected = sample[3];
            assertEquals(expected, actual, "broken sample " + Arrays.toString(sample));
        }
    }
}

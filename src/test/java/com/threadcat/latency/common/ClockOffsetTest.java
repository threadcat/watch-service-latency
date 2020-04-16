package com.threadcat.latency.common;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClockOffsetTest {

    @Test
    void testAdjust() {
        ClockOffset clockOffset = new ClockOffset();
        long timeA = 100L;
        long timeC = 110L;
        // Format: [ timeB, expectation ]
        long[][] samples = {
                {105, 105},
                {112, 110},
                {101, 100},
                {105, 104},
        };
        for (long[] sample : samples) {
            long actual = clockOffset.adjust(timeA, sample[0], timeC);
            long expected = sample[1];
            assertEquals(expected, actual, "Sample " + Arrays.toString(sample));
        }
    }
}

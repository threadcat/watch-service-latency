package com.threadcat.latency.utils;

import com.threadcat.latency.common.LinuxTaskSet;

/**
 * Timing example:
 * Executed 100000 times in 6 ms, nanoTime() granularity avg=23.199 max=7208
 * currentMillis() granularity 1
 * <p>
 * Without warming up and CPU isolation:
 * Executed 100000 times in 16 ms, nanoTime() granularity avg=47.889 max=551107
 * currentMillis() granularity 1
 */
public class TimeGranularity {
    private static final String THREAD_NAME = "time-granularity";

    public static void main(String[] args) throws Exception {
        Thread.currentThread().setName(THREAD_NAME);
        LinuxTaskSet.setCpuMask(THREAD_NAME, "0x4");
        nanoLatencyAndGranularity();
        milliGranularity();
    }

    private static void nanoLatencyAndGranularity() {
        int warmup = 1_000_000;
        int measure = 1_000_000;
        int num = measure + warmup;
        long[] times = new long[num];
        for (int i = 0; i < num; i++) {
            times[i] = System.nanoTime();
        }
        double elapsed = times[num - 1] - times[warmup];
        double latency = elapsed / measure;
        // Analysing result
        double total = 0;
        int counter = 0;
        long max = 0;
        for (int i = warmup + 1; i < num; i++) {
            long delta = times[i] - times[i - 1];
            if (delta != 0L) {
                if (delta > max) {
                    max = delta;
                }
                total += delta;
                counter++;
            }
        }
        double granularity = total / counter;
        System.out.printf(
                "Executed %d times in %.3f milliseconds, nanoTime() latency avg %.3f, granularity avg %.3f, max %d ns\n",
                measure, 1e-6 * elapsed, latency, granularity, max);
    }

    private static void milliGranularity() {
        long timeA = System.currentTimeMillis();
        long timeB;
        do {
            timeB = System.currentTimeMillis();
        } while (timeA == timeB);
        System.out.printf("currentMillis() granularity %d", timeB - timeA);
    }
}

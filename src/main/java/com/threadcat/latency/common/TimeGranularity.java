package com.threadcat.latency.common;

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
    private static final String TIME_GRNULARITY = "time-granularity";

    public static void main(String[] args) throws Exception {
        Thread.currentThread().setName(TIME_GRNULARITY);
        NixTaskSet.setCpuMask(TIME_GRNULARITY, "0x4");
        Statistics stat = new Statistics();
        long warmup = 100_000;
        long measure = 100_000;
        long count = warmup + measure;
        for (long i = 0; i < count; i++) {
            long timeA = System.nanoTime();
            long timeB;
            do {
                timeB = System.nanoTime();
            } while (timeA == timeB);
            if (i == warmup) {
                stat.reset(System.currentTimeMillis());
            }
            stat.update(timeA, timeB);
        }
        stat.stop(System.currentTimeMillis());
        System.out.printf("Executed %d times in %d ms, nanoTime() granularity avg=%.3f max=%d\n",
                stat.counter(), stat.elapsed(), stat.avg(), stat.max());
        System.out.printf("currentMillis() granularity %d", millisDelta());
    }

    private static long millisDelta() {
        long timeA = System.currentTimeMillis();
        long timeB;
        do {
            timeB = System.currentTimeMillis();
        } while (timeA == timeB);
        return timeB - timeA;
    }
}

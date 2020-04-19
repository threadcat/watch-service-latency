package com.threadcat.latency.common;

/**
 * Ping statistics controller for warming up and timing.
 *
 * @author threadcat
 */
public class PingClient {
    private static final String MICRO = (char) 0xB5 + "s";
    private Statistics statistics = new Statistics();
    private DistantClock clockOffset = new DistantClock();
    private long warmup;

    public PingClient(long warmup) {
        this.warmup = warmup;
        System.out.println("Started");
    }

    public void update(long sequenceA, long sequenceB, long timeA, long timeB) {
        long timeC = System.nanoTime();
        if (sequenceA != sequenceB) {
            throw new RuntimeException(String.format("Unexpected sequence number %s != %s", sequenceA, sequenceB));
        }
        long serverTime = clockOffset.adjust(timeA, timeB, timeC);
        if (clockOffset.isOffsetUpdated() && sequenceA > warmup) {
            System.out.println("Server time adjusted after warming up, new offset: " + clockOffset.getOffset());
        }
        if (sequenceA == warmup) {
            System.out.println("Finished warming up");
            statistics.reset(System.currentTimeMillis());
        }
        statistics.update(timeA, serverTime);
        statistics.update(serverTime, timeC);
    }

    public void printSummary() {
        statistics.stop(System.currentTimeMillis());
        long counter = statistics.counter() / 2;
        double elapsed = 1e-3 * statistics.elapsed(); // seconds
        double max = 1e-3 * statistics.max(); // microseconds
        double avg = 1e-3 * statistics.avg(); // microseconds
        System.out.printf("Executed %s pings in %.3f seconds, one-way max latency %.3f %s, average %.3f %s\n",
                counter, elapsed, max, MICRO, avg, MICRO);
    }
}

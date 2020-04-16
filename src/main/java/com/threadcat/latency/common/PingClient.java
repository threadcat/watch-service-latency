package com.threadcat.latency.common;

/**
 * @author threadcat
 */
public class PingClient {
    private static final String MICRO = (char) 0xB5 + "s";
    private Statistics statistics = new Statistics();
    private ClockOffset clockOffset = new ClockOffset();
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
        if (clockOffset.isAdjusted() && sequenceA > warmup) {
            System.out.println("Server time adjusted after warming up, new offset: " + clockOffset.getOffset());
        }
        if (sequenceA == warmup) {
            System.out.println("Finished warming up");
            statistics.reset();
        }
        statistics.update(timeA, serverTime);
        statistics.update(serverTime, timeC);
    }

    public void printSummary() {
        statistics.stop();
        System.out.printf("Executed %s pings in %.3f seconds, one-way max latency %.3f %s, average %.3f %s\n",
                statistics.counter() / 2, statistics.elapsed(), statistics.max(), MICRO, statistics.avg(), MICRO);
    }
}

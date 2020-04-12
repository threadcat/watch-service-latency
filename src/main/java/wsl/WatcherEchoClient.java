package wsl;

import java.nio.channels.FileChannel;
import java.nio.file.WatchService;

import static wsl.WatcherEchoServer.*;

/**
 * WatchService latency test client.
 * Writes incremental sequence to file 'A' getting response from echo server through file 'B'.
 * Prints out summary after execution.
 */
public class WatcherEchoClient {

    public static void main(String[] args) throws Exception {
        FileChannel channel_a = openFile(FILE_A);
        FileChannel channel_b = openFile(FILE_B);
        WatchService ws = registerWatch(WATCH_DIR_B);
        // Warming up
        for (int i = 0; i < 100_000; i++) {
            write(i, 0L, channel_a);
            read(ws, channel_b);
        }
        // Testing
        Statistics statistics = new Statistics();
        long counter = 10_000;
        statistics.start(System.currentTimeMillis());
        for (long i = 0; i < counter; i++) {
            long time_a = System.nanoTime();
            write(i, 0L, channel_a);
            read(ws, channel_b);
            long n = BUFFER_RD.getLong(0);
            long time_b = BUFFER_RD.getLong(8);
            long time_echo = System.nanoTime();
            if (i != n) {
                throw new RuntimeException("Unexpected sequence number " + n);
            }
            if (time_b < time_a || time_b > time_echo) {
                throw new RuntimeException("System nano time does not hold between two JVM");
            }
            statistics.update(time_a, time_b);
            statistics.update(time_b, time_echo);
        }
        statistics.stop(System.currentTimeMillis());
        System.out.printf("Executed %s times in %.3f seconds, one-way max latency %.3f millis, average %.3f micros\n",
                counter, statistics.elapsed(), statistics.max(), statistics.avg());
    }

    private static class Statistics {
        private long maxNs;
        private long totalNs;
        private long startMs;
        private long stopMs;
        private long counter;

        public void update(long startNs, long stopNs) {
            long delta = stopNs - startNs;
            if (delta > maxNs) {
                maxNs = delta;
            }
            totalNs += delta;
            counter++;
        }

        public void start(long startMs) {
            this.startMs = startMs;
            counter = 0;
        }

        public void stop(long stopMs) {
            this.stopMs = stopMs;
        }

        // Returns millis
        public double max() {
            return maxNs * 1e-6;
        }

        // Returns micros
        public double avg() {
            return 1e-3 * totalNs / counter;
        }

        // Returns seconds
        public double elapsed() {
            return 1e-3 * (stopMs - startMs);
        }
    }
}
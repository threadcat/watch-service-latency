package latency.watcher;

import latency.common.DataHandler;
import latency.common.Statistics;

import java.nio.channels.FileChannel;
import java.nio.file.WatchService;

import static latency.watcher.WatcherEchoServer.*;

/**
 * WatchService latency test client.
 * Writes incremental sequence to file 'A' getting response from file 'B'.
 * Prints out summary after execution.
 */
public class WatcherEchoClient {

    public static void main(String[] args) throws Exception {
        FileChannel channelA = openFile(FILE_A);
        FileChannel channelB = openFile(FILE_B);
        WatchService watchService = registerWatch(WATCH_DIR_B);
        DataHandler dataHandler = new DataHandler();
        Statistics statistics = new Statistics();
        System.out.println("Started");
        long counter = 100_000;
        statistics.start();
        for (long i = 0; i < counter; i++) {
            long timeA = System.nanoTime();
            if (dataHandler.writeFile(i, 0L, channelA)) {
                poll(watchService);
                if (dataHandler.readFile(channelB)) {
                    long n = dataHandler.getSequence();
                    long timeB = dataHandler.getTimestamp();
                    long timeC = System.nanoTime();
                    dataHandler.validate(i, n, timeA, timeB, timeC);
                    statistics.update(timeA, timeB);
                    statistics.update(timeB, timeC);
                }
            }
        }
        statistics.stop();
        System.out.printf("Executed %s times in %.3f seconds, one-way max latency %.3f millis, average %.3f micros\n",
                counter, statistics.elapsed(), statistics.max(), statistics.avg());
    }
}

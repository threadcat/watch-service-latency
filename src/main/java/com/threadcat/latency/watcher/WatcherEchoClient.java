package com.threadcat.latency.watcher;

import com.threadcat.latency.common.DataHandler;
import com.threadcat.latency.common.NixTaskSet;
import com.threadcat.latency.common.PingClient;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.WatchService;

import static com.threadcat.latency.watcher.WatcherEchoServer.*;

/**
 * WatchService latency test client.
 * Writes incremental sequence to file 'A' getting response from file 'B'.
 * Prints out summary after execution.
 *
 * @author threadcat
 */
public class WatcherEchoClient {
    private static final String WATCHER_ECHO_CLIENT = "watcher_echo_client";

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("Required parameters: <directory> <cpu_mask_hex> <warmup_cycles> <measure_cycles>");
            return;
        }
        String dir = args[0];
        String cpuMask = args[1];
        int warmup = Integer.parseInt(args[2]);
        int measure = Integer.parseInt(args[3]);
        Thread.currentThread().setName(WATCHER_ECHO_CLIENT);
        NixTaskSet.setCpuMask(WATCHER_ECHO_CLIENT, cpuMask);
        eventLoop(dir, warmup, measure);
    }

    private static void eventLoop(String dir, long warmup, long measure) throws IOException, InterruptedException {
        FileChannel channelA = openFile(dir, WATCH_DIR_A, FILE_A);
        FileChannel channelB = openFile(dir, WATCH_DIR_B, FILE_B);
        WatchService watchService = registerWatch(dir, WATCH_DIR_B);
        DataHandler dataHandler = new DataHandler();
        long counter = warmup + measure;
        PingClient pingClient = new PingClient(warmup);
        for (long i = 0; i < counter; i++) {
            long timeA = System.nanoTime();
            if (dataHandler.writeFile(channelA, i, 0L)) {
                poll(watchService);
                if (dataHandler.readFile(channelB)) {
                    long n = dataHandler.getSequence();
                    long timeB = dataHandler.getTimestamp();
                    pingClient.update(i, n, timeA, timeB);
                }
            }
        }
        pingClient.printSummary();
    }
}

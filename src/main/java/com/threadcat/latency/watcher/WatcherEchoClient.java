package com.threadcat.latency.watcher;

import com.threadcat.latency.common.DataHandler;
import com.threadcat.latency.common.NixTaskSet;
import com.threadcat.latency.common.PingClient;

import java.io.File;
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

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Required parameters: <directory> <cpu_mask_hex>");
            return;
        }
        String dir = args[0];
        String cpuMask = args[1];
        Thread.currentThread().setName("watcher_echo_client");
        NixTaskSet.setCpuMask(cpuMask);
        File fileA = createFile(dir, WATCH_DIR_A, FILE_A);
        File fileB = createFile(dir, WATCH_DIR_B, FILE_B);
        FileChannel channelA = openFile(fileA);
        FileChannel channelB = openFile(fileB);
        WatchService watchService = registerWatch(fileB.getParentFile());
        DataHandler dataHandler = new DataHandler();
        long counter = 200_000;
        long warmup = counter - 100_000;
        PingClient pingClient = new PingClient(warmup);
        for (long i = 0; i < counter; i++) {
            long timeA = System.nanoTime();
            if (dataHandler.writeFile(i, 0L, channelA)) {
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

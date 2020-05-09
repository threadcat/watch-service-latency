package com.threadcat.latency.fspin;

import com.threadcat.latency.common.DataHandler;
import com.threadcat.latency.common.LinuxTaskSet;
import com.threadcat.latency.common.PingClient;

import java.io.IOException;
import java.nio.channels.FileChannel;

import static com.threadcat.latency.utils.FileUtils.*;

public class SpinningEchoClient {
    private static final String THREAD_NAME = "spin-echo-client";

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("Required parameters: <directory> <cpu_mask_hex> <warmup_cycles> <measure_cycles>");
            return;
        }
        String dir = args[0];
        String cpuMask = args[1];
        int warmup = Integer.parseInt(args[2]);
        int measure = Integer.parseInt(args[3]);
        Thread.currentThread().setName(THREAD_NAME);
        LinuxTaskSet.setCpuMask(THREAD_NAME, cpuMask);
        eventLoop(dir, warmup, measure);
    }

    private static void eventLoop(String dir, long warmup, long measure) {
        try (FileChannel channelA = openFile(dir, WATCH_DIR_A, FILE_A);
             FileChannel channelB = openFile(dir, WATCH_DIR_B, FILE_B)) {
            DataHandler dataHandler = new DataHandler();
            PingClient pingClient = new PingClient(warmup);
            long count = warmup + measure;
            for (long i = 0; i < count; i++) {
                long timeA = System.nanoTime();
                if (!dataHandler.writeFile(channelA, i, 0L)) {
                    System.out.println("Failed writing " + FILE_A);
                    return;
                }
                long n = -1L;
                do {
                    dataHandler.readFile(channelB);
                    n = dataHandler.getSequence();
                } while (i != n);
                long timeB = dataHandler.getTimestamp();
                pingClient.update(i, n, timeA, timeB);
            }
            pingClient.printSummary();
        } catch (IOException e) {
            System.out.println("Failed running echo client");
        }
    }
}

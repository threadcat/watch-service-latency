package com.threadcat.latency.mspin;

import com.threadcat.latency.common.DataHandler;
import com.threadcat.latency.common.LinuxTaskSet;
import com.threadcat.latency.common.PingClient;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static com.threadcat.latency.utils.FileUtils.*;

public class MemEchoClient {
    private static final String THREAD_NAME = "mem-echo-client";

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
            MappedByteBuffer bufferA = channelA.map(FileChannel.MapMode.READ_WRITE, 0, 16);
            MappedByteBuffer bufferB = channelB.map(FileChannel.MapMode.READ_WRITE, 0, 16);
            DataHandler dataHandler = new DataHandler(bufferB, bufferA);
            PingClient pingClient = new PingClient(warmup);
            long count = warmup + measure;
            for (long i = 0; i < count; i++) {
                long timeA = System.nanoTime();
                dataHandler.write(i, 0L);
                long n;
                do {
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

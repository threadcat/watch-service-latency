package com.threadcat.latency.fspin;

import com.threadcat.latency.common.DataHandler;
import com.threadcat.latency.common.LinuxTaskSet;

import java.io.IOException;
import java.nio.channels.FileChannel;

import static com.threadcat.latency.utils.FileUtils.*;

/**
 *
 */
public class SpinningEchoServer {
    private static final String THREAD_NAME = "spin-echo-server";

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Required parameters: <directory> <cpu_mask_hex>");
            return;
        }
        String dir = args[0];
        String cpuMask = args[1];
        Thread.currentThread().setName(THREAD_NAME);
        LinuxTaskSet.setCpuMask(THREAD_NAME, cpuMask);
        eventLoop(dir);
    }

    private static void eventLoop(String dir) {
        try (FileChannel channelA = openFile(dir, WATCH_DIR_A, FILE_A);
             FileChannel channelB = openFile(dir, WATCH_DIR_B, FILE_B)) {
            DataHandler dataHandler = new DataHandler();
            System.out.println("Started");
            long lastSequence = -1L;
            for (; ; ) {
                if (dataHandler.readFile(channelA)) {
                    long sequence = dataHandler.getSequence();
                    if (sequence != lastSequence) {
                        long timestamp = System.nanoTime();
                        dataHandler.writeFile(channelB, sequence, timestamp);
                        lastSequence = sequence;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Failed running echo server");
        }
    }
}
